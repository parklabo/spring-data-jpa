# Repository 패턴

## Repository란?

Repository는 데이터 접근 계층을 추상화한 인터페이스입니다. Spring Data JPA는 Repository 인터페이스만 정의하면 구현 클래스를 자동으로 생성해줍니다.

## Repository 계층 구조

```
Repository (마커 인터페이스)
    ↓
CrudRepository (기본 CRUD 메서드)
    ↓
PagingAndSortingRepository (페이징, 정렬)
    ↓
JpaRepository (JPA 특화 기능) ← 주로 사용
```

## JpaRepository

가장 많이 사용되는 인터페이스로, JPA에 특화된 기능을 제공합니다.

### 기본 사용법

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<엔티티 타입, ID 타입>
}
```

### 제공되는 기본 메서드

#### 저장 및 수정

```java
// 단건 저장/수정
User savedUser = userRepository.save(user);

// 여러 건 저장
List<User> savedUsers = userRepository.saveAll(userList);

// 즉시 flush (영속성 컨텍스트를 DB에 동기화)
User user = userRepository.saveAndFlush(newUser);
```

#### 조회

```java
// ID로 조회
Optional<User> user = userRepository.findById(1L);
User user = userRepository.findById(1L)
    .orElseThrow(() -> new RuntimeException("사용자 없음"));

// 전체 조회
List<User> allUsers = userRepository.findAll();

// 여러 ID로 조회
List<User> users = userRepository.findAllById(Arrays.asList(1L, 2L, 3L));

// 정렬하여 조회
List<User> sortedUsers = userRepository.findAll(Sort.by("username").ascending());

// 페이징 조회
Pageable pageable = PageRequest.of(0, 10); // 페이지 번호, 페이지 크기
Page<User> userPage = userRepository.findAll(pageable);
```

#### 삭제

```java
// ID로 삭제
userRepository.deleteById(1L);

// Entity로 삭제
userRepository.delete(user);

// 여러 건 삭제
userRepository.deleteAll(userList);

// 전체 삭제
userRepository.deleteAll();

// 배치 삭제 (성능 최적화)
userRepository.deleteAllInBatch();
```

#### 존재 여부 확인

```java
// ID 존재 확인
boolean exists = userRepository.existsById(1L);
```

#### 개수 조회

```java
// 전체 개수
long count = userRepository.count();
```

## CrudRepository

기본적인 CRUD 메서드만 제공합니다.

```java
public interface UserRepository extends CrudRepository<User, Long> {
    // save, findById, findAll, delete 등 기본 메서드 사용 가능
}
```

## PagingAndSortingRepository

페이징과 정렬 기능을 제공합니다.

```java
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    // CrudRepository 기능 + 페이징, 정렬
}
```

## 커스텀 Repository

기본 메서드 외에 추가 메서드가 필요한 경우:

### 1. 메서드 이름 규칙 사용 (쿼리 메서드)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // 메서드 이름으로 쿼리 자동 생성
    Optional<User> findByEmail(String email);
    List<User> findByUsernameContaining(String keyword);
    long countByStatus(UserStatus status);
}
```

### 2. @Query 어노테이션 사용

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE age > :age", nativeQuery = true)
    List<User> findUsersOlderThan(@Param("age") Integer age);
}
```

### 3. 커스텀 Repository 구현

복잡한 로직이 필요한 경우 직접 구현할 수 있습니다.

#### 인터페이스 정의

```java
public interface UserRepositoryCustom {
    List<User> findByComplexConditions(String username, Integer minAge);
}
```

#### 구현 클래스

```java
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> findByComplexConditions(String username, Integer minAge) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        if (username != null) {
            predicates.add(cb.like(root.get("username"), "%" + username + "%"));
        }

        if (minAge != null) {
            predicates.add(cb.greaterThan(root.get("age"), minAge));
        }

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }
}
```

#### Repository에 상속

```java
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    // JpaRepository 기능 + UserRepositoryCustom 기능
}
```

## 페이징과 정렬

### 정렬 (Sort)

```java
// 단일 필드 정렬
Sort sort = Sort.by("username").ascending();
List<User> users = userRepository.findAll(sort);

// 여러 필드 정렬
Sort sort = Sort.by("age").descending()
                .and(Sort.by("username").ascending());

// 정렬 방향 지정
Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
```

### 페이징 (Pageable)

```java
// 페이지 생성 (페이지 번호는 0부터 시작)
Pageable pageable = PageRequest.of(0, 10);  // 첫 페이지, 10개씩

// 페이징 + 정렬
Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

// 페이지 결과 조회
Page<User> userPage = userRepository.findAll(pageable);

// 페이지 정보
int totalPages = userPage.getTotalPages();        // 전체 페이지 수
long totalElements = userPage.getTotalElements();  // 전체 항목 수
int currentPage = userPage.getNumber();            // 현재 페이지 번호
int pageSize = userPage.getSize();                 // 페이지 크기
boolean hasNext = userPage.hasNext();              // 다음 페이지 존재 여부
boolean hasPrevious = userPage.hasPrevious();      // 이전 페이지 존재 여부

// 실제 데이터
List<User> users = userPage.getContent();

// 다음 페이지로 이동
if (userPage.hasNext()) {
    Pageable nextPageable = userPage.nextPageable();
    Page<User> nextPage = userRepository.findAll(nextPageable);
}
```

### Slice vs Page

```java
// Page: 전체 개수를 조회 (count 쿼리 실행)
Page<User> page = userRepository.findAll(pageable);

// Slice: 다음 페이지 존재 여부만 확인 (count 쿼리 없음, 성능 향상)
public interface UserRepository extends JpaRepository<User, Long> {
    Slice<User> findByStatus(UserStatus status, Pageable pageable);
}

Slice<User> slice = userRepository.findByStatus(UserStatus.ACTIVE, pageable);
boolean hasNext = slice.hasNext();  // 다음 페이지가 있는지만 확인
```

## Repository 메서드 명명 규칙

### 조회 시작어

- `findBy`: 조회
- `readBy`: 조회 (findBy와 동일)
- `getBy`: 조회 (findBy와 동일)
- `queryBy`: 조회 (findBy와 동일)
- `searchBy`: 조회 (findBy와 동일)
- `streamBy`: Stream으로 조회
- `countBy`: 개수 조회
- `existsBy`: 존재 여부 확인
- `deleteBy`: 삭제

### 조건 키워드

- `And`: 그리고
- `Or`: 또는
- `Between`: 사이
- `LessThan`: 미만
- `LessThanEqual`: 이하
- `GreaterThan`: 초과
- `GreaterThanEqual`: 이상
- `After`: 이후
- `Before`: 이전
- `IsNull`: null
- `IsNotNull`: not null
- `Like`: 포함
- `NotLike`: 불포함
- `StartingWith`: ~로 시작
- `EndingWith`: ~로 끝남
- `Containing`: 포함
- `OrderBy`: 정렬
- `Not`: 부정
- `In`: 포함
- `NotIn`: 불포함
- `True`: true
- `False`: false
- `IgnoreCase`: 대소문자 무시

### 예제

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE username = ? AND email = ?
    List<User> findByUsernameAndEmail(String username, String email);

    // SELECT * FROM users WHERE age BETWEEN ? AND ?
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);

    // SELECT * FROM users WHERE username LIKE %?%
    List<User> findByUsernameContaining(String keyword);

    // SELECT * FROM users ORDER BY created_at DESC
    List<User> findAllByOrderByCreatedAtDesc();

    // SELECT COUNT(*) FROM users WHERE status = ?
    long countByStatus(UserStatus status);

    // DELETE FROM users WHERE status = ?
    void deleteByStatus(UserStatus status);
}
```

## 실습 예제

프로젝트의 `src/main/java/com/example/jpa/repository/UserRepository.java`를 참고하세요.

## 다음 단계

- [03. 쿼리 메서드](03-query-methods.md)
- [04. 고급 기능](04-advanced-features.md)
