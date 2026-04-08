package com.billing.hotbill.impl.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Hotbill 모듈 Spring Configuration
 *
 * <p>Hotbill 모듈의 Bean 설정 및 Component Scan을 담당합니다.</p>
 *
 * <ul>
 *   <li>Component Scan: com.billing.hotbill.impl</li>
 *   <li>Mapper Scan: com.billing.hotbill.impl.mapper</li>
 *   <li>RestTemplate Bean 등록</li>
 * </ul>
 *
 * <p><strong>Note:</strong> 이 설정은 순수 Spring 기반입니다.
 * DataSource 및 MyBatis SqlSessionFactory는 별도로 설정해야 합니다.</p>
 */
@Configuration
@ComponentScan(basePackages = "com.billing.hotbill.impl")
@MapperScan("com.billing.hotbill.impl.mapper")
public class HotbillConfig {

    /**
     * RestTemplate Bean 등록
     *
     * <p>외부 시스템 연동을 위한 RestTemplate을 Bean으로 등록합니다.</p>
     *
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Note: DataSource 및 SqlSessionFactory는
    // 실제 운영 환경에 맞게 별도로 설정해야 합니다.
    // 테스트 환경에서는 @SpringBootTest를 통해 자동 설정됩니다.
}
