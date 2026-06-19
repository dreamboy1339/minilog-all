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

/**
 * 승인자(Approver) 관리 REST 컨트롤러.
 *
 * <p>{@code /api/v2/approvers} 경로에서 승인자 권한을 가진 사용자의 조회, 생성, 권한 부여/회수 등을 제공한다. 조회를 제외한 변경 작업은 ADMIN
 * 권한이 필요하다.
 */
@RestController
@RequestMapping("/api/v2/approvers")
public class ApproverController {

  private final UserService userService;

  @Autowired
  public ApproverController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 승인자 권한을 가진 사용자 목록을 조회한다. (GET /api/v2/approvers)
   *
   * @return 승인자 사용자 목록
   */
  @GetMapping
  @Operation(summary = "Get approvers")
  public ResponseEntity<List<UserResponseDto>> getApprovers() {
    return ResponseEntity.ok(userService.getUsersByRole(Role.ROLE_APPROVER));
  }

  /**
   * 승인자 권한을 가진 새 사용자를 생성한다. (POST /api/v2/approvers, ADMIN 전용)
   *
   * @param request 생성할 사용자 정보
   * @return 생성된 승인자 정보
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  @Operation(summary = "Create approver")
  public ResponseEntity<UserResponseDto> createApprover(@RequestBody UserRequestDto request) {
    return ResponseEntity.ok(userService.createUserWithRole(request, Role.ROLE_APPROVER));
  }

  /**
   * 기존 사용자에게 승인자 권한을 추가한다. (POST /api/v2/approvers/{userId}, ADMIN 전용)
   *
   * @param userId 권한을 부여할 사용자 ID
   * @return 권한이 추가된 사용자 정보
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{userId}")
  @Operation(summary = "Add approver role")
  public ResponseEntity<UserResponseDto> addApprover(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.addRole(userId, Role.ROLE_APPROVER));
  }

  /**
   * 승인자 사용자의 정보를 수정한다. (PUT /api/v2/approvers/{userId}, ADMIN 전용)
   *
   * @param userId 수정할 사용자 ID
   * @param request 수정할 사용자 정보
   * @return 수정된 승인자 정보
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{userId}")
  @Operation(summary = "Update approver")
  public ResponseEntity<UserResponseDto> updateApprover(
      @PathVariable Long userId, @RequestBody UserRequestDto request) {
    return ResponseEntity.ok(userService.updateUserWithRole(userId, request, Role.ROLE_APPROVER));
  }

  /**
   * 사용자에게서 승인자 권한을 회수한다. (DELETE /api/v2/approvers/{userId}, ADMIN 전용)
   *
   * @param userId 권한을 회수할 사용자 ID
   * @return 본문 없는 204 응답
   */
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{userId}")
  @Operation(summary = "Remove approver role")
  public ResponseEntity<Void> removeApprover(@PathVariable Long userId) {
    userService.removeRole(userId, Role.ROLE_APPROVER);
    return ResponseEntity.noContent().build();
  }
}
