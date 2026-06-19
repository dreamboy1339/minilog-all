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

/**
 * 게시글(Article) 관리 REST 컨트롤러.
 *
 * <p>{@code /api/v2/article} 경로에서 게시글의 작성, 단건 조회, 수정, 삭제와 작성자별 목록 조회를 제공한다. 작성/수정/삭제는 인증된 사용자 정보를
 * 기준으로 처리된다.
 */
@RestController
@RequestMapping("/api/v2/article")
public class ArticleController {

  private final ArticleService articleService;

  @Autowired
  public ArticleController(ArticleService articleService) {
    this.articleService = articleService;
  }

  /**
   * 새 게시글을 작성한다. (POST /api/v2/article)
   *
   * @param userDetails 인증된 작성자 정보
   * @param article 작성할 게시글 내용
   * @return 생성된 게시글 정보
   */
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

  /**
   * 게시글 ID로 단건 게시글을 조회한다. (GET /api/v2/article/{articleId})
   *
   * @param articleId 조회할 게시글 ID
   * @return 게시글 정보
   */
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

  /**
   * 게시글을 수정한다. (PUT /api/v2/article/{articleId})
   *
   * @param userDetails 인증된 사용자 정보(작성자 검증용)
   * @param articleId 수정할 게시글 ID
   * @param article 수정할 게시글 내용
   * @return 수정된 게시글 정보
   */
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

  /**
   * 게시글을 삭제한다. (DELETE /api/v2/article/{articleId})
   *
   * @param userDetails 인증된 사용자 정보(작성자 검증용)
   * @param articleId 삭제할 게시글 ID
   * @return 본문 없는 204 응답
   */
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

  /**
   * 특정 작성자가 쓴 게시글 목록을 조회한다. (GET /api/v2/article?authorId=)
   *
   * @param authorId 작성자 사용자 ID
   * @return 해당 작성자의 게시글 목록
   */
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
