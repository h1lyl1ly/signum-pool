const noneFoundYet = "None found yet!";
const loading = "Loading...";
const minerNotFound = "Miner not found";

let miners = new Array(0);
let colors = new Array(11);

for (let i = 0; i < colors.length; i++) {
    colors[i] = generateColour();
}

var entityMap = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
    '/': '&#x2F;',
    '`': '&#x60;',
    '=': '&#x3D;'
};

function escapeHtml(string) {
    return typeof string === 'string' ? String(string).replace(/[&<>"'`=\/]/g, function (s) {
        return entityMap[s];
    }) : string;
}

let chart = null;

function generateColour() {
    return "rgb(" + Math.floor(Math.random() * 255) + "," + Math.floor(Math.random() * 255) + "," + Math.floor(Math.random() * 255) + ")";
}

function getPoolInfo() {
    fetch("/api/getConfig").then(http => {
        return http.json();
    }).then(response => {
        document.getElementById("poolNameTitle").innerText = escapeHtml(response.poolName);
        document.title = escapeHtml("Burst Pool (" + response.poolName + ")");
        document.getElementById("poolName").innerText = escapeHtml(response.poolName);
        document.getElementById("poolAccount").innerText = escapeHtml(response.poolAccountRS + " (" + response.poolAccount + ")");
        document.getElementById("nAvg").innerText = escapeHtml(response.nAvg);
        document.getElementById("nMin").innerText = escapeHtml(response.nMin);
        document.getElementById("maxDeadline").innerText = escapeHtml(response.maxDeadline);
        document.getElementById("processLag").innerText = escapeHtml(response.processLag + " Blocks");
        document.getElementById("feeRecipient").innerText = escapeHtml(response.feeRecipientRS);
        document.getElementById("poolFee").innerText = escapeHtml((parseFloat(response.poolFeePercentage)*100).toFixed(3) + "%");
        document.getElementById("winnerReward").innerText = escapeHtml((parseFloat(response.winnerRewardPercentage)*100).toFixed(3) + "%");
        document.getElementById("minimumPayout").innerText = escapeHtml(response.defaultMinimumPayout + " BURST");
        document.getElementById("minPayoutsAtOnce").innerText = escapeHtml(response.minPayoutsPerTransaction);
        document.getElementById("payoutTxFee").innerText = escapeHtml(response.transactionFee + " BURST");
        document.getElementById("poolVersion").innerText = escapeHtml(response.version);
    });
}

function getCurrentRound() {
    fetch("/api/getCurrentRound").then(http => {
        return http.json();
    }).then(response => {
        document.getElementById("currentRoundElapsed").innerText = escapeHtml(response.roundElapsed);
        document.getElementById("blockHeight").innerText = escapeHtml(response.miningInfo.height);
        document.getElementById("baseTarget").innerText = escapeHtml(response.miningInfo.baseTarget);
        if (response.bestDeadline != null) {
            document.getElementById("bestDeadline").innerText = escapeHtml(response.bestDeadline.deadline);
            document.getElementById("bestMiner").innerText = escapeHtml(response.bestDeadline.minerRS);
            document.getElementById("bestNonce").innerText = escapeHtml(response.bestDeadline.nonce);
        } else {
            document.getElementById("bestDeadline").innerText = escapeHtml(noneFoundYet);
            document.getElementById("bestMiner").innerText = escapeHtml(noneFoundYet);
            document.getElementById("bestNonce").innerText = escapeHtml(noneFoundYet);
        }
    });
}

function getTop10Miners() {
    fetch("api/getTop10Miners").then(http => {
        return http.json();
    }).then(response => {
        let topTenMiners = response.topMiners;
        let topMinerNames = Array();
        let topMinerShares = Array();
        let minerColors = colors.slice(0, topTenMiners.length + 1);
        for (let i = 0; i < topTenMiners.length; i++) {
            let miner = topTenMiners[i];
            topMinerNames.push(escapeHtml(miner.name == null ? miner.addressRS : miner.addressRS + " (" + miner.name + ")"));
            topMinerShares.push(miner.share);
        }
        topMinerNames.push("Other");
        topMinerShares.push(response.othersShare);
        if (chart == null) {
            chart = new Chart(document.getElementById("sharesChart"), {
                type: "pie",
                data: {
                    datasets: [{
                        data: topMinerShares,
                        backgroundColor: minerColors
                    }],
                    labels: topMinerNames
                },
                options: {
                    title: {
                        display: true,
                        text: "Pool Shares"
                    },
                    responsive: true,
                    maintainAspectRatio: false,
                }
            });
        } else {
            chart.data.datasets[0].data = topMinerShares;
            chart.data.datasets[0].backgroundColor = minerColors;
            chart.data.labels = topMinerNames;
            chart.update();
        }
    });
}

function getMiners() {
    fetch("/api/getMiners").then(http => {
        return http.json();
    }).then(response => {
        let table = document.getElementById("miners");
        table.innerHTML = "<tr><th>Miner</th><th>Pending Balance</th><th>Effective Capacity</th><th>nConf (Last (nAvg + processLag) rounds)</th><th>Share</th><th>Software</th></tr>";
        for (let i = 0; i < response.miners.length; i++) {
            let miner = escapeHtml(response.miners[i]);
            let minerAddress = escapeHtml(miner.name == null ? miner.addressRS : miner.addressRS + " (" + miner.name + ")");
            let userAgent = escapeHtml(miner.userAgent == null? "Unknown" : miner.userAgent);
            table.innerHTML += "<tr><td>"+minerAddress+"</td><td>"+miner.pendingBalance+"</td><td>"+formatCapacity(miner.estimatedCapacity)+" TB</td><td>"+miner.nConf+"</td><td>"+(parseFloat(miner.share)*100).toFixed(3)+"%</td><td>"+userAgent+"</td></tr>";
        }
        document.getElementById("minerCount").innerText = response.miners.length;
        document.getElementById("poolCapacity").innerText = formatCapacity(response.poolCapacity) + " TB";
        miners = response.miners;
    });
}

function prepareMinerInfo(address) {
    let minerAddress = escapeHtml(document.getElementById("minerAddress"));
    let minerName = escapeHtml(document.getElementById("minerName"));
    let minerPending = escapeHtml(document.getElementById("minerPending"));
    let minerMinimumPayout = escapeHtml(document.getElementById("minerMinimumPayout"));
    let minerCapacity = escapeHtml(document.getElementById("minerCapacity"));
    let minerNConf = escapeHtml(document.getElementById("minerNConf"));
    let minerShare = escapeHtml(document.getElementById("minerShare"));
    let minerSoftware = escapeHtml(document.getElementById("minerSoftware"));
    let setMinimumPayoutButton = $("#setMinimumPayoutButton");
    
    minerAddress.innerText = escapeHtml(address);
    minerName.innerText = escapeHtml(loading);
    minerPending.innerText = escapeHtml(loading);
    minerMinimumPayout.innerText = escapeHtml(loading);
    minerCapacity.innerText = escapeHtml(loading);
    minerNConf.innerText = escapeHtml(loading);
    minerShare.innerText = escapeHtml(loading);
    minerSoftware.innerText = escapeHtml(loading);

    let miner = null;
    miners.forEach(aMiner => {
        if (aMiner.addressRS === address || aMiner.address.toString() === address || aMiner.name === address) {
            miner = aMiner;
        }
    });

    if (miner == null) {
        minerName.innerText = escapeHtml(minerNotFound);
        minerPending.innerText = escapeHtml(minerNotFound);
        minerMinimumPayout.innerText = escapeHtml(minerNotFound);
        minerCapacity.innerText = escapeHtml(minerNotFound);
        minerNConf.innerText = escapeHtml(minerNotFound);
        minerShare.innerText = escapeHtml(minerNotFound);
        minerSoftware.innerText = escapeHtml(minerNotFound);
        setMinimumPayoutButton.hide();
        return;
    }

    let name = escapeHtml(miner.name == null ? "Not Set" : miner.name);
    let userAgent = escapeHtml(miner.userAgent == null ? "Unknown" : miner.userAgent);

    minerAddress.innerText = escapeHtml(miner.addressRS);
    minerName.innerText = escapeHtml(name);
    minerPending.innerText = escapeHtml(miner.pendingBalance);
    minerMinimumPayout.innerText = escapeHtml(miner.minimumPayout);
    minerCapacity.innerText = formatCapacity(miner.estimatedCapacity);
    minerNConf.innerText = escapeHtml(miner.nConf);
    minerShare.innerText = escapeHtml((parseFloat(miner.share)*100).toFixed(3) + "%");
    minerSoftware.innerText = escapeHtml(userAgent);
    setMinimumPayoutButton.show();
}

function formatCapacity(capacity) {
    return parseFloat(capacity).toFixed(3);
}

function onPageLoad() {
    $('#minerInfoModal').on('show.bs.modal', function (event) {
        prepareMinerInfo(document.getElementById("addressInput").value);
    });
    $('#setMinimumPayoutModal').on('show.bs.modal', function (event) {
        document.getElementById("setMinimumAddress").value = escapeHtml(document.getElementById("minerAddress").innerText);
        $("#setMinimumResult").hide();
    });
    document.getElementById("addressInput").addEventListener("keyup", function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            document.getElementById("getMinerButton").click();
        }
    })
}

function loadCss(file) {
    const fileref = document.createElement("link");
    fileref.setAttribute("rel", "stylesheet");
    fileref.setAttribute("type", "text/css");
    fileref.setAttribute("href", file);
    if (typeof fileref !== "undefined") document.getElementsByTagName("head")[0].appendChild(fileref)
}

function switchTheme() {
    if (getCookie("theme") === "light") {
        setCookie("theme", "dark");
    } else {
        setCookie("theme", "light");
    }
    location.reload(true);
}

function setCookie(name, value) {
    document.cookie = name + "=" + value + ";";
}

function getCookie(name) {
    name += "=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca =decodedCookie.split(';');
    for(let i = 0; i <ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function generateSetMinimumMessage() {
    let address = escapeHtml(document.getElementById("setMinimumAddress").value);
    let newPayout = escapeHtml(document.getElementById("setMinimumAmount").value);
    if (document.getElementById("setMinimumAmount").value === "") {
        alert("Please set new minimum payout");
        return;
    }
    fetch("/api/getSetMinimumMessage?address="+address+"&newPayout="+newPayout).then(http => {
        return http.json();
    }).then(response => {
        document.getElementById("setMinimumMessage").value = escapeHtml(response.message);
    });
}

function getWonBlocks() {
    fetch("/api/getWonBlocks").then(response => {
        return response.json();
    }).then(response => {
        let wonBlocks = response.wonBlocks;
        let table = document.getElementById("wonBlocksTable");
        table.innerHTML = "<tr><th>Height</th><th>ID</th><th>Winner Name</th><th>Winner Address</th><th>Reward + Fees</th></tr>";
        for (let i = 0; i < wonBlocks.length; i++) {
            let wonBlock = wonBlocks[i];
            let height = escapeHtml(wonBlock.height);
            let id = escapeHtml(wonBlock.id);
            let winner = escapeHtml(wonBlock.generator);
            let reward = escapeHtml(wonBlock.reward);
            let minerName = "";
            miners.forEach(miner => {
                if (miner.addressRS === winner) {
                    minerName = escapeHtml(miner.name);
                }
            });
            table.innerHTML += "<tr><td>"+height+"</td><td>"+id+"</td><td>"+minerName+"</td><td>"+winner+"</td><td>"+reward+"</td></tr>";
        }
    });
}

function setMinimumPayout() {
    var message = escapeHtml(document.getElementById("setMinimumMessage").value);
    var publicKey = escapeHtml(document.getElementById("setMinimumPublicKey").value);
    var signature = escapeHtml(document.getElementById("setMinimumSignature").value);
    if (message === "") {
        alert("Please generate message");
        return;
    }
    if (publicKey === "") {
        alert("Please enter public key");
        return;
    }
    if (signature === "") {
        alert("Please enter signature");
        return;
    }
    fetch("/api/setMinerMinimumPayout?assignment="+message+"&publicKey="+publicKey+"&signature="+signature, { method: "POST" }).then(http => {
        return http.json();
    }).then(response => {
        document.getElementById("setMinimumResultText").innerText = escapeHtml(response);
        $("#setMinimumResult").show();
    });
}

if (getCookie("theme") === "light") {
    loadCss("https://stackpath.bootstrapcdn.com/bootswatch/4.3.1/flatly/bootstrap.min.css");
} else {
    loadCss("https://stackpath.bootstrapcdn.com/bootswatch/4.3.1/darkly/bootstrap.min.css");
}

getPoolInfo();
getCurrentRound();
getMiners();
getTop10Miners();

setInterval(getCurrentRound, 1000);
setInterval(getMiners, 60000); // TODO only refresh this when we detect that we forged a block
setInterval(getTop10Miners, 60000);
