package com.example.jpa.service;

import com.example.jpa.entity.User;
import com.example.jpa.entity.User.UserStatus;
import com.example.jpa.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Service 클래스
 *
 * @Transactional 어노테이션:
 * - 클래스 레벨에 readOnly = true: 기본적으로 모든 메서드는 읽기 전용
 * - 메서드 레벨에 @Transactional: 해당 메서드는 쓰기 가능 트랜잭션
 *
 * 트랜잭션의 장점:
 * 1. 원자성(Atomicity): 모두 성공하거나 모두 실패
 * 2. 일관성(Consistency): 데이터 무결성 유지
 * 3. 격리성(Isolation): 동시 실행 트랜잭션 간 격리
 * 4. 지속성(Durability): 완료된 트랜잭션의 영구 반영
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 생성자 주입 (권장 방식)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자 생성
     * @Transactional: 쓰기 트랜잭션 필요
     */
    @Transactional
    public User createUser(String username, String email, Integer age) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + email);
        }

        User user = new User(username, email, age);
        return userRepository.save(user);
    }

    /**
     * 사용자 생성 (전화번호 포함)
     */
    @Transactional
    public User createUser(String username, String email, String phoneNumber, Integer age) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + email);
        }

        User user = new User(username, email, phoneNumber, age);
        return userRepository.save(user);
    }

    /**
     * 모든 사용자 조회
     * readOnly = true (기본값 사용)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * ID로 사용자 조회
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
    }

    /**
     * 이메일로 사용자 조회
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 사용자명으로 검색 (부분 일치)
     */
    public List<User> searchUsersByUsername(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

    /**
     * 사용자명으로 검색 (페이징)
     */
    public Page<User> searchUsersByUsername(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContaining(keyword, pageable);
    }

    /**
     * 사용자 정보 업데이트
     * 변경 감지(Dirty Checking)를 이용한 업데이트
     * - Entity의 필드 값만 변경하면 트랜잭션 종료 시 자동으로 UPDATE 쿼리 실행
     */
    @Transactional
    public User updateUser(Long id, String username, String phoneNumber) {
        User user = getUserById(id);
        user.updateProfile(username, phoneNumber);
        // save() 호출 불필요 - 변경 감지로 자동 저장
        return user;
    }

    /**
     * 사용자 이메일 업데이트
     */
    @Transactional
    public User updateEmail(Long id, String newEmail) {
        // 새 이메일 중복 체크
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + newEmail);
        }

        User user = getUserById(id);
        user.setEmail(newEmail);
        return user;
    }

    /**
     * 사용자 상태 변경
     */
    @Transactional
    public User updateUserStatus(Long id, UserStatus status) {
        User user = getUserById(id);
        user.setStatus(status);
        return user;
    }

    /**
     * 사용자 활성화
     */
    @Transactional
    public User activateUser(Long id) {
        User user = getUserById(id);
        user.activate();
        return user;
    }

    /**
     * 사용자 비활성화
     */
    @Transactional
    public User deactivateUser(Long id) {
        User user = getUserById(id);
        user.deactivate();
        return user;
    }

    /**
     * 사용자 정지
     */
    @Transactional
    public User banUser(Long id) {
        User user = getUserById(id);
        user.ban();
        return user;
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * 상태별 사용자 조회
     */
    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    /**
     * 활성 사용자 조회
     */
    public List<User> getActiveUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }

    /**
     * 나이 범위로 사용자 검색
     */
    public List<User> getUsersByAgeRange(Integer minAge, Integer maxAge) {
        return userRepository.findByAgeBetween(minAge, maxAge);
    }

    /**
     * 최근 가입 사용자 조회
     */
    public List<User> getRecentUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 특정 날짜 이후 가입한 사용자
     */
    public List<User> getUsersJoinedAfter(LocalDateTime date) {
        return userRepository.findByCreatedAtAfter(date);
    }

    /**
     * 전체 사용자 수
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * 상태별 사용자 수
     */
    public long getUserCountByStatus(UserStatus status) {
        return userRepository.countByStatus(status);
    }

    /**
     * 활성 사용자 수
     */
    public long getActiveUserCount() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }
}
