package com.billing.hotbill.impl.client;

import com.billing.hotbill.api.enums.HotbillUsecase;

import java.util.List;

/**
 * 외부 과금 시스템 클라이언트 인터페이스
 *
 * 이동전화, IPTV 등 외부 시스템에서 과금자료를 비동기로 받아오는 클라이언트의 공통 인터페이스
 */
public interface ExternalChargeClient {

    /**
     * 과금자료 비동기 요청
     *
     * @param contractNumbers 계약번호 리스트
     * @param usecase Hotbill usecase
     */
    void requestChargeDataAsync(List<String> contractNumbers, HotbillUsecase usecase);
}
