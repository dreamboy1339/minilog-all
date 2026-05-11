package com.asdf.minilog.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.service.ArticleService;
import com.asdf.minilog.util.JwtUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FeedController.class)
@WithMockUser(username = "Test User")
public class FeedControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ArticleService articleService;

  @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private JwtUtil jwtUtil;

  private final LocalDateTime fixtureDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private final String formattedFixtureDateTime = fixtureDateTime.format(formatter);

  @BeforeEach
  public void setup() {
    //noinspection resource
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetFeedList() throws Exception {
    ArticleResponseDto articleResponseDto =
        ArticleResponseDto.builder()
            .articleId(2L)
            .content("Test Content2")
            .authorId(2L)
            .authorName("Test User2")
            .createdAt(fixtureDateTime)
            .build();
    when(articleService.getFeedListByFollowerId(anyLong()))
        .thenReturn(Collections.singletonList(articleResponseDto));

    mockMvc
        .perform(get("/api/v2/feed?followerId=1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].articleId").value(2L))
        .andExpect(jsonPath("$[0].content").value("Test Content2"))
        .andExpect(jsonPath("$[0].authorId").value(2L))
        .andExpect(jsonPath("$[0].authorName").value("Test User2"))
        .andExpect(jsonPath("$[0].createdAt").value(formattedFixtureDateTime));
  }
}
