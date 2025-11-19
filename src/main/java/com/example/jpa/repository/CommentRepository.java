package com.example.jpa.repository;

import com.example.jpa.entity.Comment;
import com.example.jpa.entity.Post;
import com.example.jpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment Repository 인터페이스
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 조회
    List<Comment> findByPost(Post post);

    // 게시글 ID로 댓글 조회
    List<Comment> findByPostId(Long postId);

    // 사용자별 댓글 조회
    List<Comment> findByUser(User user);

    // 사용자 ID로 댓글 조회
    List<Comment> findByUserId(Long userId);

    // 게시글별 댓글을 생성일 오름차순으로 조회
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    // 특정 게시글의 댓글 수 조회
    long countByPost(Post post);

    long countByPostId(Long postId);

    // 특정 사용자의 댓글 수 조회
    long countByUserId(Long userId);

    // 게시글과 작성자 정보를 함께 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.post WHERE c.post.id = :postId")
    List<Comment> findCommentsWithUserAndPost(@Param("postId") Long postId);
}
