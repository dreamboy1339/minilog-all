package com.asdf.minilog.controller;

import com.asdf.minilog.dto.UserRequestDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.security.MinilogUserDetails;
import com.asdf.minilog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자(User) 관리 REST 컨트롤러.
 *
 * <p>{@code /api/v2/user} 경로에서 사용자의 전체 목록 조회, 단건 조회, 생성, 수정, 삭제를 제공한다. 삭제는 ADMIN 권한이 필요하다.
 */
@RestController
@RequestMapping("/api/v2/user")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 전체 사용자 목록을 조회한다. (GET /api/v2/user)
   *
   * @return 전체 사용자 목록
   */
  @GetMapping
  @Operation(summary = "Get all users")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Iterable<UserResponseDto>> getUsers() {
    return ResponseEntity.ok(userService.getUsers());
  }

  /**
   * 사용자 ID로 단건 사용자를 조회한다. 없으면 404를 반환한다. (GET /api/v2/user/{userId})
   *
   * @param userId 조회할 사용자 ID
   * @return 사용자 정보 또는 404 응답
   */
  @GetMapping("/{userId}")
  @Operation(summary = "Get user by id")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
    return userService
        .getUserById(userId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * 새 사용자를 생성한다. (POST /api/v2/user)
   *
   * @param user 생성할 사용자 정보
   * @return 생성된 사용자 정보
   */
  @PostMapping
  @Operation(summary = "Create user")
  @ApiResponses({@ApiResponse(responseCode = "201", description = "Created")})
  public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto user) {
    UserResponseDto createdUser = userService.createUser(user);
    return ResponseEntity.ok(createdUser);
  }

  /**
   * 사용자 정보를 수정한다. (PUT /api/v2/user/{userId})
   *
   * @param userDetails 인증된 사용자 정보(본인 검증용)
   * @param userId 수정할 사용자 ID
   * @param updatedUser 수정할 사용자 정보
   * @return 수정된 사용자 정보
   */
  @PutMapping("/{userId}")
  @Operation(summary = "Update user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<UserResponseDto> updateUser(
      @Parameter(hidden = true) @AuthenticationPrincipal MinilogUserDetails userDetails,
      @PathVariable Long userId,
      @RequestBody UserRequestDto updatedUser) {
    UserResponseDto user = userService.updateUser(userDetails, userId, updatedUser);
    return ResponseEntity.ok(user);
  }

  /**
   * 사용자를 삭제한다. ADMIN 권한이 필요하다. (DELETE /api/v2/user/{userId})
   *
   * @param userId 삭제할 사용자 ID
   * @return 본문 없는 204 응답
   */
  @PreAuthorize("hasRole('ADMIN')") // Only admins can delete users
  @DeleteMapping("/{userId}")
  @Operation(summary = "Delete user")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No content")
  })
  public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }
}
