package com.asdf.minilog.controller;

import com.asdf.minilog.dto.UserRequestDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.entity.Role;
import com.asdf.minilog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/reviewers")
public class ReviewerController {

  private final UserService userService;

  @Autowired
  public ReviewerController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  @Operation(summary = "Get reviewers")
  public ResponseEntity<List<UserResponseDto>> getReviewers() {
    return ResponseEntity.ok(userService.getUsersByRole(Role.ROLE_REVIEWER));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  @Operation(summary = "Create reviewer")
  public ResponseEntity<UserResponseDto> createReviewer(@RequestBody UserRequestDto request) {
    return ResponseEntity.ok(userService.createUserWithRole(request, Role.ROLE_REVIEWER));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{userId}")
  @Operation(summary = "Add reviewer role")
  public ResponseEntity<UserResponseDto> addReviewer(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.addRole(userId, Role.ROLE_REVIEWER));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{userId}")
  @Operation(summary = "Update reviewer")
  public ResponseEntity<UserResponseDto> updateReviewer(
      @PathVariable Long userId, @RequestBody UserRequestDto request) {
    return ResponseEntity.ok(userService.updateUserWithRole(userId, request, Role.ROLE_REVIEWER));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{userId}")
  @Operation(summary = "Remove reviewer role")
  public ResponseEntity<Void> removeReviewer(@PathVariable Long userId) {
    userService.removeRole(userId, Role.ROLE_REVIEWER);
    return ResponseEntity.noContent().build();
  }
}
