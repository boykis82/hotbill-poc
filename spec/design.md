# Hotbill 모듈 시스템 아키텍처 & 설계 문서

## 1. 시스템 아키텍처

### 1.1 전체 구조
```
┌─────────────────────────────────────────────────────────────┐
│                     External Clients                         │
│         (Mobile App, Backoffice, External Systems)          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                            │
│  - HotbillTerminationController                             │
│  - HotbillRealtimeController                                │
│  - HotbillSettlementController                              │
│  - ChargeDataController                                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer (API)                       │
│                   Hotbill Interface                          │
│  - requestHotbill()                                         │
│  - queryHotbillResult()                                     │
│  - requestSettlement()                                      │
│  - cancelSettlement()                                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│               Service Layer (Implementation)                 │
│                     HotbillImpl                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         HotbillOrchestrator                         │   │
│  │  - HotbillTemplate (Template Method)                │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │     ChargeDataStrategy (Strategy Pattern)           │   │
│  │  - SyncChargeDataStrategy (인터넷)                  │   │
│  │  - AsyncChargeDataStrategy (이동전화, IPTV)        │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │    UsecaseValidator (Strategy Pattern)              │   │
│  │  - SimpleInquiryValidator                           │   │
│  │  - TerminationInquiryValidator                      │   │
│  │  - PortOutValidator                                 │   │
│  │  - ForcedTerminationValidator                       │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Domain Layer                               │
│  - HotbillRequest                                           │
│  - HotbillResult                                            │
│  - ChargeData                                               │
│  - SettlementRequest                                        │
│  - HotbillStatus (Enum)                                     │
│  - ContractType (Enum)                                      │
│  - HotbillUsecase (Enum)                                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Repository (MyBatis Mapper)                 │   │
│  │  - HotbillStatusMapper                              │   │
│  │  - ChargeDataMapper                                 │   │
│  │  - BillingInfoMapper                                │   │
│  │  - PostingHistoryMapper                             │   │
│  │  - CancellationLogMapper                            │   │
│  │  - TableUpdateMapperRegistry                        │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         External System Client                      │   │
│  │  - TelephonyChargeClient                            │   │
│  │  - ContentChargeClient                              │   │
│  │  - BillingCalculationClient (Stub)                  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Database (Oracle)                       │
│  - TB_HOTBILL_STATUS                                        │
│  - TB_CHARGE_DATA                                           │
│  - TB_HOTBILL_BILLING_INFO                                  │
│  - TB_BILLING_INFO                                          │
│  - TB_POSTING_HISTORY                                       │
│  - TB_CANCELLATION_LOG                                      │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 모듈 구조
```
hotbill/
├── hotbill-api/                    # API 모듈
│   └── src/main/java/
│       └── com/billing/hotbill/api/
│           ├── Hotbill.java        # 핵심 서비스 인터페이스
│           ├── dto/                # DTO
│           │   ├── HotbillRequest.java
│           │   ├── HotbillResult.java
│           │   ├── ChargeData.java
│           │   ├── SettlementRequest.java
│           │   └── CancellationRequest.java
│           └── enums/              # 열거형
│               ├── HotbillStatus.java
│               ├── ContractType.java
│               └── HotbillUsecase.java
│
└── hotbill-impl/                   # 구현 모듈
    └── src/main/java/
        └── com/billing/hotbill/impl/
            ├── HotbillImpl.java    # 서비스 구현체
            ├── orchestrator/       # 오케스트레이터
            │   ├── HotbillOrchestrator.java
            │   └── template/
            │       ├── HotbillTemplate.java
            │       └── DefaultHotbillTemplate.java
            ├── strategy/           # 전략 패턴
            │   ├── chargedata/
            │   │   ├── ChargeDataStrategy.java
            │   │   ├── SyncChargeDataStrategy.java
            │   │   ├── AsyncChargeDataStrategy.java
            │   │   └── ChargeDataStrategyFactory.java
            │   └── validator/
            │       ├── UsecaseValidator.java
            │       ├── SimpleInquiryValidator.java
            │       ├── TerminationInquiryValidator.java
            │       ├── PortOutValidator.java
            │       ├── ForcedTerminationValidator.java
            │       └── UsecaseValidatorFactory.java
            ├── service/            # 내부 서비스
            │   ├── StatusService.java
            │   ├── ChargeDataService.java
            │   ├── BillingService.java
            │   ├── SettlementService.java
            │   └── CancellationService.java
            ├── client/             # 외부 연동 클라이언트
            │   ├── TelephonyChargeClient.java
            │   ├── ContentChargeClient.java
            │   └── BillingCalculationClient.java (stub)
            ├── mapper/             # MyBatis Mapper
            │   ├── HotbillStatusMapper.java
            │   ├── ChargeDataMapper.java
            │   ├── BillingInfoMapper.java
            │   ├── PostingHistoryMapper.java
            │   ├── CancellationLogMapper.java
            │   └── update/         # 테이블별 업데이트 매퍼
            │       ├── TableUpdateMapper.java (인터페이스)
            │       ├── TableUpdateMapperRegistry.java
            │       ├── ContractTableUpdateMapper.java
            │       └── ServiceTableUpdateMapper.java
            ├── controller/         # REST Controller
            │   ├── HotbillTerminationController.java
            │   ├── HotbillRealtimeController.java
            │   ├── HotbillSettlementController.java
            │   └── ChargeDataController.java
            └── config/
                └── HotbillConfig.java
```

## 2. 핵심 설계 패턴

### 2.1 Template Method Pattern (Hotbill Usecase 확장)

**목적**: Hotbill usecase별 특화 validation 로직을 확장 가능하게 처리

**핵심 로직**: 각 계약별로 계약유형과 usecase 조합에 따라 동기/비동기 처리 분기

```java
// HotbillTemplate.java
public abstract class HotbillTemplate {

    private final StatusService statusService;
    private final ChargeDataStrategyFactory chargeDataStrategyFactory;
    private final BillingService billingService;

    /**
     * Hotbill 요청 처리 템플릿 메서드
     */
    public final HotbillResult executeHotbill(HotbillRequest request) {
        // 1. 사전 검증 (확장 포인트)
        validate(request);

        // 2. 상태 초기화
        statusService.updateStatus(request.getContractNumbers(), HotbillStatus.REQUESTING);

        // 3. 계약별로 동기/비동기 그룹핑
        Map<ProcessingType, List<ContractInfo>> groupedContracts =
            groupContractsByProcessingType(request);

        HotbillResult result = new HotbillResult();

        // 4. 비동기 처리 대상 계약들
        List<ContractInfo> asyncContracts = groupedContracts.get(ProcessingType.ASYNC);
        if (!asyncContracts.isEmpty()) {
            HotbillRequest asyncRequest = request.withContracts(asyncContracts);
            ChargeDataStrategy asyncStrategy = chargeDataStrategyFactory.getAsyncStrategy();
            HotbillResult asyncResult = asyncStrategy.process(asyncRequest);
            result.merge(asyncResult);
        }

        // 5. 동기 처리 대상 계약들
        List<ContractInfo> syncContracts = groupedContracts.get(ProcessingType.SYNC);
        if (!syncContracts.isEmpty()) {
            HotbillRequest syncRequest = request.withContracts(syncContracts);
            ChargeDataStrategy syncStrategy = chargeDataStrategyFactory.getSyncStrategy();
            HotbillResult syncResult = syncStrategy.process(syncRequest);
            result.merge(syncResult);
        }

        // 6. 사후 처리 (확장 포인트)
        postProcess(request, result);

        return result;
    }

    /**
     * 계약들을 동기/비동기 처리 타입별로 그룹핑
     */
    private Map<ProcessingType, List<ContractInfo>> groupContractsByProcessingType(
            HotbillRequest request) {

        return request.getContracts().stream()
            .collect(Collectors.groupingBy(contract ->
                determineProcessingType(contract.getContractType(), request.getUsecase())
            ));
    }

    /**
     * 계약유형과 usecase 조합으로 처리 타입 결정
     */
    private ProcessingType determineProcessingType(
            ContractType contractType,
            HotbillUsecase usecase) {

        return chargeDataStrategyFactory.determineProcessingType(contractType, usecase);
    }

    /**
     * Usecase별 검증 로직 (확장 포인트)
     */
    protected abstract void validate(HotbillRequest request);

    /**
     * Usecase별 사후 처리 로직 (확장 포인트)
     */
    protected void postProcess(HotbillRequest request, HotbillResult result) {
        // 기본 구현: 아무것도 하지 않음
    }
}

// DefaultHotbillTemplate.java
public class DefaultHotbillTemplate extends HotbillTemplate {

    private final UsecaseValidatorFactory validatorFactory;

    @Override
    protected void validate(HotbillRequest request) {
        UsecaseValidator validator = validatorFactory.getValidator(request.getUsecase());
        validator.validate(request);
    }
}

// ProcessingType.java
public enum ProcessingType {
    SYNC,   // 동기 처리
    ASYNC   // 비동기 처리
}
```

### 2.2 Strategy Pattern (과금자료 처리 전략)

**목적**: 계약 유형별 동기/비동기 과금자료 처리를 유연하게 관리

**핵심**: 계약유형별로 그룹핑하여 외부 시스템 호출 최소화

```java
// ChargeDataStrategy.java
public interface ChargeDataStrategy {
    HotbillResult process(HotbillRequest request);
}

// SyncChargeDataStrategy.java (인터넷)
@Component
public class SyncChargeDataStrategy implements ChargeDataStrategy {

    private final StatusService statusService;
    private final BillingCalculationClient billingClient;

    @Override
    public HotbillResult process(HotbillRequest request) {
        List<String> contractNumbers = request.getContractNumbers();

        // 1. 상태: 요금계산 전
        statusService.updateStatus(contractNumbers, HotbillStatus.BEFORE_CALCULATION);

        // 2. 요금계산 API 호출 (동기)
        HotbillResult result = billingClient.calculate(request);

        // 3. 상태 갱신
        updateStatusByResult(result);

        return result;
    }

    private void updateStatusByResult(HotbillResult result) {
        result.getSuccessList().forEach(item ->
            statusService.updateStatus(item.getContractNumber(), HotbillStatus.COMPLETED)
        );
        result.getFailureList().forEach(item ->
            statusService.updateStatus(item.getContractNumber(), HotbillStatus.FAILED)
        );
    }
}

// AsyncChargeDataStrategy.java (이동전화, IPTV)
@Component
public class AsyncChargeDataStrategy implements ChargeDataStrategy {

    private final StatusService statusService;
    private final Map<ContractType, ExternalChargeClient> clientMap;

    @Override
    public HotbillResult process(HotbillRequest request) {
        // 계약 유형별로 그룹핑 (동일 계약 유형은 한번에 요청)
        Map<ContractType, List<ContractInfo>> contractsByType =
            request.getContracts().stream()
                .collect(Collectors.groupingBy(ContractInfo::getContractType));

        // 각 계약 유형별로 외부 시스템에 비동기 요청
        contractsByType.forEach((type, contracts) -> {
            ExternalChargeClient client = clientMap.get(type);
            List<String> contractNumbers = contracts.stream()
                .map(ContractInfo::getContractNumber)
                .collect(Collectors.toList());

            // 외부 시스템 비동기 호출
            client.requestChargeDataAsync(contractNumbers, request.getUsecase());
        });

        // 비동기 요청 완료 (즉시 반환)
        return HotbillResult.asyncProcessing(request.getContractNumbers());
    }
}

// ChargeDataStrategyFactory.java
@Component
public class ChargeDataStrategyFactory {

    private final SyncChargeDataStrategy syncStrategy;
    private final AsyncChargeDataStrategy asyncStrategy;

    /**
     * 동기 전략 반환
     */
    public ChargeDataStrategy getSyncStrategy() {
        return syncStrategy;
    }

    /**
     * 비동기 전략 반환
     */
    public ChargeDataStrategy getAsyncStrategy() {
        return asyncStrategy;
    }

    /**
     * 계약유형과 usecase 조합으로 처리 타입 결정
     */
    public ProcessingType determineProcessingType(
            ContractType contractType,
            HotbillUsecase usecase) {

        // 계약 유형과 usecase 조합에 따른 처리 타입 결정
        // 향후 설정 테이블이나 설정 파일로 외부화 가능
        boolean requiresExternalChargeData = switch (contractType) {
            case MOBILE_PHONE -> true;  // 이동전화: 전화과금자료 시스템 연동
            case IPTV -> true;          // IPTV: 컨텐츠과금자료 시스템 연동
            case INTERNET -> false;     // 인터넷: 연동 불필요
        };

        // Usecase별 예외 처리 (특정 usecase는 연동 생략)
        if (requiresExternalChargeData && shouldSkipExternalIntegration(usecase)) {
            return ProcessingType.SYNC;
        }

        return requiresExternalChargeData ? ProcessingType.ASYNC : ProcessingType.SYNC;
    }

    /**
     * Usecase별 외부 연동 생략 여부 (확장 포인트)
     */
    private boolean shouldSkipExternalIntegration(HotbillUsecase usecase) {
        // 예: 특정 usecase는 과금자료 연동을 생략할 수 있음
        return false;
    }
}
```

### 2.3 Strategy Pattern (Usecase별 Validator)

**목적**: Hotbill usecase별 검증 로직을 독립적으로 관리

```java
// UsecaseValidator.java
public interface UsecaseValidator {
    void validate(HotbillRequest request);
}

// SimpleInquiryValidator.java
public class SimpleInquiryValidator implements UsecaseValidator {
    @Override
    public void validate(HotbillRequest request) {
        // 단순 요금 조회 검증 로직
        validateContractExists(request.getContractNumbers());
    }
}

// TerminationInquiryValidator.java
public class TerminationInquiryValidator implements UsecaseValidator {
    @Override
    public void validate(HotbillRequest request) {
        // 해지 요금 조회 검증 로직
        validateContractExists(request.getContractNumbers());
        validateContractTerminatable(request.getContractNumbers());
    }
}

// UsecaseValidatorFactory.java
@Component
public class UsecaseValidatorFactory {

    private final Map<HotbillUsecase, UsecaseValidator> validatorMap;

    public UsecaseValidatorFactory(
            SimpleInquiryValidator simpleInquiryValidator,
            TerminationInquiryValidator terminationInquiryValidator,
            PortOutValidator portOutValidator,
            ForcedTerminationValidator forcedTerminationValidator) {

        this.validatorMap = Map.of(
            HotbillUsecase.SIMPLE_INQUIRY, simpleInquiryValidator,
            HotbillUsecase.TERMINATION_INQUIRY, terminationInquiryValidator,
            HotbillUsecase.PORT_OUT, portOutValidator,
            HotbillUsecase.FORCED_TERMINATION, forcedTerminationValidator
        );
    }

    public UsecaseValidator getValidator(HotbillUsecase usecase) {
        UsecaseValidator validator = validatorMap.get(usecase);
        if (validator == null) {
            throw new IllegalArgumentException("Unknown usecase: " + usecase);
        }
        return validator;
    }
}
```

### 2.4 Registry Pattern (테이블별 Update Mapper 관리)

**목적**: Dynamic SQL 없이 테이블별 UPDATE 쿼리를 유연하게 관리

```java
// TableUpdateMapper.java (인터페이스)
public interface TableUpdateMapper {
    String getTableName();
    void update(Map<String, Object> updateData);
}

// ContractTableUpdateMapper.java
@Mapper
public interface ContractTableUpdateMapper extends TableUpdateMapper {

    @Override
    default String getTableName() {
        return "TB_CONTRACT";
    }

    void updateTerminationDate(
        @Param("contractNumber") String contractNumber,
        @Param("terminationDate") LocalDateTime terminationDate
    );

    @Override
    default void update(Map<String, Object> updateData) {
        String contractNumber = (String) updateData.get("contractNumber");
        LocalDateTime terminationDate = (LocalDateTime) updateData.get("terminationDate");
        updateTerminationDate(contractNumber, terminationDate);
    }
}

// TableUpdateMapperRegistry.java
@Component
public class TableUpdateMapperRegistry {

    private final Map<String, TableUpdateMapper> mapperMap;

    public TableUpdateMapperRegistry(List<TableUpdateMapper> mappers) {
        this.mapperMap = mappers.stream()
            .collect(Collectors.toMap(
                TableUpdateMapper::getTableName,
                mapper -> mapper
            ));
    }

    public TableUpdateMapper getMapper(String tableName) {
        TableUpdateMapper mapper = mapperMap.get(tableName);
        if (mapper == null) {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }
        return mapper;
    }
}
```

## 3. 핵심 도메인 모델

### 3.1 열거형 (Enum)

```java
// HotbillStatus.java
public enum HotbillStatus {
    REQUESTING("요청중"),
    BEFORE_CALCULATION("요금계산 전"),
    CHARGE_DATA_ERROR("과금자료 오류"),
    COMPLETED("요금계산 완료"),
    FAILED("실패"),
    SETTLEMENT_COMPLETED("해지정산 완료");

    private final String description;

    HotbillStatus(String description) {
        this.description = description;
    }
}

// ContractType.java
public enum ContractType {
    MOBILE_PHONE("이동전화"),
    INTERNET("인터넷"),
    IPTV("IPTV");

    private final String description;

    ContractType(String description) {
        this.description = description;
    }
}

// HotbillUsecase.java
public enum HotbillUsecase {
    SIMPLE_INQUIRY("단순 요금 조회"),
    TERMINATION_INQUIRY("해지 요금 조회"),
    PORT_OUT("번호이동 portout"),
    FORCED_TERMINATION("직권해지를 위한 요금 조회");

    private final String description;

    HotbillUsecase(String description) {
        this.description = description;
    }
}
```

### 3.2 DTO

```java
// HotbillRequest.java
public class HotbillRequest {
    private HotbillUsecase usecase;
    private List<ContractInfo> contracts;
    private LocalDateTime requestDateTime;

    public List<String> getContractNumbers() {
        return contracts.stream()
            .map(ContractInfo::getContractNumber)
            .collect(Collectors.toList());
    }

    /**
     * 특정 계약들로 새로운 요청 객체 생성 (동기/비동기 그룹핑용)
     */
    public HotbillRequest withContracts(List<ContractInfo> newContracts) {
        HotbillRequest newRequest = new HotbillRequest();
        newRequest.setUsecase(this.usecase);
        newRequest.setContracts(newContracts);
        newRequest.setRequestDateTime(this.requestDateTime);
        return newRequest;
    }
}

// ContractInfo.java
public class ContractInfo {
    private String contractNumber;
    private ContractType contractType;
}

// ChargeData.java
public class ChargeData {
    private String contractNumber;
    private List<ChargeItem> chargeItems;
    private String errorCode;
    private String errorMessage;

    public boolean hasError() {
        return errorCode != null && !errorCode.isEmpty();
    }
}

// ChargeItem.java
public class ChargeItem {
    private String chargeCode;      // 과금항목
    private BigDecimal amount;      // 청구금액
}

// HotbillResult.java
public class HotbillResult {
    private List<BillingInfo> successList;
    private List<FailureInfo> failureList;
    private boolean asyncProcessing;  // 비동기 처리 여부

    /**
     * 다른 결과와 병합 (동기/비동기 결과 통합용)
     */
    public void merge(HotbillResult other) {
        if (other.getSuccessList() != null) {
            this.successList.addAll(other.getSuccessList());
        }
        if (other.getFailureList() != null) {
            this.failureList.addAll(other.getFailureList());
        }
        // 하나라도 비동기면 비동기로 표시
        this.asyncProcessing = this.asyncProcessing || other.isAsyncProcessing();
    }

    /**
     * 비동기 처리 결과 생성
     */
    public static HotbillResult asyncProcessing(List<String> contractNumbers) {
        HotbillResult result = new HotbillResult();
        result.setAsyncProcessing(true);
        result.setSuccessList(Collections.emptyList());
        result.setFailureList(Collections.emptyList());
        return result;
    }
}

// BillingInfo.java
public class BillingInfo {
    private String contractNumber;
    private LocalDate hotbillDate;
    private BigDecimal totalAmount;
    private List<ChargeItem> chargeItems;
}
```

## 4. 데이터베이스 설계

### 4.1 핫빌 진행상태 테이블 (TB_HOTBILL_STATUS)
```sql
CREATE TABLE TB_HOTBILL_STATUS (
    CONTRACT_NUMBER     VARCHAR2(20)    NOT NULL,   -- 계약번호
    HOTBILL_DATE        DATE            NOT NULL,   -- Hotbill일자
    USECASE             VARCHAR2(20)    NOT NULL,   -- Usecase 코드
    STATUS              VARCHAR2(20)    NOT NULL,   -- 진행상태
    REQUEST_DATETIME    TIMESTAMP       NOT NULL,   -- 요청일시
    UPDATE_DATETIME     TIMESTAMP       NOT NULL,   -- 갱신일시
    ERROR_MESSAGE       VARCHAR2(500),              -- 오류메시지
    CONSTRAINT PK_HOTBILL_STATUS PRIMARY KEY (CONTRACT_NUMBER, HOTBILL_DATE)
);
```

### 4.2 과금자료 테이블 (TB_CHARGE_DATA)
```sql
CREATE TABLE TB_CHARGE_DATA (
    CONTRACT_NUMBER     VARCHAR2(20)    NOT NULL,   -- 계약번호
    HOTBILL_DATE        DATE            NOT NULL,   -- Hotbill일자
    CHARGE_CODE         VARCHAR2(20)    NOT NULL,   -- 과금항목코드
    AMOUNT              NUMBER(15,2)    NOT NULL,   -- 청구금액
    ERROR_CODE          VARCHAR2(20),               -- 오류코드
    ERROR_MESSAGE       VARCHAR2(500),              -- 오류메시지
    CREATE_DATETIME     TIMESTAMP       NOT NULL,   -- 생성일시
    CONSTRAINT PK_CHARGE_DATA PRIMARY KEY (CONTRACT_NUMBER, HOTBILL_DATE, CHARGE_CODE)
);
```

### 4.3 핫빌청구정보 테이블 (TB_HOTBILL_BILLING_INFO)
```sql
CREATE TABLE TB_HOTBILL_BILLING_INFO (
    CONTRACT_NUMBER     VARCHAR2(20)    NOT NULL,   -- 계약번호
    HOTBILL_DATE        DATE            NOT NULL,   -- Hotbill일자
    SEQ_NO              NUMBER(5)       NOT NULL,   -- 일련번호
    CHARGE_CODE         VARCHAR2(20)    NOT NULL,   -- 과금항목코드
    AMOUNT              NUMBER(15,2)    NOT NULL,   -- 청구금액
    SETTLEMENT_YN       CHAR(1)         DEFAULT 'N',-- 해지정산여부
    CREATE_DATETIME     TIMESTAMP       NOT NULL,   -- 생성일시
    UPDATE_DATETIME     TIMESTAMP       NOT NULL,   -- 갱신일시
    CONSTRAINT PK_HOTBILL_BILLING_INFO PRIMARY KEY (CONTRACT_NUMBER, HOTBILL_DATE, SEQ_NO)
);
```

### 4.4 청구정보 테이블 (TB_BILLING_INFO)
```sql
CREATE TABLE TB_BILLING_INFO (
    CONTRACT_NUMBER     VARCHAR2(20)    NOT NULL,   -- 계약번호
    BILLING_MONTH       CHAR(6)         NOT NULL,   -- 청구년월
    SEQ_NO              NUMBER(5)       NOT NULL,   -- 일련번호
    CHARGE_CODE         VARCHAR2(20)    NOT NULL,   -- 과금항목코드
    AMOUNT              NUMBER(15,2)    NOT NULL,   -- 청구금액
    HOTBILL_YN          CHAR(1)         DEFAULT 'N',-- Hotbill여부
    CREATE_DATETIME     TIMESTAMP       NOT NULL,   -- 생성일시
    CONSTRAINT PK_BILLING_INFO PRIMARY KEY (CONTRACT_NUMBER, BILLING_MONTH, SEQ_NO)
);
```

### 4.5 포스팅이력 테이블 (TB_POSTING_HISTORY)
```sql
CREATE TABLE TB_POSTING_HISTORY (
    CONTRACT_NUMBER     VARCHAR2(20)    NOT NULL,   -- 계약번호
    HOTBILL_DATE        DATE            NOT NULL,   -- Hotbill일자
    SEQ_NO              NUMBER(5)       NOT NULL,   -- 일련번호
    POSTING_DATA        CLOB            NOT NULL,   -- 포스팅데이터 (JSON)
    CREATE_DATETIME     TIMESTAMP       NOT NULL,   -- 생성일시
    CONSTRAINT PK_POSTING_HISTORY PRIMARY KEY (CONTRACT_NUMBER, HOTBILL_DATE, SEQ_NO)
);

-- POSTING_DATA JSON 구조 예시:
-- {
--   "targetTable": "TB_CONTRACT",
--   "tablePk": {"contractNumber": "CTR001"},
--   "updates": [
--     {"column": "TERMINATION_DATE", "value": "2025-03-05 14:30:00"},
--     {"column": "TERMINATION_YN", "value": "Y"}
--   ]
-- }
```

### 4.6 핫빌취소로그 테이블 (TB_CANCELLATION_LOG)
```sql
CREATE TABLE TB_CANCELLATION_LOG (
    CONTRACT_NUMBER     VARCHAR2(20)    NOT NULL,   -- 계약번호
    HOTBILL_DATE        DATE            NOT NULL,   -- Hotbill일자
    SEQ_NO              NUMBER(5)       NOT NULL,   -- 일련번호
    TABLE_NAME          VARCHAR2(50)    NOT NULL,   -- 대상테이블명
    SNAPSHOT_DATA       CLOB            NOT NULL,   -- 변경 전 스냅샷 (JSON)
    CREATE_DATETIME     TIMESTAMP       NOT NULL,   -- 생성일시
    CONSTRAINT PK_CANCELLATION_LOG PRIMARY KEY (CONTRACT_NUMBER, HOTBILL_DATE, SEQ_NO)
);

-- SNAPSHOT_DATA JSON 구조 예시:
-- {
--   "tableName": "TB_CONTRACT",
--   "tablePk": {"contractNumber": "CTR001"},
--   "beforeData": {
--     "terminationDate": null,
--     "terminationYn": "N"
--   }
-- }
```

## 5. API 설계

### 5.1 Hotbill 인터페이스

```java
public interface Hotbill {

    /**
     * Hotbill 요청
     */
    HotbillResult requestHotbill(HotbillRequest request);

    /**
     * Hotbill 결과조회
     */
    HotbillResult queryHotbillResult(HotbillQueryRequest request);

    /**
     * 해지정산 요청
     */
    SettlementResult requestSettlement(SettlementRequest request);

    /**
     * 해지정산 당일취소
     */
    CancellationResult cancelSettlement(CancellationRequest request);
}
```

### 5.2 REST API Endpoints

```java
// HotbillTerminationController.java
@RestController
@RequestMapping("/api/hotbill/termination")
public class HotbillTerminationController {

    private final Hotbill hotbill;

    /**
     * 해지요금 요청
     * POST /api/hotbill/termination/request
     */
    @PostMapping("/request")
    public ResponseEntity<HotbillResult> requestTerminationBilling(
            @RequestBody HotbillRequest request) {
        request.setUsecase(HotbillUsecase.TERMINATION_INQUIRY);
        HotbillResult result = hotbill.requestHotbill(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 해지요금 결과조회
     * POST /api/hotbill/termination/query
     */
    @PostMapping("/query")
    public ResponseEntity<HotbillResult> queryTerminationBilling(
            @RequestBody HotbillQueryRequest request) {
        HotbillResult result = hotbill.queryHotbillResult(request);
        return ResponseEntity.ok(result);
    }
}

// HotbillRealtimeController.java
@RestController
@RequestMapping("/api/hotbill/realtime")
public class HotbillRealtimeController {

    private final Hotbill hotbill;

    /**
     * 실시간요금 요청
     * POST /api/hotbill/realtime/request
     */
    @PostMapping("/request")
    public ResponseEntity<HotbillResult> requestRealtimeBilling(
            @RequestBody HotbillRequest request) {
        request.setUsecase(HotbillUsecase.SIMPLE_INQUIRY);
        HotbillResult result = hotbill.requestHotbill(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 실시간요금 결과조회
     * POST /api/hotbill/realtime/query
     */
    @PostMapping("/query")
    public ResponseEntity<HotbillResult> queryRealtimeBilling(
            @RequestBody HotbillQueryRequest request) {
        HotbillResult result = hotbill.queryHotbillResult(request);
        return ResponseEntity.ok(result);
    }
}

// HotbillSettlementController.java
@RestController
@RequestMapping("/api/hotbill/settlement")
public class HotbillSettlementController {

    private final Hotbill hotbill;

    /**
     * 해지정산 요청
     * POST /api/hotbill/settlement/request
     */
    @PostMapping("/request")
    public ResponseEntity<SettlementResult> requestSettlement(
            @RequestBody SettlementRequest request) {
        SettlementResult result = hotbill.requestSettlement(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 해지정산 당일취소
     * POST /api/hotbill/settlement/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<CancellationResult> cancelSettlement(
            @RequestBody CancellationRequest request) {
        CancellationResult result = hotbill.cancelSettlement(request);
        return ResponseEntity.ok(result);
    }
}

// ChargeDataController.java
@RestController
@RequestMapping("/api/hotbill/chargedata")
public class ChargeDataController {

    private final ChargeDataService chargeDataService;
    private final BillingService billingService;

    /**
     * 과금자료 등록 (외부 시스템 콜백)
     * POST /api/hotbill/chargedata/register
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerChargeData(
            @RequestBody List<ChargeData> chargeDataList) {
        chargeDataService.registerChargeData(chargeDataList);
        return ResponseEntity.ok().build();
    }
}
```

## 6. 핵심 서비스 설계

### 6.1 HotbillImpl

```java
@Service
public class HotbillImpl implements Hotbill {

    private final HotbillOrchestrator orchestrator;
    private final BillingService billingService;
    private final SettlementService settlementService;
    private final CancellationService cancellationService;

    @Override
    public HotbillResult requestHotbill(HotbillRequest request) {
        return orchestrator.executeHotbill(request);
    }

    @Override
    public HotbillResult queryHotbillResult(HotbillQueryRequest request) {
        return billingService.queryHotbillResult(request);
    }

    @Override
    public SettlementResult requestSettlement(SettlementRequest request) {
        return settlementService.processSettlement(request);
    }

    @Override
    public CancellationResult cancelSettlement(CancellationRequest request) {
        return cancellationService.processCancellation(request);
    }
}
```

### 6.2 ChargeDataService (비동기 콜백 처리)

```java
@Service
public class ChargeDataService {

    private final ChargeDataMapper chargeDataMapper;
    private final StatusService statusService;
    private final BillingCalculationClient billingClient;
    private final BillingService billingService;

    /**
     * 과금자료 등록 및 요금계산 처리
     */
    @Transactional
    public void registerChargeData(List<ChargeData> chargeDataList) {
        for (ChargeData chargeData : chargeDataList) {
            // 1. 기존 데이터 삭제
            chargeDataMapper.deleteByContractNumber(chargeData.getContractNumber());

            // 2. 신규 데이터 저장
            chargeDataMapper.insert(chargeData);

            // 3. 오류 체크
            if (chargeData.hasError()) {
                statusService.updateStatus(
                    chargeData.getContractNumber(),
                    HotbillStatus.CHARGE_DATA_ERROR
                );
                continue;
            }

            // 4. 상태: 요금계산 전
            statusService.updateStatus(
                chargeData.getContractNumber(),
                HotbillStatus.BEFORE_CALCULATION
            );
        }

        // 5. 정상 데이터만 요금계산 수행
        List<ChargeData> validData = chargeDataList.stream()
            .filter(data -> !data.hasError())
            .collect(Collectors.toList());

        if (!validData.isEmpty()) {
            calculateBilling(validData);
        }
    }

    private void calculateBilling(List<ChargeData> chargeDataList) {
        // 요금계산 API 호출
        HotbillResult result = billingClient.calculate(chargeDataList);

        // 결과별 상태 갱신
        result.getSuccessList().forEach(billing ->
            statusService.updateStatus(billing.getContractNumber(), HotbillStatus.COMPLETED)
        );
        result.getFailureList().forEach(failure ->
            statusService.updateStatus(failure.getContractNumber(), HotbillStatus.FAILED)
        );
    }
}
```

### 6.3 SettlementService (해지정산)

```java
@Service
public class SettlementService {

    private final StatusService statusService;
    private final PostingHistoryMapper postingHistoryMapper;
    private final CancellationLogMapper cancellationLogMapper;
    private final BillingInfoMapper billingInfoMapper;
    private final TableUpdateMapperRegistry mapperRegistry;

    @Transactional
    public SettlementResult processSettlement(SettlementRequest request) {
        // 1. 사전 검증
        validateSettlementReady(request.getContractNumbers());

        // 2. 포스팅이력 조회 및 업무 테이블 갱신
        List<PostingHistory> postingHistories =
            postingHistoryMapper.selectByContracts(request.getContractNumbers());

        for (PostingHistory history : postingHistories) {
            // 2-1. 변경 전 스냅샷 저장
            saveSnapshotBeforeUpdate(history);

            // 2-2. 업무 테이블 UPDATE
            updateBusinessTable(history);
        }

        // 3. 핫빌청구정보 → 청구정보 이관
        migrateBillingInfo(request.getContractNumbers());

        // 4. 핫빌청구정보 해지정산여부 갱신
        billingInfoMapper.updateSettlementYn(request.getContractNumbers(), "Y");

        // 5. 상태: 해지정산 완료
        statusService.updateStatus(request.getContractNumbers(), HotbillStatus.SETTLEMENT_COMPLETED);

        return SettlementResult.success(request.getContractNumbers());
    }

    private void validateSettlementReady(List<String> contractNumbers) {
        List<String> notCompletedContracts = statusService.findNotCompletedContracts(contractNumbers);
        if (!notCompletedContracts.isEmpty()) {
            throw new IllegalStateException("Hotbill not completed: " + notCompletedContracts);
        }
    }

    private void saveSnapshotBeforeUpdate(PostingHistory history) {
        PostingData postingData = parsePostingData(history.getPostingData());

        // 변경 전 데이터 조회
        Map<String, Object> beforeData = queryBeforeData(postingData);

        // 취소로그 저장
        CancellationLog log = CancellationLog.builder()
            .contractNumber(history.getContractNumber())
            .hotbillDate(history.getHotbillDate())
            .tableName(postingData.getTargetTable())
            .snapshotData(toJson(beforeData))
            .build();

        cancellationLogMapper.insert(log);
    }

    private void updateBusinessTable(PostingHistory history) {
        PostingData postingData = parsePostingData(history.getPostingData());

        // 테이블별 Mapper 조회
        TableUpdateMapper mapper = mapperRegistry.getMapper(postingData.getTargetTable());

        // UPDATE 실행
        Map<String, Object> updateData = buildUpdateData(postingData);
        mapper.update(updateData);
    }
}
```

### 6.4 CancellationService (해지정산 당일취소)

```java
@Service
public class CancellationService {

    private final StatusService statusService;
    private final CancellationLogMapper cancellationLogMapper;
    private final BillingInfoMapper billingInfoMapper;
    private final TableUpdateMapperRegistry mapperRegistry;

    @Transactional
    public CancellationResult processCancellation(CancellationRequest request) {
        // 1. 사전 검증
        validateCancellationReady(request.getContractNumbers());

        // 2. 취소로그 조회 및 데이터 원복
        List<CancellationLog> logs =
            cancellationLogMapper.selectByContracts(request.getContractNumbers());

        for (CancellationLog log : logs) {
            restoreBusinessTable(log);
        }

        // 3. 청구정보 테이블 데이터 삭제
        billingInfoMapper.deleteHotbillData(request.getContractNumbers());

        // 4. 핫빌청구정보 해지정산여부 갱신
        billingInfoMapper.updateSettlementYn(request.getContractNumbers(), "N");

        // 5. 상태: 요금계산 완료로 원복
        statusService.updateStatus(request.getContractNumbers(), HotbillStatus.COMPLETED);

        return CancellationResult.success(request.getContractNumbers());
    }

    private void validateCancellationReady(List<String> contractNumbers) {
        List<String> notSettledContracts =
            statusService.findNotSettledContracts(contractNumbers);
        if (!notSettledContracts.isEmpty()) {
            throw new IllegalStateException("Settlement not completed: " + notSettledContracts);
        }
    }

    private void restoreBusinessTable(CancellationLog log) {
        // 스냅샷 데이터 파싱
        Map<String, Object> snapshotData = parseSnapshotData(log.getSnapshotData());

        // 테이블별 Mapper 조회
        TableUpdateMapper mapper = mapperRegistry.getMapper(log.getTableName());

        // 원복 UPDATE 실행
        mapper.update(snapshotData);
    }
}
```

## 7. 프로세스 플로우 예시

### 7.1 혼합 계약 유형 처리 시나리오

**상황**: 3개의 계약에 대해 해지요금 조회 요청
- 계약1: 이동전화 (비동기 필요)
- 계약2: 인터넷 (동기)
- 계약3: IPTV (비동기 필요)

**처리 과정**:

```
1. REST API 인입
   POST /api/hotbill/termination/request
   {
     "contracts": [
       {"contractNumber": "CTR001", "contractType": "MOBILE_PHONE"},
       {"contractNumber": "CTR002", "contractType": "INTERNET"},
       {"contractNumber": "CTR003", "contractType": "IPTV"}
     ]
   }

2. HotbillTemplate.executeHotbill() 실행
   - Validation 수행
   - 3개 계약 모두 상태: "요청중"으로 갱신

3. 계약별 처리 타입 결정 및 그룹핑
   - 비동기 그룹: [CTR001(이동전화), CTR003(IPTV)]
   - 동기 그룹: [CTR002(인터넷)]

4. 비동기 그룹 처리 (AsyncChargeDataStrategy)
   - 계약 유형별 재그룹핑:
     * MOBILE_PHONE: [CTR001]
     * IPTV: [CTR003]
   - 전화과금자료 시스템에 CTR001 요청 (비동기)
   - 컨텐츠과금자료 시스템에 CTR003 요청 (비동기)
   - 즉시 반환: asyncProcessing=true

5. 동기 그룹 처리 (SyncChargeDataStrategy)
   - CTR002 상태: "요금계산 전"으로 갱신
   - 요금계산 API 호출 (동기)
   - CTR002 상태: "요금계산 완료"로 갱신
   - 계산 결과 반환

6. 결과 병합 및 응답
   - 비동기 결과 + 동기 결과 병합
   - asyncProcessing=true (비동기 계약이 포함되어 있음)
   - CTR002의 청구금액은 즉시 응답에 포함
   - CTR001, CTR003은 비동기 처리 중

7. [비동기 콜백 - 전화과금자료 시스템]
   POST /api/hotbill/chargedata/register
   {
     "chargeDataList": [
       {
         "contractNumber": "CTR001",
         "chargeItems": [...]
       }
     ]
   }
   - 과금자료 저장
   - CTR001 상태: "요금계산 전"으로 갱신
   - 요금계산 API 호출
   - CTR001 상태: "요금계산 완료"로 갱신

8. [비동기 콜백 - 컨텐츠과금자료 시스템]
   POST /api/hotbill/chargedata/register
   {
     "chargeDataList": [
       {
         "contractNumber": "CTR003",
         "chargeItems": [...]
       }
     ]
   }
   - 과금자료 저장
   - CTR003 상태: "요금계산 전"으로 갱신
   - 요금계산 API 호출
   - CTR003 상태: "요금계산 완료"로 갱신

9. 클라이언트에서 결과 조회
   POST /api/hotbill/termination/query
   {
     "contractNumbers": ["CTR001", "CTR002", "CTR003"]
   }
   - 모든 계약이 "요금계산 완료" 상태인지 확인
   - 핫빌청구정보 조회 및 반환
```

### 7.2 전체 동기 처리 시나리오

**상황**: 2개의 인터넷 계약에 대해 실시간요금 조회

```
1. REST API 인입
   POST /api/hotbill/realtime/request
   {
     "contracts": [
       {"contractNumber": "CTR101", "contractType": "INTERNET"},
       {"contractNumber": "CTR102", "contractType": "INTERNET"}
     ]
   }

2. 처리 타입 결정
   - 동기 그룹: [CTR101, CTR102]
   - 비동기 그룹: 없음

3. 동기 처리
   - 상태: "요청중" → "요금계산 전"
   - 요금계산 API 호출
   - 상태: "요금계산 완료"
   - 즉시 결과 반환 (asyncProcessing=false)
```

### 7.3 전체 비동기 처리 시나리오

**상황**: 5개의 이동전화 계약에 대해 해지요금 조회

```
1. REST API 인입
   POST /api/hotbill/termination/request
   {
     "contracts": [
       {"contractNumber": "CTR201", "contractType": "MOBILE_PHONE"},
       {"contractNumber": "CTR202", "contractType": "MOBILE_PHONE"},
       {"contractNumber": "CTR203", "contractType": "MOBILE_PHONE"},
       {"contractNumber": "CTR204", "contractType": "MOBILE_PHONE"},
       {"contractNumber": "CTR205", "contractType": "MOBILE_PHONE"}
     ]
   }

2. 처리 타입 결정
   - 비동기 그룹: [CTR201~205]
   - 동기 그룹: 없음

3. 비동기 처리
   - 계약 유형별 그룹핑: MOBILE_PHONE [CTR201~205]
   - 전화과금자료 시스템에 5개 계약 일괄 요청 (1번의 API 호출)
   - 즉시 반환 (asyncProcessing=true)

4. [비동기 콜백]
   POST /api/hotbill/chargedata/register
   - 5개 계약의 과금자료 일괄 처리
   - 각 계약별로 요금계산 수행
```

## 8. 확장 시나리오

### 8.1 신규 Hotbill Usecase 추가

**예시: "일시정지를 위한 요금 조회" usecase 추가**

1. Enum 추가:
```java
// HotbillUsecase.java
public enum HotbillUsecase {
    // ... 기존 usecase
    SUSPENSION_INQUIRY("일시정지를 위한 요금 조회");  // 추가
}
```

2. Validator 추가:
```java
// SuspensionInquiryValidator.java (신규)
@Component
public class SuspensionInquiryValidator implements UsecaseValidator {
    @Override
    public void validate(HotbillRequest request) {
        // 일시정지 특화 검증 로직
        validateContractExists(request.getContractNumbers());
        validateContractSuspendable(request.getContractNumbers());
    }
}
```

3. Factory 등록:
```java
// UsecaseValidatorFactory.java
public UsecaseValidatorFactory(..., SuspensionInquiryValidator suspensionValidator) {
    this.validatorMap = Map.of(
        // ... 기존 매핑
        HotbillUsecase.SUSPENSION_INQUIRY, suspensionValidator  // 추가
    );
}
```

**기존 코드 변경 없음!**

### 8.2 신규 계약 유형 추가

**예시: "5G 특화망" 계약 유형 추가 (대외 연동 필요)**

1. Enum 추가:
```java
// ContractType.java
public enum ContractType {
    // ... 기존 타입
    PRIVATE_5G("5G 특화망");  // 추가
}
```

2. External Client 추가:
```java
// Private5GChargeClient.java (신규)
@Component
public class Private5GChargeClient implements ExternalChargeClient {
    @Override
    public void requestChargeDataAsync(List<String> contractNumbers, HotbillUsecase usecase) {
        // 5G 특화망 과금 시스템 REST API 호출
    }
}
```

3. Strategy 설정 수정:
```java
// ChargeDataStrategyFactory.java
private boolean isAsyncContractType(ContractType type, HotbillUsecase usecase) {
    return switch (type) {
        // ... 기존 타입
        case PRIVATE_5G -> true;  // 추가
    };
}
```

**기존 코드 최소 변경!**

### 8.3 신규 포스팅 대상 테이블 추가

**예시: "TB_SERVICE" 테이블 UPDATE 추가**

1. Mapper 추가:
```java
// ServiceTableUpdateMapper.java (신규)
@Mapper
public interface ServiceTableUpdateMapper extends TableUpdateMapper {

    @Override
    default String getTableName() {
        return "TB_SERVICE";
    }

    void updateServiceEndDate(
        @Param("contractNumber") String contractNumber,
        @Param("endDate") LocalDateTime endDate
    );

    @Override
    default void update(Map<String, Object> updateData) {
        String contractNumber = (String) updateData.get("contractNumber");
        LocalDateTime endDate = (LocalDateTime) updateData.get("endDate");
        updateServiceEndDate(contractNumber, endDate);
    }
}
```

2. Spring이 자동으로 Registry에 등록 (변경 없음)

**기존 코드 변경 없음!**

## 9. 트랜잭션 관리

### 9.1 트랜잭션 경계

- **Hotbill 요청 (동기)**: 전체 프로세스를 하나의 트랜잭션으로 처리
- **Hotbill 요청 (비동기)**: 요청 등록까지만 트랜잭션 처리
- **과금자료 등록**: 각 계약별로 독립 트랜잭션 (부분 실패 허용)
- **해지정산**: 전체 프로세스를 하나의 트랜잭션으로 처리
- **해지정산 당일취소**: 전체 프로세스를 하나의 트랜잭션으로 처리

### 9.2 롤백 정책

- 비즈니스 예외: 롤백
- 기술 예외: 롤백
- 외부 연동 실패: 상태만 갱신, 트랜잭션 커밋

## 10. 예외 처리 전략

```java
// 비즈니스 예외
public class HotbillException extends RuntimeException {
    private final ErrorCode errorCode;
}

public enum ErrorCode {
    CONTRACT_NOT_FOUND("계약을 찾을 수 없습니다"),
    INVALID_STATUS("유효하지 않은 상태입니다"),
    CALCULATION_NOT_COMPLETED("요금계산이 완료되지 않았습니다"),
    SETTLEMENT_NOT_COMPLETED("해지정산이 완료되지 않았습니다"),
    EXTERNAL_SYSTEM_ERROR("외부 시스템 연동 오류");
}
```

## 11. 테스트 전략

### 11.1 단위 테스트
- Validator 테스트
- Strategy 테스트
- Service 메서드 테스트

### 11.2 통합 테스트
- REST API 테스트
- 데이터베이스 연동 테스트
- 트랜잭션 테스트

### 11.3 E2E 테스트
- 동기 프로세스 전체 플로우
- 비동기 프로세스 전체 플로우
- 혼합 처리 플로우 (동기/비동기 혼재)
- 해지정산 및 취소 플로우
