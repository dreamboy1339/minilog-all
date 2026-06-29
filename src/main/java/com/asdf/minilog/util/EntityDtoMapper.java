package com.asdf.minilog.util;

import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.dto.AttachmentResponseDto;
import com.asdf.minilog.dto.DeviceResponseDto;
import com.asdf.minilog.dto.DeviceSummaryDto;
import com.asdf.minilog.dto.DeviceWithTasksResponseDto;
import com.asdf.minilog.dto.FollowResponseDto;
import com.asdf.minilog.dto.TaskReportResponseDto;
import com.asdf.minilog.dto.TaskResponseDto;
import com.asdf.minilog.dto.TaskSummaryDto;
import com.asdf.minilog.dto.TaskWithDeviceResponseDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.entity.main.Article;
import com.asdf.minilog.entity.main.Attachment;
import com.asdf.minilog.entity.main.Follow;
import com.asdf.minilog.entity.main.TaskReport;
import com.asdf.minilog.entity.main.User;
import com.asdf.minilog.entity.task.Device;
import com.asdf.minilog.entity.task.Task;
import java.util.List;

/**
 * 엔티티와 응답 DTO 간 변환을 담당하는 유틸리티.
 *
 * <p>정적 메서드로 각 엔티티를 화면/응답에 맞는 DTO로 매핑하며, 반대로 일부 요청 값을 엔티티로 변환하는 메서드도 제공한다.
 */
public class EntityDtoMapper {

  /** {@link Article} 엔티티를 {@link ArticleResponseDto}로 변환한다. */
  public static ArticleResponseDto toDto(Article article) {
    return ArticleResponseDto.builder()
        .articleId(article.getId())
        .content(article.getContent())
        .authorId(article.getAuthor().getId())
        .authorName(article.getAuthor().getUserName())
        .createdAt(article.getCreatedAt())
        .build();
  }

  /** {@link Attachment} 엔티티를 {@link AttachmentResponseDto}로 변환한다. */
  public static AttachmentResponseDto toDto(Attachment attachment) {
    return AttachmentResponseDto.builder()
        .id(attachment.getId())
        .originalFileName(attachment.getOriginalFileName())
        .contentType(attachment.getContentType())
        .fileSize(attachment.getFileSize())
        .createdAt(attachment.getCreatedAt())
        .build();
  }

  /** {@link Follow} 엔티티를 {@link FollowResponseDto}로 변환한다. */
  public static FollowResponseDto toDto(Follow follow) {
    return FollowResponseDto.builder()
        .followerId(follow.getFollower().getId())
        .followeeId(follow.getFollowee().getId())
        .build();
  }

  /** {@link User} 엔티티를 {@link UserResponseDto}로 변환한다. */
  public static UserResponseDto toDto(User user) {
    return UserResponseDto.builder()
        .id(user.getId())
        .username(user.getUserName())
        .roles(user.getRoles())
        .build();
  }

  /** 팔로워/팔로위 ID로 {@link Follow} 엔티티를 생성한다. */
  public static Follow toEntity(Long followerId, Long followeeId) {
    return Follow.builder()
        .follower(User.builder().id(followerId).build())
        .followee(User.builder().id(followeeId).build())
        .build();
  }

  /** {@link Device} 엔티티를 {@link DeviceResponseDto}로 변환한다. */
  public static DeviceResponseDto toDto(Device device) {
    return DeviceResponseDto.builder()
        .id(device.getId())
        .name(device.getName())
        .type(device.getType())
        .createdAt(device.getCreatedAt())
        .updatedAt(device.getUpdatedAt())
        .build();
  }

  /** {@link Device}를 목록용 요약 DTO인 {@link DeviceSummaryDto}로 변환한다. */
  public static DeviceSummaryDto toSummaryDto(Device device) {
    return DeviceSummaryDto.builder()
        .id(device.getId())
        .name(device.getName())
        .type(device.getType())
        .build();
  }

  /** {@link Task} 엔티티를 {@link TaskResponseDto}로 변환한다. */
  public static TaskResponseDto toDto(Task task) {
    return TaskResponseDto.builder()
        .id(task.getId())
        .deviceId(task.getDevice().getId())
        .name(task.getName())
        .description(task.getDescription())
        .status(task.getStatus())
        .createdAt(task.getCreatedAt())
        .updatedAt(task.getUpdatedAt())
        .build();
  }

  /** {@link Task}를 목록용 요약 DTO인 {@link TaskSummaryDto}로 변환한다. */
  public static TaskSummaryDto toSummaryDto(Task task) {
    return TaskSummaryDto.builder()
        .id(task.getId())
        .name(task.getName())
        .description(task.getDescription())
        .status(task.getStatus())
        .build();
  }

  /** {@link Device}와 해당 작업 목록을 묶어 {@link DeviceWithTasksResponseDto}로 변환한다. */
  public static DeviceWithTasksResponseDto toWithTasksDto(Device device, List<Task> tasks) {
    return DeviceWithTasksResponseDto.builder()
        .id(device.getId())
        .name(device.getName())
        .type(device.getType())
        .createdAt(device.getCreatedAt())
        .updatedAt(device.getUpdatedAt())
        .tasks(tasks.stream().map(EntityDtoMapper::toSummaryDto).toList())
        .build();
  }

  /** {@link Task}를 소속 디바이스 요약과 함께 {@link TaskWithDeviceResponseDto}로 변환한다. */
  public static TaskWithDeviceResponseDto toWithDeviceDto(Task task) {
    return TaskWithDeviceResponseDto.builder()
        .id(task.getId())
        .name(task.getName())
        .description(task.getDescription())
        .status(task.getStatus())
        .createdAt(task.getCreatedAt())
        .updatedAt(task.getUpdatedAt())
        .device(toSummaryDto(task.getDevice()))
        .build();
  }

  /**
   * {@link TaskReport} 엔티티를 {@link TaskReportResponseDto}로 변환한다.
   *
   * <p>리뷰어/승인자는 미지정일 수 있으므로 null 안전 처리한다.
   */
  public static TaskReportResponseDto toDto(TaskReport report) {
    return TaskReportResponseDto.builder()
        .id(report.getId())
        .taskId(report.getTaskId())
        .authorId(report.getAuthor().getId())
        .authorName(report.getAuthor().getUserName())
        .reviewerId(report.getReviewer() == null ? null : report.getReviewer().getId())
        .reviewerName(report.getReviewer() == null ? null : report.getReviewer().getUserName())
        .approverId(report.getApprover() == null ? null : report.getApprover().getId())
        .approverName(report.getApprover() == null ? null : report.getApprover().getUserName())
        .content(report.getContent())
        .status(report.getStatus())
        .createdAt(report.getCreatedAt())
        .updatedAt(report.getUpdatedAt())
        .build();
  }
}
