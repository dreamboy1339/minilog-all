package com.asdf.minilog.controller;

import com.asdf.minilog.dto.TaskReportRequestDto;
import com.asdf.minilog.dto.TaskReportResponseDto;
import com.asdf.minilog.dto.TaskReportSubmitRequestDto;
import com.asdf.minilog.dto.TaskReportUpdateRequestDto;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.TaskReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 작업 보고서(Task Report) 관리 REST 컨트롤러.
 *
 * <p>{@code /api/v2/reports} 경로에서 작업 보고서의 CRUD와 함께 제출 → 검토 → 승인으로 이어지는 상태 전이(워크플로) 엔드포인트를 제공한다. 모든
 * 변경 작업은 인증된 사용자 정보를 기준으로 처리된다.
 */
@RestController
@RequestMapping("/api/v2/reports")
public class TaskReportController {

  private final TaskReportService taskReportService;

  @Autowired
  public TaskReportController(TaskReportService taskReportService) {
    this.taskReportService = taskReportService;
  }

  /**
   * 새 작업 보고서를 작성한다. (POST /api/v2/reports)
   *
   * @param userDetails 인증된 작성자 정보
   * @param request 작성할 보고서 정보
   * @return 생성된 보고서 정보
   */
  @PostMapping
  @Operation(summary = "Create a task report")
  public ResponseEntity<TaskReportResponseDto> createReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @Valid @RequestBody TaskReportRequestDto request) {
    return ResponseEntity.ok(taskReportService.createReport(userDetails.getId(), request));
  }

  /**
   * 전체 작업 보고서 목록을 조회한다. (GET /api/v2/reports)
   *
   * @return 전체 보고서 목록
   */
  @GetMapping
  @Operation(summary = "Get all task reports")
  public ResponseEntity<List<TaskReportResponseDto>> getReports() {
    return ResponseEntity.ok(taskReportService.getReports());
  }

  /**
   * 보고서 ID로 단건 작업 보고서를 조회한다. (GET /api/v2/reports/{reportId})
   *
   * @param reportId 조회할 보고서 ID
   * @return 보고서 정보
   */
  @GetMapping("/{reportId}")
  @Operation(summary = "Get task report by id")
  public ResponseEntity<TaskReportResponseDto> getReport(@PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.getReport(reportId));
  }

  /**
   * 작업 보고서 내용을 수정한다. (PUT /api/v2/reports/{reportId})
   *
   * @param userDetails 인증된 사용자 정보(작성자 검증용)
   * @param reportId 수정할 보고서 ID
   * @param request 수정할 보고서 정보
   * @return 수정된 보고서 정보
   */
  @PutMapping("/{reportId}")
  @Operation(summary = "Update task report")
  public ResponseEntity<TaskReportResponseDto> updateReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId,
      @RequestBody TaskReportUpdateRequestDto request) {
    return ResponseEntity.ok(
        taskReportService.updateReport(userDetails.getId(), reportId, request));
  }

  /**
   * 작업 보고서를 삭제한다. (DELETE /api/v2/reports/{reportId})
   *
   * @param userDetails 인증된 사용자 정보(작성자 검증용)
   * @param reportId 삭제할 보고서 ID
   * @return 본문 없는 204 응답
   */
  @DeleteMapping("/{reportId}")
  @Operation(summary = "Delete task report")
  public ResponseEntity<Void> deleteReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    taskReportService.deleteReport(userDetails.getId(), reportId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 작성 중인 보고서를 제출하여 검토 대기 상태로 전환한다. (POST /api/v2/reports/{reportId}/submit)
   *
   * @param userDetails 인증된 사용자 정보
   * @param reportId 제출할 보고서 ID
   * @param request 제출 시 필요한 정보(검토자 지정 등)
   * @return 제출 처리된 보고서 정보
   */
  @PostMapping("/{reportId}/submit")
  @Operation(summary = "Submit task report")
  public ResponseEntity<TaskReportResponseDto> submitReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId,
      @Valid @RequestBody TaskReportSubmitRequestDto request) {
    return ResponseEntity.ok(
        taskReportService.submitReport(userDetails.getId(), reportId, request));
  }

  /**
   * 제출한 보고서를 취소하여 다시 작성 상태로 되돌린다. (POST /api/v2/reports/{reportId}/cancel)
   *
   * @param userDetails 인증된 사용자 정보
   * @param reportId 취소할 보고서 ID
   * @return 취소 처리된 보고서 정보
   */
  @PostMapping("/{reportId}/cancel")
  @Operation(summary = "Cancel task report writing")
  public ResponseEntity<TaskReportResponseDto> cancelReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.cancelReport(userDetails.getId(), reportId));
  }

  /**
   * 검토자가 보고서 검토를 시작한다. (POST /api/v2/reports/{reportId}/start-review)
   *
   * @param userDetails 인증된 검토자 정보
   * @param reportId 검토를 시작할 보고서 ID
   * @return 검토 중 상태로 전환된 보고서 정보
   */
  @PostMapping("/{reportId}/start-review")
  @Operation(summary = "Start report review")
  public ResponseEntity<TaskReportResponseDto> startReview(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.startReview(userDetails.getId(), reportId));
  }

  /**
   * 검토자가 보고서 검토를 반려한다. (POST /api/v2/reports/{reportId}/reject-review)
   *
   * @param userDetails 인증된 검토자 정보
   * @param reportId 반려할 보고서 ID
   * @return 검토 반려 처리된 보고서 정보
   */
  @PostMapping("/{reportId}/reject-review")
  @Operation(summary = "Reject report review")
  public ResponseEntity<TaskReportResponseDto> rejectReview(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.rejectReview(userDetails.getId(), reportId));
  }

  /**
   * 검토자가 검토를 완료하여 승인 대기 상태로 전환한다. (POST /api/v2/reports/{reportId}/complete-review)
   *
   * @param userDetails 인증된 검토자 정보
   * @param reportId 검토를 완료할 보고서 ID
   * @return 검토 완료 처리된 보고서 정보
   */
  @PostMapping("/{reportId}/complete-review")
  @Operation(summary = "Complete report review")
  public ResponseEntity<TaskReportResponseDto> completeReview(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.completeReview(userDetails.getId(), reportId));
  }

  /**
   * 승인자가 보고서 승인을 반려한다. (POST /api/v2/reports/{reportId}/reject-approval)
   *
   * @param userDetails 인증된 승인자 정보
   * @param reportId 승인 반려할 보고서 ID
   * @return 승인 반려 처리된 보고서 정보
   */
  @PostMapping("/{reportId}/reject-approval")
  @Operation(summary = "Reject report approval")
  public ResponseEntity<TaskReportResponseDto> rejectApproval(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.rejectApproval(userDetails.getId(), reportId));
  }

  /**
   * 승인자가 보고서를 최종 승인한다. (POST /api/v2/reports/{reportId}/approve)
   *
   * @param userDetails 인증된 승인자 정보
   * @param reportId 승인할 보고서 ID
   * @return 승인 완료된 보고서 정보
   */
  @PostMapping("/{reportId}/approve")
  @Operation(summary = "Approve task report")
  public ResponseEntity<TaskReportResponseDto> approveReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.approveReport(userDetails.getId(), reportId));
  }
}
