package com.asdf.minilog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.asdf.minilog.dto.TaskReportRequestDto;
import com.asdf.minilog.dto.TaskReportResponseDto;
import com.asdf.minilog.dto.TaskReportSubmitRequestDto;
import com.asdf.minilog.dto.TaskReportUpdateRequestDto;
import com.asdf.minilog.entity.main.ReportStatus;
import com.asdf.minilog.entity.main.Role;
import com.asdf.minilog.entity.main.TaskReport;
import com.asdf.minilog.entity.main.User;
import com.asdf.minilog.entity.task.Device;
import com.asdf.minilog.entity.task.Task;
import com.asdf.minilog.entity.task.TaskStatus;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.repository.main.TaskReportRepository;
import com.asdf.minilog.repository.main.UserRepository;
import com.asdf.minilog.repository.task.TaskRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskReportServiceTest {

  @Mock private TaskReportRepository taskReportRepository;
  @Mock private TaskRepository taskRepository;
  @Mock private UserRepository userRepository;

  private TaskReportService taskReportService;

  private final LocalDateTime fixture = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

  @BeforeEach
  void setUp() {
    taskReportService = new TaskReportService(taskReportRepository, taskRepository, userRepository);
  }

  @Test
  void createReport_completedTask_savesDraft() {
    Task task = task(10L, TaskStatus.COMPLETED);
    User author = user(1L, "author", Role.ROLE_AUTHOR);
    when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
    when(taskReportRepository.existsByTaskId(10L)).thenReturn(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(author));
    when(taskReportRepository.save(any(TaskReport.class)))
        .thenAnswer(
            invocation -> {
              TaskReport report = invocation.getArgument(0);
              report.setId(100L);
              report.setCreatedAt(fixture);
              report.setUpdatedAt(fixture);
              return report;
            });

    TaskReportResponseDto result =
        taskReportService.createReport(
            1L, TaskReportRequestDto.builder().taskId(10L).content("report").build());

    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getTaskId()).isEqualTo(10L);
    assertThat(result.getAuthorId()).isEqualTo(1L);
    assertThat(result.getStatus()).isEqualTo(ReportStatus.DRAFT);
  }

  @Test
  void createReport_requiresCompletedTask() {
    when(taskRepository.findById(10L)).thenReturn(Optional.of(task(10L, TaskStatus.IN_PROGRESS)));

    assertThatThrownBy(
            () ->
                taskReportService.createReport(
                    1L, TaskReportRequestDto.builder().taskId(10L).content("report").build()))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskReportRepository, never()).save(any());
  }

  @Test
  void createReport_rejectsDuplicateTaskReport() {
    when(taskRepository.findById(10L)).thenReturn(Optional.of(task(10L, TaskStatus.COMPLETED)));
    when(taskReportRepository.existsByTaskId(10L)).thenReturn(true);

    assertThatThrownBy(
            () ->
                taskReportService.createReport(
                    1L, TaskReportRequestDto.builder().taskId(10L).content("report").build()))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskReportRepository, never()).save(any());
  }

  @Test
  void submitReport_setsReviewerApproverAndSubmitted() {
    TaskReport report = report(100L, ReportStatus.DRAFT, user(1L, "author", Role.ROLE_AUTHOR));
    User reviewer = user(2L, "reviewer", Role.ROLE_REVIEWER);
    User approver = user(3L, "approver", Role.ROLE_APPROVER);
    when(taskReportRepository.findById(100L)).thenReturn(Optional.of(report));
    when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
    when(userRepository.findById(3L)).thenReturn(Optional.of(approver));
    when(taskReportRepository.save(any(TaskReport.class))).thenAnswer(inv -> inv.getArgument(0));

    TaskReportResponseDto result =
        taskReportService.submitReport(
            1L, 100L, TaskReportSubmitRequestDto.builder().reviewerId(2L).approverId(3L).build());

    assertThat(result.getStatus()).isEqualTo(ReportStatus.SUBMITTED);
    assertThat(result.getReviewerId()).isEqualTo(2L);
    assertThat(result.getApproverId()).isEqualTo(3L);
  }

  @Test
  void workflow_progressesToApproved() {
    User author = user(1L, "author", Role.ROLE_AUTHOR);
    User reviewer = user(2L, "reviewer", Role.ROLE_REVIEWER);
    User approver = user(3L, "approver", Role.ROLE_APPROVER);
    TaskReport report = report(100L, ReportStatus.SUBMITTED, author);
    report.setReviewer(reviewer);
    report.setApprover(approver);
    when(taskReportRepository.findById(100L)).thenReturn(Optional.of(report));
    when(taskReportRepository.save(any(TaskReport.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThat(taskReportService.startReview(2L, 100L).getStatus()).isEqualTo(ReportStatus.REVIEW);
    assertThat(taskReportService.completeReview(2L, 100L).getStatus())
        .isEqualTo(ReportStatus.APPROVAL);
    assertThat(taskReportService.approveReport(3L, 100L).getStatus())
        .isEqualTo(ReportStatus.APPROVED);
  }

  @Test
  void rejectReview_returnsToSubmitted() {
    User reviewer = user(2L, "reviewer", Role.ROLE_REVIEWER);
    TaskReport report = report(100L, ReportStatus.REVIEW, user(1L, "author", Role.ROLE_AUTHOR));
    report.setReviewer(reviewer);
    when(taskReportRepository.findById(100L)).thenReturn(Optional.of(report));
    when(taskReportRepository.save(any(TaskReport.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThat(taskReportService.rejectReview(2L, 100L).getStatus())
        .isEqualTo(ReportStatus.SUBMITTED);
  }

  @Test
  void rejectApproval_returnsToReview() {
    User approver = user(3L, "approver", Role.ROLE_APPROVER);
    TaskReport report = report(100L, ReportStatus.APPROVAL, user(1L, "author", Role.ROLE_AUTHOR));
    report.setApprover(approver);
    when(taskReportRepository.findById(100L)).thenReturn(Optional.of(report));
    when(taskReportRepository.save(any(TaskReport.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThat(taskReportService.rejectApproval(3L, 100L).getStatus())
        .isEqualTo(ReportStatus.REVIEW);
  }

  @Test
  void deleteReport_onlyAuthorCanDeleteDraft() {
    TaskReport report = report(100L, ReportStatus.DRAFT, user(1L, "author", Role.ROLE_AUTHOR));
    when(taskReportRepository.findById(100L)).thenReturn(Optional.of(report));

    assertThatThrownBy(() -> taskReportService.deleteReport(9L, 100L))
        .isInstanceOf(NotAuthorizedException.class);
    verify(taskReportRepository, never()).deleteById(anyLong());
  }

  @Test
  void updateReport_requiresDraftOrSubmitted() {
    TaskReport report = report(100L, ReportStatus.REVIEW, user(1L, "author", Role.ROLE_AUTHOR));
    when(taskReportRepository.findById(100L)).thenReturn(Optional.of(report));

    assertThatThrownBy(
            () ->
                taskReportService.updateReport(
                    1L, 100L, TaskReportUpdateRequestDto.builder().content("updated").build()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private Task task(Long id, TaskStatus status) {
    return Task.builder()
        .id(id)
        .device(Device.builder().id(1L).name("device").type("sensor").build())
        .name("task")
        .description("desc")
        .status(status)
        .createdAt(fixture)
        .updatedAt(fixture)
        .build();
  }

  private TaskReport report(Long id, ReportStatus status, User author) {
    return TaskReport.builder()
        .id(id)
        .taskId(10L)
        .author(author)
        .content("report")
        .status(status)
        .createdAt(fixture)
        .updatedAt(fixture)
        .build();
  }

  private User user(Long id, String name, Role role) {
    return User.builder().id(id).userName(name).roles(Set.of(role)).build();
  }
}
