package com.billing.hotbill.api.enums;

/**
 * 계약 유형
 */
public enum ContractType {
    /**
     * 이동전화
     */
    MOBILE_PHONE("이동전화"),

    /**
     * 인터넷
     */
    INTERNET("인터넷"),

    /**
     * IPTV
     */
    IPTV("IPTV");

    private final String description;

    ContractType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
