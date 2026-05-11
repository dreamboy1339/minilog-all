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

@RestController
@RequestMapping("/api/v2/feed")
public class FeedController {

  private final ArticleService articleService;

  @Autowired
  public FeedController(ArticleService articleService) {
    this.articleService = articleService;
  }

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
