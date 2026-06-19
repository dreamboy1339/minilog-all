package com.asdf.minilog.controller;

import com.asdf.minilog.dto.TaskRequestDto;
import com.asdf.minilog.dto.TaskResponseDto;
import com.asdf.minilog.dto.TaskWithDeviceResponseDto;
import com.asdf.minilog.service.TaskService;
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

/**
 * 작업(Task) 관리 REST 컨트롤러.
 *
 * <p>{@code /api/v2/tasks} 경로에서 작업의 생성, 단건 조회, 수정, 삭제와 페이징 목록 조회를 제공한다. 작업이 속한 장비까지 함께 조회하는 엔드포인트도
 * 포함한다.
 */
@RestController
@RequestMapping("/api/v2/tasks")
public class TaskController {

  private final TaskService taskService;

  @Autowired
  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  /**
   * 새 작업을 생성한다. (POST /api/v2/tasks)
   *
   * @param request 생성할 작업 정보(연결할 장비 포함)
   * @return 생성된 작업 정보
   */
  @PostMapping
  @Operation(summary = "Create a new task")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Task created successfully"),
    @ApiResponse(responseCode = "404", description = "Device not found"),
    @ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request) {
    return ResponseEntity.ok(taskService.createTask(request));
  }

  /**
   * 작업 ID로 단건 작업을 조회한다. (GET /api/v2/tasks/{id})
   *
   * @param id 조회할 작업 ID
   * @return 작업 정보
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get task by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Task not found")
  })
  public ResponseEntity<TaskResponseDto> getTask(@PathVariable Long id) {
    return ResponseEntity.ok(taskService.getTask(id));
  }

  /**
   * 작업 정보를 수정한다. (PUT /api/v2/tasks/{id})
   *
   * @param id 수정할 작업 ID
   * @param request 수정할 작업 정보
   * @return 수정된 작업 정보
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update task")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Task or device not found")
  })
  public ResponseEntity<TaskResponseDto> updateTask(
      @PathVariable Long id, @Valid @RequestBody TaskRequestDto request) {
    return ResponseEntity.ok(taskService.updateTask(id, request));
  }

  /**
   * 작업을 삭제한다. (DELETE /api/v2/tasks/{id})
   *
   * @param id 삭제할 작업 ID
   * @return 본문 없는 204 응답
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete task")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Task not found")
  })
  public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * 작업 목록을 페이징하여 조회한다. (GET /api/v2/tasks)
   *
   * @param pageable 페이징 정보(기본 size 20, id 오름차순)
   * @return 페이징된 작업 목록
   */
  @GetMapping
  @Operation(summary = "Get tasks (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<TaskResponseDto>> getTasks(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(taskService.getTasks(pageable));
  }

  /**
   * 각 작업이 속한 장비(Device)까지 포함하여 페이징 조회한다. (GET /api/v2/tasks/with-device)
   *
   * @param pageable 페이징 정보(기본 size 20, id 오름차순)
   * @return 장비 정보가 포함된 페이징 작업 목록
   */
  @GetMapping("/with-device")
  @Operation(summary = "Get tasks with their device (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<TaskWithDeviceResponseDto>> getTasksWithDevice(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(taskService.getTasksWithDevice(pageable));
  }
}
