package com.asdf.minilog.controller;

import com.asdf.minilog.dto.ArticleRequestDto;
import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/article")
public class ArticleController {

  private final ArticleService articleService;

  @Autowired
  public ArticleController(ArticleService articleService) {
    this.articleService = articleService;
  }

  @PostMapping
  @Operation(summary = "Create a new article")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Article created successfully"),
    @ApiResponse(responseCode = "404", description = "Article not found")
  })
  public ResponseEntity<ArticleResponseDto> createArticle(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @RequestBody ArticleRequestDto article) {
    ArticleResponseDto createdArticle =
        articleService.createArticle(article.getContent(), userDetails.getId());
    return ResponseEntity.ok(createdArticle);
  }

  @GetMapping("/{articleId}")
  @Operation(summary = "Get article by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Article not found")
  })
  public ResponseEntity<ArticleResponseDto> getArticle(@PathVariable Long articleId) {
    var article = articleService.getArticleById(articleId);
    return ResponseEntity.ok(article);
  }

  @PutMapping("/{articleId}")
  @Operation(summary = "Update article")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Article not found")
  })
  public ResponseEntity<ArticleResponseDto> updateArticle(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long articleId,
      @RequestBody ArticleRequestDto article) {
    var updatedArticle =
        articleService.updateArticle(userDetails.getId(), articleId, article.getContent());
    return ResponseEntity.ok(updatedArticle);
  }

  @DeleteMapping("/{articleId}")
  @Operation(summary = "Delete article")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Article not found")
  })
  public ResponseEntity<Void> deleteArticle(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long articleId) {
    articleService.deleteArticle(userDetails.getId(), articleId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get articles by user id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Article not found")
  })
  public ResponseEntity<List<ArticleResponseDto>> getArticleByUserId(@RequestParam Long authorId) {
    var articles = articleService.getArticleListByUserId(authorId);
    return ResponseEntity.ok(articles);
  }
}
