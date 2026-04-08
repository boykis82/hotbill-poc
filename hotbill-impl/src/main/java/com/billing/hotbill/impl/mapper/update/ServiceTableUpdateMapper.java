package com.billing.hotbill.impl.mapper.update;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * TB_SERVICE 테이블 UPDATE Mapper
 */
@Mapper
public interface ServiceTableUpdateMapper extends TableUpdateMapper {

    @Override
    default String getTableName() {
        return "TB_SERVICE";
    }

    /**
     * 서비스 종료일자 갱신
     */
    void updateServiceEndDate(
        @Param("contractNumber") String contractNumber,
        @Param("endDate") LocalDateTime endDate
    );

    @Override
    default void update(Map<String, Object> updateData) {
        String contractNumber = (String) updateData.get("contractNumber");
        LocalDateTime endDate = (LocalDateTime) updateData.get("endDate");
        updateServiceEndDate(contractNumber, endDate);
    }
}
