package com.billing.hotbill.impl.mapper.update;

import java.util.Map;

/**
 * 테이블별 UPDATE Mapper 인터페이스
 * Registry Pattern으로 테이블별 UPDATE 쿼리를 유연하게 관리
 */
public interface TableUpdateMapper {

    /**
     * 테이블명 반환
     */
    String getTableName();

    /**
     * 테이블 UPDATE 수행
     * @param updateData 업데이트할 데이터 (Map 형태)
     */
    void update(Map<String, Object> updateData);
}
