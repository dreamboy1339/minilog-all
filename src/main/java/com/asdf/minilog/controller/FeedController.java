package com.asdf.minilog.controller;

import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 피드(Feed) 조회 REST 컨트롤러.
 *
 * <p>{@code /api/v2/feed} 경로에서 특정 사용자가 팔로우하는 대상들의 게시글을 모아 피드 형태로 조회한다.
 */
@RestController
@RequestMapping("/api/v2/feed")
public class FeedController {

  private final ArticleService articleService;

  @Autowired
  public FeedController(ArticleService articleService) {
    this.articleService = articleService;
  }

  /**
   * 팔로워 ID 기준으로 팔로우 중인 사용자들의 게시글 피드를 조회한다. (GET /api/v2/feed?followerId=)
   *
   * @param followerId 피드를 조회할 팔로워(사용자) ID
   * @return 팔로우 대상들의 게시글 목록
   */
  @GetMapping
  @Operation(summary = "Get feeds by follower id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not Found")
  })
  public ResponseEntity<List<ArticleResponseDto>> getFeeds(@RequestParam Long followerId) {
    List<ArticleResponseDto> feedList = articleService.getFeedListByFollowerId(followerId);
    return ResponseEntity.ok(feedList);
  }
}
