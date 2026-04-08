package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.CancellationLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 핫빌취소로그 Mapper
 */
@Mapper
public interface CancellationLogMapper {

    /**
     * 취소로그 저장
     */
    void insert(CancellationLogEntity entity);

    /**
     * 계약번호 리스트로 취소로그 조회
     */
    List<CancellationLogEntity> selectByContracts(
        @Param("contractNumbers") List<String> contractNumbers,
        @Param("hotbillDate") LocalDate hotbillDate
    );
}
