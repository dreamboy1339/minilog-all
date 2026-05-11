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

import com.asdf.minilog.dto.DeviceRequestDto;
import com.asdf.minilog.dto.DeviceResponseDto;
import com.asdf.minilog.dto.DeviceWithTasksResponseDto;
import com.asdf.minilog.dto.TaskSummaryDto;
import com.asdf.minilog.exception.DeviceNotFoundException;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.DeviceService;
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
@WebMvcTest(DeviceController.class)
public class DeviceControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DeviceService deviceService;

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

  private DeviceResponseDto sampleResponse() {
    return DeviceResponseDto.builder()
        .id(1L)
        .name("device-1")
        .type("sensor")
        .createdAt(fixtureDateTime)
        .updatedAt(fixtureDateTime)
        .build();
  }

  @Test
  public void testCreateDevice() throws Exception {
    DeviceRequestDto request = DeviceRequestDto.builder().name("device-1").type("sensor").build();
    when(deviceService.createDevice(any(DeviceRequestDto.class))).thenReturn(sampleResponse());

    mockMvc
        .perform(
            post("/api/v2/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("device-1"))
        .andExpect(jsonPath("$.type").value("sensor"))
        .andExpect(jsonPath("$.createdAt").value(formattedFixtureDateTime))
        .andExpect(jsonPath("$.updatedAt").value(formattedFixtureDateTime));
  }

  @Test
  public void testGetDevice() throws Exception {
    when(deviceService.getDevice(anyLong())).thenReturn(sampleResponse());

    mockMvc
        .perform(get("/api/v2/devices/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("device-1"))
        .andExpect(jsonPath("$.type").value("sensor"));
  }

  @Test
  public void testUpdateDevice() throws Exception {
    DeviceRequestDto request =
        DeviceRequestDto.builder().name("device-updated").type("gateway").build();
    DeviceResponseDto response =
        DeviceResponseDto.builder()
            .id(1L)
            .name("device-updated")
            .type("gateway")
            .createdAt(fixtureDateTime)
            .updatedAt(fixtureDateTime)
            .build();
    when(deviceService.updateDevice(anyLong(), any(DeviceRequestDto.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/api/v2/devices/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("device-updated"))
        .andExpect(jsonPath("$.type").value("gateway"));
  }

  @Test
  public void testDeleteDevice() throws Exception {
    mockMvc.perform(delete("/api/v2/devices/1").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  public void testGetDevices() throws Exception {
    Page<DeviceResponseDto> page =
        new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 20), 1);
    when(deviceService.getDevices(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/v2/devices").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].name").value("device-1"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  public void testGetDevicesWithTasks() throws Exception {
    DeviceWithTasksResponseDto withTasks =
        DeviceWithTasksResponseDto.builder()
            .id(1L)
            .name("device-1")
            .type("sensor")
            .createdAt(fixtureDateTime)
            .updatedAt(fixtureDateTime)
            .tasks(
                List.of(
                    TaskSummaryDto.builder()
                        .id(10L)
                        .name("task-a")
                        .description("first task")
                        .build()))
            .build();
    Page<DeviceWithTasksResponseDto> page =
        new PageImpl<>(List.of(withTasks), PageRequest.of(0, 20), 1);
    when(deviceService.getDevicesWithTasks(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/v2/devices/with-tasks").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].tasks[0].id").value(10L))
        .andExpect(jsonPath("$.content[0].tasks[0].name").value("task-a"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  public void testDeviceNotFound() throws Exception {
    when(deviceService.getDevice(anyLong()))
        .thenThrow(new DeviceNotFoundException("Device with id 999 not found"));

    mockMvc
        .perform(get("/api/v2/devices/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Device with id 999 not found"));
  }
}
