package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.ChargeDataEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("ChargeDataMapper 테스트")
class ChargeDataMapperTest {

    @Autowired
    private ChargeDataMapper mapper;

    private ChargeDataEntity testEntity;

    @BeforeEach
    void setUp() {
        testEntity = new ChargeDataEntity(
            "CTR001",
            LocalDate.now(),
            "CHG001",
            new BigDecimal("10000.00")
        );
    }

    @Test
    @DisplayName("insert - 과금자료 저장")
    void testInsert() {
        // when
        mapper.insert(testEntity);

        // then
        List<ChargeDataEntity> found = mapper.selectByContractNumber(testEntity.getContractNumber(), testEntity.getHotbillDate());
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getContractNumber()).isEqualTo(testEntity.getContractNumber());
        assertThat(found.get(0).getChargeCode()).isEqualTo(testEntity.getChargeCode());
        assertThat(found.get(0).getAmount()).isEqualByComparingTo(testEntity.getAmount());
    }

    @Test
    @DisplayName("insert - 여러 과금자료 저장")
    void testInsertMultiple() {
        // given
        ChargeDataEntity entity1 = new ChargeDataEntity("CTR001", LocalDate.now(), "CHG001", new BigDecimal("10000"));
        ChargeDataEntity entity2 = new ChargeDataEntity("CTR001", LocalDate.now(), "CHG002", new BigDecimal("20000"));

        // when
        mapper.insert(entity1);
        mapper.insert(entity2);

        // then
        List<ChargeDataEntity> found = mapper.selectByContractNumber("CTR001", LocalDate.now());
        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("deleteByContractNumber - 계약번호로 삭제")
    void testDeleteByContractNumber() {
        // given
        ChargeDataEntity entity1 = new ChargeDataEntity("CTR001", LocalDate.now(), "CHG001", new BigDecimal("10000"));
        ChargeDataEntity entity2 = new ChargeDataEntity("CTR001", LocalDate.now(), "CHG002", new BigDecimal("20000"));
        mapper.insert(entity1);
        mapper.insert(entity2);

        // when
        mapper.deleteByContractNumber("CTR001", LocalDate.now());

        // then
        List<ChargeDataEntity> found = mapper.selectByContractNumber("CTR001", LocalDate.now());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("selectByContractNumber - 계약번호로 조회")
    void testSelectByContractNumber() {
        // given
        mapper.insert(testEntity);

        // when
        List<ChargeDataEntity> found = mapper.selectByContractNumber(testEntity.getContractNumber(), testEntity.getHotbillDate());

        // then
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getContractNumber()).isEqualTo(testEntity.getContractNumber());
    }

    @Test
    @DisplayName("insert - 오류 코드가 있는 과금자료 저장")
    void testInsertWithError() {
        // given
        ChargeDataEntity errorEntity = new ChargeDataEntity("CTR002", LocalDate.now(), "CHG001", BigDecimal.ZERO);
        errorEntity.setErrorCode("ERR001");
        errorEntity.setErrorMessage("과금자료 조회 실패");

        // when
        mapper.insert(errorEntity);

        // then
        List<ChargeDataEntity> found = mapper.selectByContractNumber("CTR002", LocalDate.now());
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).hasError()).isTrue();
        assertThat(found.get(0).getErrorCode()).isEqualTo("ERR001");
        assertThat(found.get(0).getErrorMessage()).isEqualTo("과금자료 조회 실패");
    }

    @Test
    @DisplayName("deleteByContractNumber 후 insert - 기존 데이터 삭제 후 재저장")
    void testDeleteAndReinsert() {
        // given - 기존 데이터 저장
        ChargeDataEntity oldEntity = new ChargeDataEntity("CTR001", LocalDate.now(), "CHG001", new BigDecimal("10000"));
        mapper.insert(oldEntity);

        // when - 삭제 후 새 데이터 저장
        mapper.deleteByContractNumber("CTR001", LocalDate.now());
        ChargeDataEntity newEntity = new ChargeDataEntity("CTR001", LocalDate.now(), "CHG002", new BigDecimal("20000"));
        mapper.insert(newEntity);

        // then
        List<ChargeDataEntity> found = mapper.selectByContractNumber("CTR001", LocalDate.now());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getChargeCode()).isEqualTo("CHG002");
        assertThat(found.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("20000"));
    }
}
