package com.asdf.minilog.service;

import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.entity.main.Article;
import com.asdf.minilog.entity.main.User;
import com.asdf.minilog.exception.ArticleNotFoundException;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.main.ArticleRepository;
import com.asdf.minilog.repository.main.UserRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글(Article) 도메인의 비즈니스 로직을 담당하는 서비스.
 *
 * <p>게시글의 생성/수정/삭제/조회와 더불어, 팔로우 관계 기반의 피드 조회를 제공한다. 수정·삭제는 작성자 본인만 수행할 수 있도록 권한을 검증한다.
 */
@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class ArticleService {

  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;

  @Autowired
  public ArticleService(ArticleRepository articleRepository, UserRepository userRepository) {
    this.articleRepository = articleRepository;
    this.userRepository = userRepository;
  }

  /**
   * 지정한 사용자를 작성자로 하여 새 게시글을 생성한다.
   *
   * @param content 게시글 본문
   * @param userId 작성자 사용자 ID
   * @return 생성된 게시글 정보
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  public ArticleResponseDto createArticle(String content, Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  String message = String.format("User with id %d not found", userId);
                  return new UserNotFoundException(message);
                });

    Article article = Article.builder().content(content).author(user).build();

    Article savedArticle = articleRepository.save(article);
    return EntityDtoMapper.toDto(savedArticle);
  }

  /**
   * 게시글을 삭제한다. 작성자 본인만 삭제할 수 있다.
   *
   * @param authorId 삭제를 요청한 사용자 ID
   * @param articleId 삭제할 게시글 ID
   * @throws ArticleNotFoundException 게시글을 찾을 수 없는 경우
   * @throws NotAuthorizedException 요청자가 작성자가 아닌 경우
   */
  public void deleteArticle(Long authorId, Long articleId) {
    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(
                () -> {
                  String message = String.format("Article with id %d not found", articleId);
                  return new ArticleNotFoundException(message);
                });

    // 작성자 본인만 삭제 가능하도록 권한 검증
    if (!article.getAuthor().getId().equals(authorId)) {
      throw new NotAuthorizedException("You are not authorized to delete this article");
    }

    articleRepository.deleteById(articleId);
  }

  /**
   * 게시글 본문을 수정한다. 작성자 본인만 수정할 수 있다.
   *
   * @param authorId 수정을 요청한 사용자 ID
   * @param articleId 수정할 게시글 ID
   * @param content 변경할 본문
   * @return 수정된 게시글 정보
   * @throws ArticleNotFoundException 게시글을 찾을 수 없는 경우
   * @throws NotAuthorizedException 요청자가 작성자가 아닌 경우
   */
  public ArticleResponseDto updateArticle(Long authorId, Long articleId, String content) {
    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(
                () -> {
                  String message = String.format("Article with id %d not found", articleId);
                  return new ArticleNotFoundException(message);
                });

    // 작성자 본인만 수정 가능하도록 권한 검증
    if (!article.getAuthor().getId().equals(authorId)) {
      throw new NotAuthorizedException("You are not authorized to update this article");
    }

    article.setContent(content);

    Article updatedArticle = articleRepository.save(article);
    return EntityDtoMapper.toDto(updatedArticle);
  }

  /**
   * 단건 게시글을 조회한다.
   *
   * @param articleId 조회할 게시글 ID
   * @return 게시글 정보
   * @throws ArticleNotFoundException 게시글을 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public ArticleResponseDto getArticleById(Long articleId) {
    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(
                () -> {
                  String message = String.format("Article with id %d not found", articleId);
                  return new ArticleNotFoundException(message);
                });

    return EntityDtoMapper.toDto(article);
  }

  /**
   * 특정 사용자가 팔로우한 사람들의 게시글 피드 목록을 조회한다.
   *
   * @param userId 피드를 조회할 사용자(팔로워) ID
   * @return 팔로우 대상들의 게시글 목록
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public List<ArticleResponseDto> getFeedListByFollowerId(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  String message = String.format("User with id %d not found", userId);
                  return new UserNotFoundException(message);
                });

    var feedList = articleRepository.findAllByFollowerId(user.getId());
    return feedList.stream().map(EntityDtoMapper::toDto).toList();
  }

  /**
   * 특정 사용자가 작성한 게시글 목록을 조회한다.
   *
   * @param userId 작성자 사용자 ID
   * @return 해당 사용자가 작성한 게시글 목록
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public List<ArticleResponseDto> getArticleListByUserId(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  String message = String.format("User with id %d not found", userId);
                  return new UserNotFoundException(message);
                });

    var articleList = articleRepository.findAllByAuthorId(user.getId());
    return articleList.stream().map(EntityDtoMapper::toDto).toList();
  }
}
