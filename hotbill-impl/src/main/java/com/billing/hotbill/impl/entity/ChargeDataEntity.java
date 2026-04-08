package com.billing.hotbill.impl.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 과금자료 엔티티
 */
public class ChargeDataEntity {
    private String contractNumber;
    private LocalDate hotbillDate;
    private String chargeCode;
    private BigDecimal amount;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime createDatetime;

    public ChargeDataEntity() {
    }

    public ChargeDataEntity(String contractNumber, LocalDate hotbillDate, String chargeCode, BigDecimal amount) {
        this.contractNumber = contractNumber;
        this.hotbillDate = hotbillDate;
        this.chargeCode = chargeCode;
        this.amount = amount;
        this.createDatetime = LocalDateTime.now();
    }

    public boolean hasError() {
        return errorCode != null && !errorCode.isEmpty();
    }

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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }
}
