package burst.pool.miners;

import burst.kit.crypto.BurstCrypto;
import burst.kit.entity.BurstAddress;
import burst.kit.entity.BurstID;
import burst.kit.entity.BurstValue;
import burst.kit.entity.response.Account;
import burst.kit.entity.response.Block;
import burst.kit.entity.response.MiningInfo;
import burst.kit.service.BurstNodeService;
import burst.pool.entity.Payout;
import burst.pool.entity.WonBlock;
import burst.pool.storage.config.PropertyService;
import burst.pool.storage.config.Props;
import burst.pool.storage.persistent.StorageService;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MinerTracker {
    private static final Logger logger = LoggerFactory.getLogger(MinerTracker.class);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final PropertyService propertyService;
    private final BurstCrypto burstCrypto = BurstCrypto.getInstance();
    private final BurstNodeService nodeService;
    private final AtomicBoolean currentlyProcessingBlock = new AtomicBoolean(false);
    
    private final Semaphore payoutSemaphore = new Semaphore(1);

    public MinerTracker(BurstNodeService nodeService, PropertyService propertyService) {
        this.nodeService = nodeService;
        this.propertyService = propertyService;
    }

    public BigInteger onMinerSubmittedDeadline(StorageService storageService, BurstAddress minerAddress, BigInteger deadline, MiningInfo miningInfo, String userAgent) {
        // waitUntilNotProcessingBlock();
        Miner miner = getOrCreate(storageService, minerAddress);
        miner.setUserAgent(userAgent);
        
        long baseTarget = miningInfo.getBaseTarget();
        int blockHeight = (int) miningInfo.getHeight();
        
        if(miner.getCommitmentHeight() != blockHeight) {
            try {
                // Get the latest available and not an explicit block height because this can generate exceptions
                // in case the node is doing a roll-back for a short lived fork.
                Account accountResponse = nodeService.getAccount(minerAddress, null, true, true).blockingGet();
                onMinerAccount(miner, accountResponse, blockHeight);
            }
            catch (Exception e) {
                // miner.setCommitment(null, null, blockHeight);
                onMinerAccountError(e);
            }
        }
        
        double commitmentFactor = 1.0;
        if(blockHeight >= propertyService.getInt(Props.pocPlusBlock)) {
            // PoC+ logic
            BurstValue commitment = miner.getCommitment();
            
            commitmentFactor = getCommitmentFactor(commitment, miningInfo.getAverageCommitmentNQT());            
        }
        miner.processNewDeadline(new Deadline(deadline, BigInteger.valueOf(baseTarget), miner.getSharePercent(), blockHeight,
                commitmentFactor, commitmentFactor));
        
        // Now the effective deadline 
        double newDeadline = deadline.longValue()/commitmentFactor;
        deadline = BigInteger.valueOf((long)newDeadline);
        
        return deadline;
    }
    
    public static double getCommitmentFactor(BurstValue commitment, long averageCommitment) {
        double commitmentFactor = ((double)commitment.longValue())/averageCommitment;
        commitmentFactor = Math.pow(commitmentFactor, 0.4515449935);
        commitmentFactor = Math.min(8.0, commitmentFactor);
        commitmentFactor = Math.max(0.125, commitmentFactor);
        
        return commitmentFactor;
    }

    private Miner getOrCreate(StorageService storageService, BurstAddress minerAddress) {
        Miner miner = storageService.getMiner(minerAddress);
        if (miner == null) {
            miner = storageService.newMiner(minerAddress);
        }
        return miner;
    }

    public void onBlockWon(StorageService transactionalStorageService, Block block, BurstValue blockReward) {
        logger.info("Block won! Block height: " + block.getHeight() + ", forger: " + block.getGenerator().getFullAddress());

        BurstValue reward = blockReward;
        
        Miner winningMiner = getOrCreate(transactionalStorageService, block.getGenerator());

        // Take pool fee
        float feePercentage = propertyService.getFloat(Props.poolFeePercentage);
        if(winningMiner.getSharePercent() < 20) {
            // charge the solo fee
            feePercentage = propertyService.getFloat(Props.poolSoloFeePercentage);
        }
        
        BurstValue poolTake = reward.multiply(feePercentage);
        reward = reward.subtract(poolTake);
        PoolFeeRecipient poolFeeRecipient = transactionalStorageService.getPoolFeeRecipient();
        poolFeeRecipient.increasePending(poolTake, null);
        
        PoolFeeRecipient donationRecipient = transactionalStorageService.getPoolDonationRecipient();

        // Take winner share
        double winnerShare = 1.0d - winningMiner.getSharePercent()/100d;
        BurstValue winnerTake = reward.multiply(winnerShare);
        winningMiner.increasePending(winnerTake, donationRecipient);
        reward = reward.subtract(winnerTake);
        
        transactionalStorageService.addWonBlock(new WonBlock((int) block.getHeight(), block.getId(), block.getGenerator(), block.getNonce(), blockReward, reward));

        List<Miner> miners = transactionalStorageService.getMiners();

        updateMiners(transactionalStorageService, block);

        // Update each miner's pending
        AtomicReference<BurstValue> amountTaken = new AtomicReference<>(BurstValue.fromBurst(0));
        BurstValue poolReward = reward;
        miners.forEach(miner -> amountTaken.updateAndGet(a -> a.add(miner.takeShare(poolReward, donationRecipient))));

        // Evenly share result. This makes sure that poolReward is taken, even if the amountTaken was greater than poolReward
        // Essentially prevents the pool from overpaying or underpaying. Even if it gave out too much to the fee recipient and reward recipient, it will now take the extra from the pending of miners.
        if (!miners.isEmpty()) {
            BurstValue amountRemainingEach = poolReward.subtract(amountTaken.get()).divide(miners.size());
            if (logger.isInfoEnabled()) {
                logger.info("Amount remaining each is {}", amountRemainingEach.toPlanck());
            }
            miners.forEach(miner -> miner.increasePending(amountRemainingEach, donationRecipient));
        }

        logger.info("Finished processing winnings for block " + block.getHeight() + ". Reward ( + fees) is " + blockReward + ", pool fee is " + poolTake + ", forger take is " + winnerTake + ", miners took " + amountTaken.get());
    }

    public void onBlockNotWon(StorageService transactionalStorageService, Block block) {
        updateMiners(transactionalStorageService, block);
    }

    private void updateMiners(StorageService transactionalStorageService, Block block) {
        
        // prune old deadlines from the DB
        long blockHeight = block.getHeight();
        long lastHeight = blockHeight - propertyService.getInt(Props.nAvg);
        transactionalStorageService.removeDeadlinesBefore(lastHeight);
        
        List<Miner> miners = transactionalStorageService.getMiners();
        
        double poolCapacity = 0d;
        for(Miner miner : miners) {
            // Update each miner's effective capacity
            miner.recalculateCapacity(block);

            // Calculate pool capacity
            poolCapacity += miner.getSharedCapacity();
        }

        for(Miner miner : miners) {
            // Update each miner's share
            miner.recalculateShare(poolCapacity);
        }
    }

    public void payoutIfNeeded(StorageService storageService, BurstValue transactionFee) {
        logger.info("Attempting payout...");
        if (payoutSemaphore.availablePermits() == 0) {
            logger.info("Cannot payout - payout is already in progress.");
            return;
        }

        try {
            payoutSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Set<Payable> payableMinersSet = new HashSet<>();
        for (Payable miner : storageService.getMiners()) {
            if (miner.getMinimumPayout().compareTo(miner.getPending()) <= 0) {
                payableMinersSet.add(miner);
            }
        }

        PoolFeeRecipient poolFeeRecipient = storageService.getPoolFeeRecipient();
        if (poolFeeRecipient.getMinimumPayout().compareTo(poolFeeRecipient.getPending()) <= 0) {
            payableMinersSet.add(poolFeeRecipient);
        }
        
        PoolFeeRecipient poolDonationRecipient = storageService.getPoolDonationRecipient();
        if (poolDonationRecipient.getMinimumPayout().compareTo(poolDonationRecipient.getPending()) <= 0) {
            payableMinersSet.add(poolDonationRecipient);
        }

        if ((payableMinersSet.size() < 2 && propertyService.getInt(Props.minPayoutsPerTransaction) != 1) || (payableMinersSet.size() < propertyService.getInt(Props.minPayoutsPerTransaction) && payableMinersSet.size() < storageService.getMinerCount())) {
            payoutSemaphore.release();
            logger.info("Cannot payout. There are {} payable miners, required {}, miner count {}", payableMinersSet.size(), propertyService.getInt(Props.minPayoutsPerTransaction), storageService.getMinerCount());
            return;
        }

        Payable[] payableMiners = payableMinersSet.size() <= 64 ? payableMinersSet.toArray(new Payable[0]) : Arrays.copyOfRange(payableMinersSet.toArray(new Payable[0]), 0, 64);

        BurstValue transactionFeePaidPerMiner = transactionFee.divide(payableMiners.length);
        logger.info("TFPM is {}", transactionFeePaidPerMiner.toPlanck());
        Map<Payable, BurstValue> payees = new HashMap<>(); // Does not have subtracted transaction fee
        Map<BurstAddress, BurstValue> recipients = new HashMap<>();
        StringBuilder logMessage = new StringBuilder("Paying out to miners");
        ByteBuffer transactionAttachment = ByteBuffer.allocate(8 * 2 * payableMiners.length);
        for (Payable payable : payableMiners) {
            BurstValue pending = payable.getPending();
            payees.put(payable, pending);
            BurstValue actualPayout = pending.subtract(transactionFeePaidPerMiner);
            recipients.put(payable.getAddress(), actualPayout);
            transactionAttachment.putLong(payable.getAddress().getBurstID().getSignedLongId());
            transactionAttachment.putLong(actualPayout.toPlanck().longValue());
            logMessage.append(", ").append(payable.getAddress().getFullAddress()).append("(").append(actualPayout.toPlanck()).append("/").append(pending.toPlanck()).append(")");
        }
        logger.info(logMessage.toString());

        byte[] publicKey = burstCrypto.getPublicKey(propertyService.getString(Props.passphrase));

        AtomicReference<BurstID> transactionId = new AtomicReference<>();

        Single<byte[]> transaction = null;
        if(payableMinersSet.size() == 1) {
            transaction = nodeService.generateTransaction(payableMiners[0].getAddress(), publicKey, payableMiners[0].getPending().subtract(transactionFee), transactionFee, 1440, null);
        }
        else {
            transaction = nodeService.generateMultiOutTransaction(publicKey, transactionFee, 1440, recipients);
        }
        
        compositeDisposable.add(transaction
        .retry(propertyService.getInt(Props.payoutRetryCount))
        .map(response -> burstCrypto.signTransaction(propertyService.getString(Props.passphrase), response))
        .map(signedBytes -> { // TODO somehow integrate this into burstkit4j
            byte[] unsigned = new byte[signedBytes.length];
            byte[] signature = new byte[64];
            System.arraycopy(signedBytes, 0, unsigned, 0, signedBytes.length);
            System.arraycopy(signedBytes, 96, signature, 0, 64);
            for (int i = 96; i < 96 + 64; i++) {
                unsigned[i] = 0;
            }
            MessageDigest sha256 = burstCrypto.getSha256();
            byte[] signatureHash = sha256.digest(signature);
            sha256.update(unsigned);
            byte[] fullHash = sha256.digest(signatureHash);
            transactionId.set(burstCrypto.hashToId(fullHash));
            return signedBytes;
        })
        .flatMap(signedBytes -> nodeService.broadcastTransaction(signedBytes)
                    .retry(propertyService.getInt(Props.payoutRetryCount)))
        .subscribe(response -> onPaidOut(storageService, transactionId.get(), payees, publicKey, transactionFee, 1440, transactionAttachment.array()), this::onPayoutError));
    }

    private void onPaidOut(StorageService storageService, BurstID transactionID, Map<Payable, BurstValue> paidMiners, byte[] senderPublicKey, BurstValue fee, int deadline, byte[] transactionAttachment) {
        waitUntilNotProcessingBlock();
        for (Map.Entry<Payable, BurstValue> payment : paidMiners.entrySet()) {
            payment.getKey().decreasePending(payment.getValue());
        }
        storageService.addPayout(new Payout(transactionID, senderPublicKey, fee, deadline, transactionAttachment));
        logger.info("Paid out, transaction id " + transactionID);
        payoutSemaphore.release();
    }

    private void onPayoutError(Throwable throwable) {
        logger.error("Error occurred whilst paying out", throwable);
        payoutSemaphore.release();
    }

    private void onMinerAccount(Miner miner, Account accountResponse, int height) {
        if (miner == null)
            return;
        if (accountResponse.getName() != null)
            miner.setName(accountResponse.getName());
        miner.setCommitment(accountResponse.getCommitment(), accountResponse.getCommittedBalance(), height);
    }

    private void onMinerAccountError(Throwable throwable) {
        logger.warn("Error obtaining miner account info", throwable);
    }

    private void waitUntilNotProcessingBlock() {
        while (currentlyProcessingBlock.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setCurrentlyProcessingBlock(boolean currentlyProcessingBlock){
        this.currentlyProcessingBlock.set(currentlyProcessingBlock);
    }
}
