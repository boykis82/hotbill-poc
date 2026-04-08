package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.ChargeDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 과금자료 Mapper
 */
@Mapper
public interface ChargeDataMapper {

    /**
     * 과금자료 저장
     */
    void insert(ChargeDataEntity entity);

    /**
     * 계약번호로 과금자료 삭제 (재저장 전 기존 데이터 삭제용)
     */
    void deleteByContractNumber(
        @Param("contractNumber") String contractNumber,
        @Param("hotbillDate") LocalDate hotbillDate
    );

    /**
     * 계약번호로 과금자료 조회
     */
    List<ChargeDataEntity> selectByContractNumber(
        @Param("contractNumber") String contractNumber,
        @Param("hotbillDate") LocalDate hotbillDate
    );
}
