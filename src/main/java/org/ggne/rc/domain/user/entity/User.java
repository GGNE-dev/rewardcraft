package org.ggne.rc.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"provider", "provider_user_id"}  // 같은 소셜 계정으로 중복 가입 방지
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자 — 외부에서 직접 new User() 금지
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // PostgreSQL BIGSERIAL (auto increment)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)  // DB에 "KAKAO", "GOOGLE" 문자열로 저장 (ORDINAL은 순서 변경 시 깨짐)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;  // 소셜 플랫폼이 발급한 고유 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String nickname, OAuthProvider provider,
                String providerUserId, UserRole role) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // setter 대신 의도를 드러내는 비즈니스 메서드
    public void updateNickname(String newNickname) {
        if (newNickname == null || newNickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비워둘 수 없습니다.");
        }
        this.nickname = newNickname;
    }
}
