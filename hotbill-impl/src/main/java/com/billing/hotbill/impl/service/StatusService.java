package com.billing.hotbill.impl.service;

import com.billing.hotbill.api.enums.HotbillStatus;
import com.billing.hotbill.impl.mapper.HotbillStatusMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Hotbill 상태 관리 서비스
 *
 * 핫빌 진행상태를 관리하는 서비스입니다.
 * 현재 날짜(LocalDate.now())를 hotbillDate로 사용합니다.
 */
@Service
public class StatusService {

    private final HotbillStatusMapper hotbillStatusMapper;

    public StatusService(HotbillStatusMapper hotbillStatusMapper) {
        this.hotbillStatusMapper = hotbillStatusMapper;
    }

    /**
     * 상태 갱신 (단건)
     *
     * @param contractNumber 계약번호
     * @param status 변경할 상태
     */
    @Transactional
    public void updateStatus(String contractNumber, HotbillStatus status) {
        LocalDate hotbillDate = LocalDate.now();
        hotbillStatusMapper.updateStatus(contractNumber, hotbillDate, status);
    }

    /**
     * 상태 갱신 (다건)
     *
     * @param contractNumbers 계약번호 목록
     * @param status 변경할 상태
     */
    @Transactional
    public void updateStatus(List<String> contractNumbers, HotbillStatus status) {
        LocalDate hotbillDate = LocalDate.now();
        hotbillStatusMapper.updateStatusBatch(contractNumbers, hotbillDate, status);
    }

    /**
     * 완료되지 않은 계약 조회
     *
     * COMPLETED 상태가 아닌 계약들을 조회합니다.
     *
     * @param contractNumbers 조회할 계약번호 목록
     * @return 미완료 계약번호 목록
     */
    @Transactional(readOnly = true)
    public List<String> findNotCompletedContracts(List<String> contractNumbers) {
        LocalDate hotbillDate = LocalDate.now();
        return hotbillStatusMapper.findNotCompletedContracts(contractNumbers, hotbillDate);
    }

    /**
     * 정산되지 않은 계약 조회
     *
     * SETTLEMENT_COMPLETED 상태가 아닌 계약들을 조회합니다.
     *
     * @param contractNumbers 조회할 계약번호 목록
     * @return 미정산 계약번호 목록
     */
    @Transactional(readOnly = true)
    public List<String> findNotSettledContracts(List<String> contractNumbers) {
        LocalDate hotbillDate = LocalDate.now();
        return hotbillStatusMapper.findNotSettledContracts(contractNumbers, hotbillDate);
    }
}
