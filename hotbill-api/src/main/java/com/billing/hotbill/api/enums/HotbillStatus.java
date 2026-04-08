package com.billing.hotbill.api.enums;

/**
 * Hotbill 진행상태
 */
public enum HotbillStatus {
    /**
     * 요청중
     */
    REQUESTING("요청중"),

    /**
     * 요금계산 전
     */
    BEFORE_CALCULATION("요금계산 전"),

    /**
     * 과금자료 오류
     */
    CHARGE_DATA_ERROR("과금자료 오류"),

    /**
     * 요금계산 완료
     */
    COMPLETED("요금계산 완료"),

    /**
     * 실패
     */
    FAILED("실패"),

    /**
     * 해지정산 완료
     */
    SETTLEMENT_COMPLETED("해지정산 완료");

    private final String description;

    HotbillStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
