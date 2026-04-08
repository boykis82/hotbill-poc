package com.billing.hotbill.impl.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 청구정보 엔티티
 */
public class BillingInfoEntity {
    private String contractNumber;
    private String billingMonth;
    private Integer seqNo;
    private String chargeCode;
    private BigDecimal amount;
    private String hotbillYn;
    private LocalDateTime createDatetime;

    public BillingInfoEntity() {
    }

    public BillingInfoEntity(String contractNumber, String billingMonth, Integer seqNo,
                             String chargeCode, BigDecimal amount) {
        this.contractNumber = contractNumber;
        this.billingMonth = billingMonth;
        this.seqNo = seqNo;
        this.chargeCode = chargeCode;
        this.amount = amount;
        this.hotbillYn = "N";
        this.createDatetime = LocalDateTime.now();
    }

    public BillingInfoEntity(String contractNumber, String billingMonth, Integer seqNo,
                             String chargeCode, BigDecimal amount, String hotbillYn) {
        this.contractNumber = contractNumber;
        this.billingMonth = billingMonth;
        this.seqNo = seqNo;
        this.chargeCode = chargeCode;
        this.amount = amount;
        this.hotbillYn = hotbillYn;
        this.createDatetime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(String billingMonth) {
        this.billingMonth = billingMonth;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public String getChargeCode() {
        return chargeCode;
    }

    public void setChargeCode(String chargeCode) {
        this.chargeCode = chargeCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getHotbillYn() {
        return hotbillYn;
    }

    public void setHotbillYn(String hotbillYn) {
        this.hotbillYn = hotbillYn;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }
}
