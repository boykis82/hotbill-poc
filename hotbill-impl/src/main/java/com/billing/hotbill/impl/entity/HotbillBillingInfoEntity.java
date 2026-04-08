package com.billing.hotbill.impl.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 핫빌청구정보 엔티티
 */
public class HotbillBillingInfoEntity {
    private String contractNumber;
    private LocalDate hotbillDate;
    private Integer seqNo;
    private String chargeCode;
    private BigDecimal amount;
    private String settlementYn;
    private LocalDateTime createDatetime;
    private LocalDateTime updateDatetime;

    public HotbillBillingInfoEntity() {
    }

    public HotbillBillingInfoEntity(String contractNumber, LocalDate hotbillDate, Integer seqNo,
                                    String chargeCode, BigDecimal amount) {
        this.contractNumber = contractNumber;
        this.hotbillDate = hotbillDate;
        this.seqNo = seqNo;
        this.chargeCode = chargeCode;
        this.amount = amount;
        this.settlementYn = "N";
        this.createDatetime = LocalDateTime.now();
        this.updateDatetime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public LocalDate getHotbillDate() {
        return hotbillDate;
    }

    public void setHotbillDate(LocalDate hotbillDate) {
        this.hotbillDate = hotbillDate;
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

    public String getSettlementYn() {
        return settlementYn;
    }

    public void setSettlementYn(String settlementYn) {
        this.settlementYn = settlementYn;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }

    public LocalDateTime getUpdateDatetime() {
        return updateDatetime;
    }

    public void setUpdateDatetime(LocalDateTime updateDatetime) {
        this.updateDatetime = updateDatetime;
    }
}
