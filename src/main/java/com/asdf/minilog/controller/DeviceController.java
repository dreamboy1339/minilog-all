package com.asdf.minilog.controller;

import com.asdf.minilog.dto.DeviceRequestDto;
import com.asdf.minilog.dto.DeviceResponseDto;
import com.asdf.minilog.dto.DeviceWithTasksResponseDto;
import com.asdf.minilog.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/devices")
public class DeviceController {

  private final DeviceService deviceService;

  @Autowired
  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @PostMapping
  @Operation(summary = "Create a new device")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Device created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<DeviceResponseDto> createDevice(
      @Valid @RequestBody DeviceRequestDto request) {
    return ResponseEntity.ok(deviceService.createDevice(request));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get device by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Device not found")
  })
  public ResponseEntity<DeviceResponseDto> getDevice(@PathVariable Long id) {
    return ResponseEntity.ok(deviceService.getDevice(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update device")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Device not found")
  })
  public ResponseEntity<DeviceResponseDto> updateDevice(
      @PathVariable Long id, @Valid @RequestBody DeviceRequestDto request) {
    return ResponseEntity.ok(deviceService.updateDevice(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete device")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Device not found")
  })
  public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
    deviceService.deleteDevice(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get devices (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<DeviceResponseDto>> getDevices(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(deviceService.getDevices(pageable));
  }

  @GetMapping("/with-tasks")
  @Operation(summary = "Get devices with their tasks (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<DeviceWithTasksResponseDto>> getDevicesWithTasks(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(deviceService.getDevicesWithTasks(pageable));
  }
}
