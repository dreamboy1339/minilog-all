package com.asdf.minilog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.asdf.minilog.dto.TaskRequestDto;
import com.asdf.minilog.dto.TaskResponseDto;
import com.asdf.minilog.dto.TaskWithDeviceResponseDto;
import com.asdf.minilog.entity.task.Device;
import com.asdf.minilog.entity.task.Task;
import com.asdf.minilog.entity.task.TaskStatus;
import com.asdf.minilog.exception.DeviceNotFoundException;
import com.asdf.minilog.exception.TaskNotFoundException;
import com.asdf.minilog.repository.task.DeviceRepository;
import com.asdf.minilog.repository.task.TaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;
  @Mock private DeviceRepository deviceRepository;

  @InjectMocks private TaskService taskService;

  private final LocalDateTime fixture = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

  private Device device(Long id) {
    return Device.builder()
        .id(id)
        .name("d" + id)
        .type("sensor")
        .createdAt(fixture)
        .updatedAt(fixture)
        .build();
  }

  private Task task(Long id, Device parent) {
    return Task.builder()
        .id(id)
        .device(parent)
        .name("t" + id)
        .description("desc")
        .createdAt(fixture)
        .updatedAt(fixture)
        .build();
  }

  @BeforeEach
  void setUp() {
    taskService = new TaskService(taskRepository, deviceRepository);
  }

  @Test
  void createTask_saves() {
    Device d = device(1L);
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(d));
    when(taskRepository.save(any(Task.class)))
        .thenAnswer(
            inv -> {
              Task t = inv.getArgument(0);
              t.setId(10L);
              t.setCreatedAt(fixture);
              t.setUpdatedAt(fixture);
              return t;
            });

    TaskResponseDto result =
        taskService.createTask(
            TaskRequestDto.builder().deviceId(1L).name("t10").description("d").build());

    assertThat(result.getId()).isEqualTo(10L);
    assertThat(result.getDeviceId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("t10");
    assertThat(result.getStatus()).isEqualTo(TaskStatus.STARTED);
  }

  @Test
  void createTask_deviceNotFound() {
    when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> taskService.createTask(TaskRequestDto.builder().deviceId(99L).name("t").build()))
        .isInstanceOf(DeviceNotFoundException.class);
    verify(taskRepository, never()).save(any());
  }

  @Test
  void getTask_returnsDto() {
    when(taskRepository.findById(10L)).thenReturn(Optional.of(task(10L, device(1L))));

    TaskResponseDto result = taskService.getTask(10L);

    assertThat(result.getId()).isEqualTo(10L);
    assertThat(result.getDeviceId()).isEqualTo(1L);
  }

  @Test
  void getTask_notFound() {
    when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.getTask(999L)).isInstanceOf(TaskNotFoundException.class);
  }

  @Test
  void updateTask_sameDevice() {
    Device d = device(1L);
    Task t = task(10L, d);
    when(taskRepository.findById(10L)).thenReturn(Optional.of(t));
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    TaskResponseDto result =
        taskService.updateTask(
            10L, TaskRequestDto.builder().deviceId(1L).name("updated").description("d2").build());

    assertThat(result.getName()).isEqualTo("updated");
    assertThat(result.getDescription()).isEqualTo("d2");
    assertThat(result.getStatus()).isEqualTo(TaskStatus.STARTED);
    verify(deviceRepository, never()).findById(anyLong());
  }

  @Test
  void updateTask_changesStatus() {
    Device d = device(1L);
    Task t = task(10L, d);
    when(taskRepository.findById(10L)).thenReturn(Optional.of(t));
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    TaskResponseDto result =
        taskService.updateTask(
            10L,
            TaskRequestDto.builder()
                .deviceId(1L)
                .name("updated")
                .description("d2")
                .status(TaskStatus.COMPLETED)
                .build());

    assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
  }

  @Test
  void updateTask_changeDevice() {
    Device oldDevice = device(1L);
    Device newDevice = device(2L);
    Task t = task(10L, oldDevice);
    when(taskRepository.findById(10L)).thenReturn(Optional.of(t));
    when(deviceRepository.findById(2L)).thenReturn(Optional.of(newDevice));
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    TaskResponseDto result =
        taskService.updateTask(
            10L, TaskRequestDto.builder().deviceId(2L).name("updated").description("d2").build());

    assertThat(result.getDeviceId()).isEqualTo(2L);
  }

  @Test
  void deleteTask_existing() {
    when(taskRepository.existsById(10L)).thenReturn(true);

    taskService.deleteTask(10L);

    verify(taskRepository).deleteById(10L);
  }

  @Test
  void deleteTask_notFound() {
    when(taskRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> taskService.deleteTask(999L))
        .isInstanceOf(TaskNotFoundException.class);
  }

  @Test
  void getTasks_paged() {
    Device d = device(1L);
    Page<Task> page = new PageImpl<>(List.of(task(10L, d), task(11L, d)), PageRequest.of(0, 20), 2);
    when(taskRepository.findAll(any(Pageable.class))).thenReturn(page);

    Page<TaskResponseDto> result = taskService.getTasks(PageRequest.of(0, 20));

    assertThat(result.getTotalElements()).isEqualTo(2);
  }

  @Test
  void getTasksWithDevice_paged() {
    Device d = device(1L);
    Page<Task> page = new PageImpl<>(List.of(task(10L, d)), PageRequest.of(0, 20), 1);
    when(taskRepository.findAllWithDevice(any(Pageable.class))).thenReturn(page);

    Page<TaskWithDeviceResponseDto> result = taskService.getTasksWithDevice(PageRequest.of(0, 20));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getDevice().getId()).isEqualTo(1L);
  }
}
