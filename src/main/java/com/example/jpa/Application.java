package com.example.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data JPA 학습 애플리케이션
 *
 * @EnableJpaAuditing: JPA Auditing 기능 활성화
 *                     엔티티의 생성/수정 시간을 자동으로 관리
 */
@SpringBootApplication
@EnableJpaAuditing
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        System.out.println("\n" +
            "========================================\n" +
            "  Spring Data JPA 학습 애플리케이션 시작  \n" +
            "========================================\n" +
            "H2 Console: http://localhost:8080/h2-console\n" +
            "JDBC URL: jdbc:h2:mem:testdb\n" +
            "Username: sa\n" +
            "Password: \n" +
            "========================================\n");
    }
}
