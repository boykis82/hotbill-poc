package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.BillingInfoEntity;
import com.billing.hotbill.impl.entity.HotbillBillingInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 청구정보 Mapper
 */
@Mapper
public interface BillingInfoMapper {

    /**
     * 핫빌청구정보 저장
     */
    void insertHotbillBillingInfo(HotbillBillingInfoEntity entity);

    /**
     * 핫빌청구정보 조회
     */
    List<HotbillBillingInfoEntity> selectHotbillBillingInfo(
        @Param("contractNumber") String contractNumber,
        @Param("hotbillDate") LocalDate hotbillDate
    );

    /**
     * 핫빌청구정보 해지정산여부 갱신
     */
    void updateSettlementYn(
        @Param("contractNumbers") List<String> contractNumbers,
        @Param("hotbillDate") LocalDate hotbillDate,
        @Param("settlementYn") String settlementYn
    );

    /**
     * 청구정보 저장 (해지정산 이관용)
     */
    void insertBillingInfo(BillingInfoEntity entity);

    /**
     * 핫빌 데이터 삭제 (당일취소 시)
     */
    void deleteHotbillData(
        @Param("contractNumbers") List<String> contractNumbers,
        @Param("billingMonth") String billingMonth
    );

    /**
     * 청구정보 조회 (테스트용)
     */
    List<BillingInfoEntity> selectBillingInfoByContract(
        @Param("contractNumber") String contractNumber,
        @Param("billingMonth") String billingMonth
    );
}
