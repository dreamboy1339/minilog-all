package com.asdf.minilog.service;

import com.asdf.minilog.dto.TaskRequestDto;
import com.asdf.minilog.dto.TaskResponseDto;
import com.asdf.minilog.dto.TaskWithDeviceResponseDto;
import com.asdf.minilog.entity.Device;
import com.asdf.minilog.entity.Task;
import com.asdf.minilog.entity.TaskStatus;
import com.asdf.minilog.exception.DeviceNotFoundException;
import com.asdf.minilog.exception.TaskNotFoundException;
import com.asdf.minilog.repository.DeviceRepository;
import com.asdf.minilog.repository.TaskRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class TaskService {

  private final TaskRepository taskRepository;
  private final DeviceRepository deviceRepository;

  @Autowired
  public TaskService(TaskRepository taskRepository, DeviceRepository deviceRepository) {
    this.taskRepository = taskRepository;
    this.deviceRepository = deviceRepository;
  }

  public TaskResponseDto createTask(TaskRequestDto request) {
    Device device = findDeviceOrThrow(request.getDeviceId());

    Task task =
        Task.builder()
            .device(device)
            .name(request.getName())
            .description(request.getDescription())
            .status(request.getStatus() == null ? TaskStatus.STARTED : request.getStatus())
            .build();
    Task saved = taskRepository.save(task);
    return EntityDtoMapper.toDto(saved);
  }

  public TaskResponseDto updateTask(Long id, TaskRequestDto request) {
    Task task = findTaskOrThrow(id);
    if (!task.getDevice().getId().equals(request.getDeviceId())) {
      Device newDevice = findDeviceOrThrow(request.getDeviceId());
      task.setDevice(newDevice);
    }
    task.setName(request.getName());
    task.setDescription(request.getDescription());
    if (request.getStatus() != null) {
      task.setStatus(request.getStatus());
    }
    Task updated = taskRepository.save(task);
    return EntityDtoMapper.toDto(updated);
  }

  public void deleteTask(Long id) {
    if (!taskRepository.existsById(id)) {
      throw new TaskNotFoundException(String.format("Task with id %d not found", id));
    }
    taskRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public TaskResponseDto getTask(Long id) {
    return EntityDtoMapper.toDto(findTaskOrThrow(id));
  }

  @Transactional(readOnly = true)
  public Page<TaskResponseDto> getTasks(Pageable pageable) {
    return taskRepository.findAll(pageable).map(EntityDtoMapper::toDto);
  }

  @Transactional(readOnly = true)
  public Page<TaskWithDeviceResponseDto> getTasksWithDevice(Pageable pageable) {
    return taskRepository.findAllWithDevice(pageable).map(EntityDtoMapper::toWithDeviceDto);
  }

  private Task findTaskOrThrow(Long id) {
    return taskRepository
        .findById(id)
        .orElseThrow(
            () -> new TaskNotFoundException(String.format("Task with id %d not found", id)));
  }

  private Device findDeviceOrThrow(Long id) {
    return deviceRepository
        .findById(id)
        .orElseThrow(
            () -> new DeviceNotFoundException(String.format("Device with id %d not found", id)));
  }
}
