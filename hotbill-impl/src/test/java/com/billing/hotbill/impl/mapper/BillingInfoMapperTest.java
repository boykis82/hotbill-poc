package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.BillingInfoEntity;
import com.billing.hotbill.impl.entity.HotbillBillingInfoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("BillingInfoMapper 테스트")
class BillingInfoMapperTest {

    @Autowired
    private BillingInfoMapper mapper;

    @Test
    @DisplayName("insertHotbillBillingInfo - 핫빌청구정보 저장")
    void testInsertHotbillBillingInfo() {
        // given
        HotbillBillingInfoEntity entity = new HotbillBillingInfoEntity(
            "CTR001", LocalDate.now(), 1, "CHG001", new BigDecimal("10000")
        );

        // when
        mapper.insertHotbillBillingInfo(entity);

        // then
        List<HotbillBillingInfoEntity> found = mapper.selectHotbillBillingInfo("CTR001", LocalDate.now());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getContractNumber()).isEqualTo("CTR001");
        assertThat(found.get(0).getSettlementYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("selectHotbillBillingInfo - 핫빌청구정보 조회")
    void testSelectHotbillBillingInfo() {
        // given
        HotbillBillingInfoEntity entity1 = new HotbillBillingInfoEntity("CTR001", LocalDate.now(), 1, "CHG001", new BigDecimal("10000"));
        HotbillBillingInfoEntity entity2 = new HotbillBillingInfoEntity("CTR001", LocalDate.now(), 2, "CHG002", new BigDecimal("20000"));
        mapper.insertHotbillBillingInfo(entity1);
        mapper.insertHotbillBillingInfo(entity2);

        // when
        List<HotbillBillingInfoEntity> found = mapper.selectHotbillBillingInfo("CTR001", LocalDate.now());

        // then
        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("updateSettlementYn - 해지정산여부 갱신")
    void testUpdateSettlementYn() {
        // given
        HotbillBillingInfoEntity entity = new HotbillBillingInfoEntity("CTR001", LocalDate.now(), 1, "CHG001", new BigDecimal("10000"));
        mapper.insertHotbillBillingInfo(entity);

        // when
        mapper.updateSettlementYn(Arrays.asList("CTR001"), LocalDate.now(), "Y");

        // then
        List<HotbillBillingInfoEntity> found = mapper.selectHotbillBillingInfo("CTR001", LocalDate.now());
        assertThat(found.get(0).getSettlementYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("insertBillingInfo - 청구정보 저장 (이관용)")
    void testInsertBillingInfo() {
        // given
        BillingInfoEntity entity = new BillingInfoEntity("CTR001", "202603", 1, "CHG001", new BigDecimal("10000"));

        // when
        mapper.insertBillingInfo(entity);

        // then
        List<BillingInfoEntity> found = mapper.selectBillingInfoByContract("CTR001", "202603");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getHotbillYn()).isEqualTo("Y");
    }

    @Test
    @DisplayName("deleteHotbillData - 핫빌 데이터 삭제")
    void testDeleteHotbillData() {
        // given
        BillingInfoEntity entity1 = new BillingInfoEntity("CTR001", "202603", 1, "CHG001", new BigDecimal("10000"));
        entity1.setHotbillYn("Y");
        BillingInfoEntity entity2 = new BillingInfoEntity("CTR001", "202603", 2, "CHG002", new BigDecimal("20000"));
        entity2.setHotbillYn("N");
        mapper.insertBillingInfo(entity1);
        mapper.insertBillingInfo(entity2);

        // when
        mapper.deleteHotbillData(Arrays.asList("CTR001"), "202603");

        // then
        List<BillingInfoEntity> found = mapper.selectBillingInfoByContract("CTR001", "202603");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getHotbillYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("migrateBillingInfo - 핫빌청구정보를 청구정보로 이관")
    void testMigrateBillingInfo() {
        // given
        HotbillBillingInfoEntity hotbillEntity = new HotbillBillingInfoEntity("CTR001", LocalDate.now(), 1, "CHG001", new BigDecimal("10000"));
        mapper.insertHotbillBillingInfo(hotbillEntity);

        // when - 핫빌청구정보 조회 후 청구정보로 저장
        List<HotbillBillingInfoEntity> hotbillData = mapper.selectHotbillBillingInfo("CTR001", LocalDate.now());
        for (HotbillBillingInfoEntity hb : hotbillData) {
            BillingInfoEntity billingEntity = new BillingInfoEntity(
                hb.getContractNumber(),
                "202603",
                hb.getSeqNo(),
                hb.getChargeCode(),
                hb.getAmount()
            );
            mapper.insertBillingInfo(billingEntity);
        }

        // then
        List<BillingInfoEntity> found = mapper.selectBillingInfoByContract("CTR001", "202603");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
    }
}
