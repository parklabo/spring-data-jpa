package com.example.jpa.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity 클래스
 *
 * 주요 어노테이션:
 * - @Entity: JPA Entity임을 선언
 * - @Table: 실제 데이터베이스 테이블 이름 지정
 * - @EntityListeners: Auditing 기능을 위한 리스너 등록
 * - @Id: 기본 키 지정
 * - @GeneratedValue: 기본 키 자동 생성 전략
 * - @Column: 컬럼의 세부 속성 설정
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_username", columnList = "username")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 일대다 관계: 한 명의 사용자는 여러 게시글을 작성할 수 있음
     * mappedBy: 연관관계의 주인이 Post.author임을 명시
     * cascade: User가 삭제되면 작성한 Post도 모두 삭제
     * orphanRemoval: 부모와의 관계가 끊어진 자식 엔티티 삭제
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA는 기본 생성자를 필요로 함 (protected 또는 public)
    protected User() {
    }

    public User(String username, String email, Integer age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }

    public User(String username, String email, String phoneNumber, Integer age) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.age = age;
    }

    // 비즈니스 메서드
    public void updateProfile(String username, String phoneNumber) {
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }

    // 연관관계 편의 메서드
    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setAuthor(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 사용자 상태를 나타내는 Enum
     */
    public enum UserStatus {
        ACTIVE,    // 활성
        INACTIVE,  // 비활성
        BANNED     // 정지
    }
}
