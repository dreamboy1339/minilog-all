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

@RestController
@RequestMapping("/api/v2/tasks")
public class TaskController {

  private final TaskService taskService;

  @Autowired
  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

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

  @GetMapping("/{id}")
  @Operation(summary = "Get task by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Task not found")
  })
  public ResponseEntity<TaskResponseDto> getTask(@PathVariable Long id) {
    return ResponseEntity.ok(taskService.getTask(id));
  }

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

  @GetMapping
  @Operation(summary = "Get tasks (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<TaskResponseDto>> getTasks(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(taskService.getTasks(pageable));
  }

  @GetMapping("/with-device")
  @Operation(summary = "Get tasks with their device (paged)")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Page<TaskWithDeviceResponseDto>> getTasksWithDevice(
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return ResponseEntity.ok(taskService.getTasksWithDevice(pageable));
  }
}
