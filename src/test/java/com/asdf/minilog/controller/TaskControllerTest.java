package com.asdf.minilog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asdf.minilog.dto.DeviceSummaryDto;
import com.asdf.minilog.dto.TaskRequestDto;
import com.asdf.minilog.dto.TaskResponseDto;
import com.asdf.minilog.dto.TaskWithDeviceResponseDto;
import com.asdf.minilog.exception.TaskNotFoundException;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.TaskService;
import com.asdf.minilog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TaskController.class)
public class TaskControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TaskService taskService;

  @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private JwtUtil jwtUtil;

  private final ObjectMapper objectMapper = new ObjectMapper();

  LocalDateTime fixtureDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  String formattedFixtureDateTime = fixtureDateTime.format(formatter);

  @BeforeEach
  public void setup() {
    //noinspection resource
    MockitoAnnotations.openMocks(this);

    MinilogUserDetails userDetails =
        new MinilogUserDetails(1L, "Test User", "password", List.of(() -> "ROLE_AUTHOR"));
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private TaskResponseDto sampleResponse() {
    return TaskResponseDto.builder()
        .id(10L)
        .deviceId(1L)
        .name("task-a")
        .description("first task")
        .createdAt(fixtureDateTime)
        .updatedAt(fixtureDateTime)
        .build();
  }

  @Test
  public void testCreateTask() throws Exception {
    TaskRequestDto request =
        TaskRequestDto.builder().deviceId(1L).name("task-a").description("first task").build();
    when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(sampleResponse());

    mockMvc
        .perform(
            post("/api/v2/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10L))
        .andExpect(jsonPath("$.deviceId").value(1L))
        .andExpect(jsonPath("$.name").value("task-a"))
        .andExpect(jsonPath("$.description").value("first task"))
        .andExpect(jsonPath("$.createdAt").value(formattedFixtureDateTime))
        .andExpect(jsonPath("$.updatedAt").value(formattedFixtureDateTime));
  }

  @Test
  public void testGetTask() throws Exception {
    when(taskService.getTask(anyLong())).thenReturn(sampleResponse());

    mockMvc
        .perform(get("/api/v2/tasks/10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10L))
        .andExpect(jsonPath("$.deviceId").value(1L))
        .andExpect(jsonPath("$.name").value("task-a"));
  }

  @Test
  public void testUpdateTask() throws Exception {
    TaskRequestDto request =
        TaskRequestDto.builder()
            .deviceId(1L)
            .name("task-updated")
            .description("desc-updated")
            .build();
    TaskResponseDto response =
        TaskResponseDto.builder()
            .id(10L)
            .deviceId(1L)
            .name("task-updated")
            .description("desc-updated")
            .createdAt(fixtureDateTime)
            .updatedAt(fixtureDateTime)
            .build();
    when(taskService.updateTask(anyLong(), any(TaskRequestDto.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/api/v2/tasks/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10L))
        .andExpect(jsonPath("$.name").value("task-updated"))
        .andExpect(jsonPath("$.description").value("desc-updated"));
  }

  @Test
  public void testDeleteTask() throws Exception {
    mockMvc.perform(delete("/api/v2/tasks/10").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  public void testGetTasks() throws Exception {
    Page<TaskResponseDto> page =
        new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 20), 1);
    when(taskService.getTasks(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/v2/tasks").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(10L))
        .andExpect(jsonPath("$.content[0].name").value("task-a"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  public void testGetTasksWithDevice() throws Exception {
    TaskWithDeviceResponseDto withDevice =
        TaskWithDeviceResponseDto.builder()
            .id(10L)
            .name("task-a")
            .description("first task")
            .createdAt(fixtureDateTime)
            .updatedAt(fixtureDateTime)
            .device(DeviceSummaryDto.builder().id(1L).name("device-1").type("sensor").build())
            .build();
    Page<TaskWithDeviceResponseDto> page =
        new PageImpl<>(List.of(withDevice), PageRequest.of(0, 20), 1);
    when(taskService.getTasksWithDevice(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/v2/tasks/with-device").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(10L))
        .andExpect(jsonPath("$.content[0].device.id").value(1L))
        .andExpect(jsonPath("$.content[0].device.name").value("device-1"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  public void testTaskNotFound() throws Exception {
    when(taskService.getTask(anyLong()))
        .thenThrow(new TaskNotFoundException("Task with id 999 not found"));

    mockMvc
        .perform(get("/api/v2/tasks/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Task with id 999 not found"));
  }
}
