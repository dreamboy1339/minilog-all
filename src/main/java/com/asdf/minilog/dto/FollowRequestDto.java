package com.asdf.minilog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;

/**
 * 사용자 팔로우 요청 본문을 담는 DTO.
 *
 * <p>한 사용자가 다른 사용자를 팔로우할 때 서버로 전달하는 요청 페이로드이다.
 */
@Data
public class FollowRequestDto {

  /** 팔로우를 요청하는 사용자 ID. 인증 정보로 대체되어 더 이상 사용하지 않는다(2.0부터 제거 예정). */
  @Deprecated(since = "2.0", forRemoval = true)
  @Schema(
      description = "The ID of the user who wants to follow another user",
      example = "1",
      required = true,
      deprecated = true)
  private Long followerId;

  /** 팔로우 대상이 되는 사용자 ID (필수). */
  @NonNull private Long followeeId;
}
