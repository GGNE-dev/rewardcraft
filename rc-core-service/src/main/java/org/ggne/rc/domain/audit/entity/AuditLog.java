package org.ggne.rc.domain.audit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_actor_created", columnList = "actor_id, created_at"),  // 특정 운영자가 최근에 한 일 조회
        @Index(name = "idx_audit_target", columnList = "target_type, target_id")        // 특정 사용자가 최근에 한 일 조회
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long actorId;           // 행위를 수행한 운영자 userId

    @Column(nullable = false)
    private String actorRole;       // 행위 시점의 역할 (OPERATOR, ADMIN)

    @Column(nullable = false, length = 100)
    private String action;          // 행위 종류 (USER_BAN, CHALLENGE_DELETE 등)

    @Column(nullable = false, length = 50)
    private String targetType;      // 대상 도메인 (USER, CHALLENGE 등)

    @Column(nullable = false, length = 100)
    private String targetId;        // 대상 ID

    @Column(columnDefinition = "TEXT")
    private String beforeJson;      // 변경 전 상태 (JSON)

    @Column(columnDefinition = "TEXT")
    private String afterJson;       // 변경 후 상태 — 메서드 반환값 직렬화

    @Column(length = 45)
    private String ip;              // 요청 IP

    @Column(length = 500)
    private String userAgent;       // 요청 User-Agent

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public AuditLog(Long actorId, String actorRole, String action, String targetType,
                    String targetId, String beforeJson, String afterJson,
                    String ip, String userAgent) {
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.beforeJson = beforeJson;
        this.afterJson = afterJson;
        this.ip = ip;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
    }
}
