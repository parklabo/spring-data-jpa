-- 초기 데이터 삽입 (개발 및 테스트용)
-- Spring Boot는 schema.sql과 data.sql을 자동으로 실행

-- 사용자 데이터
INSERT INTO users (username, email, phone_number, age, status, created_at, updated_at)
VALUES
    ('홍길동', 'hong@example.com', '010-1234-5678', 25, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('김철수', 'kim@example.com', '010-2345-6789', 30, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('이영희', 'lee@example.com', '010-3456-7890', 28, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('박민수', 'park@example.com', '010-4567-8901', 35, 'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('정수진', 'jung@example.com', '010-5678-9012', 27, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 게시글 데이터
INSERT INTO posts (title, content, view_count, published, author_id, created_at, updated_at)
VALUES
    ('Spring Data JPA 시작하기', 'Spring Data JPA는 데이터 접근 계층을 쉽게 구현할 수 있게 도와줍니다.', 150, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JPA Entity 설계 패턴', 'Entity 설계 시 고려해야 할 사항들을 정리했습니다.', 230, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Repository 패턴의 이해', 'Repository 패턴은 데이터 소스에 대한 추상화를 제공합니다.', 180, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('쿼리 메서드 활용하기', '메서드 이름만으로 쿼리를 생성하는 방법을 알아봅니다.', 95, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('N+1 문제 해결하기', 'JPA 사용 시 흔히 발생하는 N+1 문제와 해결 방법', 320, true, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('임시 저장 글', '아직 작성 중인 글입니다.', 5, false, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 댓글 데이터
INSERT INTO comments (content, post_id, user_id, created_at, updated_at)
VALUES
    ('좋은 글 감사합니다!', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('많은 도움이 되었습니다.', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('궁금한 점이 있는데 댓글로 질문해도 될까요?', 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('물론이죠! 무엇이든 물어보세요.', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Entity 설계 시 주의사항 정리가 유용했어요.', 2, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Repository 패턴을 처음 접했는데 이해가 잘 되네요.', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('예제 코드가 있으면 더 좋을 것 같아요.', 4, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('N+1 문제 해결 방법이 명확하게 설명되어 있어서 좋습니다.', 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Fetch Join과 EntityGraph 비교가 인상적이었습니다.', 5, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
