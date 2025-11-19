# Entity 기초

## Entity란?

Entity는 데이터베이스의 테이블과 매핑되는 Java 클래스입니다. JPA가 관리하는 객체로, 데이터베이스의 레코드를 객체로 표현합니다.

## Entity 생성 규칙

### 1. 필수 어노테이션

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드들...
}
```

- `@Entity`: 이 클래스가 JPA Entity임을 선언
- `@Table`: 매핑할 데이터베이스 테이블 이름 지정 (생략 시 클래스 이름 사용)
- `@Id`: 기본 키(Primary Key) 필드 지정
- `@GeneratedValue`: 기본 키 자동 생성 전략

### 2. 기본 생성자 필수

JPA는 리플렉션을 사용하여 Entity 객체를 생성하므로 기본 생성자가 필요합니다.

```java
// protected 또는 public으로 선언
protected User() {
}
```

### 3. final 클래스 불가

JPA는 프록시를 생성하기 위해 Entity를 상속하므로 `final` 클래스는 사용할 수 없습니다.

## 기본 키 생성 전략

### IDENTITY

데이터베이스에 위임 (MySQL의 AUTO_INCREMENT 등)

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

### SEQUENCE

데이터베이스 시퀀스 사용 (Oracle, PostgreSQL 등)

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
@SequenceGenerator(name = "user_seq", sequenceName = "user_sequence")
private Long id;
```

### TABLE

키 생성 전용 테이블 사용 (모든 DB 호환)

```java
@Id
@GeneratedValue(strategy = GenerationType.TABLE, generator = "user_gen")
@TableGenerator(name = "user_gen", table = "id_generator")
private Long id;
```

### AUTO

데이터베이스에 따라 자동 선택 (기본값)

```java
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```

## 컬럼 매핑

### @Column

컬럼의 세부 속성을 설정합니다.

```java
@Column(
    name = "user_name",        // 데이터베이스 컬럼 이름
    nullable = false,          // NOT NULL 제약 조건
    unique = true,             // UNIQUE 제약 조건
    length = 50,               // 컬럼 길이 (String 타입)
    columnDefinition = "TEXT", // 컬럼 정의를 직접 지정
    updatable = false,         // 수정 불가능
    insertable = true          // 삽입 가능
)
private String username;
```

### @Enumerated

Enum 타입 매핑

```java
public enum UserStatus {
    ACTIVE, INACTIVE, BANNED
}

// EnumType.STRING 권장 (순서가 바뀌어도 안전)
@Enumerated(EnumType.STRING)
private UserStatus status;

// EnumType.ORDINAL은 위험 (순서가 바뀌면 데이터 오류)
@Enumerated(EnumType.ORDINAL)  // 사용 비권장!
private UserStatus status;
```

### @Temporal

날짜/시간 타입 매핑 (Java 8 이전)

```java
@Temporal(TemporalType.DATE)      // 날짜만 (yyyy-MM-dd)
private Date date;

@Temporal(TemporalType.TIME)      // 시간만 (HH:mm:ss)
private Date time;

@Temporal(TemporalType.TIMESTAMP) // 날짜와 시간
private Date timestamp;
```

Java 8 이후에는 LocalDateTime 사용 권장:

```java
private LocalDateTime createdAt;  // @Temporal 불필요
private LocalDate birthDate;
private LocalTime workTime;
```

### @Lob

Large Object 매핑 (대용량 데이터)

```java
@Lob
private String content;  // CLOB (문자 대용량)

@Lob
private byte[] image;    // BLOB (바이너리 대용량)
```

### @Transient

데이터베이스에 매핑하지 않는 필드

```java
@Transient
private String tempData;  // DB에 저장되지 않음
```

## 인덱스 설정

### 단일 컬럼 인덱스

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_username", columnList = "username")
})
public class User {
    // ...
}
```

### 복합 인덱스

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_name_email", columnList = "username, email")
})
public class User {
    // ...
}
```

## Auditing (감사)

엔티티의 생성/수정 시간을 자동으로 관리합니다.

### 1. 설정

```java
@SpringBootApplication
@EnableJpaAuditing  // Auditing 활성화
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. Entity에 적용

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class User {

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
```

### 3. 작성자 정보 제공 (선택사항)

```java
@Configuration
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // 현재 로그인한 사용자 정보 반환
            // Spring Security 사용 시:
            // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // return Optional.of(auth.getName());

            return Optional.of("system");
        };
    }
}
```

## Entity 설계 모범 사례

### 1. 불변성 보장

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String email;  // 이메일은 변경 불가

    // Setter 대신 비즈니스 메서드 제공
    public void updateProfile(String username) {
        this.username = username;
    }
}
```

### 2. 기본값 설정

```java
@Entity
public class Post {
    @Column(nullable = false)
    private Integer viewCount = 0;  // 기본값 설정

    @Column(nullable = false)
    private Boolean published = false;
}
```

### 3. toString 오버라이드 주의

양방향 연관관계에서 toString()은 무한 루프를 발생시킬 수 있습니다.

```java
// ❌ 나쁜 예
@Override
public String toString() {
    return "User{id=" + id + ", posts=" + posts + "}";
    // posts.toString() 호출 → Post의 toString() → author.toString() → 무한 루프!
}

// ✅ 좋은 예
@Override
public String toString() {
    return "User{id=" + id + ", username='" + username + "'}";
    // 연관관계 필드는 제외
}
```

### 4. equals & hashCode

```java
@Entity
public class User {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

## 실습 예제

프로젝트의 `src/main/java/com/example/jpa/entity/User.java`를 참고하세요.

## 다음 단계

- [02. Repository 패턴](02-repository-pattern.md)
- [03. 쿼리 메서드](03-query-methods.md)
- [04. 고급 기능](04-advanced-features.md)
