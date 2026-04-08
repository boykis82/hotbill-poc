package com.billing.hotbill.impl.mapper;

import com.billing.hotbill.impl.entity.PostingHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 포스팅이력 Mapper
 */
@Mapper
public interface PostingHistoryMapper {

    /**
     * 포스팅이력 저장
     */
    void insert(PostingHistoryEntity entity);

    /**
     * 계약번호 리스트로 포스팅이력 조회
     */
    List<PostingHistoryEntity> selectByContracts(
        @Param("contractNumbers") List<String> contractNumbers,
        @Param("hotbillDate") LocalDate hotbillDate
    );
}
