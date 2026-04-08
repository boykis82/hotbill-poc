package com.billing.hotbill.impl.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 핫빌취소로그 엔티티
 */
public class CancellationLogEntity {
    private String contractNumber;
    private LocalDate hotbillDate;
    private Integer seqNo;
    private String tableName;
    private String snapshotData;  // JSON
    private LocalDateTime createDatetime;

    public CancellationLogEntity() {
    }

    public CancellationLogEntity(String contractNumber, LocalDate hotbillDate, Integer seqNo,
                                 String tableName, String snapshotData) {
        this.contractNumber = contractNumber;
        this.hotbillDate = hotbillDate;
        this.seqNo = seqNo;
        this.tableName = tableName;
        this.snapshotData = snapshotData;
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }
}
