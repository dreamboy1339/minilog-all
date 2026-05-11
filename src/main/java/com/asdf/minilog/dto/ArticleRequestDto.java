package com.asdf.minilog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ArticleRequestDto {

  @NonNull private String content;

  @Deprecated(since = "2.0", forRemoval = true)
  @Schema(
      description = "The ID of the author who created the article",
      example = "1",
      required = true,
      deprecated = true)
  private Long authorId;
}
