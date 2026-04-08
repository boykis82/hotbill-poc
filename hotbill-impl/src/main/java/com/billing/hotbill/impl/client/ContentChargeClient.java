package com.billing.hotbill.impl.client;

import com.billing.hotbill.api.enums.HotbillUsecase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 컨텐츠과금자료 시스템 클라이언트
 *
 * IPTV 계약의 과금자료를 외부 시스템에 비동기로 요청하는 클라이언트
 * 실제 외부 API는 stub으로 처리
 */
@Component
public class ContentChargeClient implements ExternalChargeClient {

    private static final Logger log = LoggerFactory.getLogger(ContentChargeClient.class);

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public ContentChargeClient(
            RestTemplate restTemplate,
            @Value("${external.content.api.url:http://localhost:8080/api/content/charge}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    /**
     * 과금자료 비동기 요청
     *
     * @param contractNumbers 계약번호 리스트
     * @param usecase Hotbill usecase
     * @throws RestClientException 외부 시스템 호출 실패 시
     */
    @Override
    public void requestChargeDataAsync(List<String> contractNumbers, HotbillUsecase usecase) {
        log.info("컨텐츠과금자료 시스템 비동기 요청 시작 - 계약 수: {}, usecase: {}",
                contractNumbers.size(), usecase);

        try {
            // 요청 바디 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contractNumbers", contractNumbers);
            requestBody.put("usecase", usecase.name());

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 엔티티 생성
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // REST API 호출 (비동기 - 즉시 반환)
            // 실제로는 외부 시스템의 REST API를 호출하지만, stub으로 처리
            restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                Void.class
            );

            log.info("컨텐츠과금자료 시스템 비동기 요청 완료 - 계약 수: {}", contractNumbers.size());

            // 비동기 요청이므로 즉시 반환
            // 외부 시스템은 나중에 콜백 API를 통해 과금자료를 전달함

        } catch (RestClientException e) {
            log.error("컨텐츠과금자료 시스템 호출 실패 - 계약 수: {}, usecase: {}, 오류: {}",
                    contractNumbers.size(), usecase, e.getMessage(), e);
            throw e;
        }
    }
}
