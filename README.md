# Spring Data JPA 학습 가이드

> Spring Data JPA를 활용한 데이터베이스 영속성 관리 학습 프로젝트

## 📚 목차

- [Spring Data JPA란?](#spring-data-jpa란)
- [주요 개념](#주요-개념)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [예제 코드](#예제-코드)
- [주요 어노테이션](#주요-어노테이션)
- [Repository 패턴](#repository-패턴)
- [쿼리 메서드](#쿼리-메서드)
- [고급 기능](#고급-기능)
- [참고 자료](#참고-자료)

## Spring Data JPA란?

**JPA (Java Persistence API)** 는 자바 애플리케이션에서 관계형 데이터베이스를 사용하는 방식을 정의한 인터페이스입니다. JPA는 ORM(Object-Relational Mapping) 기술의 표준으로, 객체와 관계형 데이터베이스의 테이블을 매핑해줍니다.

**Spring Data JPA**는 JPA를 Spring 환경에서 더 쉽게 사용할 수 있도록 도와주는 프레임워크입니다. Repository 추상화를 통해 데이터 접근 계층을 간편하게 구현할 수 있습니다.

### 주요 장점

- **생산성 향상**: 반복적인 CRUD 코드를 자동으로 생성
- **타입 안정성**: 컴파일 타임에 오류 검증
- **데이터베이스 독립성**: 다양한 DB에 대해 동일한 코드 사용 가능
- **쿼리 메서드**: 메서드 이름으로 쿼리 자동 생성
- **페이징 및 정렬**: 간편한 페이징 처리

## 주요 개념

### ORM (Object-Relational Mapping)

객체 지향 프로그래밍의 객체와 관계형 데이터베이스의 데이터를 자동으로 매핑하는 기술입니다.

```
Java 객체 (Entity) ←→ 데이터베이스 테이블 (Table)
```

### Entity

데이터베이스의 테이블에 대응하는 클래스입니다. JPA에서 관리하는 객체로, 데이터베이스의 레코드를 객체로 표현합니다.

### Repository

데이터베이스에 접근하는 계층을 추상화한 인터페이스입니다. Spring Data JPA는 인터페이스 선언만으로 기본적인 CRUD 기능을 제공합니다.

### Persistence Context

JPA가 Entity를 관리하는 영역으로, 1차 캐시, 변경 감지 등의 기능을 제공합니다.

## 프로젝트 구조

```
spring-data-jpa/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/jpa/
│   │   │       ├── entity/          # Entity 클래스
│   │   │       │   ├── User.java
│   │   │       │   ├── Post.java
│   │   │       │   └── Comment.java
│   │   │       ├── repository/      # Repository 인터페이스
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── PostRepository.java
│   │   │       │   └── CommentRepository.java
│   │   │       ├── service/         # 비즈니스 로직
│   │   │       │   └── UserService.java
│   │   │       └── Application.java # 메인 클래스
│   │   └── resources/
│   │       ├── application.yml      # 설정 파일
│   │       └── data.sql             # 초기 데이터
│   └── test/
│       └── java/                    # 테스트 코드
├── docs/                            # 상세 문서
│   ├── 01-entity-basics.md
│   ├── 02-repository-pattern.md
│   ├── 03-query-methods.md
│   └── 04-advanced-features.md
├── examples/                        # 실습 예제
│   ├── basic-crud/
│   ├── relationships/
│   └── custom-queries/
├── pom.xml                          # Maven 설정
└── README.md
```

## 시작하기

### 1. 의존성 추가

**Maven (pom.xml)**

```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <version>3.2.0</version>
    </dependency>

    <!-- H2 Database (개발/테스트용) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- MySQL Driver (운영환경) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**Gradle (build.gradle)**

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa:3.2.0'
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'com.mysql:mysql-connector-j'
}
```

### 2. 데이터베이스 설정

**application.yml**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
```

### 3. Entity 생성

**User.java**

```java
package com.example.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    protected User() {
    }

    // 생성자
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    // Getter & Setter
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
```

### 4. Repository 생성

**UserRepository.java**

```java
package com.example.jpa.repository;

import com.example.jpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 사용자명으로 검색 (부분 일치)
    List<User> findByUsernameContaining(String keyword);

    // 사용자명으로 존재 여부 확인
    boolean existsByUsername(String username);
}
```

### 5. Service 계층 생성

**UserService.java**

```java
package com.example.jpa.service;

import com.example.jpa.entity.User;
import com.example.jpa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 사용자 생성
    @Transactional
    public User createUser(String username, String email) {
        User user = new User(username, email);
        return userRepository.save(user);
    }

    // 모든 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ID로 사용자 조회
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }

    // 사용자 정보 업데이트
    @Transactional
    public User updateUser(Long id, String newUsername) {
        User user = getUserById(id);
        user.setUsername(newUsername);
        return user; // 변경 감지(Dirty Checking)로 자동 업데이트
    }

    // 사용자 삭제
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

## 예제 코드

### 기본 CRUD 작업

```java
// 생성 (Create)
User user = new User("홍길동", "hong@example.com");
userRepository.save(user);

// 조회 (Read)
User foundUser = userRepository.findById(1L).orElseThrow();
List<User> allUsers = userRepository.findAll();

// 수정 (Update)
foundUser.setUsername("김철수");
userRepository.save(foundUser);

// 삭제 (Delete)
userRepository.deleteById(1L);
```

### 쿼리 메서드 사용

```java
// 이메일로 검색
Optional<User> user = userRepository.findByEmail("hong@example.com");

// 사용자명에 특정 키워드 포함
List<User> users = userRepository.findByUsernameContaining("홍");

// 존재 여부 확인
boolean exists = userRepository.existsByUsername("홍길동");
```

## 주요 어노테이션

### Entity 관련

| 어노테이션 | 설명 | 예제 |
|-----------|------|------|
| `@Entity` | JPA Entity 클래스임을 선언 | `@Entity` |
| `@Table` | 매핑할 테이블 이름 지정 | `@Table(name = "users")` |
| `@Id` | 기본 키(Primary Key) 지정 | `@Id` |
| `@GeneratedValue` | 기본 키 생성 전략 | `@GeneratedValue(strategy = GenerationType.IDENTITY)` |
| `@Column` | 컬럼 매핑 및 속성 설정 | `@Column(name = "user_name", length = 50, nullable = false)` |

### 관계 매핑

| 어노테이션 | 설명 | 사용 예 |
|-----------|------|---------|
| `@OneToOne` | 1:1 관계 | 사용자 ↔ 프로필 |
| `@OneToMany` | 1:N 관계 | 게시글 ↔ 댓글들 |
| `@ManyToOne` | N:1 관계 | 댓글들 ↔ 게시글 |
| `@ManyToMany` | N:N 관계 | 학생들 ↔ 강좌들 |

### 예제: 관계 매핑

**Post.java (게시글)**

```java
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 다대일 관계: 여러 게시글 → 한 명의 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    // 일대다 관계: 한 게시글 → 여러 댓글
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 생성자, Getter, Setter...
}
```

**Comment.java (댓글)**

```java
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 생성자, Getter, Setter...
}
```

## Repository 패턴

### 주요 인터페이스

1. **Repository**: 마커 인터페이스
2. **CrudRepository**: 기본 CRUD 메서드 제공
3. **PagingAndSortingRepository**: 페이징 및 정렬 기능
4. **JpaRepository**: JPA 특화 기능 포함 (가장 많이 사용)

### JpaRepository 주요 메서드

```java
// 저장 및 수정
save(entity)          // 엔티티 저장
saveAll(entities)     // 여러 엔티티 저장

// 조회
findById(id)          // ID로 조회
findAll()             // 전체 조회
findAll(Sort)         // 정렬하여 조회
findAll(Pageable)     // 페이징 조회
count()               // 전체 개수

// 삭제
deleteById(id)        // ID로 삭제
delete(entity)        // 엔티티 삭제
deleteAll()           // 전체 삭제

// 존재 여부
existsById(id)        // ID 존재 확인
```

## 쿼리 메서드

### 메서드 이름으로 쿼리 생성

Spring Data JPA는 메서드 이름을 분석하여 자동으로 쿼리를 생성합니다.

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE username = ?
    List<User> findByUsername(String username);

    // SELECT * FROM users WHERE email = ? AND username = ?
    List<User> findByEmailAndUsername(String email, String username);

    // SELECT * FROM users WHERE username LIKE %?%
    List<User> findByUsernameContaining(String keyword);

    // SELECT * FROM users WHERE created_at > ?
    List<User> findByCreatedAtAfter(LocalDateTime date);

    // SELECT * FROM users ORDER BY username DESC
    List<User> findAllByOrderByUsernameDesc();

    // SELECT COUNT(*) FROM users WHERE email = ?
    long countByEmail(String email);

    // DELETE FROM users WHERE username = ?
    void deleteByUsername(String username);
}
```

### 쿼리 메서드 키워드

| 키워드 | 예제 | JPQL |
|--------|------|------|
| `findBy` | findByUsername | `WHERE username = ?` |
| `And` | findByUsernameAndEmail | `WHERE username = ? AND email = ?` |
| `Or` | findByUsernameOrEmail | `WHERE username = ? OR email = ?` |
| `Between` | findByCreatedAtBetween | `WHERE createdAt BETWEEN ? AND ?` |
| `LessThan` | findByAgeLessThan | `WHERE age < ?` |
| `GreaterThan` | findByAgeGreaterThan | `WHERE age > ?` |
| `Like` | findByUsernameLike | `WHERE username LIKE ?` |
| `Containing` | findByUsernameContaining | `WHERE username LIKE %?%` |
| `StartingWith` | findByUsernameStartingWith | `WHERE username LIKE ?%` |
| `EndingWith` | findByUsernameEndingWith | `WHERE username LIKE %?` |
| `OrderBy` | findByUsernameOrderByCreatedAtDesc | `ORDER BY createdAt DESC` |
| `Not` | findByUsernameNot | `WHERE username <> ?` |
| `In` | findByUsernameIn | `WHERE username IN (?)` |
| `NotIn` | findByUsernameNotIn | `WHERE username NOT IN (?)` |
| `IsNull` | findByEmailIsNull | `WHERE email IS NULL` |
| `IsNotNull` | findByEmailIsNotNull | `WHERE email IS NOT NULL` |

### @Query 어노테이션

복잡한 쿼리는 `@Query`를 사용하여 직접 작성할 수 있습니다.

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // JPQL
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findUserByEmail(String email);

    // Native SQL
    @Query(value = "SELECT * FROM users WHERE username LIKE %?1%", nativeQuery = true)
    List<User> searchByUsername(String keyword);

    // Named Parameter
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.email = :email")
    Optional<User> findByUsernameAndEmail(
        @Param("username") String username,
        @Param("email") String email
    );

    // 수정 쿼리
    @Modifying
    @Query("UPDATE User u SET u.username = ?2 WHERE u.id = ?1")
    int updateUsername(Long id, String newUsername);
}
```

## 고급 기능

### 1. 페이징과 정렬

```java
// Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByUsernameContaining(String keyword, Pageable pageable);
}

// 사용 예제
Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
Page<User> userPage = userRepository.findByUsernameContaining("홍", pageable);

System.out.println("전체 페이지 수: " + userPage.getTotalPages());
System.out.println("전체 항목 수: " + userPage.getTotalElements());
System.out.println("현재 페이지 번호: " + userPage.getNumber());
List<User> users = userPage.getContent();
```

### 2. Specification을 이용한 동적 쿼리

```java
public class UserSpecifications {

    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) -> cb.equal(root.get("username"), username);
    }

    public static Specification<User> emailContains(String email) {
        return (root, query, cb) -> cb.like(root.get("email"), "%" + email + "%");
    }
}

// 사용
Specification<User> spec = Specification.where(UserSpecifications.hasUsername("홍길동"))
                                        .and(UserSpecifications.emailContains("example"));
List<User> users = userRepository.findAll(spec);
```

### 3. Auditing (감사)

엔티티의 생성/수정 시간과 사용자를 자동으로 기록합니다.

```java
@EntityListeners(AuditingEntityListener.class)
@Entity
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}

// Application 클래스에 @EnableJpaAuditing 추가
@SpringBootApplication
@EnableJpaAuditing
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4. QueryDSL

타입 안전한 쿼리 작성을 위한 프레임워크입니다.

```java
QUser user = QUser.user;

List<User> users = queryFactory
    .selectFrom(user)
    .where(user.username.contains("홍")
        .and(user.email.endsWith("@example.com")))
    .orderBy(user.createdAt.desc())
    .fetch();
```

## 실습 예제

### 예제 1: 블로그 시스템

`examples/blog-system/` 디렉토리에서 확인하세요.

- User, Post, Comment Entity
- 1:N, N:1 관계 매핑
- CRUD 작업 구현
- 검색 및 페이징 기능

### 예제 2: 쇼핑몰 시스템

`examples/shop-system/` 디렉토리에서 확인하세요.

- Customer, Product, Order, OrderItem Entity
- N:N 관계 매핑
- 주문 처리 로직
- 복잡한 쿼리 작성

### 예제 3: 소셜 미디어

`examples/social-media/` 디렉토리에서 확인하세요.

- User, Post, Like, Follow Entity
- 자기 참조 관계
- 복합 키 사용
- 동적 쿼리 구현

## 모범 사례

### 1. Entity 설계

```java
// ✅ 좋은 예
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // 기본 생성자는 protected
    protected User() {}

    // 비즈니스 로직을 위한 생성자
    public User(String email) {
        this.email = email;
    }
}
```

### 2. N+1 문제 해결

```java
// ❌ N+1 문제 발생
List<Post> posts = postRepository.findAll();
for (Post post : posts) {
    User author = post.getAuthor(); // 각 Post마다 쿼리 실행!
}

// ✅ Fetch Join 사용
@Query("SELECT p FROM Post p JOIN FETCH p.author")
List<Post> findAllWithAuthor();

// ✅ EntityGraph 사용
@EntityGraph(attributePaths = {"author"})
List<Post> findAll();
```

### 3. Transaction 관리

```java
@Service
@Transactional(readOnly = true) // 기본은 읽기 전용
public class UserService {

    @Transactional // 쓰기 작업만 따로 설정
    public User createUser(String username) {
        // ...
    }

    public List<User> getUsers() {
        // readOnly = true (기본값 사용)
    }
}
```

## 성능 최적화 팁

1. **FetchType 설정**: `@ManyToOne`, `@OneToOne`은 LAZY로 설정
2. **Batch Size 설정**: `spring.jpa.properties.hibernate.default_batch_fetch_size=100`
3. **2차 캐시 활용**: Ehcache 등을 이용한 캐싱
4. **쿼리 최적화**: 필요한 컬럼만 조회 (DTO Projection)
5. **인덱스 활용**: 자주 검색하는 컬럼에 인덱스 생성

## 참고 자료

### 공식 문서
- [Spring Data JPA 공식 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [JPA 2.2 스펙](https://jakarta.ee/specifications/persistence/2.2/)
- [Hibernate ORM 문서](https://hibernate.org/orm/documentation/)

### 학습 자료
- [Baeldung - Spring Data JPA](https://www.baeldung.com/spring-data-jpa-tutorial)
- [Spring Guides - Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### 책 추천
- "자바 ORM 표준 JPA 프로그래밍" - 김영한
- "스프링 부트와 AWS로 혼자 구현하는 웹 서비스" - 이동욱

## 기여하기

이 프로젝트는 학습 목적으로 작성되었습니다. 개선 사항이나 추가할 예제가 있다면 Pull Request를 보내주세요!

## 라이선스

MIT License

---

**마지막 업데이트**: 2025년 11월
**Spring Boot 버전**: 3.2.x
**Java 버전**: 17+
