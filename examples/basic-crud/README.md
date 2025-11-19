# 기본 CRUD 예제

이 예제는 Spring Data JPA를 사용한 기본적인 CRUD(Create, Read, Update, Delete) 작업을 보여줍니다.

## 실습 내용

### 1. Create (생성)

```java
// UserService.java
@Transactional
public User createUser(String username, String email, Integer age) {
    User user = new User(username, email, age);
    return userRepository.save(user);
}

// 사용 예제
User user = userService.createUser("홍길동", "hong@example.com", 25);
System.out.println("생성된 사용자 ID: " + user.getId());
```

### 2. Read (조회)

```java
// ID로 단건 조회
User user = userService.getUserById(1L);

// 전체 조회
List<User> allUsers = userService.getAllUsers();

// 조건 조회
List<User> activeUsers = userService.getUsersByStatus(UserStatus.ACTIVE);

// 페이징 조회
Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
Page<User> userPage = userRepository.findAll(pageable);

System.out.println("전체 사용자 수: " + userPage.getTotalElements());
System.out.println("전체 페이지 수: " + userPage.getTotalPages());
userPage.getContent().forEach(System.out::println);
```

### 3. Update (수정)

```java
// 변경 감지(Dirty Checking)를 이용한 수정
@Transactional
public User updateUser(Long id, String username, String phoneNumber) {
    User user = getUserById(id);
    user.updateProfile(username, phoneNumber);
    // save() 호출 불필요 - 트랜잭션 종료 시 자동 업데이트
    return user;
}

// 사용 예제
User updatedUser = userService.updateUser(1L, "김철수", "010-1234-5678");
System.out.println("수정된 사용자: " + updatedUser);
```

### 4. Delete (삭제)

```java
// ID로 삭제
userService.deleteUser(1L);

// Entity로 삭제
User user = userRepository.findById(1L).orElseThrow();
userRepository.delete(user);

// 조건으로 삭제
userRepository.deleteByStatus(UserStatus.BANNED);
```

## 실행 방법

1. 프로젝트 루트에서 애플리케이션 실행:
   ```bash
   mvn spring-boot:run
   ```

2. H2 Console 접속: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (빈칸)

3. 데이터 확인:
   ```sql
   SELECT * FROM users;
   SELECT * FROM posts;
   SELECT * FROM comments;
   ```

## 주요 학습 포인트

### 1. Repository save() 메서드

```java
User savedUser = userRepository.save(user);
```

- ID가 없으면 INSERT (새로운 엔티티)
- ID가 있으면 UPDATE (기존 엔티티 수정)

### 2. Optional 처리

```java
// ❌ 나쁜 예
User user = userRepository.findById(1L).get();  // NoSuchElementException 위험

// ✅ 좋은 예
User user = userRepository.findById(1L)
    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

// 또는
Optional<User> userOpt = userRepository.findById(1L);
if (userOpt.isPresent()) {
    User user = userOpt.get();
    // 처리...
}
```

### 3. 변경 감지 (Dirty Checking)

```java
@Transactional
public void updateUsername(Long id, String newUsername) {
    User user = userRepository.findById(id).orElseThrow();
    user.setUsername(newUsername);
    // userRepository.save(user); ← 불필요!
    // 트랜잭션 종료 시 JPA가 자동으로 UPDATE 실행
}
```

JPA는 트랜잭션 내에서 Entity의 변경을 감지하고 자동으로 UPDATE 쿼리를 생성합니다.

### 4. 트랜잭션 범위

```java
@Service
@Transactional(readOnly = true)  // 기본은 읽기 전용
public class UserService {

    // 읽기 전용 메서드는 성능 최적화
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 쓰기 작업은 명시적으로 @Transactional
    @Transactional
    public User createUser(String username, String email, Integer age) {
        User user = new User(username, email, age);
        return userRepository.save(user);
    }
}
```

## 다음 단계

- `../relationships/` - Entity 간 관계 매핑 학습
- `../blog-system/` - 블로그 시스템 구현 예제
