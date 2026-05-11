package com.asdf.minilog.service;

import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.entity.Article;
import com.asdf.minilog.entity.User;
import com.asdf.minilog.exception.ArticleNotFoundException;
import com.asdf.minilog.exception.NotAuthorizedException;
import com.asdf.minilog.exception.UserNotFoundException;
import com.asdf.minilog.repository.ArticleRepository;
import com.asdf.minilog.repository.UserRepository;
import com.asdf.minilog.util.EntityDtoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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

  public void deleteArticle(Long authorId, Long articleId) {
    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(
                () -> {
                  String message = String.format("Article with id %d not found", articleId);
                  return new ArticleNotFoundException(message);
                });

    if (!article.getAuthor().getId().equals(authorId)) {
      throw new NotAuthorizedException("You are not authorized to delete this article");
    }

    articleRepository.deleteById(articleId);
  }

  public ArticleResponseDto updateArticle(Long authorId, Long articleId, String content) {
    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(
                () -> {
                  String message = String.format("Article with id %d not found", articleId);
                  return new ArticleNotFoundException(message);
                });

    if (!article.getAuthor().getId().equals(authorId)) {
      throw new NotAuthorizedException("You are not authorized to update this article");
    }

    article.setContent(content);

    Article updatedArticle = articleRepository.save(article);
    return EntityDtoMapper.toDto(updatedArticle);
  }

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
