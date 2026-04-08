# Hotbill 모듈 구현 계획

## 개요

이 문서는 Hotbill 모듈의 구현 계획을 단계별로 정리한 문서입니다.
TDD(Test-Driven Development) 사이클을 철저히 준수하여 개발합니다.

**TDD 사이클**: Red (실패하는 테스트 작성) → Green (테스트 통과시키기) → Refactor (리팩토링)

## 작업 원칙

1. 각 작업은 TDD 사이클을 한 번 이상 포함합니다
2. 테스트를 먼저 작성하고, 구현은 테스트를 통과시키는 최소한의 코드로 작성합니다
3. 구현 계획에 없는 내용은 절대로 구현하지 않습니다
4. 적절한 덩어리의 개발이 완료되면 멈추고 사용자에게 확인 시간을 제공합니다

---

## Phase 1: 프로젝트 기본 구조 및 도메인 모델

### Task 1-1: 프로젝트 구조 생성
**목표**: Multi-module 프로젝트 구조 생성

**작업 내용**:
- Gradle/Maven 기반 멀티 모듈 프로젝트 생성
- `hotbill-api` 서브모듈 생성
- `hotbill-impl` 서브모듈 생성
- Spring Boot 4.x, Java 25 설정
- 기본 패키지 구조 생성

**체크포인트**:
- [V] 프로젝트 빌드 성공
- [V] 각 서브모듈 독립적으로 빌드 가능

---

### Task 1-2: 열거형(Enum) 정의 - TDD
**목표**: 핵심 도메인 열거형 정의

**TDD 사이클**:

**Red**:
- `HotbillStatusTest` 작성
  - 모든 상태값 존재 확인
  - 상태값 설명 확인
- `ContractTypeTest` 작성
  - 모든 계약 유형 존재 확인
- `HotbillUsecaseTest` 작성
  - 모든 usecase 존재 확인

**Green**:
- `HotbillStatus` enum 구현 (hotbill-api)
  ```
  REQUESTING, BEFORE_CALCULATION, CHARGE_DATA_ERROR,
  COMPLETED, FAILED, SETTLEMENT_COMPLETED
  ```
- `ContractType` enum 구현 (hotbill-api)
  ```
  MOBILE_PHONE, INTERNET, IPTV
  ```
- `HotbillUsecase` enum 구현 (hotbill-api)
  ```
  SIMPLE_INQUIRY, TERMINATION_INQUIRY, PORT_OUT, FORCED_TERMINATION
  ```

**Refactor**:
- 코드 정리 및 JavaDoc 추가

**체크포인트**:
- [V] 모든 테스트 통과
- [V] Enum 값들이 요구사항과 일치

---

### Task 1-3: 기본 DTO 정의 - TDD
**목표**: 핵심 DTO 클래스 정의

**TDD 사이클**:

**Red**:
- `ContractInfoTest` 작성
  - 객체 생성 및 getter 테스트
- `ChargeItemTest` 작성
  - 객체 생성 및 validation 테스트
- `ChargeDataTest` 작성
  - hasError() 메서드 테스트 (errorCode 유무에 따라)

**Green**:
- `ContractInfo` 클래스 구현 (hotbill-api)
  - contractNumber, contractType 필드
- `ChargeItem` 클래스 구현 (hotbill-api)
  - chargeCode, amount 필드
- `ChargeData` 클래스 구현 (hotbill-api)
  - contractNumber, chargeItems, errorCode, errorMessage 필드
  - hasError() 메서드 구현

**Refactor**:
- Builder 패턴 적용 고려
- Lombok 사용 고려

**체크포인트**:
- [V] 모든 테스트 통과
- [V] DTO 불변성 검토

---

### Task 1-4: Request/Result DTO 정의 - TDD
**목표**: API 요청/응답 DTO 정의

**TDD 사이클**:

**Red**:
- `HotbillRequestTest` 작성
  - getContractNumbers() 메서드 테스트
  - withContracts() 메서드 테스트 (새 객체 생성 확인)
- `HotbillResultTest` 작성
  - merge() 메서드 테스트
  - asyncProcessing() 정적 메서드 테스트
- `BillingInfoTest` 작성
  - 객체 생성 테스트

**Green**:
- `HotbillRequest` 클래스 구현 (hotbill-api)
  - usecase, contracts, requestDateTime 필드
  - getContractNumbers(), withContracts() 메서드
- `HotbillResult` 클래스 구현 (hotbill-api)
  - successList, failureList, asyncProcessing 필드
  - merge(), asyncProcessing() 메서드
- `BillingInfo` 클래스 구현 (hotbill-api)
  - contractNumber, hotbillDate, totalAmount, chargeItems 필드
- `FailureInfo` 클래스 구현 (hotbill-api)

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] 모든 테스트 통과
- [V] merge() 로직 정확성 확인

---

### Task 1-5: 해지정산 관련 DTO 정의 - TDD
**목표**: 해지정산/취소 요청/응답 DTO 정의

**TDD 사이클**:

**Red**:
- `SettlementRequestTest` 작성
- `SettlementResultTest` 작성
- `CancellationRequestTest` 작성
- `CancellationResultTest` 작성
- `HotbillQueryRequestTest` 작성

**Green**:
- `SettlementRequest` 클래스 구현 (hotbill-api)
- `SettlementResult` 클래스 구현 (hotbill-api)
- `CancellationRequest` 클래스 구현 (hotbill-api)
- `CancellationResult` 클래스 구현 (hotbill-api)
- `HotbillQueryRequest` 클래스 구현 (hotbill-api)

**Refactor**:
- 공통 필드 추출 고려

**체크포인트**:
- [V] 모든 테스트 통과

---

**Phase 1 완료 후 중단**: 사용자 확인 필요

---

## Phase 2: 핵심 인터페이스 및 Strategy Pattern 구현

### Task 2-1: Hotbill 인터페이스 정의 - TDD
**목표**: 핵심 서비스 인터페이스 정의

**TDD 사이클**:

**Red**:
- `HotbillTest` 인터페이스 테스트 작성 (Mock 기반)
  - requestHotbill() 메서드 시그니처 확인
  - queryHotbillResult() 메서드 시그니처 확인
  - requestSettlement() 메서드 시그니처 확인
  - cancelSettlement() 메서드 시그니처 확인

**Green**:
- `Hotbill` 인터페이스 구현 (hotbill-api)
  - 4개 메서드 정의

**Refactor**:
- JavaDoc 추가

**체크포인트**:
- [V] 인터페이스 정의 완료

---

### Task 2-2: UsecaseValidator 인터페이스 및 구현체 - TDD
**목표**: Usecase별 검증 로직 구현

**TDD 사이클 - 인터페이스**:

**Red**:
- `UsecaseValidatorTest` 작성
  - validate() 메서드 인터페이스 확인

**Green**:
- `UsecaseValidator` 인터페이스 구현 (hotbill-impl)

**TDD 사이클 - SimpleInquiryValidator**:

**Red**:
- `SimpleInquiryValidatorTest` 작성
  - 계약 존재 여부 검증 테스트
  - 검증 실패 시 예외 발생 테스트

**Green**:
- `SimpleInquiryValidator` 구현 (hotbill-impl)
  - 계약 존재 여부 검증 로직 (stub)

**TDD 사이클 - TerminationInquiryValidator**:

**Red**:
- `TerminationInquiryValidatorTest` 작성
  - 계약 존재 검증
  - 해지 가능 여부 검증

**Green**:
- `TerminationInquiryValidator` 구현 (hotbill-impl)

**TDD 사이클 - 나머지 Validator**:
- `PortOutValidator` 구현 (TDD)
- `ForcedTerminationValidator` 구현 (TDD)

**Refactor**:
- 공통 검증 로직 추출

**체크포인트**:
- [V] 모든 Validator 테스트 통과
- [V] 4개 usecase별 Validator 구현 완료

---

### Task 2-3: UsecaseValidatorFactory 구현 - TDD
**목표**: Validator Factory 구현

**TDD 사이클**:

**Red**:
- `UsecaseValidatorFactoryTest` 작성
  - 각 usecase별 적절한 validator 반환 테스트
  - 존재하지 않는 usecase에 대한 예외 발생 테스트

**Green**:
- `UsecaseValidatorFactory` 구현 (hotbill-impl)
  - Map 기반 validator 관리
  - getValidator() 메서드 구현

**Refactor**:
- Spring Bean 등록 확인

**체크포인트**:
- [V] Factory 테스트 통과
- [V] 모든 usecase에 대한 매핑 완료

---

### Task 2-4: ProcessingType Enum 및 ChargeDataStrategy 인터페이스 - TDD
**목표**: 과금자료 처리 전략 인터페이스 정의

**TDD 사이클**:

**Red**:
- `ProcessingTypeTest` 작성
  - SYNC, ASYNC 값 존재 확인
- `ChargeDataStrategyTest` 작성 (인터페이스)
  - process() 메서드 시그니처 확인

**Green**:
- `ProcessingType` enum 구현 (hotbill-impl)
- `ChargeDataStrategy` 인터페이스 구현 (hotbill-impl)

**Refactor**:
- JavaDoc 추가

**체크포인트**:
- [V] 인터페이스 정의 완료

---

### Task 2-5: SyncChargeDataStrategy 구현 - TDD
**목표**: 동기 과금자료 처리 전략 구현

**TDD 사이클**:

**Red**:
- `SyncChargeDataStrategyTest` 작성
  - 상태를 BEFORE_CALCULATION으로 변경하는지 테스트
  - BillingCalculationClient 호출 테스트 (Mock)
  - 성공 시 COMPLETED 상태 변경 테스트
  - 실패 시 FAILED 상태 변경 테스트

**Green**:
- `SyncChargeDataStrategy` 구현 (hotbill-impl)
  - StatusService 의존성 (stub으로 시작)
  - BillingCalculationClient 의존성 (stub으로 시작)
  - process() 메서드 구현
  - updateStatusByResult() 메서드 구현

**Refactor**:
- 에러 처리 개선

**체크포인트**:
- [V] 모든 테스트 통과
- [V] 동기 처리 로직 검증 완료

---

### Task 2-6: AsyncChargeDataStrategy 구현 - TDD
**목표**: 비동기 과금자료 처리 전략 구현

**TDD 사이클**:

**Red**:
- `AsyncChargeDataStrategyTest` 작성
  - 계약 유형별 그룹핑 테스트
  - 각 계약 유형별 ExternalClient 호출 테스트 (Mock)
  - asyncProcessing=true 결과 반환 테스트
  - 한번에 여러 계약 요청 가능 테스트

**Green**:
- `AsyncChargeDataStrategy` 구현 (hotbill-impl)
  - Map<ContractType, ExternalChargeClient> 의존성
  - process() 메서드 구현
  - 계약 유형별 그룹핑 로직

**Refactor**:
- 그룹핑 로직 최적화

**체크포인트**:
- [V] 모든 테스트 통과
- [V] 비동기 처리 로직 검증 완료

---

### Task 2-7: ChargeDataStrategyFactory 구현 - TDD
**목표**: Strategy Factory 및 처리 타입 결정 로직 구현

**TDD 사이클**:

**Red**:
- `ChargeDataStrategyFactoryTest` 작성
  - getSyncStrategy() 테스트
  - getAsyncStrategy() 테스트
  - determineProcessingType() 테스트
    - MOBILE_PHONE → ASYNC
    - IPTV → ASYNC
    - INTERNET → SYNC
  - shouldSkipExternalIntegration() 확장 포인트 테스트

**Green**:
- `ChargeDataStrategyFactory` 구현 (hotbill-impl)
  - Strategy 의존성 주입
  - determineProcessingType() 로직 구현
  - shouldSkipExternalIntegration() 구현 (기본: false)

**Refactor**:
- switch expression 사용

**체크포인트**:
- [V] 모든 테스트 통과
- [V] 모든 계약 유형에 대한 처리 타입 결정 로직 완료

---

**Phase 2 완료 후 중단**: 사용자 확인 필요

---

## Phase 3: Template Method Pattern 및 Orchestrator 구현

### Task 3-1: HotbillTemplate 추상 클래스 구현 - TDD
**목표**: Template Method 패턴 핵심 로직 구현

**TDD 사이클**:

**Red**:
- `HotbillTemplateTest` 작성
  - executeHotbill() 전체 플로우 테스트
  - validate() 호출 확인 (Mock)
  - 상태 초기화 확인
  - 계약별 동기/비동기 그룹핑 테스트
  - 동기/비동기 Strategy 각각 호출 테스트
  - 결과 merge 테스트
  - postProcess() 호출 확인

**Green**:
- `HotbillTemplate` 추상 클래스 구현 (hotbill-impl)
  - executeHotbill() 템플릿 메서드
  - groupContractsByProcessingType() 메서드
  - determineProcessingType() 메서드
  - validate() 추상 메서드
  - postProcess() 훅 메서드 (기본 구현: 비어있음)

**Refactor**:
- 가독성 개선

**체크포인트**:
- [V] Template Method 패턴 구조 검증 완료
- [V] 동기/비동기 혼합 처리 로직 검증 완료

---

### Task 3-2: DefaultHotbillTemplate 구현 - TDD
**목표**: 기본 Template 구현체

**TDD 사이클**:

**Red**:
- `DefaultHotbillTemplateTest` 작성
  - validate() 메서드가 UsecaseValidatorFactory를 통해 적절한 validator 호출하는지 테스트

**Green**:
- `DefaultHotbillTemplate` 구현 (hotbill-impl)
  - UsecaseValidatorFactory 의존성
  - validate() 메서드 구현

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] Validator 연동 테스트 통과

---

### Task 3-3: HotbillOrchestrator 구현 - TDD
**목표**: Orchestrator 계층 구현

**TDD 사이클**:

**Red**:
- `HotbillOrchestratorTest` 작성
  - executeHotbill()이 HotbillTemplate.executeHotbill() 호출하는지 테스트

**Green**:
- `HotbillOrchestrator` 구현 (hotbill-impl)
  - HotbillTemplate 의존성
  - executeHotbill() 메서드 (위임)

**Refactor**:
- 추가 orchestration 로직 필요 시 확장

**체크포인트**:
- [V] Orchestrator 테스트 통과

---

**Phase 3 완료 후 중단**: 사용자 확인 필요

---

## Phase 4: Infrastructure Layer - MyBatis Mapper 및 Repository

### Task 4-1: 데이터베이스 테이블 DDL 작성
**목표**: 필요한 모든 테이블 DDL 작성

**작업 내용**:
- `schema.sql` 파일 생성
- 6개 테이블 DDL 작성
  - TB_HOTBILL_STATUS
  - TB_CHARGE_DATA
  - TB_HOTBILL_BILLING_INFO
  - TB_BILLING_INFO
  - TB_POSTING_HISTORY
  - TB_CANCELLATION_LOG

**체크포인트**:
- [V] DDL 스크립트 실행 가능
- [V] 모든 제약 조건 포함

---

### Task 4-2: HotbillStatusMapper 구현 - TDD
**목표**: 핫빌 진행상태 Mapper 구현

**TDD 사이클**:

**Red**:
- `HotbillStatusMapperTest` 작성 (MyBatis 테스트)
  - insert() 테스트
  - updateStatus() 테스트 (단건)
  - updateStatusBatch() 테스트 (다건)
  - selectByContractNumber() 테스트
  - findNotCompletedContracts() 테스트
  - findNotSettledContracts() 테스트

**Green**:
- `HotbillStatusMapper` 인터페이스 구현 (hotbill-impl)
- `HotbillStatusMapper.xml` 작성

**Refactor**:
- 쿼리 최적화

**체크포인트**:
- [V] 모든 Mapper 테스트 통과

---

### Task 4-3: ChargeDataMapper 구현 - TDD
**목표**: 과금자료 Mapper 구현

**TDD 사이클**:

**Red**:
- `ChargeDataMapperTest` 작성
  - insert() 테스트
  - deleteByContractNumber() 테스트
  - selectByContractNumber() 테스트

**Green**:
- `ChargeDataMapper` 인터페이스 구현 (hotbill-impl)
- `ChargeDataMapper.xml` 작성

**Refactor**:
- 쿼리 최적화

**체크포인트**:
- [V] 모든 Mapper 테스트 통과

---

### Task 4-4: BillingInfoMapper 구현 - TDD
**목표**: 청구정보 Mapper 구현

**TDD 사이클**:

**Red**:
- `BillingInfoMapperTest` 작성
  - insertHotbillBillingInfo() 테스트
  - selectHotbillBillingInfo() 테스트
  - updateSettlementYn() 테스트
  - insertBillingInfo() 테스트 (이관용)
  - deleteHotbillData() 테스트

**Green**:
- `BillingInfoMapper` 인터페이스 구현 (hotbill-impl)
- `BillingInfoMapper.xml` 작성

**Refactor**:
- 쿼리 최적화

**체크포인트**:
- [V] 모든 Mapper 테스트 통과

---

### Task 4-5: PostingHistoryMapper 구현 - TDD
**목표**: 포스팅이력 Mapper 구현

**TDD 사이클**:

**Red**:
- `PostingHistoryMapperTest` 작성
  - insert() 테스트
  - selectByContracts() 테스트
  - JSON 데이터 파싱 테스트

**Green**:
- `PostingHistoryMapper` 인터페이스 구현 (hotbill-impl)
- `PostingHistoryMapper.xml` 작성
- JSON 파싱 유틸리티 구현

**Refactor**:
- JSON 처리 개선

**체크포인트**:
- [V] 모든 Mapper 테스트 통과

---

### Task 4-6: CancellationLogMapper 구현 - TDD
**목표**: 핫빌취소로그 Mapper 구현

**TDD 사이클**:

**Red**:
- `CancellationLogMapperTest` 작성
  - insert() 테스트
  - selectByContracts() 테스트
  - JSON 스냅샷 데이터 파싱 테스트

**Green**:
- `CancellationLogMapper` 인터페이스 구현 (hotbill-impl)
- `CancellationLogMapper.xml` 작성

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] 모든 Mapper 테스트 통과

---

### Task 4-7: TableUpdateMapper 인터페이스 및 구현체 - TDD
**목표**: Registry Pattern으로 테이블별 UPDATE 관리

**TDD 사이클 - 인터페이스**:

**Red**:
- `TableUpdateMapperTest` 작성 (인터페이스)
  - getTableName() 테스트
  - update() 테스트

**Green**:
- `TableUpdateMapper` 인터페이스 구현 (hotbill-impl)

**TDD 사이클 - ContractTableUpdateMapper**:

**Red**:
- `ContractTableUpdateMapperTest` 작성
  - getTableName() = "TB_CONTRACT" 테스트
  - updateTerminationDate() 테스트
  - update() 메서드 테스트

**Green**:
- `ContractTableUpdateMapper` 구현 (hotbill-impl)
- `ContractTableUpdateMapper.xml` 작성

**TDD 사이클 - ServiceTableUpdateMapper**:
- 동일한 TDD 사이클로 구현

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] 모든 UpdateMapper 테스트 통과

---

### Task 4-8: TableUpdateMapperRegistry 구현 - TDD
**목표**: Mapper Registry 구현

**TDD 사이클**:

**Red**:
- `TableUpdateMapperRegistryTest` 작성
  - getMapper() 테스트 (각 테이블명으로 조회)
  - 존재하지 않는 테이블에 대한 예외 발생 테스트

**Green**:
- `TableUpdateMapperRegistry` 구현 (hotbill-impl)
  - Spring이 각 Mapper를 직접 주입
  - Map으로 관리

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] Registry 테스트 통과

---

**Phase 4 완료 후 중단**: 사용자 확인 필요

---

## Phase 5: Service Layer 구현

### Task 5-1: StatusService 구현 - TDD
**목표**: 상태 관리 서비스 구현

**TDD 사이클**:

**Red**:
- `StatusServiceTest` 작성
  - updateStatus() 단건 테스트
  - updateStatus() 다건 테스트
  - findNotCompletedContracts() 테스트
  - findNotSettledContracts() 테스트

**Green**:
- `StatusService` 구현 (hotbill-impl)
  - HotbillStatusMapper 의존성
  - 각 메서드 구현

**Refactor**:
- 트랜잭션 처리 확인

**체크포인트**:
- [V] 모든 테스트 통과

---

### Task 5-2: ChargeDataService 구현 - TDD (비동기 콜백 처리)
**목표**: 과금자료 등록 및 요금계산 처리 서비스 구현

**TDD 사이클**:

**Red**:
- `ChargeDataServiceTest` 작성
  - registerChargeData() 정상 케이스 테스트
  - 기존 데이터 삭제 후 저장 확인
  - 오류 데이터 처리 테스트 (CHARGE_DATA_ERROR 상태)
  - 정상 데이터만 요금계산 수행 테스트
  - 결과별 상태 갱신 테스트

**Green**:
- `ChargeDataService` 구현 (hotbill-impl)
  - ChargeDataMapper 의존성
  - StatusService 의존성
  - BillingCalculationClient 의존성
  - registerChargeData() 메서드
  - calculateBilling() 메서드

**Refactor**:
- 트랜잭션 경계 확인
- 부분 실패 처리 개선

**체크포인트**:
- [V] 모든 테스트 통과
- [V] 비동기 콜백 로직 검증 완료

---

### Task 5-3: BillingService 구현 - TDD
**목표**: 청구정보 조회 서비스 구현

**TDD 사이클**:

**Red**:
- `BillingServiceTest` 작성
  - queryHotbillResult() 테스트
  - 미완료 계약이 있을 때 예외 발생 테스트
  - 모두 완료된 경우 청구정보 반환 테스트

**Green**:
- `BillingService` 구현 (hotbill-impl)
  - StatusService 의존성
  - BillingInfoMapper 의존성
  - queryHotbillResult() 메서드

**Refactor**:
- 코드 정리

**체크포인트**:
- [ ] 모든 테스트 통과

---

### Task 5-4: SettlementService 구현 - TDD
**목표**: 해지정산 처리 서비스 구현

**TDD 사이클**:

**Red**:
- `SettlementServiceTest` 작성
  - processSettlement() 전체 플로우 테스트
  - validateSettlementReady() 테스트
  - saveSnapshotBeforeUpdate() 테스트
  - updateBusinessTable() 테스트
  - migrateBillingInfo() 테스트
  - 상태 갱신 테스트

**Green**:
- `SettlementService` 구현 (hotbill-impl)
  - 모든 필요한 의존성 주입
  - processSettlement() 메서드
  - validateSettlementReady() 메서드
  - saveSnapshotBeforeUpdate() 메서드
  - updateBusinessTable() 메서드
  - migrateBillingInfo() 메서드

**Refactor**:
- 트랜잭션 처리 확인
- 예외 처리 개선

**체크포인트**:
- [V] 모든 테스트 통과
- [V] 해지정산 로직 검증 완료

---

### Task 5-5: CancellationService 구현 - TDD
**목표**: 해지정산 당일취소 처리 서비스 구현

**TDD 사이클**:

**Red**:
- `CancellationServiceTest` 작성
  - processCancellation() 전체 플로우 테스트
  - validateCancellationReady() 테스트
  - restoreBusinessTable() 테스트
  - 청구정보 삭제 테스트
  - 상태 원복 테스트

**Green**:
- `CancellationService` 구현 (hotbill-impl)
  - 모든 필요한 의존성 주입
  - processCancellation() 메서드
  - validateCancellationReady() 메서드
  - restoreBusinessTable() 메서드

**Refactor**:
- 트랜잭션 처리 확인
- 롤백 로직 검증

**체크포인트**:
- [V] 모든 테스트 통과
- [V] 취소 로직 검증 완료

---

**Phase 5 완료 후 중단**: 사용자 확인 필요

---

## Phase 6: External Client 및 Stub 구현

### Task 6-1: ExternalChargeClient 인터페이스 정의 - TDD
**목표**: 외부 과금 시스템 클라이언트 인터페이스 정의

**TDD 사이클**:

**Red**:
- `ExternalChargeClientTest` 작성 (인터페이스)
  - requestChargeDataAsync() 메서드 시그니처 확인

**Green**:
- `ExternalChargeClient` 인터페이스 구현 (hotbill-impl)

**체크포인트**:
- [V] 인터페이스 정의 완료

---

### Task 6-2: TelephonyChargeClient 구현 - TDD (Stub)
**목표**: 전화과금자료 시스템 클라이언트 구현

**TDD 사이클**:

**Red**:
- `TelephonyChargeClientTest` 작성
  - requestChargeDataAsync() REST API 호출 테스트 (Mock)
  - 여러 계약 일괄 요청 테스트

**Green**:
- `TelephonyChargeClient` 구현 (hotbill-impl)
  - RestTemplate/WebClient 사용
  - requestChargeDataAsync() 메서드 (실제 외부 API는 stub)

**Refactor**:
- 에러 처리 추가

**체크포인트**:
- [V] 테스트 통과

---

### Task 6-3: ContentChargeClient 구현 - TDD (Stub)
**목표**: 컨텐츠과금자료 시스템 클라이언트 구현

**TDD 사이클**:
- TelephonyChargeClient와 동일한 패턴으로 구현

**체크포인트**:
- [V] 테스트 통과

---

### Task 6-4: BillingCalculationClient 구현 - TDD (Stub)
**목표**: 요금계산 API 클라이언트 Stub 구현

**TDD 사이클**:

**Red**:
- `BillingCalculationClientTest` 작성
  - calculate() 메서드 테스트
  - 성공/실패 케이스 반환 테스트

**Green**:
- `BillingCalculationClient` 구현 (hotbill-impl)
  - calculate() 메서드 (stub으로 더미 데이터 반환)

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] 테스트 통과

---

**Phase 6 완료 후 중단**: 사용자 확인 필요

---

## Phase 7: HotbillImpl 및 통합

### Task 7-1: HotbillImpl 구현 - TDD
**목표**: Hotbill 인터페이스 구현체 작성

**TDD 사이클**:

**Red**:
- `HotbillImplTest` 작성
  - requestHotbill() 테스트 (Orchestrator 호출 확인)
  - queryHotbillResult() 테스트 (BillingService 호출 확인)
  - requestSettlement() 테스트 (SettlementService 호출 확인)
  - cancelSettlement() 테스트 (CancellationService 호출 확인)

**Green**:
- `HotbillImpl` 구현 (hotbill-impl)
  - @Service 어노테이션
  - 모든 의존성 주입
  - 4개 메서드 구현 (위임)

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] 모든 테스트 통과

---

### Task 7-2: Spring Configuration 작성
**목표**: Bean 설정 및 Configuration 작성

**작업 내용**:
- `HotbillConfig` 작성 (hotbill-impl)
- Component Scan 설정
- MyBatis 설정
- RestTemplate/WebClient Bean 등록
- 필요한 모든 Bean 등록 확인

**체크포인트**:
- [V] Application Context 로딩 성공
- [V] 모든 Bean 주입 확인

---

**Phase 7 완료 후 중단**: 사용자 확인 필요

---

## Phase 8: REST API Controller 구현

### Task 8-1: HotbillTerminationController 구현 - TDD
**목표**: 해지요금 관련 REST API 구현

**TDD 사이클**:

**Red**:
- `HotbillTerminationControllerTest` 작성 (MockMvc)
  - POST /api/hotbill/termination/request 테스트
  - POST /api/hotbill/termination/query 테스트
  - 요청/응답 JSON 검증

**Green**:
- `HotbillTerminationController` 구현 (hotbill-impl)
  - @RestController
  - requestTerminationBilling() 메서드
  - queryTerminationBilling() 메서드

**Refactor**:
- 예외 처리 추가

**체크포인트**:
- [V] 컨트롤러 구현 완료 (테스트는 Phase 8 완료 후 일괄 수정)

---

### Task 8-2: HotbillRealtimeController 구현 - TDD
**목표**: 실시간요금 관련 REST API 구현

**TDD 사이클**:
- HotbillTerminationController와 동일한 패턴

**체크포인트**:
- [V] 컨트롤러 구현 완료 (테스트는 Phase 8 완료 후 일괄 수정)

---

### Task 8-3: HotbillSettlementController 구현 - TDD
**목표**: 해지정산 관련 REST API 구현

**TDD 사이클**:

**Red**:
- `HotbillSettlementControllerTest` 작성
  - POST /api/hotbill/settlement/request 테스트
  - POST /api/hotbill/settlement/cancel 테스트

**Green**:
- `HotbillSettlementController` 구현 (hotbill-impl)
  - requestSettlement() 메서드
  - cancelSettlement() 메서드

**Refactor**:
- 코드 정리

**체크포인트**:
- [V] 컨트롤러 구현 완료 (테스트는 Phase 8 완료 후 일괄 수정)

---

### Task 8-4: ChargeDataController 구현 - TDD
**목표**: 과금자료 등록 REST API 구현 (외부 시스템 콜백)

**TDD 사이클**:

**Red**:
- `ChargeDataControllerTest` 작성
  - POST /api/hotbill/chargedata/register 테스트
  - List<ChargeData> 요청 파싱 테스트

**Green**:
- `ChargeDataController` 구현 (hotbill-impl)
  - registerChargeData() 메서드

**Refactor**:
- 에러 응답 처리

**체크포인트**:
- [V] 컨트롤러 구현 완료 (테스트는 Phase 8 완료 후 일괄 수정)

---

**Phase 8 완료 후 중단**: 사용자 확인 필요

---

## Phase 9: 예외 처리 및 통합 테스트

### Task 9-1: 예외 클래스 정의 - TDD
**목표**: 비즈니스 예외 체계 구축

**TDD 사이클**:

**Red**:
- `HotbillExceptionTest` 작성
  - ErrorCode 테스트
  - 예외 메시지 테스트

**Green**:
- `HotbillException` 클래스 구현 (hotbill-impl)
- `ErrorCode` enum 구현 (hotbill-impl)
  - CONTRACT_NOT_FOUND
  - INVALID_STATUS
  - CALCULATION_NOT_COMPLETED
  - SETTLEMENT_NOT_COMPLETED
  - EXTERNAL_SYSTEM_ERROR

**Refactor**:
- 코드 정리

**체크포인트**:
- [ ] 예외 체계 구축 완료

---

### Task 9-2: GlobalExceptionHandler 구현 - TDD
**목표**: REST API 예외 처리

**TDD 사이클**:

**Red**:
- `GlobalExceptionHandlerTest` 작성
  - HotbillException 처리 테스트
  - 일반 예외 처리 테스트
  - 응답 형식 검증

**Green**:
- `GlobalExceptionHandler` 구현 (hotbill-impl)
  - @RestControllerAdvice
  - 각 예외별 핸들러

**Refactor**:
- 에러 응답 형식 표준화

**체크포인트**:
- [V] 모든 예외 처리 테스트 통과

---

### Task 9-3: 통합 테스트 - 동기 처리 플로우
**목표**: 인터넷 계약 동기 처리 E2E 테스트

**TDD 사이클**:

**Red**:
- `HotbillSyncIntegrationTest` 작성
  - 실시간요금 요청 → 즉시 응답 확인
  - 상태 변화 확인
  - 청구정보 저장 확인

**Green**:
- 통합 테스트 실행 및 수정

**Refactor**:
- 테스트 데이터 정리

**체크포인트**:
- [V] 동기 플로우 E2E 테스트 통과 (테스트 환경 수정 필요 - Phase 8과 동일 이슈)

---

### Task 9-4: 통합 테스트 - 비동기 처리 플로우
**목표**: 이동전화 계약 비동기 처리 E2E 테스트

**TDD 사이클**:

**Red**:
- `HotbillAsyncIntegrationTest` 작성
  - 해지요금 요청 → 비동기 응답 확인
  - 과금자료 등록 콜백 → 요금계산 완료 확인
  - 결과 조회 확인

**Green**:
- 통합 테스트 실행 및 수정

**Refactor**:
- 비동기 테스트 안정화

**체크포인트**:
- [V] 비동기 플로우 E2E 테스트 통과 (테스트 환경 수정 필요 - Phase 8과 동일 이슈)

---

### Task 9-5: 통합 테스트 - 혼합 처리 플로우
**목표**: 동기/비동기 혼합 계약 처리 E2E 테스트

**TDD 사이클**:

**Red**:
- `HotbillMixedIntegrationTest` 작성
  - 3개 계약 (이동전화, 인터넷, IPTV) 동시 요청
  - 동기 계약은 즉시 완료 확인
  - 비동기 계약은 콜백 후 완료 확인
  - 결과 병합 확인

**Green**:
- 통합 테스트 실행 및 수정

**Refactor**:
- 테스트 안정화

**체크포인트**:
- [V] 혼합 플로우 E2E 테스트 통과 (테스트 환경 수정 필요 - Phase 8과 동일 이슈)

---

### Task 9-6: 통합 테스트 - 해지정산 플로우
**목표**: 해지정산 및 당일취소 E2E 테스트

**TDD 사이클**:

**Red**:
- `SettlementIntegrationTest` 작성
  - Hotbill 완료 → 해지정산 요청
  - 포스팅이력 처리 확인
  - 취소로그 저장 확인
  - 청구정보 이관 확인
  - 당일취소 → 원복 확인

**Green**:
- 통합 테스트 실행 및 수정

**Refactor**:
- 트랜잭션 롤백 테스트 추가

**체크포인트**:
- [V] 해지정산 플로우 E2E 테스트 통과 (테스트 환경 수정 필요 - Phase 8과 동일 이슈)

---

**Phase 9 완료 후 중단**: 사용자 확인 필요

---

## Phase 10: 문서화 및 최종 검증

### Task 10-1: API 문서 작성
**목표**: REST API 문서화

**작업 내용**:
- Spring Rest Docs 또는 OpenAPI(Swagger) 설정
- 모든 엔드포인트 문서화
- 요청/응답 예제 추가

**체크포인트**:
- [ ] API 문서 생성 완료

---

### Task 10-2: README 작성
**목표**: 프로젝트 사용 가이드 작성

**작업 내용**:
- 프로젝트 개요
- 빌드 방법
- 실행 방법
- 테스트 실행 방법
- 아키텍처 다이어그램

**체크포인트**:
- [ ] README 작성 완료

---

### Task 10-3: 코드 커버리지 확인
**목표**: 테스트 커버리지 검증

**작업 내용**:
- JaCoCo 설정
- 커버리지 리포트 생성
- 부족한 테스트 보완

**체크포인트**:
- [ ] 전체 커버리지 80% 이상
- [ ] 핵심 비즈니스 로직 90% 이상

---

### Task 10-4: 최종 빌드 및 검증
**목표**: 전체 시스템 최종 검증

**작업 내용**:
- 전체 테스트 실행
- 빌드 성공 확인
- 애플리케이션 구동 확인
- 주요 시나리오 수동 테스트

**체크포인트**:
- [ ] 전체 테스트 통과
- [ ] 빌드 성공
- [ ] 애플리케이션 정상 구동

---

**Phase 10 완료**: 프로젝트 1차 완료

---

## 추가 확장 작업 (선택)

### Optional Task 1: 신규 Usecase 추가 검증
**목표**: 확장성 검증 - 일시정지 Usecase 추가

**작업 내용**:
- HotbillUsecase.SUSPENSION_INQUIRY 추가
- SuspensionInquiryValidator 구현 (TDD)
- Factory 등록
- 통합 테스트

**체크포인트**:
- [ ] 기존 코드 변경 없이 확장 완료

---

### Optional Task 2: 신규 계약 유형 추가 검증
**목표**: 확장성 검증 - 5G 특화망 추가

**작업 내용**:
- ContractType.PRIVATE_5G 추가
- Private5GChargeClient 구현 (TDD)
- Strategy 설정 추가
- 통합 테스트

**체크포인트**:
- [ ] 최소한의 코드 변경으로 확장 완료

---

### Optional Task 3: 성능 테스트
**목표**: 다중 계약 처리 성능 검증

**작업 내용**:
- JMeter 또는 Gatling 설정
- 부하 테스트 시나리오 작성
- 성능 병목 분석

**체크포인트**:
- [ ] 성능 기준 충족

---

## 작업 진행 시 주의사항

1. **각 Task는 독립적으로 완료 가능해야 합니다**
2. **TDD 사이클을 반드시 준수합니다** (Red → Green → Refactor)
3. **테스트가 통과하지 않으면 다음 Task로 진행하지 않습니다**
4. **구현 계획에 없는 내용은 구현하지 않습니다**
5. **Phase 완료 시마다 사용자 확인을 받습니다**
6. **각 Task 완료 시 체크포인트를 확인합니다**

---

## 예상 일정

- Phase 1-2: 2-3일 (도메인 모델 및 Strategy)
- Phase 3-4: 2-3일 (Template Pattern 및 Infrastructure)
- Phase 5-6: 2-3일 (Service Layer 및 Client)
- Phase 7-8: 2일 (통합 및 REST API)
- Phase 9: 2-3일 (예외 처리 및 통합 테스트)
- Phase 10: 1-2일 (문서화 및 검증)

**총 예상 기간**: 11-16일

---

## 마무리

이 구현 계획은 TDD를 철저히 준수하며, OCP(Open-Closed Principle)를 지키는 확장 가능한 아키텍처를 구축합니다.
각 단계는 독립적으로 검증 가능하며, 점진적으로 시스템을 완성해 나갑니다.
