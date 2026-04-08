package com.billing.hotbill.api;

import com.billing.hotbill.api.dto.*;

/**
 * Hotbill 핵심 서비스 인터페이스
 *
 * <p>실시간 요금계산(Hotbill) 모듈의 핵심 API를 제공합니다.</p>
 *
 * <ul>
 *   <li>Hotbill 요청 처리</li>
 *   <li>Hotbill 결과 조회</li>
 *   <li>해지정산 요청</li>
 *   <li>해지정산 당일취소</li>
 * </ul>
 */
public interface Hotbill {

    /**
     * Hotbill 요청을 처리합니다.
     *
     * <p>N개의 계약에 대해 실시간 요금계산을 요청합니다.
     * 계약 유형에 따라 동기/비동기로 처리됩니다.</p>
     *
     * @param request Hotbill 요청 정보
     * @return Hotbill 처리 결과
     */
    HotbillResult requestHotbill(HotbillRequest request);

    /**
     * Hotbill 결과를 조회합니다.
     *
     * <p>비동기로 처리된 Hotbill의 완료 여부 및 결과를 조회합니다.</p>
     *
     * @param request Hotbill 조회 요청 정보
     * @return Hotbill 결과
     */
    HotbillResult queryHotbillResult(HotbillQueryRequest request);

    /**
     * 해지정산을 요청합니다.
     *
     * <p>Hotbill이 완료된 계약에 대해 해지정산을 처리합니다.
     * 포스팅이력을 기반으로 업무 테이블을 갱신하고,
     * 핫빌청구정보를 본 청구정보 테이블로 이관합니다.</p>
     *
     * @param request 해지정산 요청 정보
     * @return 해지정산 처리 결과
     */
    SettlementResult requestSettlement(SettlementRequest request);

    /**
     * 해지정산을 당일 취소합니다.
     *
     * <p>해지정산이 완료된 계약에 대해 취소 처리합니다.
     * 핫빌취소로그를 기반으로 데이터를 원복합니다.</p>
     *
     * @param request 해지정산 취소 요청 정보
     * @return 해지정산 취소 처리 결과
     */
    CancellationResult cancelSettlement(CancellationRequest request);
}
