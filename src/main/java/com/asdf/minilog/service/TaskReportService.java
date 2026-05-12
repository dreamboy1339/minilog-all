package com.asdf.minilog.service;

import com.asdf.minilog.dto.TaskReportRequestDto;
import com.asdf.minilog.dto.TaskReportResponseDto;
import com.asdf.minilog.dto.TaskReportSubmitRequestDto;
import com.asdf.minilog.dto.TaskReportUpdateRequestDto;
import com.asdf.minilog.entity.ReportStatus;
import com.asdf.minilog.entity.Role;
import com.asdf.minilog.entity.Task;
import com.asdf.minilog.entity.TaskReport;
import com.asdf.minilog.entity.TaskStatus;
import com.asdf.minilog.entity.User;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.exception.TaskNotFoundException;
import com.asdf.minilog.exception.TaskReportNotFoundException;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.TaskReportRepository;
import com.asdf.minilog.repository.TaskRepository;
import com.asdf.minilog.repository.UserRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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

  public TaskReportResponseDto createReport(Long authorId, TaskReportRequestDto request) {
    Task task = findTaskOrThrow(request.getTaskId());
    if (task.getStatus() != TaskStatus.COMPLETED) {
      throw new IllegalArgumentException("Task report can only be created for completed tasks");
    }
    if (taskReportRepository.existsByTaskId(task.getId())) {
      throw new IllegalArgumentException("Task report already exists for this task");
    }

    User author = findUserOrThrow(authorId);
    TaskReport report =
        TaskReport.builder()
            .task(task)
            .author(author)
            .content(request.getContent())
            .status(ReportStatus.DRAFT)
            .build();

    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  @Transactional(readOnly = true)
  public TaskReportResponseDto getReport(Long reportId) {
    return EntityDtoMapper.toDto(findReportOrThrow(reportId));
  }

  @Transactional(readOnly = true)
  public List<TaskReportResponseDto> getReports() {
    return taskReportRepository.findAll().stream().map(EntityDtoMapper::toDto).toList();
  }

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

  public void deleteReport(Long authorId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireAuthor(report, authorId, "delete");
    requireStatus(report, ReportStatus.DRAFT, "Report can only be deleted in draft status");

    taskReportRepository.deleteById(report.getId());
  }

  public TaskReportResponseDto submitReport(
      Long authorId, Long reportId, TaskReportSubmitRequestDto request) {
    TaskReport report = findReportOrThrow(reportId);
    requireAuthor(report, authorId, "submit");
    requireStatus(report, ReportStatus.DRAFT, "Report can only be submitted in draft status");

    User reviewer = findUserOrThrow(request.getReviewerId());
    requireRole(reviewer, Role.ROLE_REVIEWER, "Selected user is not a reviewer");
    User approver = findUserOrThrow(request.getApproverId());
    requireRole(approver, Role.ROLE_APPROVER, "Selected user is not an approver");

    report.setReviewer(reviewer);
    report.setApprover(approver);
    report.setStatus(ReportStatus.SUBMITTED);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  public TaskReportResponseDto cancelReport(Long authorId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireAuthor(report, authorId, "cancel");
    requireStatusIn(
        report,
        "Report can only be canceled in draft or submitted status",
        ReportStatus.DRAFT,
        ReportStatus.SUBMITTED);

    report.setReviewer(null);
    report.setApprover(null);
    report.setStatus(ReportStatus.DRAFT);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  public TaskReportResponseDto startReview(Long reviewerId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireReviewer(report, reviewerId, "start review");
    requireStatus(report, ReportStatus.SUBMITTED, "Review can only start from submitted status");

    report.setStatus(ReportStatus.REVIEW);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  public TaskReportResponseDto rejectReview(Long reviewerId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireReviewer(report, reviewerId, "reject review");
    requireStatus(report, ReportStatus.REVIEW, "Review can only be rejected in review status");

    report.setStatus(ReportStatus.SUBMITTED);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  public TaskReportResponseDto completeReview(Long reviewerId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireReviewer(report, reviewerId, "complete review");
    requireStatus(report, ReportStatus.REVIEW, "Review can only be completed in review status");

    report.setStatus(ReportStatus.APPROVAL);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  public TaskReportResponseDto rejectApproval(Long approverId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireApprover(report, approverId, "reject approval");
    requireStatus(
        report, ReportStatus.APPROVAL, "Approval can only be rejected in approval status");

    report.setStatus(ReportStatus.REVIEW);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  public TaskReportResponseDto approveReport(Long approverId, Long reportId) {
    TaskReport report = findReportOrThrow(reportId);
    requireApprover(report, approverId, "approve");
    requireStatus(report, ReportStatus.APPROVAL, "Report can only be approved in approval status");

    report.setStatus(ReportStatus.APPROVED);
    return EntityDtoMapper.toDto(taskReportRepository.save(report));
  }

  private TaskReport findReportOrThrow(Long reportId) {
    return taskReportRepository
        .findById(reportId)
        .orElseThrow(
            () ->
                new TaskReportNotFoundException(
                    String.format("Task report with id %d not found", reportId)));
  }

  private Task findTaskOrThrow(Long taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(
            () -> new TaskNotFoundException(String.format("Task with id %d not found", taskId)));
  }

  private User findUserOrThrow(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new UserNotFoundException(String.format("User with id %d not found", userId)));
  }

  private void requireAuthor(TaskReport report, Long userId, String action) {
    if (!report.getAuthor().getId().equals(userId)) {
      throw new NotAuthorizedException(
          String.format("You are not authorized to %s this report", action));
    }
  }

  private void requireReviewer(TaskReport report, Long userId, String action) {
    if (report.getReviewer() == null || !report.getReviewer().getId().equals(userId)) {
      throw new NotAuthorizedException(
          String.format("You are not authorized to %s this report", action));
    }
  }

  private void requireApprover(TaskReport report, Long userId, String action) {
    if (report.getApprover() == null || !report.getApprover().getId().equals(userId)) {
      throw new NotAuthorizedException(
          String.format("You are not authorized to %s this report", action));
    }
  }

  private void requireRole(User user, Role role, String message) {
    if (user.getRoles() == null || !user.getRoles().contains(role)) {
      throw new IllegalArgumentException(message);
    }
  }

  private void requireStatus(TaskReport report, ReportStatus expectedStatus, String message) {
    if (report.getStatus() != expectedStatus) {
      throw new IllegalArgumentException(message);
    }
  }

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
