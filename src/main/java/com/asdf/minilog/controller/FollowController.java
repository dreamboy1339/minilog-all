package com.asdf.minilog.controller;

import com.asdf.minilog.dto.FollowRequestDto;
import com.asdf.minilog.dto.FollowResponseDto;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.FollowService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 팔로우(Follow) 관리 REST 컨트롤러.
 *
 * <p>{@code /api/v2/follow} 경로에서 사용자 간 팔로우/언팔로우와 팔로우 목록 조회를 제공한다. 팔로우/언팔로우는 인증된 사용자를 기준으로 처리된다.
 */
@RestController
@RequestMapping("/api/v2/follow")
public class FollowController {

  private final FollowService followService;

  @Autowired
  public FollowController(FollowService followService) {
    this.followService = followService;
  }

  /**
   * 인증된 사용자가 다른 사용자를 팔로우한다. (POST /api/v2/follow)
   *
   * @param userDetails 인증된 사용자(팔로워) 정보
   * @param request 팔로우 대상(followee) 정보
   * @return 생성된 팔로우 정보
   */
  @PostMapping
  @Operation(summary = "Follow a user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Follow successful"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<FollowResponseDto> follow(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @RequestBody FollowRequestDto request) {
    Long followerId = userDetails.getId();
    Long followingId = request.getFolloweeId();
    FollowResponseDto follow = followService.follow(followerId, followingId);
    return ResponseEntity.ok(follow);
  }

  /**
   * 인증된 사용자가 특정 사용자에 대한 팔로우를 해제한다. (DELETE /api/v2/follow/{followeeId})
   *
   * @param userDetails 인증된 사용자(팔로워) 정보
   * @param followeeId 언팔로우할 대상 사용자 ID
   * @return 본문 없는 200 응답
   */
  @DeleteMapping("/{followeeId}")
  @Operation(summary = "Unfollow a user")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Unfollow successful"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Void> unfollow(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long followeeId) {
    followService.unfollow(userDetails.getId(), followeeId);
    return ResponseEntity.ok().build();
  }

  /**
   * 특정 사용자의 팔로우 목록을 조회한다. (GET /api/v2/follow/{followerId})
   *
   * @param followerId 조회 기준이 되는 사용자 ID
   * @return 팔로우 목록
   */
  @GetMapping("/{followerId}")
  @Operation(summary = "Get followers of a user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<List<FollowResponseDto>> getFollowers(@PathVariable Long followerId) {
    List<FollowResponseDto> follows = followService.getFollowList(followerId);
    return ResponseEntity.ok(follows);
  }
}
