# 블로그 시스템 예제

이 예제는 User, Post, Comment Entity 간의 관계를 활용한 블로그 시스템을 구현합니다.

## Entity 관계도

```
User (1) ──< (N) Post (1) ──< (N) Comment (N) >── (1) User
   │                                                      │
   └──────────────────────────────────────────────────────┘
```

- User와 Post: 1:N 관계 (한 사용자는 여러 게시글 작성)
- Post와 Comment: 1:N 관계 (한 게시글은 여러 댓글 보유)
- User와 Comment: 1:N 관계 (한 사용자는 여러 댓글 작성)

## 주요 기능

### 1. 게시글 작성

```java
// PostService.java
@Transactional
public Post createPost(Long authorId, String title, String content) {
    User author = userRepository.findById(authorId)
        .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다"));

    Post post = new Post(title, content, author);
    return postRepository.save(post);
}

// 사용 예제
Post post = postService.createPost(1L, "Spring Data JPA 소개", "JPA는...");
```

### 2. 게시글 조회 (N+1 문제 해결)

#### ❌ N+1 문제 발생

```java
// 게시글 목록 조회
List<Post> posts = postRepository.findAll();  // 1번의 쿼리

// 각 게시글의 작성자 조회
for (Post post : posts) {
    String authorName = post.getAuthor().getUsername();  // N번의 쿼리!
}
// 총 1 + N번의 쿼리 실행 (N+1 문제)
```

#### ✅ Fetch Join으로 해결

```java
// PostRepository.java
@Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.published = true")
List<Post> findAllPublishedWithAuthor();

// 사용
List<Post> posts = postRepository.findAllPublishedWithAuthor();
// 1번의 쿼리로 게시글과 작성자를 함께 조회
```

#### ✅ EntityGraph로 해결

```java
@EntityGraph(attributePaths = {"author"})
List<Post> findByPublishedTrue();
```

### 3. 댓글 작성

```java
// 댓글 생성
Comment comment = new Comment("좋은 글 감사합니다!", post, user);
commentRepository.save(comment);

// 또는 연관관계 편의 메서드 사용
post.addComment(new Comment("유익한 정보네요!"));
commentRepository.save(comment);
```

### 4. 게시글과 댓글 함께 조회

```java
// PostRepository.java
@Query("SELECT DISTINCT p FROM Post p " +
       "LEFT JOIN FETCH p.comments " +
       "LEFT JOIN FETCH p.author " +
       "WHERE p.id = :id")
Post findPostWithCommentsAndAuthor(@Param("id") Long id);

// 사용
Post post = postRepository.findPostWithCommentsAndAuthor(1L);
System.out.println("게시글: " + post.getTitle());
System.out.println("작성자: " + post.getAuthor().getUsername());
System.out.println("댓글 수: " + post.getComments().size());
```

### 5. 게시글 검색 및 페이징

```java
// 제목으로 검색 (페이징)
Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
Page<Post> postPage = postRepository.findByTitleContaining("Spring", pageable);

// 발행된 게시글만 조회 (페이징)
Page<Post> publishedPosts = postRepository.findByPublishedTrue(pageable);

// 작성자별 게시글 조회
Page<Post> userPosts = postRepository.findByAuthorId(1L, pageable);
```

## 관계 매핑 상세

### @ManyToOne (다대일)

```java
@Entity
public class Post {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
}

@Entity
public class Comment {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

- `fetch = FetchType.LAZY`: 지연 로딩 (필요할 때만 조회)
- `@JoinColumn`: 외래 키 컬럼 지정

### @OneToMany (일대다)

```java
@Entity
public class User {
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();
}

@Entity
public class Post {
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
```

- `mappedBy`: 연관관계의 주인 반대편 (외래 키가 없는 쪽)
- `cascade`: 영속성 전이 (부모 저장/삭제 시 자식도 함께 처리)
- `orphanRemoval`: 부모와의 관계가 끊어진 자식 엔티티 삭제

### 연관관계 편의 메서드

```java
@Entity
public class User {
    // 양방향 관계를 안전하게 설정
    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setAuthor(null);
    }
}
```

## Cascade와 OrphanRemoval

### CascadeType

```java
@OneToMany(cascade = CascadeType.ALL)
private List<Post> posts;

// CascadeType 종류:
// - ALL: 모든 작업 전이
// - PERSIST: 저장 시 함께 저장
// - MERGE: 병합 시 함께 병합
// - REMOVE: 삭제 시 함께 삭제
// - REFRESH: 새로고침 시 함께 새로고침
// - DETACH: 분리 시 함께 분리
```

### orphanRemoval

```java
@OneToMany(mappedBy = "post", orphanRemoval = true)
private List<Comment> comments;

// 사용 예
Post post = postRepository.findById(1L).orElseThrow();
post.getComments().remove(0);  // 리스트에서 제거
// orphanRemoval = true이면 해당 Comment가 DB에서도 삭제됨
```

## FetchType

### LAZY (지연 로딩) - 권장

```java
@ManyToOne(fetch = FetchType.LAZY)
private User author;

// Post 조회 시 User는 조회하지 않음
Post post = postRepository.findById(1L).orElseThrow();

// User 정보에 접근할 때 쿼리 실행
String username = post.getAuthor().getUsername();
```

### EAGER (즉시 로딩) - 비권장

```java
@ManyToOne(fetch = FetchType.EAGER)
private User author;

// Post 조회 시 User도 함께 조회
Post post = postRepository.findById(1L).orElseThrow();
// 이미 author가 로딩됨
```

**권장사항**: 기본적으로 LAZY 사용, 필요한 경우에만 Fetch Join 사용

## 실행 예제

```java
// 1. 사용자 생성
User user = userService.createUser("홍길동", "hong@example.com", 25);

// 2. 게시글 작성
Post post = postService.createPost(
    user.getId(),
    "Spring Data JPA 튜토리얼",
    "이 글은 Spring Data JPA를 배우는 사람들을 위한 튜토리얼입니다."
);

// 3. 게시글 발행
postService.publishPost(post.getId());

// 4. 댓글 작성
Comment comment = new Comment("유익한 정보 감사합니다!", post, user);
commentRepository.save(comment);

// 5. 게시글 조회 (댓글과 함께)
Post retrievedPost = postRepository.findPostWithCommentsAndAuthor(post.getId());
System.out.println("제목: " + retrievedPost.getTitle());
System.out.println("작성자: " + retrievedPost.getAuthor().getUsername());
System.out.println("댓글 수: " + retrievedPost.getComments().size());

// 6. 게시글 검색
List<Post> searchResults = postRepository.searchByTitleOrContent("JPA");
```

## 주요 학습 포인트

1. **양방향 연관관계**: User ↔ Post, Post ↔ Comment
2. **N+1 문제와 해결**: Fetch Join, EntityGraph
3. **Cascade와 OrphanRemoval**: 영속성 전이
4. **FetchType**: LAZY vs EAGER
5. **연관관계 편의 메서드**: 양방향 관계 안전하게 설정

## 다음 단계

- `../relationships/` - 다양한 관계 매핑 패턴 학습
- `docs/04-advanced-features.md` - 고급 기능 학습
