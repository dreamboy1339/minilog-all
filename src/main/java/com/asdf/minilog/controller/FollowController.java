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

@RestController
@RequestMapping("/api/v2/follow")
public class FollowController {

  private final FollowService followService;

  @Autowired
  public FollowController(FollowService followService) {
    this.followService = followService;
  }

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
