package com.asdf.minilog.service;

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
import com.asdf.minilog.util.EntityDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 작업(Task) 도메인의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>작업의 생성/수정/삭제/조회 및 페이징 조회를 제공한다. 모든 작업은 특정 장비(Device)에 소속되며, 작업을 다룰 때 연결된 장비의 존재 여부를 검증한다.
 */
@Service
@Transactional(transactionManager = "taskTransactionManager", isolation = Isolation.REPEATABLE_READ)
public class TaskService {

  private final TaskRepository taskRepository;
  private final DeviceRepository deviceRepository;

  @Autowired
  public TaskService(TaskRepository taskRepository, DeviceRepository deviceRepository) {
    this.taskRepository = taskRepository;
    this.deviceRepository = deviceRepository;
  }

  /**
   * 지정한 장비에 소속된 새 작업을 생성한다.
   *
   * @param request 작업 생성 정보(장비 ID, 이름, 설명, 상태 등)
   * @return 생성된 작업 정보
   * @throws DeviceNotFoundException 연결할 장비를 찾을 수 없는 경우
   */
  public TaskResponseDto createTask(TaskRequestDto request) {
    Device device = findDeviceOrThrow(request.getDeviceId());

    Task task =
        Task.builder()
            .device(device)
            .name(request.getName())
            .description(request.getDescription())
            // 상태가 지정되지 않으면 기본값 STARTED로 생성
            .status(request.getStatus() == null ? TaskStatus.STARTED : request.getStatus())
            .build();
    Task saved = taskRepository.save(task);
    return EntityDtoMapper.toDto(saved);
  }

  /**
   * 작업 정보를 수정한다. 장비 ID가 변경된 경우 새 장비로 재연결하며, 상태는 값이 있을 때만 변경한다.
   *
   * @param id 수정할 작업 ID
   * @param request 변경할 정보
   * @return 수정된 작업 정보
   * @throws TaskNotFoundException 작업을 찾을 수 없는 경우
   * @throws DeviceNotFoundException 변경하려는 장비를 찾을 수 없는 경우
   */
  public TaskResponseDto updateTask(Long id, TaskRequestDto request) {
    Task task = findTaskOrThrow(id);
    // 장비가 바뀐 경우에만 새 장비 존재 여부를 확인하고 재연결
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

  /**
   * 작업을 삭제한다.
   *
   * @param id 삭제할 작업 ID
   * @throws TaskNotFoundException 작업이 존재하지 않는 경우
   */
  public void deleteTask(Long id) {
    if (!taskRepository.existsById(id)) {
      throw new TaskNotFoundException(String.format("Task with id %d not found", id));
    }
    taskRepository.deleteById(id);
  }

  /**
   * 단건 작업을 조회한다.
   *
   * @param id 조회할 작업 ID
   * @return 작업 정보
   * @throws TaskNotFoundException 작업을 찾을 수 없는 경우
   */
  @Transactional(transactionManager = "taskTransactionManager", readOnly = true)
  public TaskResponseDto getTask(Long id) {
    return EntityDtoMapper.toDto(findTaskOrThrow(id));
  }

  /**
   * 작업 목록을 페이징하여 조회한다.
   *
   * @param pageable 페이징 정보
   * @return 페이징된 작업 목록
   */
  @Transactional(transactionManager = "taskTransactionManager", readOnly = true)
  public Page<TaskResponseDto> getTasks(Pageable pageable) {
    return taskRepository.findAll(pageable).map(EntityDtoMapper::toDto);
  }

  /**
   * 작업 목록을 페이징 조회하면서 각 작업이 소속된 장비(Device) 정보를 함께 반환한다.
   *
   * @param pageable 페이징 정보
   * @return 장비 정보가 포함된 페이징된 작업 목록
   */
  @Transactional(transactionManager = "taskTransactionManager", readOnly = true)
  public Page<TaskWithDeviceResponseDto> getTasksWithDevice(Pageable pageable) {
    return taskRepository.findAllWithDevice(pageable).map(EntityDtoMapper::toWithDeviceDto);
  }

  /**
   * 작업을 조회하고, 없으면 예외를 던지는 내부 헬퍼.
   *
   * @throws TaskNotFoundException 작업을 찾을 수 없는 경우
   */
  private Task findTaskOrThrow(Long id) {
    return taskRepository
        .findById(id)
        .orElseThrow(
            () -> new TaskNotFoundException(String.format("Task with id %d not found", id)));
  }

  /**
   * 장비를 조회하고, 없으면 예외를 던지는 내부 헬퍼.
   *
   * @throws DeviceNotFoundException 장비를 찾을 수 없는 경우
   */
  private Device findDeviceOrThrow(Long id) {
    return deviceRepository
        .findById(id)
        .orElseThrow(
            () -> new DeviceNotFoundException(String.format("Device with id %d not found", id)));
  }
}
