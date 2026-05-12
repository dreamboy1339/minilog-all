package com.asdf.minilog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asdf.minilog.dto.TaskReportRequestDto;
import com.asdf.minilog.dto.TaskReportResponseDto;
import com.asdf.minilog.dto.TaskReportSubmitRequestDto;
import com.asdf.minilog.dto.TaskReportUpdateRequestDto;
import com.asdf.minilog.entity.ReportStatus;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.TaskReportService;
import com.asdf.minilog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskReportController.class)
class TaskReportControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TaskReportService taskReportService;

  @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private JwtUtil jwtUtil;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final LocalDateTime fixture = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

  @BeforeEach
  void setUp() {
    //noinspection resource
    MockitoAnnotations.openMocks(this);

    MinilogUserDetails userDetails =
        new MinilogUserDetails(1L, "author", "password", List.of(() -> "ROLE_AUTHOR"));
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void createReport_returnsCreatedReport() throws Exception {
    when(taskReportService.createReport(anyLong(), any(TaskReportRequestDto.class)))
        .thenReturn(response(ReportStatus.DRAFT));

    mockMvc
        .perform(
            post("/api/v2/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        TaskReportRequestDto.builder().taskId(10L).content("report").build()))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100L))
        .andExpect(jsonPath("$.taskId").value(10L))
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  void getReports_returnsAllReports() throws Exception {
    when(taskReportService.getReports()).thenReturn(List.of(response(ReportStatus.SUBMITTED)));

    mockMvc
        .perform(get("/api/v2/reports"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(100L))
        .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
  }

  @Test
  void updateReport_returnsUpdatedReport() throws Exception {
    when(taskReportService.updateReport(
            anyLong(), anyLong(), any(TaskReportUpdateRequestDto.class)))
        .thenReturn(response(ReportStatus.SUBMITTED));

    mockMvc
        .perform(
            put("/api/v2/reports/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        TaskReportUpdateRequestDto.builder().content("updated").build()))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUBMITTED"));
  }

  @Test
  void deleteReport_returnsNoContent() throws Exception {
    mockMvc.perform(delete("/api/v2/reports/100").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  void submitReport_returnsSubmitted() throws Exception {
    when(taskReportService.submitReport(
            anyLong(), anyLong(), any(TaskReportSubmitRequestDto.class)))
        .thenReturn(response(ReportStatus.SUBMITTED));

    mockMvc
        .perform(
            post("/api/v2/reports/100/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        TaskReportSubmitRequestDto.builder().reviewerId(2L).approverId(3L).build()))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUBMITTED"));
  }

  @Test
  void workflowEndpoints_returnStatus() throws Exception {
    when(taskReportService.startReview(anyLong(), anyLong()))
        .thenReturn(response(ReportStatus.REVIEW));
    when(taskReportService.completeReview(anyLong(), anyLong()))
        .thenReturn(response(ReportStatus.APPROVAL));
    when(taskReportService.approveReport(anyLong(), anyLong()))
        .thenReturn(response(ReportStatus.APPROVED));

    mockMvc
        .perform(post("/api/v2/reports/100/start-review").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REVIEW"));
    mockMvc
        .perform(post("/api/v2/reports/100/complete-review").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVAL"));
    mockMvc
        .perform(post("/api/v2/reports/100/approve").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"));
  }

  @Test
  void notAuthorized_returnsForbidden() throws Exception {
    when(taskReportService.getReport(100L))
        .thenThrow(new NotAuthorizedException("You are not authorized"));

    mockMvc.perform(get("/api/v2/reports/100")).andExpect(status().isForbidden());
  }

  private TaskReportResponseDto response(ReportStatus status) {
    return TaskReportResponseDto.builder()
        .id(100L)
        .taskId(10L)
        .authorId(1L)
        .authorName("author")
        .reviewerId(2L)
        .reviewerName("reviewer")
        .approverId(3L)
        .approverName("approver")
        .content("report")
        .status(status)
        .createdAt(fixture)
        .updatedAt(fixture)
        .build();
  }
}
