package com.asdf.minilog.service;

import com.asdf.minilog.dto.TaskReportRequestDto;
import com.asdf.minilog.dto.TaskReportResponseDto;
import com.asdf.minilog.dto.TaskReportSubmitRequestDto;
import com.asdf.minilog.dto.TaskReportUpdateRequestDto;
import com.asdf.minilog.entity.main.ReportStatus;
import com.asdf.minilog.entity.main.Role;
import com.asdf.minilog.entity.main.TaskReport;
import com.asdf.minilog.entity.main.User;
import com.asdf.minilog.entity.task.Task;
import com.asdf.minilog.entity.task.TaskStatus;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.exception.TaskNotFoundException;
import com.asdf.minilog.exception.TaskReportNotFoundException;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.main.TaskReportRepository;
import com.asdf.minilog.repository.main.UserRepository;
import com.asdf.minilog.repository.task.TaskRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 작업 보고서(TaskReport) 도메인의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>완료된 작업에 대한 보고서를 작성하고, 작성자 → 검토자(Reviewer) → 승인자(Approver)로 이어지는 결재 흐름을 처리한다. 보고서의 상태는 다음 순서로
 * 전이된다.
 *
 * <pre>
 *   DRAFT(작성중) → SUBMITTED(제출) → REVIEW(검토중) → APPROVAL(승인대기) → APPROVED(승인완료)
 * </pre>
 *
 * <p>각 단계는 해당 역할을 가진 지정 사용자만 수행할 수 있으며, 반려 시 이전 상태로 되돌아간다. 모든 상태 전이 메서드는 작업을 수행하기 전에 권한과 현재 상태를
 * 검증한다.
 */
@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class TaskReportService {

  private final TaskReportRepository taskReportRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  @Autowired
  public TaskReportService(
      TaskReportRepository taskReportRepository,
      TaskRepository taskRepository,
      UserRepository userRepository) {
    this.taskReportRepository = taskReportRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
  }

  /**
   * 완료된 작업에 대한 보고서를 DRAFT(작성중) 상태로 생성한다.
   *
   * <p>대상 작업이 완료(COMPLETED) 상태여야 하며, 한 작업당 보고서는 하나만 존재할 수 있다.
   *
   * @param authorId 작성자 사용자 ID
   * @param request 대상 작업 ID와 보고서 내용
   * @return 생성된 보고서 정보
   * @throws TaskNotFoundException 대상 작업을 찾을 수 없는 경우
   * @throws UserNotFoundException 작성자를 찾을 수 없는 경우
   * @throws IllegalArgumentException 작업이 완료 상태가 아니거나 이미 보고서가 존재하는 경우
   */
  public TaskReportResponseDto createReport(Long authorId, TaskReportRequestDto request) {
    Task task = findTaskOrThrow(request.getTaskId());
    // 완료된 작업에 대해서만 보고서 작성 가능
    if (task.getStatus() != TaskStatus.COMPLETED) {
      throw new IllegalArgumentException("Task report can only be created for completed tasks");
    }
    // 작업당 보고서는 1건만 허용
    if (taskReportRepository.existsByTaskId(task.getId())) {
      throw new IllegalArgumentException("Task report already exists for this task");
    }

    User author = findUserOrThrow(authorId);
    TaskReport report =
        TaskReport.builder()
            .taskId(task.getId())
            .author(author)
            .content(request.getContent())
            .status(ReportStatus.DRAFT)
            .build();

    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 단건 보고서를 조회한다.
   *
   * @param reportId 조회할 보고서 ID
   * @return 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public TaskReportResponseDto getReport(Long reportId) {
    return EntityDtoMapper.toDto(findReportOrThrow(reportId));
  }

  /**
   * 전체 보고서 목록을 조회한다.
   *
   * @return 보고서 목록
   */
  @Transactional(readOnly = true)
  public List<TaskReportResponseDto> getReports() {
    return taskReportRepository.findAll().stream().map(EntityDtoMapper::toDto).toList();
  }

  /**
   * 보고서 내용을 수정한다. 작성자 본인만 가능하며, DRAFT 또는 SUBMITTED 상태에서만 수정할 수 있다.
   *
   * @param authorId 수정을 요청한 사용자 ID
   * @param reportId 수정할 보고서 ID
   * @param request 변경할 내용
   * @return 수정된 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 요청자가 작성자가 아닌 경우
   * @throws IllegalArgumentException 수정 가능한 상태가 아닌 경우
   */
  public TaskReportResponseDto updateReport(
      Long authorId, Long reportId, TaskReportUpdateRequestDto request) {
    TaskReport report = findReportOrThrow(reportId);
    requireAuthor(report, authorId, "update");
    requireStatusIn(
        report,
        "Report can only be updated in draft or submitted status",
        ReportStatus.DRAFT,
        ReportStatus.SUBMITTED);

    report.setContent(request.getContent());
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 보고서를 삭제한다. 작성자 본인만 가능하며, DRAFT 상태에서만 삭제할 수 있다.
   *
   * @param authorId 삭제를 요청한 사용자 ID
   * @param reportId 삭제할 보고서 ID
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 요청자가 작성자가 아닌 경우
   * @throws IllegalArgumentException DRAFT 상태가 아닌 경우
   */
  public void deleteReport(Long authorId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireAuthor(report, authorId, "delete");
    requireStatus(report, ReportStatus.DRAFT, "Report can only be deleted in draft status");

    taskReportRepository.deleteById(report.getId());
  }

  /**
   * 보고서를 제출하여 DRAFT → SUBMITTED 로 전이한다. 작성자 본인만 가능하다.
   *
   * <p>제출 시 검토자와 승인자를 지정하며, 각 사용자가 올바른 역할(ROLE_REVIEWER / ROLE_APPROVER)을 보유하고 있어야 한다.
   *
   * @param authorId 제출을 요청한 사용자 ID
   * @param reportId 제출할 보고서 ID
   * @param request 검토자 ID와 승인자 ID
   * @return 제출된 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 요청자가 작성자가 아닌 경우
   * @throws UserNotFoundException 검토자 또는 승인자를 찾을 수 없는 경우
   * @throws IllegalArgumentException DRAFT 상태가 아니거나 지정한 사용자의 역할이 올바르지 않은 경우
   */
  public TaskReportResponseDto submitReport(
      Long authorId, Long reportId, TaskReportSubmitRequestDto request) {
    TaskReport report = findReportOrThrow(reportId);
    requireAuthor(report, authorId, "submit");
    requireStatus(report, ReportStatus.DRAFT, "Report can only be submitted in draft status");

    // 검토자·승인자로 지정된 사용자가 실제 해당 역할을 보유했는지 검증
    User reviewer = findUserOrThrow(request.getReviewerId());
    requireRole(reviewer, Role.ROLE_REVIEWER, "Selected user is not a reviewer");
    User approver = findUserOrThrow(request.getApproverId());
    requireRole(approver, Role.ROLE_APPROVER, "Selected user is not an approver");

    report.setReviewer(reviewer);
    report.setApprover(approver);
    report.setStatus(ReportStatus.SUBMITTED);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 제출을 취소하여 DRAFT 상태로 되돌린다. 작성자 본인만 가능하며, DRAFT 또는 SUBMITTED 상태에서만 호출할 수 있다.
   *
   * <p>지정했던 검토자·승인자 정보는 초기화된다.
   *
   * @param authorId 취소를 요청한 사용자 ID
   * @param reportId 취소할 보고서 ID
   * @return DRAFT로 되돌아간 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 요청자가 작성자가 아닌 경우
   * @throws IllegalArgumentException DRAFT 또는 SUBMITTED 상태가 아닌 경우
   */
  public TaskReportResponseDto cancelReport(Long authorId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireAuthor(report, authorId, "cancel");
    requireStatusIn(
        report,
        "Report can only be canceled in draft or submitted status",
        ReportStatus.DRAFT,
        ReportStatus.SUBMITTED);

    // 지정했던 결재자 정보를 초기화하고 작성중 상태로 복귀
    report.setReviewer(null);
    report.setApprover(null);
    report.setStatus(ReportStatus.DRAFT);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 검토를 시작하여 SUBMITTED → REVIEW 로 전이한다. 지정된 검토자만 호출할 수 있다.
   *
   * @param reviewerId 검토자 사용자 ID
   * @param reportId 대상 보고서 ID
   * @return 검토중 상태가 된 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 지정된 검토자가 아닌 경우
   * @throws IllegalArgumentException SUBMITTED 상태가 아닌 경우
   */
  public TaskReportResponseDto startReview(Long reviewerId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireReviewer(report, reviewerId, "start review");
    requireStatus(report, ReportStatus.SUBMITTED, "Review can only start from submitted status");

    report.setStatus(ReportStatus.REVIEW);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 검토를 반려하여 REVIEW → SUBMITTED 로 되돌린다. 지정된 검토자만 호출할 수 있다.
   *
   * @param reviewerId 검토자 사용자 ID
   * @param reportId 대상 보고서 ID
   * @return SUBMITTED로 되돌아간 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 지정된 검토자가 아닌 경우
   * @throws IllegalArgumentException REVIEW 상태가 아닌 경우
   */
  public TaskReportResponseDto rejectReview(Long reviewerId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireReviewer(report, reviewerId, "reject review");
    requireStatus(report, ReportStatus.REVIEW, "Review can only be rejected in review status");

    report.setStatus(ReportStatus.SUBMITTED);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 검토를 완료하여 REVIEW → APPROVAL(승인대기) 로 전이한다. 지정된 검토자만 호출할 수 있다.
   *
   * @param reviewerId 검토자 사용자 ID
   * @param reportId 대상 보고서 ID
   * @return 승인대기 상태가 된 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 지정된 검토자가 아닌 경우
   * @throws IllegalArgumentException REVIEW 상태가 아닌 경우
   */
  public TaskReportResponseDto completeReview(Long reviewerId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireReviewer(report, reviewerId, "complete review");
    requireStatus(report, ReportStatus.REVIEW, "Review can only be completed in review status");

    report.setStatus(ReportStatus.APPROVAL);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 승인을 반려하여 APPROVAL → REVIEW 로 되돌린다. 지정된 승인자만 호출할 수 있다.
   *
   * @param approverId 승인자 사용자 ID
   * @param reportId 대상 보고서 ID
   * @return REVIEW로 되돌아간 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 지정된 승인자가 아닌 경우
   * @throws IllegalArgumentException APPROVAL 상태가 아닌 경우
   */
  public TaskReportResponseDto rejectApproval(Long approverId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireApprover(report, approverId, "reject approval");
    requireStatus(
        report, ReportStatus.APPROVAL, "Approval can only be rejected in approval status");

    report.setStatus(ReportStatus.REVIEW);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 보고서를 최종 승인하여 APPROVAL → APPROVED 로 전이한다. 지정된 승인자만 호출할 수 있다.
   *
   * @param approverId 승인자 사용자 ID
   * @param reportId 대상 보고서 ID
   * @return 승인완료 상태가 된 보고서 정보
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   * @throws NotAuthorizedException 지정된 승인자가 아닌 경우
   * @throws IllegalArgumentException APPROVAL 상태가 아닌 경우
   */
  public TaskReportResponseDto approveReport(Long approverId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireApprover(report, approverId, "approve");
    requireStatus(report, ReportStatus.APPROVAL, "Report can only be approved in approval status");

    report.setStatus(ReportStatus.APPROVED);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  /**
   * 보고서를 조회하고, 없으면 예외를 던지는 내부 헬퍼.
   *
   * @throws TaskReportNotFoundException 보고서를 찾을 수 없는 경우
   */
  private TaskReport findReportOrThrow(Long reportId) {
    return taskReportRepository
        .findById(reportId)
        .orElseThrow(
            () ->
                new TaskReportNotFoundException(
                    String.format("Task report with id %d not found", reportId)));
  }

  /**
   * 작업을 조회하고, 없으면 예외를 던지는 내부 헬퍼.
   *
   * @throws TaskNotFoundException 작업을 찾을 수 없는 경우
   */
  private Task findTaskOrThrow(Long taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(
            () -> new TaskNotFoundException(String.format("Task with id %d not found", taskId)));
  }

  /**
   * 사용자를 조회하고, 없으면 예외를 던지는 내부 헬퍼.
   *
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  private User findUserOrThrow(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new UserNotFoundException(String.format("User with id %d not found", userId)));
  }

  /**
   * 요청자가 보고서 작성자인지 검증한다.
   *
   * @throws NotAuthorizedException 작성자가 아닌 경우
   */
  private void requireAuthor(TaskReport report, Long userId, String action) {
    if (!report.getAuthor().getId().equals(userId)) {
      throw new NotAuthorizedException(
          String.format("You are not authorized to %s this report", action));
    }
  }

  /**
   * 요청자가 보고서에 지정된 검토자인지 검증한다.
   *
   * @throws NotAuthorizedException 지정된 검토자가 아닌 경우
   */
  private void requireReviewer(TaskReport report, Long userId, String action) {
    if (report.getReviewer() == null || !report.getReviewer().getId().equals(userId)) {
      throw new NotAuthorizedException(
          String.format("You are not authorized to %s this report", action));
    }
  }

  /**
   * 요청자가 보고서에 지정된 승인자인지 검증한다.
   *
   * @throws NotAuthorizedException 지정된 승인자가 아닌 경우
   */
  private void requireApprover(TaskReport report, Long userId, String action) {
    if (report.getApprover() == null || !report.getApprover().getId().equals(userId)) {
      throw new NotAuthorizedException(
          String.format("You are not authorized to %s this report", action));
    }
  }

  /**
   * 사용자가 특정 권한(Role)을 보유했는지 검증한다.
   *
   * @throws IllegalArgumentException 해당 권한을 보유하지 않은 경우
   */
  private void requireRole(User user, Role role, String message) {
    if (user.getRoles() == null || !user.getRoles().contains(role)) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * 보고서가 기대하는 상태와 일치하는지 검증한다.
   *
   * @throws IllegalArgumentException 기대 상태와 다른 경우
   */
  private void requireStatus(TaskReport report, ReportStatus expectedStatus, String message) {
    if (report.getStatus() != expectedStatus) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * 보고서가 허용된 여러 상태 중 하나에 해당하는지 검증한다.
   *
   * @throws IllegalArgumentException 허용된 상태 중 어느 것에도 해당하지 않는 경우
   */
  private void requireStatusIn(
      TaskReport report, String message, ReportStatus... expectedStatuses) {
    for (ReportStatus expectedStatus : expectedStatuses) {
      if (report.getStatus() == expectedStatus) {
        return;
      }
    }
    throw new IllegalArgumentException(message);
  }
}
