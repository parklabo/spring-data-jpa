package com.example.jpa.repository;

import com.example.jpa.entity.User;
import com.example.jpa.entity.User.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository 인터페이스
 *
 * JpaRepository를 상속받아 기본 CRUD 메서드를 자동으로 사용 가능
 * - save(entity): 저장 및 수정
 * - findById(id): ID로 조회
 * - findAll(): 전체 조회
 * - delete(entity): 삭제
 * - count(): 개수 조회
 *
 * 추가로 메서드 이름 규칙을 따라 쿼리 메서드를 정의하면
 * Spring Data JPA가 자동으로 쿼리를 생성해줌
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========== 기본 쿼리 메서드 ==========

    /**
     * 이메일로 사용자 찾기
     * SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * 사용자명으로 검색 (부분 일치)
     * SELECT * FROM users WHERE username LIKE %?%
     */
    List<User> findByUsernameContaining(String keyword);

    /**
     * 사용자명으로 존재 여부 확인
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    // ========== 복합 조건 쿼리 ==========

    /**
     * 사용자명과 이메일로 검색
     * SELECT * FROM users WHERE username = ? AND email = ?
     */
    Optional<User> findByUsernameAndEmail(String username, String email);

    /**
     * 나이 범위로 검색
     * SELECT * FROM users WHERE age BETWEEN ? AND ?
     */
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);

    /**
     * 나이가 특정 값보다 큰 사용자 검색
     * SELECT * FROM users WHERE age > ?
     */
    List<User> findByAgeGreaterThan(Integer age);

    /**
     * 나이가 특정 값보다 작은 사용자 검색
     */
    List<User> findByAgeLessThan(Integer age);

    // ========== 상태 기반 쿼리 ==========

    /**
     * 상태로 사용자 검색
     * SELECT * FROM users WHERE status = ?
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 활성화된 사용자 수 조회
     */
    long countByStatus(UserStatus status);

    /**
     * 상태가 아닌 사용자 검색
     * SELECT * FROM users WHERE status != ?
     */
    List<User> findByStatusNot(UserStatus status);

    // ========== 날짜 기반 쿼리 ==========

    /**
     * 특정 날짜 이후에 생성된 사용자
     * SELECT * FROM users WHERE created_at > ?
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * 특정 날짜 이전에 생성된 사용자
     */
    List<User> findByCreatedAtBefore(LocalDateTime date);

    /**
     * 특정 기간 내 생성된 사용자
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // ========== 정렬 쿼리 ==========

    /**
     * 모든 사용자를 사용자명 내림차순으로 조회
     * SELECT * FROM users ORDER BY username DESC
     */
    List<User> findAllByOrderByUsernameDesc();

    /**
     * 모든 사용자를 생성일 내림차순으로 조회
     */
    List<User> findAllByOrderByCreatedAtDesc();

    /**
     * 상태별 사용자를 생성일 내림차순으로 조회
     */
    List<User> findByStatusOrderByCreatedAtDesc(UserStatus status);

    // ========== 페이징 쿼리 ==========

    /**
     * 사용자명으로 검색 (페이징)
     * Pageable을 통해 페이지 번호, 크기, 정렬 조건 설정 가능
     */
    Page<User> findByUsernameContaining(String keyword, Pageable pageable);

    /**
     * 상태별 사용자 검색 (페이징)
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    // ========== @Query 어노테이션 사용 ==========

    /**
     * JPQL을 사용한 커스텀 쿼리
     * JPQL은 Entity 객체를 대상으로 쿼리를 작성
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmail(@Param("email") String email);

    /**
     * Native SQL 쿼리 사용
     * 실제 데이터베이스 SQL 사용 (성능 최적화가 필요한 경우)
     */
    @Query(value = "SELECT * FROM users WHERE username LIKE %:keyword%", nativeQuery = true)
    List<User> searchByUsername(@Param("keyword") String keyword);

    /**
     * JPQL로 특정 조건의 사용자 검색
     */
    @Query("SELECT u FROM User u WHERE u.age >= :minAge AND u.status = :status")
    List<User> findActiveUsersOlderThan(
        @Param("minAge") Integer minAge,
        @Param("status") UserStatus status
    );

    /**
     * 사용자의 게시글 수를 함께 조회 (JOIN 사용)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.posts WHERE u.id = :id")
    Optional<User> findUserWithPosts(@Param("id") Long id);

    /**
     * 수정 쿼리 (@Modifying 필수)
     * 사용자명 업데이트
     */
    @Modifying
    @Query("UPDATE User u SET u.username = :username WHERE u.id = :id")
    int updateUsername(@Param("id") Long id, @Param("username") String username);

    /**
     * 수정 쿼리: 사용자 상태 변경
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") UserStatus status);

    /**
     * 삭제 쿼리
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.status = :status")
    int deleteByStatus(@Param("status") UserStatus status);

    // ========== 통계 쿼리 ==========

    /**
     * 나이별 사용자 수 조회
     */
    @Query("SELECT u.age, COUNT(u) FROM User u GROUP BY u.age")
    List<Object[]> countUsersByAge();

    /**
     * 상태별 사용자 수 조회
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();

    /**
     * DTO Projection - 필요한 컬럼만 조회하여 성능 최적화
     */
    @Query("SELECT new com.example.jpa.repository.UserSummary(u.id, u.username, u.email) FROM User u")
    List<UserSummary> findUserSummaries();

    /**
     * UserSummary DTO 클래스
     * 필요한 정보만 조회할 때 사용
     */
    class UserSummary {
        private Long id;
        private String username;
        private String email;

        public UserSummary(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }
}
