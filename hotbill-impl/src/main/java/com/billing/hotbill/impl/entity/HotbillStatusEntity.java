package com.billing.hotbill.impl.entity;

import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.api.enums.HotbillUsecase;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 핫빌 진행상태 엔티티
 */
public class HotbillStatusEntity {
    private String contractNumber;
    private LocalDate hotbillDate;
    private HotbillUsecase usecase;
    private HotbillStatus status;
    private LocalDateTime requestDatetime;
    private LocalDateTime updateDatetime;
    private String errorMessage;

    public HotbillStatusEntity() {
    }

    public HotbillStatusEntity(String contractNumber, LocalDate hotbillDate, HotbillUsecase usecase,
                               HotbillStatus status, LocalDateTime requestDatetime) {
        this.contractNumber = contractNumber;
        this.hotbillDate = hotbillDate;
        this.usecase = usecase;
        this.status = status;
        this.requestDatetime = requestDatetime;
        this.updateDatetime = LocalDateTime.now();
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

    public HotbillUsecase getUsecase() {
        return usecase;
    }

    public void setUsecase(HotbillUsecase usecase) {
        this.usecase = usecase;
    }

    public HotbillStatus getStatus() {
        return status;
    }

    public void setStatus(HotbillStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestDatetime() {
        return requestDatetime;
    }

    public void setRequestDatetime(LocalDateTime requestDatetime) {
        this.requestDatetime = requestDatetime;
    }

    public LocalDateTime getUpdateDatetime() {
        return updateDatetime;
    }

    public void setUpdateDatetime(LocalDateTime updateDatetime) {
        this.updateDatetime = updateDatetime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
