package com.billing.hotbill.api.enums;

/**
 * Hotbill Usecase
 */
public enum HotbillUsecase {
    /**
     * 단순 요금 조회
     */
    SIMPLE_INQUIRY("단순 요금 조회"),

    /**
     * 해지 요금 조회
     */
    TERMINATION_INQUIRY("해지 요금 조회"),

    /**
     * 번호이동 portout
     */
    PORT_OUT("번호이동 portout"),

    /**
     * 직권해지를 위한 요금 조회
     */
    FORCED_TERMINATION("직권해지를 위한 요금 조회");

    private final String description;

    HotbillUsecase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
