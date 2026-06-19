package com.asdf.minilog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 게시글(Article) 생성/수정 요청 본문을 담는 DTO.
 *
 * <p>클라이언트가 게시글을 작성할 때 서버로 전달하는 요청 페이로드이다.
 */
@Data
@Builder
public class ArticleRequestDto {

  /** 게시글 본문 내용 (필수). */
  @NonNull private String content;

  /** 작성자 ID. 인증 정보로 대체되어 더 이상 사용하지 않는다(2.0부터 제거 예정). */
  @Deprecated(since = "2.0", forRemoval = true)
  @Schema(
      description = "The ID of the author who created the article",
      example = "1",
      required = true,
      deprecated = true)
  private Long authorId;
}
