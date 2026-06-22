package com.asdf.minilog.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 게시글(Article) 조회 응답 본문을 담는 DTO.
 *
 * <p>서버가 클라이언트에게 게시글 정보를 내려줄 때 사용하는 응답 페이로드이다. 작성자 이름과 생성 시각 등 표시에 필요한 정보를 포함한다.
 */
@Data
@Builder
public class ArticleResponseDto {

  @NonNull private Long articleId;

  @NonNull private String content;

  @NonNull private Long authorId;

  @NonNull private String authorName;

  @NonNull private LocalDateTime createdAt;
}
