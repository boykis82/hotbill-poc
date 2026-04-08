package com.billing.hotbill.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 청구정보 DTO
 */
public class BillingInfo {

    private final String contractNumber;
    private final LocalDate hotbillDate;
    private final BigDecimal totalAmount;
    private final List<ChargeItem> chargeItems;

    /**
     * 청구정보 생성자
     *
     * @param contractNumber 계약번호 (필수)
     * @param hotbillDate Hotbill일자 (필수)
     * @param totalAmount 총 청구금액 (필수)
     * @param chargeItems 과금항목 리스트
     */
    public BillingInfo(String contractNumber, LocalDate hotbillDate,
                      BigDecimal totalAmount, List<ChargeItem> chargeItems) {
        this.contractNumber = Objects.requireNonNull(contractNumber, "계약번호는 필수입니다");
        this.hotbillDate = Objects.requireNonNull(hotbillDate, "Hotbill일자는 필수입니다");
        this.totalAmount = Objects.requireNonNull(totalAmount, "총 청구금액은 필수입니다");
        this.chargeItems = chargeItems != null
            ? Collections.unmodifiableList(new ArrayList<>(chargeItems))
            : Collections.emptyList();
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public LocalDate getHotbillDate() {
        return hotbillDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<ChargeItem> getChargeItems() {
        return chargeItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillingInfo that = (BillingInfo) o;
        return Objects.equals(contractNumber, that.contractNumber) &&
               Objects.equals(hotbillDate, that.hotbillDate) &&
               Objects.equals(totalAmount, that.totalAmount) &&
               Objects.equals(chargeItems, that.chargeItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractNumber, hotbillDate, totalAmount, chargeItems);
    }

    @Override
    public String toString() {
        return "BillingInfo{" +
               "contractNumber='" + contractNumber + '\'' +
               ", hotbillDate=" + hotbillDate +
               ", totalAmount=" + totalAmount +
               ", chargeItems=" + chargeItems +
               '}';
    }
}
