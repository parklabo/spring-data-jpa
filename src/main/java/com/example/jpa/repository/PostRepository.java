package com.example.jpa.repository;

import com.example.jpa.entity.Post;
import com.example.jpa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Post Repository 인터페이스
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 제목으로 검색
    List<Post> findByTitleContaining(String keyword);

    // 작성자로 검색
    List<Post> findByAuthor(User author);

    // 작성자 ID로 검색
    List<Post> findByAuthorId(Long authorId);

    // 발행된 게시글 조회
    List<Post> findByPublishedTrue();

    // 발행되지 않은 게시글 조회
    List<Post> findByPublishedFalse();

    // 발행된 게시글을 생성일 내림차순으로 조회
    List<Post> findByPublishedTrueOrderByCreatedAtDesc();

    // 제목으로 검색 (페이징)
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    // 발행된 게시글 페이징 조회
    Page<Post> findByPublishedTrue(Pageable pageable);

    // 작성자별 게시글 조회 (페이징)
    Page<Post> findByAuthor(User author, Pageable pageable);

    // 특정 기간 내 작성된 게시글
    List<Post> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 조회수가 특정 값보다 큰 게시글
    List<Post> findByViewCountGreaterThan(Integer viewCount);

    // 작성자와 발행 상태로 검색
    List<Post> findByAuthorAndPublished(User author, Boolean published);

    // @Query 사용: 작성자 정보를 함께 조회 (N+1 문제 해결)
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.published = true")
    List<Post> findAllPublishedWithAuthor();

    // @Query 사용: 댓글과 작성자를 함께 조회
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.comments " +
           "LEFT JOIN FETCH p.author " +
           "WHERE p.id = :id")
    Post findPostWithCommentsAndAuthor(@Param("id") Long id);

    // 특정 키워드를 제목 또는 내용에 포함하는 게시글 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Post> searchByTitleOrContent(@Param("keyword") String keyword);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int incrementViewCount(@Param("id") Long id);

    // 작성자별 게시글 수 조회
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);

    // 발행된 게시글 수
    long countByPublishedTrue();
}
