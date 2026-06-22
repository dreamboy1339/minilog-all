package com.asdf.minilog.repository.main;

import com.asdf.minilog.entity.main.Article;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** 게시글(Article) 엔티티를 관리하는 리포지토리. */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
  /**
   * 특정 작성자가 작성한 모든 게시글을 조회한다.
   *
   * @param authorId 작성자 ID
   * @return 해당 작성자의 게시글 목록
   */
  List<Article> findAllByAuthorId(Long authorId);

  /**
   * 특정 사용자가 팔로우하는 사람들이 작성한 게시글을 최신순으로 조회한다. Article-Follow를 JOIN하여 팔로잉 대상의 글만 가져온다.
   *
   * @param authorId 조회 기준이 되는 팔로워(현재 사용자) ID
   * @return 팔로잉 대상들이 작성한 게시글 목록(작성일 내림차순)
   */
  @Query(
      """
      SELECT a
      FROM Article a
          JOIN a.author u JOIN Follow f ON u.id = f.followee.id
      WHERE f.follower.id = :authorId
      ORDER BY a.createdAt DESC
      """)
  List<Article> findAllByFollowerId(@Param("authorId") Long authorId);
}
