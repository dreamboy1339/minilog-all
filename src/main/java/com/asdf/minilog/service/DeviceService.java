package com.asdf.minilog.service;

import com.asdf.minilog.dto.DeviceRequestDto;
import com.asdf.minilog.dto.DeviceResponseDto;
import com.asdf.minilog.dto.DeviceWithTasksResponseDto;
import com.asdf.minilog.entity.Device;
import com.asdf.minilog.entity.Task;
import com.asdf.minilog.exception.DeviceNotFoundException;
import com.asdf.minilog.repository.DeviceRepository;
import com.asdf.minilog.repository.TaskRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장비(Device) 도메인의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>장비의 생성/수정/삭제/조회와 페이징 조회를 제공하며, 장비에 연결된 작업(Task) 목록을 함께 조회하는 기능도 포함한다.
 */
@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final TaskRepository taskRepository;

  @Autowired
  public DeviceService(DeviceRepository deviceRepository, TaskRepository taskRepository) {
    this.deviceRepository = deviceRepository;
    this.taskRepository = taskRepository;
  }

  /**
   * 새 장비를 생성한다.
   *
   * @param request 장비 이름·타입 등 생성 정보
   * @return 생성된 장비 정보
   */
  public DeviceResponseDto createDevice(DeviceRequestDto request) {
    Device device = Device.builder().name(request.getName()).type(request.getType()).build();
    Device saved = deviceRepository.save(device);
    return EntityDtoMapper.toDto(saved);
  }

  /**
   * 기존 장비의 이름과 타입을 수정한다.
   *
   * @param id 수정할 장비 ID
   * @param request 변경할 정보
   * @return 수정된 장비 정보
   * @throws DeviceNotFoundException 장비를 찾을 수 없는 경우
   */
  public DeviceResponseDto updateDevice(Long id, DeviceRequestDto request) {
    Device device = findDeviceOrThrow(id);
    device.setName(request.getName());
    device.setType(request.getType());
    Device updated = deviceRepository.save(device);
    return EntityDtoMapper.toDto(updated);
  }

  /**
   * 장비를 삭제한다.
   *
   * @param id 삭제할 장비 ID
   * @throws DeviceNotFoundException 장비가 존재하지 않는 경우
   */
  public void deleteDevice(Long id) {
    if (!deviceRepository.existsById(id)) {
      throw new DeviceNotFoundException(String.format("Device with id %d not found", id));
    }
    deviceRepository.deleteById(id);
  }

  /**
   * 단건 장비를 조회한다.
   *
   * @param id 조회할 장비 ID
   * @return 장비 정보
   * @throws DeviceNotFoundException 장비를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public DeviceResponseDto getDevice(Long id) {
    return EntityDtoMapper.toDto(findDeviceOrThrow(id));
  }

  /**
   * 장비 목록을 페이징하여 조회한다.
   *
   * @param pageable 페이징 정보
   * @return 페이징된 장비 목록
   */
  @Transactional(readOnly = true)
  public Page<DeviceResponseDto> getDevices(Pageable pageable) {
    return deviceRepository.findAll(pageable).map(EntityDtoMapper::toDto);
  }

  /**
   * 장비 목록을 페이징 조회하면서 각 장비에 연결된 작업(Task) 목록을 함께 반환한다.
   *
   * <p>장비 ID들을 모아 작업을 일괄 조회한 뒤 장비별로 묶어 매핑하므로, 장비마다 개별 쿼리를 날리는 N+1 문제를 피한다.
   *
   * @param pageable 페이징 정보
   * @return 작업 목록이 포함된 페이징된 장비 목록
   */
  @Transactional(readOnly = true)
  public Page<DeviceWithTasksResponseDto> getDevicesWithTasks(Pageable pageable) {
    Page<Device> devicePage = deviceRepository.findAll(pageable);
    List<Device> devices = devicePage.getContent();

    if (devices.isEmpty()) {
      return devicePage.map(d -> EntityDtoMapper.toWithTasksDto(d, Collections.emptyList()));
    }

    List<Long> deviceIds = devices.stream().map(Device::getId).toList();
    Map<Long, List<Task>> tasksByDeviceId =
        taskRepository.findAllByDeviceIdIn(deviceIds).stream()
            .collect(Collectors.groupingBy(t -> t.getDevice().getId()));

    return devicePage.map(
        device ->
            EntityDtoMapper.toWithTasksDto(
                device, tasksByDeviceId.getOrDefault(device.getId(), Collections.emptyList())));
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
