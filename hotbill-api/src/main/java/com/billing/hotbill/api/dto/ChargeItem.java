package com.billing.hotbill.api.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 과금 항목 DTO
 */
public class ChargeItem {

    private final String chargeCode;
    private final BigDecimal amount;

    /**
     * 과금 항목 생성자
     *
     * @param chargeCode 과금항목코드 (필수)
     * @param amount 청구금액 (필수, 음수 불가)
     * @throws NullPointerException 필수 파라미터가 null인 경우
     * @throws IllegalArgumentException 금액이 음수인 경우
     */
    public ChargeItem(String chargeCode, BigDecimal amount) {
        this.chargeCode = Objects.requireNonNull(chargeCode, "과금항목코드는 필수입니다");
        this.amount = Objects.requireNonNull(amount, "청구금액은 필수입니다");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("청구금액은 음수가 될 수 없습니다: " + amount);
        }
    }

    public String getChargeCode() {
        return chargeCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChargeItem that = (ChargeItem) o;
        return Objects.equals(chargeCode, that.chargeCode) &&
               Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeCode, amount);
    }

    @Override
    public String toString() {
        return "ChargeItem{" +
               "chargeCode='" + chargeCode + '\'' +
               ", amount=" + amount +
               '}';
    }
}
