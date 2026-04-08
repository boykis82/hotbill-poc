package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.CancellationLogEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("CancellationLogMapper 테스트")
class CancellationLogMapperTest {

    @Autowired
    private CancellationLogMapper mapper;

    @Test
    @DisplayName("insert - 취소로그 저장")
    void testInsert() {
        // given
        String snapshotJson = "{\"tableName\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"beforeData\":{\"terminationDate\":null,\"terminationYn\":\"N\"}}";
        CancellationLogEntity entity = new CancellationLogEntity("CTR001", LocalDate.now(), 1, "TB_CONTRACT", snapshotJson);

        // when
        mapper.insert(entity);

        // then
        List<CancellationLogEntity> found = mapper.selectByContracts(Arrays.asList("CTR001"), LocalDate.now());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTableName()).isEqualTo("TB_CONTRACT");
        assertThat(found.get(0).getSnapshotData()).isEqualTo(snapshotJson);
    }

    @Test
    @DisplayName("selectByContracts - 계약번호 리스트로 조회")
    void testSelectByContracts() {
        // given
        CancellationLogEntity entity1 = new CancellationLogEntity("CTR001", LocalDate.now(), 1, "TB_CONTRACT", "{\"data\":\"test1\"}");
        CancellationLogEntity entity2 = new CancellationLogEntity("CTR001", LocalDate.now(), 2, "TB_SERVICE", "{\"data\":\"test2\"}");
        CancellationLogEntity entity3 = new CancellationLogEntity("CTR002", LocalDate.now(), 1, "TB_CONTRACT", "{\"data\":\"test3\"}");
        mapper.insert(entity1);
        mapper.insert(entity2);
        mapper.insert(entity3);

        // when
        List<CancellationLogEntity> found = mapper.selectByContracts(Arrays.asList("CTR001", "CTR002"), LocalDate.now());

        // then
        assertThat(found).hasSize(3);
    }

    @Test
    @DisplayName("insert - JSON 스냅샷 데이터 파싱 테스트")
    void testJsonSnapshotDataParsing() {
        // given
        String complexJson = "{\"tableName\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"beforeData\":{\"terminationDate\":\"2026-03-05\",\"terminationYn\":\"Y\",\"status\":\"ACTIVE\"}}";
        CancellationLogEntity entity = new CancellationLogEntity("CTR001", LocalDate.now(), 1, "TB_CONTRACT", complexJson);

        // when
        mapper.insert(entity);

        // then
        List<CancellationLogEntity> found = mapper.selectByContracts(Arrays.asList("CTR001"), LocalDate.now());
        assertThat(found.get(0).getSnapshotData()).contains("terminationDate");
        assertThat(found.get(0).getSnapshotData()).contains("terminationYn");
        assertThat(found.get(0).getSnapshotData()).contains("status");
    }

    @Test
    @DisplayName("selectByContracts - 여러 테이블 로그 조회")
    void testSelectMultipleTables() {
        // given
        CancellationLogEntity contractLog = new CancellationLogEntity("CTR001", LocalDate.now(), 1, "TB_CONTRACT", "{\"table\":\"contract\"}");
        CancellationLogEntity serviceLog = new CancellationLogEntity("CTR001", LocalDate.now(), 2, "TB_SERVICE", "{\"table\":\"service\"}");
        CancellationLogEntity billingLog = new CancellationLogEntity("CTR001", LocalDate.now(), 3, "TB_BILLING", "{\"table\":\"billing\"}");
        mapper.insert(contractLog);
        mapper.insert(serviceLog);
        mapper.insert(billingLog);

        // when
        List<CancellationLogEntity> found = mapper.selectByContracts(Arrays.asList("CTR001"), LocalDate.now());

        // then
        assertThat(found).hasSize(3);
        assertThat(found).extracting("tableName")
            .containsExactlyInAnyOrder("TB_CONTRACT", "TB_SERVICE", "TB_BILLING");
    }
}
