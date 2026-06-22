package com.asdf.minilog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.asdf.minilog.dto.DeviceRequestDto;
import com.asdf.minilog.dto.DeviceResponseDto;
import com.asdf.minilog.dto.DeviceWithTasksResponseDto;
import com.asdf.minilog.entity.task.Device;
import com.asdf.minilog.entity.task.Task;
import com.asdf.minilog.exception.DeviceNotFoundException;
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
class DeviceServiceTest {

  @Mock private DeviceRepository deviceRepository;
  @Mock private TaskRepository taskRepository;

  @InjectMocks private DeviceService deviceService;

  private final LocalDateTime fixture = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

  private Device device(Long id, String name, String type) {
    return Device.builder()
        .id(id)
        .name(name)
        .type(type)
        .createdAt(fixture)
        .updatedAt(fixture)
        .build();
  }

  private Task task(Long id, Device parent, String name) {
    return Task.builder()
        .id(id)
        .device(parent)
        .name(name)
        .description("desc")
        .createdAt(fixture)
        .updatedAt(fixture)
        .build();
  }

  @BeforeEach
  void setUp() {
    deviceService = new DeviceService(deviceRepository, taskRepository);
  }

  @Test
  void createDevice_saves() {
    DeviceRequestDto request = DeviceRequestDto.builder().name("d1").type("sensor").build();
    when(deviceRepository.save(any(Device.class))).thenReturn(device(1L, "d1", "sensor"));

    DeviceResponseDto result = deviceService.createDevice(request);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("d1");
    assertThat(result.getType()).isEqualTo("sensor");
    verify(deviceRepository, times(1)).save(any(Device.class));
  }

  @Test
  void getDevice_returnsDto() {
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(device(1L, "d1", "sensor")));

    DeviceResponseDto result = deviceService.getDevice(1L);

    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  void getDevice_notFound() {
    when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deviceService.getDevice(999L))
        .isInstanceOf(DeviceNotFoundException.class);
  }

  @Test
  void updateDevice_modifiesAndSaves() {
    Device existing = device(1L, "old", "sensor");
    when(deviceRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

    DeviceResponseDto result =
        deviceService.updateDevice(
            1L, DeviceRequestDto.builder().name("new").type("gateway").build());

    assertThat(result.getName()).isEqualTo("new");
    assertThat(result.getType()).isEqualTo("gateway");
  }

  @Test
  void deleteDevice_existing() {
    when(deviceRepository.existsById(1L)).thenReturn(true);

    deviceService.deleteDevice(1L);

    verify(deviceRepository).deleteById(1L);
  }

  @Test
  void deleteDevice_notFound() {
    when(deviceRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> deviceService.deleteDevice(999L))
        .isInstanceOf(DeviceNotFoundException.class);
    verify(deviceRepository, never()).deleteById(anyLong());
  }

  @Test
  void getDevices_paged() {
    Page<Device> page =
        new PageImpl<>(
            List.of(device(1L, "d1", "sensor"), device(2L, "d2", "gateway")),
            PageRequest.of(0, 20),
            2);
    when(deviceRepository.findAll(any(Pageable.class))).thenReturn(page);

    Page<DeviceResponseDto> result = deviceService.getDevices(PageRequest.of(0, 20));

    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  void getDevicesWithTasks_groupsTasksByDevice() {
    Device d1 = device(1L, "d1", "sensor");
    Device d2 = device(2L, "d2", "gateway");
    Page<Device> page = new PageImpl<>(List.of(d1, d2), PageRequest.of(0, 20), 2);
    when(deviceRepository.findAll(any(Pageable.class))).thenReturn(page);
    when(taskRepository.findAllByDeviceIdIn(List.of(1L, 2L)))
        .thenReturn(List.of(task(10L, d1, "t1"), task(11L, d1, "t2"), task(12L, d2, "t3")));

    Page<DeviceWithTasksResponseDto> result =
        deviceService.getDevicesWithTasks(PageRequest.of(0, 20));

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).getTasks()).hasSize(2);
    assertThat(result.getContent().get(1).getTasks()).hasSize(1);
  }

  @Test
  void getDevicesWithTasks_emptyPage_skipsTaskQuery() {
    Page<Device> empty = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
    when(deviceRepository.findAll(any(Pageable.class))).thenReturn(empty);

    Page<DeviceWithTasksResponseDto> result =
        deviceService.getDevicesWithTasks(PageRequest.of(0, 20));

    assertThat(result.getContent()).isEmpty();
    verify(taskRepository, never()).findAllByDeviceIdIn(any());
  }
}
