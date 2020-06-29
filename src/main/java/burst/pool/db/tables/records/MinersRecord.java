/*
 * This file is generated by jOOQ.
 */
package burst.pool.db.tables.records;


import burst.pool.db.tables.Miners;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MinersRecord extends UpdatableRecordImpl<MinersRecord> implements Record9<Long, Long, Long, Double, Double, Long, String, String, Integer> {

    private static final long serialVersionUID = -466185277;

    /**
     * Setter for <code>miners.db_id</code>.
     */
    public void setDbId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>miners.db_id</code>.
     */
    public Long getDbId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>miners.account_id</code>.
     */
    public void setAccountId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>miners.account_id</code>.
     */
    public Long getAccountId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>miners.pending_balance</code>.
     */
    public void setPendingBalance(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>miners.pending_balance</code>.
     */
    public Long getPendingBalance() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>miners.estimated_capacity</code>.
     */
    public void setEstimatedCapacity(Double value) {
        set(3, value);
    }

    /**
     * Getter for <code>miners.estimated_capacity</code>.
     */
    public Double getEstimatedCapacity() {
        return (Double) get(3);
    }

    /**
     * Setter for <code>miners.share</code>.
     */
    public void setShare(Double value) {
        set(4, value);
    }

    /**
     * Getter for <code>miners.share</code>.
     */
    public Double getShare() {
        return (Double) get(4);
    }

    /**
     * Setter for <code>miners.minimum_payout</code>.
     */
    public void setMinimumPayout(Long value) {
        set(5, value);
    }

    /**
     * Getter for <code>miners.minimum_payout</code>.
     */
    public Long getMinimumPayout() {
        return (Long) get(5);
    }

    /**
     * Setter for <code>miners.name</code>.
     */
    public void setName(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>miners.name</code>.
     */
    public String getName() {
        return (String) get(6);
    }

    /**
     * Setter for <code>miners.user_agent</code>.
     */
    public void setUserAgent(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>miners.user_agent</code>.
     */
    public String getUserAgent() {
        return (String) get(7);
    }

    /**
     * Setter for <code>miners.share_percent</code>.
     */
    public void setSharePercent(Integer value) {
        set(8, value);
    }

    /**
     * Getter for <code>miners.share_percent</code>.
     */
    public Integer getSharePercent() {
        return (Integer) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<Long, Long, Long, Double, Double, Long, String, String, Integer> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<Long, Long, Long, Double, Double, Long, String, String, Integer> valuesRow() {
        return (Row9) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Miners.MINERS.DB_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return Miners.MINERS.ACCOUNT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return Miners.MINERS.PENDING_BALANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field4() {
        return Miners.MINERS.ESTIMATED_CAPACITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field5() {
        return Miners.MINERS.SHARE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field6() {
        return Miners.MINERS.MINIMUM_PAYOUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Miners.MINERS.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return Miners.MINERS.USER_AGENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field9() {
        return Miners.MINERS.SHARE_PERCENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getDbId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component2() {
        return getAccountId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component3() {
        return getPendingBalance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double component4() {
        return getEstimatedCapacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double component5() {
        return getShare();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component6() {
        return getMinimumPayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component8() {
        return getUserAgent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component9() {
        return getSharePercent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getDbId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getAccountId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getPendingBalance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value4() {
        return getEstimatedCapacity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value5() {
        return getShare();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value6() {
        return getMinimumPayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getUserAgent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value9() {
        return getSharePercent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value1(Long value) {
        setDbId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value2(Long value) {
        setAccountId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value3(Long value) {
        setPendingBalance(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value4(Double value) {
        setEstimatedCapacity(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value5(Double value) {
        setShare(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value6(Long value) {
        setMinimumPayout(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value7(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value8(String value) {
        setUserAgent(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord value9(Integer value) {
        setSharePercent(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinersRecord values(Long value1, Long value2, Long value3, Double value4, Double value5, Long value6, String value7, String value8, Integer value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MinersRecord
     */
    public MinersRecord() {
        super(Miners.MINERS);
    }

    /**
     * Create a detached, initialised MinersRecord
     */
    public MinersRecord(Long dbId, Long accountId, Long pendingBalance, Double estimatedCapacity, Double share, Long minimumPayout, String name, String userAgent, Integer sharePercent) {
        super(Miners.MINERS);

        set(0, dbId);
        set(1, accountId);
        set(2, pendingBalance);
        set(3, estimatedCapacity);
        set(4, share);
        set(5, minimumPayout);
        set(6, name);
        set(7, userAgent);
        set(8, sharePercent);
    }
}
