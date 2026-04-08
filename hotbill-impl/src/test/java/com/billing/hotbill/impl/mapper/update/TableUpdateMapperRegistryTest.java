package com.billing.hotbill.impl.mapper.update;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MybatisTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TableUpdateMapperRegistry.class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("TableUpdateMapperRegistry 테스트")
class TableUpdateMapperRegistryTest {

    @Autowired
    private TableUpdateMapperRegistry registry;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("getMapper - TB_CONTRACT 테이블 Mapper 조회")
    void testGetContractMapper() {
        // when
        TableUpdateMapper mapper = registry.getMapper("TB_CONTRACT");

        // then
        assertThat(mapper).isNotNull();
        assertThat(mapper.getTableName()).isEqualTo("TB_CONTRACT");
        assertThat(mapper).isInstanceOf(ContractTableUpdateMapper.class);
    }

    @Test
    @DisplayName("getMapper - TB_SERVICE 테이블 Mapper 조회")
    void testGetServiceMapper() {
        // when
        TableUpdateMapper mapper = registry.getMapper("TB_SERVICE");

        // then
        assertThat(mapper).isNotNull();
        assertThat(mapper.getTableName()).isEqualTo("TB_SERVICE");
        assertThat(mapper).isInstanceOf(ServiceTableUpdateMapper.class);
    }

    @Test
    @DisplayName("getMapper - 존재하지 않는 테이블에 대한 예외 발생")
    void testGetMapperWithUnknownTable() {
        // when & then
        assertThatThrownBy(() -> registry.getMapper("TB_UNKNOWN"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown table");
    }

    @Test
    @DisplayName("getAllTableNames - 등록된 모든 테이블명 조회")
    void testGetAllTableNames() {
        // when
        List<String> tableNames = registry.getAllTableNames();

        // then
        assertThat(tableNames).contains("TB_CONTRACT", "TB_SERVICE");
        assertThat(tableNames).hasSize(2);
    }

    @Test
    @DisplayName("ContractTableUpdateMapper - 실제 UPDATE 수행 테스트")
    void testContractTableUpdate() {
        // given
        jdbcTemplate.execute("INSERT INTO TB_CONTRACT (CONTRACT_NUMBER, TERMINATION_YN) VALUES ('CTR001', 'N')");

        TableUpdateMapper mapper = registry.getMapper("TB_CONTRACT");
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("contractNumber", "CTR001");
        updateData.put("terminationDate", LocalDateTime.of(2026, 3, 5, 14, 30));

        // when
        mapper.update(updateData);

        // then
        String terminationYn = jdbcTemplate.queryForObject(
            "SELECT TERMINATION_YN FROM TB_CONTRACT WHERE CONTRACT_NUMBER = 'CTR001'",
            String.class
        );
        assertThat(terminationYn).isEqualTo("Y");
    }

    @Test
    @DisplayName("ServiceTableUpdateMapper - 실제 UPDATE 수행 테스트")
    void testServiceTableUpdate() {
        // given
        jdbcTemplate.execute("INSERT INTO TB_SERVICE (CONTRACT_NUMBER, SERVICE_CODE) VALUES ('CTR001', 'SVC001')");

        TableUpdateMapper mapper = registry.getMapper("TB_SERVICE");
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("contractNumber", "CTR001");
        updateData.put("endDate", LocalDateTime.of(2026, 3, 10, 10, 0));

        // when
        mapper.update(updateData);

        // then
        LocalDateTime endDate = jdbcTemplate.queryForObject(
            "SELECT END_DATE FROM TB_SERVICE WHERE CONTRACT_NUMBER = 'CTR001'",
            LocalDateTime.class
        );
        assertThat(endDate).isNotNull();
    }
}
