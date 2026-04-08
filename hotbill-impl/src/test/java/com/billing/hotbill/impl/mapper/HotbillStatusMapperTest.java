package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.api.enums.HotbillUsecase;
import com.billing.hotbill.impl.entity.HotbillStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("HotbillStatusMapper 테스트")
class HotbillStatusMapperTest {

    @Autowired
    private HotbillStatusMapper mapper;

    private HotbillStatusEntity testEntity;

    @BeforeEach
    void setUp() {
        testEntity = new HotbillStatusEntity(
            "CTR001",
            LocalDate.now(),
            HotbillUsecase.SIMPLE_INQUIRY,
            HotbillStatus.REQUESTING,
            LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("insert - 핫빌 진행상태 저장")
    void testInsert() {
        // when
        mapper.insert(testEntity);

        // then
        HotbillStatusEntity found = mapper.selectByContractNumber(testEntity.getContractNumber(), testEntity.getHotbillDate());
        assertThat(found).isNotNull();
        assertThat(found.getContractNumber()).isEqualTo(testEntity.getContractNumber());
        assertThat(found.getStatus()).isEqualTo(HotbillStatus.REQUESTING);
    }

    @Test
    @DisplayName("updateStatus - 단건 상태 갱신")
    void testUpdateStatus() {
        // given
        mapper.insert(testEntity);

        // when
        mapper.updateStatus(testEntity.getContractNumber(), testEntity.getHotbillDate(), HotbillStatus.COMPLETED);

        // then
        HotbillStatusEntity found = mapper.selectByContractNumber(testEntity.getContractNumber(), testEntity.getHotbillDate());
        assertThat(found.getStatus()).isEqualTo(HotbillStatus.COMPLETED);
    }

    @Test
    @DisplayName("updateStatusBatch - 다건 상태 갱신")
    void testUpdateStatusBatch() {
        // given
        HotbillStatusEntity entity1 = new HotbillStatusEntity("CTR001", LocalDate.now(), HotbillUsecase.SIMPLE_INQUIRY, HotbillStatus.REQUESTING, LocalDateTime.now());
        HotbillStatusEntity entity2 = new HotbillStatusEntity("CTR002", LocalDate.now(), HotbillUsecase.SIMPLE_INQUIRY, HotbillStatus.REQUESTING, LocalDateTime.now());
        mapper.insert(entity1);
        mapper.insert(entity2);

        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");

        // when
        mapper.updateStatusBatch(contractNumbers, LocalDate.now(), HotbillStatus.COMPLETED);

        // then
        HotbillStatusEntity found1 = mapper.selectByContractNumber("CTR001", LocalDate.now());
        HotbillStatusEntity found2 = mapper.selectByContractNumber("CTR002", LocalDate.now());
        assertThat(found1.getStatus()).isEqualTo(HotbillStatus.COMPLETED);
        assertThat(found2.getStatus()).isEqualTo(HotbillStatus.COMPLETED);
    }

    @Test
    @DisplayName("selectByContractNumber - 계약번호로 조회")
    void testSelectByContractNumber() {
        // given
        mapper.insert(testEntity);

        // when
        HotbillStatusEntity found = mapper.selectByContractNumber(testEntity.getContractNumber(), testEntity.getHotbillDate());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getContractNumber()).isEqualTo(testEntity.getContractNumber());
    }

    @Test
    @DisplayName("findNotCompletedContracts - 미완료 계약 조회")
    void testFindNotCompletedContracts() {
        // given
        HotbillStatusEntity entity1 = new HotbillStatusEntity("CTR001", LocalDate.now(), HotbillUsecase.SIMPLE_INQUIRY, HotbillStatus.COMPLETED, LocalDateTime.now());
        HotbillStatusEntity entity2 = new HotbillStatusEntity("CTR002", LocalDate.now(), HotbillUsecase.SIMPLE_INQUIRY, HotbillStatus.REQUESTING, LocalDateTime.now());
        HotbillStatusEntity entity3 = new HotbillStatusEntity("CTR003", LocalDate.now(), HotbillUsecase.SIMPLE_INQUIRY, HotbillStatus.FAILED, LocalDateTime.now());
        mapper.insert(entity1);
        mapper.insert(entity2);
        mapper.insert(entity3);

        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002", "CTR003");

        // when
        List<String> notCompletedContracts = mapper.findNotCompletedContracts(contractNumbers, LocalDate.now());

        // then
        assertThat(notCompletedContracts).hasSize(2);
        assertThat(notCompletedContracts).containsExactlyInAnyOrder("CTR002", "CTR003");
    }

    @Test
    @DisplayName("findNotSettledContracts - 미정산 계약 조회")
    void testFindNotSettledContracts() {
        // given
        HotbillStatusEntity entity1 = new HotbillStatusEntity("CTR001", LocalDate.now(), HotbillUsecase.TERMINATION_INQUIRY, HotbillStatus.SETTLEMENT_COMPLETED, LocalDateTime.now());
        HotbillStatusEntity entity2 = new HotbillStatusEntity("CTR002", LocalDate.now(), HotbillUsecase.TERMINATION_INQUIRY, HotbillStatus.COMPLETED, LocalDateTime.now());
        mapper.insert(entity1);
        mapper.insert(entity2);

        List<String> contractNumbers = Arrays.asList("CTR001", "CTR002");

        // when
        List<String> notSettledContracts = mapper.findNotSettledContracts(contractNumbers, LocalDate.now());

        // then
        assertThat(notSettledContracts).hasSize(1);
        assertThat(notSettledContracts).containsExactly("CTR002");
    }
}
