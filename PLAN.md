# PLAN — Devices / Tasks REST API with Paging

## 1. 목표
`REQUIREMENT.md` 기준으로 `Devices`, `Tasks` 두 테이블에 대한 CRUD REST API와, 두 도메인을 결합한 Paging 가능한 조회 API 2종을 구현한다. 기존 `Article` / `User` 도메인의 레이어드 아키텍처(Controller → Service → Repository, JPA + Hibernate Auditing, Lombok, springdoc OpenAPI, `@WebMvcTest` 기반 컨트롤러 테스트)를 그대로 따른다.

## 2. 산출물 개요
- 엔티티: `Device`, `Task`
- DTO: 요청/응답 DTO + `DeviceWithTasksResponseDto`, `TaskWithDeviceResponseDto`
- 레이어: `DeviceRepository`, `DeviceService`, `DeviceController`, `TaskRepository`, `TaskService`, `TaskController`
- 예외: `DeviceNotFoundException`, `TaskNotFoundException` + `GlobalExceptionHandler` 핸들러 추가
- 매퍼: `EntityDtoMapper`에 변환 메서드 확장
- 테스트: 컨트롤러 단위 테스트 2종 (`DeviceControllerTest`, `TaskControllerTest`) + 서비스 단위 테스트 2종

## 3. 엔티티 설계

### 3.1 `Device` (`devices` 테이블)
| 컬럼 | 타입 | 제약 |
|---|---|---|
| `id` | `Long` | PK, auto-increment |
| `name` | `String` | not null |
| `type` | `String` | not null |
| `created_at` | `LocalDateTime` | `@CreatedDate`, not null, updatable=false |
| `updated_at` | `LocalDateTime` | `@LastModifiedDate`, not null |

- `@EntityListeners(AuditingEntityListener.class)` (기존 `JpaAuditingConfig` 재사용)
- Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`

### 3.2 `Task` (`tasks` 테이블)
| 컬럼 | 타입 | 제약 |
|---|---|---|
| `id` | `Long` | PK |
| `device` | `Device` | `@ManyToOne(FetchType.LAZY)`, `@JoinColumn(name = "device_id", nullable = false)` |
| `name` | `String` | `@Column(length = 255, nullable = false)` |
| `description` | `String` | `@Column(length = 1000)` |
| `created_at` / `updated_at` | `LocalDateTime` | Auditing |

> 요구사항의 "name ≤ 255자", "description ≤ 1000자" 는 DDL 길이 제약 + DTO `@Size` 검증으로 이중 적용.

## 4. DTO 설계

### 4.1 Device
- `DeviceRequestDto`: `name`, `type`
- `DeviceResponseDto`: `id`, `name`, `type`, `createdAt`, `updatedAt`
- `DeviceWithTasksResponseDto`: `id`, `name`, `type`, `createdAt`, `updatedAt`, `tasks: List<TaskSummaryDto>`
  - `TaskSummaryDto`: `id`, `name`, `description` (device 정보 제외 — 부모와 중복 방지)

### 4.2 Task
- `TaskRequestDto`: `deviceId`, `name` (`@Size(max=255)`), `description` (`@Size(max=1000)`)
- `TaskResponseDto`: `id`, `deviceId`, `name`, `description`, `createdAt`, `updatedAt`
- `TaskWithDeviceResponseDto`: `id`, `name`, `description`, `createdAt`, `updatedAt`, `device: DeviceSummaryDto`
  - `DeviceSummaryDto`: `id`, `name`, `type`

> 결합 응답 DTO를 별도로 두는 이유: 단순 CRUD 응답을 가볍게 유지하고, 결합 API에서만 N+1 회피용 fetch join + 명시적 매핑을 쓰기 위함.

## 5. Repository

### 5.1 `DeviceRepository extends JpaRepository<Device, Long>`
- 기본 CRUD 메서드 사용.
- 결합 API용:
  ```java
  @Query(value = "SELECT DISTINCT d FROM Device d LEFT JOIN FETCH d.tasks",
         countQuery = "SELECT COUNT(d) FROM Device d")
  Page<Device> findAllWithTasks(Pageable pageable);
  ```
  - `Device`에 `@OneToMany(mappedBy = "device") List<Task> tasks` 양방향 매핑 추가 (LAZY).
  - fetch join + pageable 사용 시 `HHH000104` 경고가 발생하므로, 대안으로 **두 단계 쿼리**(① `Page<Device>` 조회 → ② `findAllByDeviceIdIn(ids)`로 tasks 일괄 로딩 → 메모리에서 그룹핑) 패턴을 채택할 수 있음. 구현 시 후자를 우선 채택하여 메모리 페이징 경고 회피.

### 5.2 `TaskRepository extends JpaRepository<Task, Long>`
- 기본 CRUD.
- 결합 API용:
  ```java
  @Query(value = "SELECT t FROM Task t JOIN FETCH t.device",
         countQuery = "SELECT COUNT(t) FROM Task t")
  Page<Task> findAllWithDevice(Pageable pageable);
  ```
  - `@ManyToOne`은 단일 연관관계라 fetch join + paging이 안전 (컬렉션 X).

## 6. Service

### 6.1 `DeviceService`
- `createDevice(DeviceRequestDto)` → `DeviceResponseDto`
- `getDevice(Long id)` → `DeviceResponseDto`
- `updateDevice(Long id, DeviceRequestDto)` → `DeviceResponseDto`
- `deleteDevice(Long id)` → void
- `getDevices(Pageable)` → `Page<DeviceResponseDto>`
- `getDevicesWithTasks(Pageable)` → `Page<DeviceWithTasksResponseDto>`
  - 위 "두 단계 쿼리" 전략 채택 시 여기서 조합.

### 6.2 `TaskService`
- `createTask(TaskRequestDto)`: `deviceId` 존재 검증 → 저장
- `getTask(Long id)`
- `updateTask(Long id, TaskRequestDto)`: deviceId 변경 시 새 Device 검증
- `deleteTask(Long id)`
- `getTasks(Pageable)` → `Page<TaskResponseDto>`
- `getTasksWithDevice(Pageable)` → `Page<TaskWithDeviceResponseDto>` (fetch join + Page.map)

- 트랜잭션: 기존 `ArticleService`와 동일하게 클래스에 `@Transactional(isolation = REPEATABLE_READ)`, 조회는 `@Transactional(readOnly = true)`.

## 7. Controller

공통: `@RestController`, springdoc `@Operation`/`@ApiResponses`. 인증 요구사항이 명시되지 않았으므로 `SecurityConfig`에서 `/api/v2/devices/**`, `/api/v2/tasks/**` 를 `permitAll`로 등록 (또는 인증 요구를 확인 후 결정 — 6.4 참조).

### 7.1 `DeviceController` (`/api/v2/devices`)
| Method | Path | 설명 |
|---|---|---|
| POST | `/` | 생성 |
| GET | `/{id}` | 단건 조회 |
| PUT | `/{id}` | 수정 |
| DELETE | `/{id}` | 삭제 (204) |
| GET | `/` | 목록 (Pageable) |
| GET | `/with-tasks` | `getDevicesWithTasks` — Pageable |

### 7.2 `TaskController` (`/api/v2/tasks`)
| Method | Path | 설명 |
|---|---|---|
| POST | `/` | 생성 |
| GET | `/{id}` | 단건 조회 |
| PUT | `/{id}` | 수정 |
| DELETE | `/{id}` | 삭제 |
| GET | `/` | 목록 (Pageable) |
| GET | `/with-device` | `getTasksWithDevice` — Pageable |

- Pageable 파라미터는 Spring의 `@PageableDefault(size = 20)` + `?page=&size=&sort=` 쿼리스트링.
- 응답은 `Page<...>` 그대로 직렬화 (기존 프로젝트에 PageResponse 래퍼가 없어 표준 직렬화 사용; 필요 시 추후 래퍼 도입).

## 8. 예외 처리
- `DeviceNotFoundException`, `TaskNotFoundException` (`RuntimeException` 상속).
- `GlobalExceptionHandler`에 핸들러 추가 — 기존 `ArticleNotFoundException` 핸들러와 동일하게 `404 + message`.
- DTO 검증 실패 (`@Valid` + `@Size`) → 400. 컨트롤러 인자에 `@Valid` 추가.

## 9. 매퍼 (`EntityDtoMapper`)
- `toDto(Device)`, `toDto(Task)`, `toWithTasksDto(Device, List<Task>)`, `toWithDeviceDto(Task)` 추가.
- 정적 메서드 유지 (기존 패턴).

## 10. Security 설정
- 현재 `SecurityConfig`가 JWT 기반인지 확인 후, 인증 비요구 시 두 경로를 `permitAll` 등록. 인증 요구이면 컨트롤러 테스트에서 `@WithMockUser` 또는 `csrf()`만 적용.
- 작업 시작 전 1차로 `SecurityConfig` 실제 정책 확인이 필요 — 정책에 따라 테스트의 `csrf()`/인증 모킹 처리 결정.

## 11. 데이터베이스 / 마이그레이션
- `spring.jpa.hibernate.ddl-auto=update` 상태이므로 엔티티 추가 시 자동 생성됨.
- 명시적 DDL은 `src/main/resources/ddl/ddl_Device.sql`, `ddl_Task.sql` 로 참고용 저장 (기존 `ddl_Article.sql` 패턴).

## 12. 테스트 전략

### 12.1 컨트롤러 (`@WebMvcTest`)
- `DeviceControllerTest`, `TaskControllerTest`: 기존 `ArticleControllerTest` 구조 그대로.
  - `@MockitoBean` 으로 Service, `JwtUtil`, `JpaMetamodelMappingContext` 주입.
  - CRUD 5종 + 결합 API(`/with-tasks`, `/with-device`) Paging 응답 (`$.content[0]...`, `$.totalElements`).
  - NotFound 시나리오 1개씩.

### 12.2 서비스 단위 (`@ExtendWith(MockitoExtension)`)
- `DeviceServiceTest`, `TaskServiceTest`: Repository를 mock, 생성/조회/수정/삭제 + NotFound 분기.

### 12.3 (선택) 통합 테스트
- Testcontainers MySQL 의존성이 이미 존재 → 시간 여유 시 `@SpringBootTest` 한 케이스로 `getDevicesWithTasks` 페이징/정렬 동작 검증. 1차 범위에서는 생략하고 단위 테스트로 충분히 커버.

## 13. 작업 순서 (구현 step)
1. `SecurityConfig` 정책 확인 → 새 경로 처리 방침 확정.
2. 엔티티 `Device`, `Task` + 양방향 매핑 추가.
3. Request/Response/With-Join DTO 추가.
4. `DeviceRepository`, `TaskRepository` (paging 쿼리 포함).
5. 예외 + `GlobalExceptionHandler` 핸들러 추가.
6. `EntityDtoMapper` 매퍼 확장.
7. `DeviceService`, `TaskService` 구현.
8. `DeviceController`, `TaskController` 구현 (springdoc 어노테이션 포함).
9. 컨트롤러/서비스 테스트 작성.
10. `./gradlew spotlessApply test` 로 포맷·테스트 검증.

## 14. 미정/확인 필요 사항
- (A) `Devices`, `Tasks` 의 인증 요구 여부 — 요구사항 명시 없음. 기본은 `permitAll`로 진행 예정. 변경 필요 시 사전 합의.
- (B) "결합 API"의 결합 방식 — `getDevicesWithTasks` 는 "Device + 그에 속한 Task 리스트", `getTasksWithDevice`는 "Task + 소속 Device" 로 해석함. 다른 의미였다면 알려주면 반영.
- (C) Pageable 기본 size (20) 와 정렬 키 (기본 `id ASC`) — 합의 가능 시 조정.
