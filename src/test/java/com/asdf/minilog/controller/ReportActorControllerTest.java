package com.asdf.minilog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asdf.minilog.dto.UserRequestDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.entity.main.Role;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.UserService;
import com.asdf.minilog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
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

@WebMvcTest({ReviewerController.class, ApproverController.class})
class ReportActorControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private JwtUtil jwtUtil;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    //noinspection resource
    MockitoAnnotations.openMocks(this);

    MinilogUserDetails userDetails =
        new MinilogUserDetails(1L, "admin", "password", List.of(() -> "ROLE_ADMIN"));
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void getReviewers_returnsReviewerUsers() throws Exception {
    when(userService.getUsersByRole(Role.ROLE_REVIEWER))
        .thenReturn(List.of(user(2L, "reviewer", Role.ROLE_REVIEWER)));

    mockMvc
        .perform(get("/api/v2/reviewers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(2L))
        .andExpect(jsonPath("$[0].roles[0]").value("ROLE_REVIEWER"));
  }

  @Test
  void createReviewer_returnsCreatedReviewer() throws Exception {
    when(userService.createUserWithRole(any(UserRequestDto.class), any(Role.class)))
        .thenReturn(user(2L, "reviewer", Role.ROLE_REVIEWER));

    mockMvc
        .perform(
            post("/api/v2/reviewers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        UserRequestDto.builder().username("reviewer").password("password").build()))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2L))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_REVIEWER"));
  }

  @Test
  void addApprover_returnsUpdatedUser() throws Exception {
    when(userService.addRole(anyLong(), any(Role.class)))
        .thenReturn(user(3L, "approver", Role.ROLE_APPROVER));

    mockMvc
        .perform(post("/api/v2/approvers/3").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roles[0]").value("ROLE_APPROVER"));
  }

  @Test
  void removeApprover_returnsNoContent() throws Exception {
    mockMvc.perform(delete("/api/v2/approvers/3").with(csrf())).andExpect(status().isNoContent());
  }

  private UserResponseDto user(Long id, String username, Role role) {
    return UserResponseDto.builder().id(id).username(username).roles(Set.of(role)).build();
  }
}
