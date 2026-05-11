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

import com.asdf.minilog.dto.ArticleRequestDto;
import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.exception.ArticleNotFoundException;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.ArticleService;
import com.asdf.minilog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ArticleController.class) // 웹 레이어만 테스트
public class ArticleControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ArticleService articleService;

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

    // set mock user authentication
    MinilogUserDetails userDetails =
        new MinilogUserDetails(1L, "Test User", "password", List.of(() -> "ROLE_AUTHOR"));
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void testCreateArticle() throws Exception {
    ArticleRequestDto requestDto = ArticleRequestDto.builder().content("Test Content").build();
    ArticleResponseDto responseDto =
        ArticleResponseDto.builder()
            .articleId(1L)
            .content("Test Content")
            .authorId(1L)
            .authorName("Test User")
            .createdAt(fixtureDateTime)
            .build();
    when(articleService.createArticle(any(String.class), anyLong())).thenReturn(responseDto);

    mockMvc
        .perform(
            post("/api/v2/article")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.articleId").value(1L))
        .andExpect(jsonPath("$.content").value("Test Content"))
        .andExpect(jsonPath("$.authorId").value(1L))
        .andExpect(jsonPath("$.authorName").value("Test User"))
        .andExpect(jsonPath("$.createdAt").value(formattedFixtureDateTime));
  }

  @Test
  public void testGetArticle() throws Exception {

    ArticleResponseDto responseDto =
        ArticleResponseDto.builder()
            .articleId(1L)
            .content("Test Content")
            .authorId(1L)
            .authorName("Test User")
            .createdAt(fixtureDateTime)
            .build();
    when(articleService.getArticleById(anyLong())).thenReturn(responseDto);

    mockMvc
        .perform(get("/api/v2/article/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.articleId").value(1L))
        .andExpect(jsonPath("$.content").value("Test Content"))
        .andExpect(jsonPath("$.authorId").value(1L))
        .andExpect(jsonPath("$.authorName").value("Test User"))
        .andExpect(jsonPath("$.createdAt").value(formattedFixtureDateTime));
  }

  @Test
  public void testUpdateArticle() throws Exception {
    ArticleRequestDto requestDto = ArticleRequestDto.builder().content("Test Content").build();
    ArticleResponseDto responseDto =
        ArticleResponseDto.builder()
            .articleId(1L)
            .content("Updated Content")
            .authorId(1L)
            .authorName("Test User")
            .createdAt(fixtureDateTime)
            .build();
    when(articleService.updateArticle(anyLong(), anyLong(), any(String.class)))
        .thenReturn(responseDto);

    mockMvc
        .perform(
            put("/api/v2/article/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.articleId").value(1L))
        .andExpect(jsonPath("$.content").value("Updated Content"))
        .andExpect(jsonPath("$.authorId").value(1L))
        .andExpect(jsonPath("$.authorName").value("Test User"))
        .andExpect(jsonPath("$.createdAt").value(formattedFixtureDateTime));
  }

  @Test
  public void testDeleteArticle() throws Exception {
    mockMvc.perform(delete("/api/v2/article/1").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  public void testGetArticleByUserId() throws Exception {
    ArticleResponseDto responseDto =
        ArticleResponseDto.builder()
            .articleId(1L)
            .content("Test Content")
            .authorId(1L)
            .authorName("Test User")
            .createdAt(fixtureDateTime)
            .build();
    List<ArticleResponseDto> responseList = Collections.singletonList(responseDto);
    when(articleService.getArticleListByUserId(anyLong())).thenReturn(responseList);

    mockMvc
        .perform(get("/api/v2/article").param("authorId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].articleId").value(1L))
        .andExpect(jsonPath("$[0].content").value("Test Content"))
        .andExpect(jsonPath("$[0].authorId").value(1L))
        .andExpect(jsonPath("$[0].authorName").value("Test User"))
        .andExpect(jsonPath("$[0].createdAt").value(formattedFixtureDateTime));
  }

  @Test
  public void testGlobalExceptionHandler() throws Exception {
    when(articleService.getArticleById(anyLong()))
        .thenThrow(new ArticleNotFoundException("Article Not Found"));

    mockMvc
        .perform(get("/api/v2/article/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Article Not Found"));
  }
}
