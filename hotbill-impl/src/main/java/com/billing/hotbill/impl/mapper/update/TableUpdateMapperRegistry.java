package com.billing.hotbill.impl.mapper.update;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TableUpdateMapper Registry
 * Spring이 자동으로 모든 TableUpdateMapper 구현체를 주입하고,
 * 테이블명으로 Mapper를 조회할 수 있도록 관리
 */
@Component
public class TableUpdateMapperRegistry {

    private final Map<String, TableUpdateMapper> mapperMap;

    public TableUpdateMapperRegistry(
            ContractTableUpdateMapper contractMapper,
            ServiceTableUpdateMapper serviceMapper) {
        this.mapperMap = new HashMap<>();
        this.mapperMap.put("TB_CONTRACT", contractMapper);
        this.mapperMap.put("TB_SERVICE", serviceMapper);
    }

    /**
     * 테이블명으로 Mapper 조회
     */
    public TableUpdateMapper getMapper(String tableName) {
        TableUpdateMapper mapper = mapperMap.get(tableName);
        if (mapper == null) {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }
        return mapper;
    }

    /**
     * 등록된 모든 테이블명 조회 (테스트용)
     */
    public List<String> getAllTableNames() {
        return mapperMap.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }
}
