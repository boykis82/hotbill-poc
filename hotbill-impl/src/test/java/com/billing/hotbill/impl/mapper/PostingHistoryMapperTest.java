package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.PostingHistoryEntity;
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
@DisplayName("PostingHistoryMapper 테스트")
class PostingHistoryMapperTest {

    @Autowired
    private PostingHistoryMapper mapper;

    @Test
    @DisplayName("insert - 포스팅이력 저장")
    void testInsert() {
        // given
        String jsonData = "{\"targetTable\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"updates\":[{\"column\":\"TERMINATION_DATE\",\"value\":\"2026-03-05 14:30:00\"}]}";
        PostingHistoryEntity entity = new PostingHistoryEntity("CTR001", LocalDate.now(), 1, jsonData);

        // when
        mapper.insert(entity);

        // then
        List<PostingHistoryEntity> found = mapper.selectByContracts(Arrays.asList("CTR001"), LocalDate.now());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPostingData()).isEqualTo(jsonData);
    }

    @Test
    @DisplayName("selectByContracts - 계약번호 리스트로 조회")
    void testSelectByContracts() {
        // given
        PostingHistoryEntity entity1 = new PostingHistoryEntity("CTR001", LocalDate.now(), 1, "{\"data\":\"test1\"}");
        PostingHistoryEntity entity2 = new PostingHistoryEntity("CTR001", LocalDate.now(), 2, "{\"data\":\"test2\"}");
        PostingHistoryEntity entity3 = new PostingHistoryEntity("CTR002", LocalDate.now(), 1, "{\"data\":\"test3\"}");
        mapper.insert(entity1);
        mapper.insert(entity2);
        mapper.insert(entity3);

        // when
        List<PostingHistoryEntity> found = mapper.selectByContracts(Arrays.asList("CTR001", "CTR002"), LocalDate.now());

        // then
        assertThat(found).hasSize(3);
    }

    @Test
    @DisplayName("insert - JSON 데이터 파싱 테스트")
    void testJsonDataParsing() {
        // given
        String complexJson = "{\"targetTable\":\"TB_CONTRACT\",\"tablePk\":{\"contractNumber\":\"CTR001\"},\"updates\":[{\"column\":\"TERMINATION_DATE\",\"value\":\"2026-03-05 14:30:00\"},{\"column\":\"TERMINATION_YN\",\"value\":\"Y\"}]}";
        PostingHistoryEntity entity = new PostingHistoryEntity("CTR001", LocalDate.now(), 1, complexJson);

        // when
        mapper.insert(entity);

        // then
        List<PostingHistoryEntity> found = mapper.selectByContracts(Arrays.asList("CTR001"), LocalDate.now());
        assertThat(found.get(0).getPostingData()).contains("TERMINATION_DATE");
        assertThat(found.get(0).getPostingData()).contains("TERMINATION_YN");
    }

    @Test
    @DisplayName("selectByContracts - 여러 seqNo 조회")
    void testSelectMultipleSeqNo() {
        // given
        for (int i = 1; i <= 5; i++) {
            PostingHistoryEntity entity = new PostingHistoryEntity("CTR001", LocalDate.now(), i, "{\"seq\":" + i + "}");
            mapper.insert(entity);
        }

        // when
        List<PostingHistoryEntity> found = mapper.selectByContracts(Arrays.asList("CTR001"), LocalDate.now());

        // then
        assertThat(found).hasSize(5);
        assertThat(found.get(0).getSeqNo()).isEqualTo(1);
        assertThat(found.get(4).getSeqNo()).isEqualTo(5);
    }
}
