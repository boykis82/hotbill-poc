package com.billing.hotbill.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Hotbill 처리 결과 DTO
 */
public class HotbillResult {

    private final List<BillingInfo> successList;
    private final List<FailureInfo> failureList;
    private boolean asyncProcessing;

    /**
     * Hotbill 결과 생성자
     *
     * @param successList 성공 리스트
     * @param failureList 실패 리스트
     * @param asyncProcessing 비동기 처리 여부
     */
    public HotbillResult(List<BillingInfo> successList, List<FailureInfo> failureList,
                        boolean asyncProcessing) {
        this.successList = successList != null
            ? new ArrayList<>(successList)
            : new ArrayList<>();
        this.failureList = failureList != null
            ? new ArrayList<>(failureList)
            : new ArrayList<>();
        this.asyncProcessing = asyncProcessing;
    }

    /**
     * 다른 결과와 병합 (동기/비동기 결과 통합용)
     *
     * @param other 병합할 결과
     */
    public void merge(HotbillResult other) {
        if (other == null) {
            return;
        }

        if (other.getSuccessList() != null) {
            this.successList.addAll(other.getSuccessList());
        }
        if (other.getFailureList() != null) {
            this.failureList.addAll(other.getFailureList());
        }
        // 하나라도 비동기면 비동기로 표시
        this.asyncProcessing = this.asyncProcessing || other.isAsyncProcessing();
    }

    /**
     * 비동기 처리 결과 생성
     *
     * @param contractNumbers 계약번호 리스트
     * @return 비동기 처리 결과 객체
     */
    public static HotbillResult asyncProcessing(List<String> contractNumbers) {
        return new HotbillResult(Collections.emptyList(), Collections.emptyList(), true);
    }

    public List<BillingInfo> getSuccessList() {
        return Collections.unmodifiableList(successList);
    }

    public List<FailureInfo> getFailureList() {
        return Collections.unmodifiableList(failureList);
    }

    public boolean isAsyncProcessing() {
        return asyncProcessing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotbillResult that = (HotbillResult) o;
        return asyncProcessing == that.asyncProcessing &&
               Objects.equals(successList, that.successList) &&
               Objects.equals(failureList, that.failureList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successList, failureList, asyncProcessing);
    }

    @Override
    public String toString() {
        return "HotbillResult{" +
               "successList=" + successList +
               ", failureList=" + failureList +
               ", asyncProcessing=" + asyncProcessing +
               '}';
    }
}
