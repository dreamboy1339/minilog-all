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

@RestController
@RequestMapping("/api/v2/user")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @Operation(summary = "Get all users")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "OK")})
  public ResponseEntity<Iterable<UserResponseDto>> getUsers() {
    return ResponseEntity.ok(userService.getUsers());
  }

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

  @PostMapping
  @Operation(summary = "Create user")
  @ApiResponses({@ApiResponse(responseCode = "201", description = "Created")})
  public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto user) {
    UserResponseDto createdUser = userService.createUser(user);
    return ResponseEntity.ok(createdUser);
  }

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
