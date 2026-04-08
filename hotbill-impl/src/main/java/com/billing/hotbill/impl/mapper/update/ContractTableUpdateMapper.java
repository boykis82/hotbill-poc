package com.billing.hotbill.impl.mapper.update;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * TB_CONTRACT 테이블 UPDATE Mapper
 */
@Mapper
public interface ContractTableUpdateMapper extends TableUpdateMapper {

    @Override
    default String getTableName() {
        return "TB_CONTRACT";
    }

    /**
     * 해지일자 갱신
     */
    void updateTerminationDate(
        @Param("contractNumber") String contractNumber,
        @Param("terminationDate") LocalDateTime terminationDate
    );

    @Override
    default void update(Map<String, Object> updateData) {
        String contractNumber = (String) updateData.get("contractNumber");
        LocalDateTime terminationDate = (LocalDateTime) updateData.get("terminationDate");
        updateTerminationDate(contractNumber, terminationDate);
    }
}
