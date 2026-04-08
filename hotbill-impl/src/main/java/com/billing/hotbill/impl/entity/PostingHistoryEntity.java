package com.billing.hotbill.impl.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 포스팅이력 엔티티
 */
public class PostingHistoryEntity {
    private String contractNumber;
    private LocalDate hotbillDate;
    private Integer seqNo;
    private String postingData;  // JSON
    private LocalDateTime createDatetime;

    public PostingHistoryEntity() {
    }

    public PostingHistoryEntity(String contractNumber, LocalDate hotbillDate, Integer seqNo, String postingData) {
        this.contractNumber = contractNumber;
        this.hotbillDate = hotbillDate;
        this.seqNo = seqNo;
        this.postingData = postingData;
        this.createDatetime = LocalDateTime.now();
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

    public String getPostingData() {
        return postingData;
    }

    public void setPostingData(String postingData) {
        this.postingData = postingData;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }
}
