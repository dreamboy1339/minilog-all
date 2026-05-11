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

  public DeviceResponseDto createDevice(DeviceRequestDto request) {
    Device device = Device.builder().name(request.getName()).type(request.getType()).build();
    Device saved = deviceRepository.save(device);
    return EntityDtoMapper.toDto(saved);
  }

  public DeviceResponseDto updateDevice(Long id, DeviceRequestDto request) {
    Device device = findDeviceOrThrow(id);
    device.setName(request.getName());
    device.setType(request.getType());
    Device updated = deviceRepository.save(device);
    return EntityDtoMapper.toDto(updated);
  }

  public void deleteDevice(Long id) {
    if (!deviceRepository.existsById(id)) {
      throw new DeviceNotFoundException(String.format("Device with id %d not found", id));
    }
    deviceRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public DeviceResponseDto getDevice(Long id) {
    return EntityDtoMapper.toDto(findDeviceOrThrow(id));
  }

  @Transactional(readOnly = true)
  public Page<DeviceResponseDto> getDevices(Pageable pageable) {
    return deviceRepository.findAll(pageable).map(EntityDtoMapper::toDto);
  }

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

  private Device findDeviceOrThrow(Long id) {
    return deviceRepository
        .findById(id)
        .orElseThrow(
            () -> new DeviceNotFoundException(String.format("Device with id %d not found", id)));
  }
}
