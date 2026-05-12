package com.asdf.minilog.util;

import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.dto.DeviceResponseDto;
import com.asdf.minilog.dto.DeviceSummaryDto;
import com.asdf.minilog.dto.DeviceWithTasksResponseDto;
import com.asdf.minilog.dto.FollowResponseDto;
import com.asdf.minilog.dto.TaskReportResponseDto;
import com.asdf.minilog.dto.TaskResponseDto;
import com.asdf.minilog.dto.TaskSummaryDto;
import com.asdf.minilog.dto.TaskWithDeviceResponseDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.entity.Article;
import com.asdf.minilog.entity.Device;
import com.asdf.minilog.entity.Follow;
import com.asdf.minilog.entity.Task;
import com.asdf.minilog.entity.TaskReport;
import com.asdf.minilog.entity.User;
import java.util.List;

public class EntityDtoMapper {

  public static ArticleResponseDto toDto(Article article) {
    return ArticleResponseDto.builder()
        .articleId(article.getId())
        .content(article.getContent())
        .authorId(article.getAuthor().getId())
        .authorName(article.getAuthor().getUserName())
        .createdAt(article.getCreatedAt())
        .build();
  }

  public static FollowResponseDto toDto(Follow follow) {
    return FollowResponseDto.builder()
        .followerId(follow.getFollower().getId())
        .followeeId(follow.getFollowee().getId())
        .build();
  }

  public static UserResponseDto toDto(User user) {
    return UserResponseDto.builder()
        .id(user.getId())
        .username(user.getUserName())
        .roles(user.getRoles())
        .build();
  }

  public static Follow toEntity(Long followerId, Long followeeId) {
    return Follow.builder()
        .follower(User.builder().id(followerId).build())
        .followee(User.builder().id(followeeId).build())
        .build();
  }

  public static DeviceResponseDto toDto(Device device) {
    return DeviceResponseDto.builder()
        .id(device.getId())
        .name(device.getName())
        .type(device.getType())
        .createdAt(device.getCreatedAt())
        .updatedAt(device.getUpdatedAt())
        .build();
  }

  public static DeviceSummaryDto toSummaryDto(Device device) {
    return DeviceSummaryDto.builder()
        .id(device.getId())
        .name(device.getName())
        .type(device.getType())
        .build();
  }

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

  public static TaskSummaryDto toSummaryDto(Task task) {
    return TaskSummaryDto.builder()
        .id(task.getId())
        .name(task.getName())
        .description(task.getDescription())
        .status(task.getStatus())
        .build();
  }

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

  public static TaskReportResponseDto toDto(TaskReport report) {
    return TaskReportResponseDto.builder()
        .id(report.getId())
        .taskId(report.getTask().getId())
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
