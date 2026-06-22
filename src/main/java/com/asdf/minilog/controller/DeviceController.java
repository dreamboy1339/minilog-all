package com.asdf.minilog.controller;

import com.asdf.minilog.dto.DeviceRequestDto;
import com.asdf.minilog.dto.DeviceResponseDto;
import com.asdf.minilog.dto.DeviceWithTasksResponseDto;
import com.asdf.minilog.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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

/**
 * 장비(Device) 관리 REST 컨트롤러.
 *
 * <p>{@code /api/v2/devices} 경로에서 장비의 생성, 단건 조회, 수정, 삭제와 페이징 목록 조회를 제공한다. 장비에 연결된 작업까지 함께 조회하는
 * 엔드포인트도 포함한다.
 */
@RestController
@RequestMapping("/api/v2/devices")
public class DeviceController {

  private final DeviceService deviceService;

  @Autowired
  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  /**
   * 새 장비를 생성한다. (POST /api/v2/devices)
   *
   * @param request 생성할 장비 정보
   * @return 생성된 장비 정보
   */
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

  /**
   * 장비 ID로 단건 장비를 조회한다. (GET /api/v2/devices/{id})
   *
   * @param id 조회할 장비 ID
   * @return 장비 정보
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get device by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Device not found")
  })
  public ResponseEntity<DeviceResponseDto> getDevice(@PathVariable Long id) {
    return ResponseEntity.ok(deviceService.getDevice(id));
  }

  /**
   * 장비 정보를 수정한다. (PUT /api/v2/devices/{id})
   *
   * @param id 수정할 장비 ID
   * @param request 수정할 장비 정보
   * @return 수정된 장비 정보
   */
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

  /**
   * 장비를 삭제한다. (DELETE /api/v2/devices/{id})
   *
   * @param id 삭제할 장비 ID
   * @return 본문 없는 204 응답
   */
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

  /**
   * 장비 목록을 페이징하여 조회한다. (GET /api/v2/devices)
   *
   * @param pageable 페이징 정보(기본 size 20, id 오름차순)
   * @return 페이징된 장비 목록
   */
  @GetMapping
  @Operation(summary = "Get devices (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<DeviceResponseDto>> getDevices(
      @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(deviceService.getDevices(pageable));
  }

  /**
   * 각 장비에 연결된 작업(Task)까지 포함하여 페이징 조회한다. (GET /api/v2/devices/with-tasks)
   *
   * @param pageable 페이징 정보(기본 size 20, id 오름차순)
   * @return 작업 목록이 포함된 페이징 장비 목록
   */
  @GetMapping("/with-tasks")
  @Operation(summary = "Get devices with their tasks (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<DeviceWithTasksResponseDto>> getDevicesWithTasks(
      @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(deviceService.getDevicesWithTasks(pageable));
  }
}
