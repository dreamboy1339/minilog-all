package com.asdf.minilog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;

@Data
public class FollowRequestDto {

  @Deprecated(since = "2.0", forRemoval = true)
  @Schema(
      description = "The ID of the user who wants to follow another user",
      example = "1",
      required = true,
      deprecated = true)
  private Long followerId;

  @NonNull private Long followeeId;
}
