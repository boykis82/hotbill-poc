package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.entity.HotbillStatusEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 핫빌 진행상태 Mapper
 */
@Mapper
public interface HotbillStatusMapper {

    /**
     * 핫빌 진행상태 저장
     */
    void insert(HotbillStatusEntity entity);

    /**
     * 단건 상태 갱신
     */
    void updateStatus(
        @Param("contractNumber") String contractNumber,
        @Param("hotbillDate") LocalDate hotbillDate,
        @Param("status") HotbillStatus status
    );

    /**
     * 다건 상태 갱신
     */
    void updateStatusBatch(
        @Param("contractNumbers") List<String> contractNumbers,
        @Param("hotbillDate") LocalDate hotbillDate,
        @Param("status") HotbillStatus status
    );

    /**
     * 계약번호로 조회
     */
    HotbillStatusEntity selectByContractNumber(
        @Param("contractNumber") String contractNumber,
        @Param("hotbillDate") LocalDate hotbillDate
    );

    /**
     * 미완료 계약 조회 (COMPLETED 상태가 아닌 것들)
     */
    List<String> findNotCompletedContracts(
        @Param("contractNumbers") List<String> contractNumbers,
        @Param("hotbillDate") LocalDate hotbillDate
    );

    /**
     * 미정산 계약 조회 (SETTLEMENT_COMPLETED 상태가 아닌 것들)
     */
    List<String> findNotSettledContracts(
        @Param("contractNumbers") List<String> contractNumbers,
        @Param("hotbillDate") LocalDate hotbillDate
    );
}
