package com.asdf.minilog.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 사용자 팔로우 결과 응답 본문을 담는 DTO.
 *
 * <p>팔로우가 생성된 뒤 팔로워와 팔로위 관계를 클라이언트에게 내려줄 때 사용하는 응답 페이로드이다.
 */
@Data
@Builder
public class FollowResponseDto {

  @NonNull private Long followerId;

  @NonNull private Long followeeId;
}
