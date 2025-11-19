package com.example.jpa.service;

import com.example.jpa.entity.Post;
import com.example.jpa.entity.User;
import com.example.jpa.repository.PostRepository;
import com.example.jpa.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Post Service 클래스
 */
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public Post createPost(Long authorId, String title, String content) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다: " + authorId));

        Post post = new Post(title, content, author);
        return postRepository.save(post);
    }

    /**
     * 게시글 조회 (조회수 증가)
     */
    @Transactional
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

        // 조회수 증가
        post.increaseViewCount();

        return post;
    }

    /**
     * 게시글 조회 (조회수 증가 없이)
     */
    public Post getPostByIdWithoutView(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    /**
     * 모든 게시글 조회
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 발행된 게시글 조회
     */
    public List<Post> getPublishedPosts() {
        return postRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    /**
     * 발행된 게시글 조회 (페이징)
     */
    public Page<Post> getPublishedPosts(Pageable pageable) {
        return postRepository.findByPublishedTrue(pageable);
    }

    /**
     * 제목으로 검색
     */
    public List<Post> searchPostsByTitle(String keyword) {
        return postRepository.findByTitleContaining(keyword);
    }

    /**
     * 제목 또는 내용으로 검색
     */
    public List<Post> searchPosts(String keyword) {
        return postRepository.searchByTitleOrContent(keyword);
    }

    /**
     * 작성자별 게시글 조회
     */
    public List<Post> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    /**
     * 작성자별 게시글 조회 (페이징)
     */
    public Page<Post> getPostsByAuthor(Long authorId, Pageable pageable) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다: " + authorId));
        return postRepository.findByAuthor(author, pageable);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public Post updatePost(Long id, String title, String content) {
        Post post = getPostByIdWithoutView(id);
        post.updateContent(title, content);
        return post;
    }

    /**
     * 게시글 발행
     */
    @Transactional
    public Post publishPost(Long id) {
        Post post = getPostByIdWithoutView(id);
        post.publish();
        return post;
    }

    /**
     * 게시글 발행 취소
     */
    @Transactional
    public Post unpublishPost(Long id) {
        Post post = getPostByIdWithoutView(id);
        post.unpublish();
        return post;
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id);
        }
        postRepository.deleteById(id);
    }

    /**
     * 인기 게시글 조회 (조회수 기준)
     */
    public List<Post> getPopularPosts(Integer minViewCount) {
        return postRepository.findByViewCountGreaterThan(minViewCount);
    }

    /**
     * 특정 기간의 게시글 조회
     */
    public List<Post> getPostsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return postRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * 작성자별 게시글 수
     */
    public long getPostCountByAuthor(Long authorId) {
        return postRepository.countByAuthorId(authorId);
    }

    /**
     * 발행된 게시글 수
     */
    public long getPublishedPostCount() {
        return postRepository.countByPublishedTrue();
    }

    /**
     * 전체 게시글 수
     */
    public long getTotalPostCount() {
        return postRepository.count();
    }
}
