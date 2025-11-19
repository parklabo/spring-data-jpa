# Entity 관계 매핑 예제

JPA에서 사용하는 다양한 관계 매핑 패턴을 학습합니다.

## 관계의 종류

### 1. 일대일 (1:1) - @OneToOne

한 엔티티가 다른 엔티티와 정확히 하나의 관계를 가집니다.

**예제: User ↔ Profile**

```java
// User.java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    public void setProfile(Profile profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}

// Profile.java
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;
    private String website;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
```

**사용 예제**:
```java
User user = new User("홍길동");
Profile profile = new Profile("개발자", "https://example.com");
user.setProfile(profile);
userRepository.save(user);  // profile도 함께 저장됨 (cascade)
```

### 2. 일대다 (1:N) - @OneToMany

한 엔티티가 다른 엔티티의 여러 인스턴스와 관계를 가집니다.

**예제: Team ↔ Members**

```java
// Team.java
@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Member> members = new ArrayList<>();

    public void addMember(Member member) {
        members.add(member);
        member.setTeam(this);
    }
}

// Member.java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
```

**사용 예제**:
```java
Team team = new Team("개발팀");
team.addMember(new Member("홍길동"));
team.addMember(new Member("김철수"));
teamRepository.save(team);
```

### 3. 다대일 (N:1) - @ManyToOne

여러 엔티티가 하나의 엔티티와 관계를 가집니다. (일대다의 반대편)

위의 Member → Team 관계가 다대일 관계입니다.

### 4. 다대다 (N:N) - @ManyToMany

여러 엔티티가 다른 여러 엔티티와 관계를 가집니다.

#### 방법 1: @ManyToMany 직접 사용 (단순한 경우)

**예제: Student ↔ Course**

```java
// Student.java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();

    public void enrollCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }
}

// Course.java
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}
```

**사용 예제**:
```java
Student student = new Student("홍길동");
Course course1 = new Course("Spring Boot");
Course course2 = new Course("JPA");

student.enrollCourse(course1);
student.enrollCourse(course2);

studentRepository.save(student);
```

#### 방법 2: 중간 테이블을 Entity로 (권장)

추가 정보가 필요한 경우 중간 테이블을 별도의 Entity로 만듭니다.

**예제: Student ↔ Enrollment ↔ Course**

```java
// Student.java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();
}

// Course.java
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();
}

// Enrollment.java (중간 테이블)
@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // 추가 정보
    private LocalDateTime enrolledAt;
    private Integer grade;
    private String status;  // ENROLLED, COMPLETED, DROPPED
}
```

**사용 예제**:
```java
Student student = studentRepository.findById(1L).orElseThrow();
Course course = courseRepository.findById(1L).orElseThrow();

Enrollment enrollment = new Enrollment();
enrollment.setStudent(student);
enrollment.setCourse(course);
enrollment.setEnrolledAt(LocalDateTime.now());
enrollment.setStatus("ENROLLED");

enrollmentRepository.save(enrollment);
```

### 5. 자기 참조 관계

같은 엔티티 타입끼리 관계를 가집니다.

**예제: 댓글의 대댓글**

```java
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    // 부모 댓글 (대댓글인 경우)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 자식 댓글들 (대댓글들)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    public void addReply(Comment reply) {
        replies.add(reply);
        reply.setParent(this);
    }
}
```

**사용 예제**:
```java
Comment parentComment = new Comment("원글 댓글입니다.");
Comment reply1 = new Comment("대댓글 1");
Comment reply2 = new Comment("대댓글 2");

parentComment.addReply(reply1);
parentComment.addReply(reply2);

commentRepository.save(parentComment);
```

**예제: 조직도 (Manager ↔ Employees)**

```java
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 상사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // 부하 직원들
    @OneToMany(mappedBy = "manager")
    private List<Employee> subordinates = new ArrayList<>();
}
```

## 연관관계의 주인

양방향 연관관계에서는 한쪽을 연관관계의 주인으로 설정해야 합니다.

### 규칙

1. **외래 키가 있는 쪽이 주인** (보통 Many 쪽)
2. 주인이 아닌 쪽은 `mappedBy` 속성으로 주인을 지정
3. **주인만이 외래 키를 관리** (등록, 수정, 삭제)
4. 주인이 아닌 쪽은 읽기만 가능

```java
// Post (주인)
@ManyToOne
@JoinColumn(name = "author_id")  // 외래 키 컬럼
private User author;

// User (주인이 아님)
@OneToMany(mappedBy = "author")  // Post.author 필드가 주인
private List<Post> posts;
```

## Cascade 전략

부모 엔티티의 작업이 자식 엔티티에 전파됩니다.

```java
@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
private List<Post> posts;

// CascadeType 종류:
// - PERSIST: 저장 전파
// - MERGE: 병합 전파
// - REMOVE: 삭제 전파
// - REFRESH: 새로고침 전파
// - DETACH: 준영속 상태 전파
// - ALL: 모든 작업 전파
```

### 예제

```java
// CascadeType.ALL 사용
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Post> posts;

// 사용
User user = new User("홍길동");
user.addPost(new Post("제목1", "내용1"));
user.addPost(new Post("제목2", "내용2"));

userRepository.save(user);  // user와 post 모두 저장됨
userRepository.delete(user); // user와 post 모두 삭제됨
```

## OrphanRemoval

부모 엔티티와의 관계가 끊어진 자식 엔티티를 자동으로 삭제합니다.

```java
@OneToMany(mappedBy = "post", orphanRemoval = true)
private List<Comment> comments;

// 사용
Post post = postRepository.findById(1L).orElseThrow();
post.getComments().remove(0);  // 리스트에서 제거
postRepository.save(post);     // 해당 Comment가 DB에서도 삭제됨
```

## 주요 학습 포인트

1. **@OneToOne**: 1:1 관계 (User ↔ Profile)
2. **@OneToMany / @ManyToOne**: 1:N 관계 (Team ↔ Members)
3. **@ManyToMany**: N:N 관계 (Student ↔ Course)
4. **자기 참조**: 같은 엔티티 간 관계 (Comment ↔ Replies)
5. **연관관계의 주인**: 외래 키가 있는 쪽
6. **Cascade**: 작업 전파
7. **OrphanRemoval**: 고아 객체 자동 삭제

## 모범 사례

### 1. 양방향 관계는 신중하게

```java
// 필요한 경우만 양방향 사용
// 대부분의 경우 단방향으로 충분

// 단방향 (권장)
@Entity
public class Post {
    @ManyToOne
    private User author;
}

// 양방향 (필요한 경우만)
@Entity
public class User {
    @OneToMany(mappedBy = "author")
    private List<Post> posts;
}
```

### 2. 연관관계 편의 메서드 제공

```java
public void addPost(Post post) {
    posts.add(post);
    post.setAuthor(this);  // 양방향 관계 설정
}
```

### 3. Lazy Loading 사용

```java
@ManyToOne(fetch = FetchType.LAZY)  // 항상 LAZY 사용
private User author;
```

## 실습 파일

프로젝트의 Entity 파일들을 참고하세요:
- `src/main/java/com/example/jpa/entity/User.java`
- `src/main/java/com/example/jpa/entity/Post.java`
- `src/main/java/com/example/jpa/entity/Comment.java`
