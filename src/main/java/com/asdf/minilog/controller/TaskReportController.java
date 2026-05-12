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

@RestController
@RequestMapping("/api/v2/reports")
public class TaskReportController {

  private final TaskReportService taskReportService;

  @Autowired
  public TaskReportController(TaskReportService taskReportService) {
    this.taskReportService = taskReportService;
  }

  @PostMapping
  @Operation(summary = "Create a task report")
  public ResponseEntity<TaskReportResponseDto> createReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @Valid @RequestBody TaskReportRequestDto request) {
    return ResponseEntity.ok(taskReportService.createReport(userDetails.getId(), request));
  }

  @GetMapping
  @Operation(summary = "Get all task reports")
  public ResponseEntity<List<TaskReportResponseDto>> getReports() {
    return ResponseEntity.ok(taskReportService.getReports());
  }

  @GetMapping("/{reportId}")
  @Operation(summary = "Get task report by id")
  public ResponseEntity<TaskReportResponseDto> getReport(@PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.getReport(reportId));
  }

  @PutMapping("/{reportId}")
  @Operation(summary = "Update task report")
  public ResponseEntity<TaskReportResponseDto> updateReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId,
      @RequestBody TaskReportUpdateRequestDto request) {
    return ResponseEntity.ok(
        taskReportService.updateReport(userDetails.getId(), reportId, request));
  }

  @DeleteMapping("/{reportId}")
  @Operation(summary = "Delete task report")
  public ResponseEntity<Void> deleteReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    taskReportService.deleteReport(userDetails.getId(), reportId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{reportId}/submit")
  @Operation(summary = "Submit task report")
  public ResponseEntity<TaskReportResponseDto> submitReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId,
      @Valid @RequestBody TaskReportSubmitRequestDto request) {
    return ResponseEntity.ok(
        taskReportService.submitReport(userDetails.getId(), reportId, request));
  }

  @PostMapping("/{reportId}/cancel")
  @Operation(summary = "Cancel task report writing")
  public ResponseEntity<TaskReportResponseDto> cancelReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.cancelReport(userDetails.getId(), reportId));
  }

  @PostMapping("/{reportId}/start-review")
  @Operation(summary = "Start report review")
  public ResponseEntity<TaskReportResponseDto> startReview(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.startReview(userDetails.getId(), reportId));
  }

  @PostMapping("/{reportId}/reject-review")
  @Operation(summary = "Reject report review")
  public ResponseEntity<TaskReportResponseDto> rejectReview(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.rejectReview(userDetails.getId(), reportId));
  }

  @PostMapping("/{reportId}/complete-review")
  @Operation(summary = "Complete report review")
  public ResponseEntity<TaskReportResponseDto> completeReview(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.completeReview(userDetails.getId(), reportId));
  }

  @PostMapping("/{reportId}/reject-approval")
  @Operation(summary = "Reject report approval")
  public ResponseEntity<TaskReportResponseDto> rejectApproval(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.rejectApproval(userDetails.getId(), reportId));
  }

  @PostMapping("/{reportId}/approve")
  @Operation(summary = "Approve task report")
  public ResponseEntity<TaskReportResponseDto> approveReport(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long reportId) {
    return ResponseEntity.ok(taskReportService.approveReport(userDetails.getId(), reportId));
  }
}
