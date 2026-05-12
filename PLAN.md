# 작업 보고서 승인 흐름 구현 계획

## Summary

- `Task`에 상태를 추가한다: `STARTED`, `IN_PROGRESS`, `COMPLETED`.
- 완료된 태스크(`COMPLETED`)에 대해서만 작업 보고서를 작성할 수 있게 한다.
- 검토자/결재자는 별도 테이블이 아니라 기존 `User`의 역할로 관리한다: `ROLE_REVIEWER`, `ROLE_APPROVER`.
- 보고서 상태는 `DRAFT`, `SUBMITTED`, `REVIEW`, `APPROVAL`, `APPROVED`로 관리하고, `작성 취소`는 별도 저장 상태가 아니라 `DRAFT`로 되돌리는 액션으로 처리한다.

## Key Changes

- `Role` enum에 `ROLE_REVIEWER`, `ROLE_APPROVER`를 추가한다.
- `Task` 엔티티와 Task DTO에 `TaskStatus status`를 추가한다. 생성 요청에서 status가 없으면 `STARTED`를 기본값으로 둔다.
- `TaskReport` 엔티티를 추가한다: `task`, `author`, `reviewer`, `approver`, `content`, `status`, `createdAt`, `updatedAt`.
- 보고서는 태스크당 1개만 생성되도록 `task_id` unique 제약을 둔다.
- `UserRepository`에 역할별 사용자 조회 기능을 추가하고, 관리자 전용 검토자/결재자 관리 API를 만든다.
- `NotAuthorizedException`을 403으로 응답하도록 `GlobalExceptionHandler`에 핸들러를 추가한다.

## Public API

- Task API
  - 기존 `/api/v2/tasks` 요청/응답에 `status` 필드를 추가한다.
- Report API
  - `POST /api/v2/reports`: 완료된 태스크의 보고서 작성, 작성자는 현재 로그인 사용자.
  - `GET /api/v2/reports`: 모든 보고서 목록 조회.
  - `GET /api/v2/reports/{id}`: 보고서 단건 조회.
  - `PUT /api/v2/reports/{id}`: 작성자만 `DRAFT` 또는 `SUBMITTED` 상태에서 본문 수정.
  - `DELETE /api/v2/reports/{id}`: 작성자만 `DRAFT` 상태에서 삭제.
  - `POST /api/v2/reports/{id}/submit`: 작성자만 실행, reviewerId/approverId 선택, `DRAFT -> SUBMITTED`.
  - `POST /api/v2/reports/{id}/cancel`: 작성자만 실행, `DRAFT` 또는 `SUBMITTED -> DRAFT`, reviewer/approver 선택값 초기화.
  - `POST /api/v2/reports/{id}/start-review`: 지정 검토자만 실행, `SUBMITTED -> REVIEW`.
  - `POST /api/v2/reports/{id}/reject-review`: 지정 검토자만 실행, `REVIEW -> SUBMITTED`.
  - `POST /api/v2/reports/{id}/complete-review`: 지정 검토자만 실행, `REVIEW -> APPROVAL`.
  - `POST /api/v2/reports/{id}/reject-approval`: 지정 결재자만 실행, `APPROVAL -> REVIEW`.
  - `POST /api/v2/reports/{id}/approve`: 지정 결재자만 실행, `APPROVAL -> APPROVED`.
- Reviewer/Approver API
  - `GET /api/v2/reviewers`, `GET /api/v2/approvers`: 역할별 사용자 목록 조회.
  - `POST /api/v2/reviewers`, `POST /api/v2/approvers`: 관리자만 신규 사용자 생성 후 역할 부여.
  - `POST /api/v2/reviewers/{userId}`, `POST /api/v2/approvers/{userId}`: 관리자만 기존 사용자에 역할 추가.
  - `PUT /api/v2/reviewers/{userId}`, `PUT /api/v2/approvers/{userId}`: 관리자만 사용자 정보 수정.
  - `DELETE /api/v2/reviewers/{userId}`, `DELETE /api/v2/approvers/{userId}`: 관리자만 해당 역할 회수.

## Test Plan

- `TaskServiceTest`, `TaskControllerTest`: Task status 기본값, 생성/수정/응답 JSON 검증.
- `TaskReportServiceTest`: 완료 태스크만 보고서 생성, 중복 보고서 방지, 작성자 권한, 삭제 가능 상태, 전체 상태 전이, 잘못된 전이 400, 권한 없는 사용자 403 검증.
- `TaskReportControllerTest`: 보고서 CRUD와 액션 API의 HTTP status 및 응답 JSON 검증.
- 검토자/결재자 관리 테스트: 관리자만 생성/추가/수정/삭제 가능, 역할별 목록 조회 검증.
- 전체 검증 명령은 `./gradlew test`로 수행한다.

## Assumptions

- 검토자/결재자는 기존 `users` 계정에 역할을 추가하는 방식으로 구현한다.
- 검토자/결재자 관리 변경 API는 `ROLE_ADMIN`만 사용할 수 있다.
- 보고서 생성은 `COMPLETED` 상태 태스크에만 허용한다.
- `작성 취소`는 DB에 별도 상태로 저장하지 않고 보고서를 `DRAFT`로 되돌리는 액션이다.
- 기존 DDL은 참고용이고, 현재 설정의 `spring.jpa.hibernate.ddl-auto=update`를 유지한다. 필요하면 `src/main/resources/ddl`에 보고서/태스크 상태 DDL 문서를 추가한다.
