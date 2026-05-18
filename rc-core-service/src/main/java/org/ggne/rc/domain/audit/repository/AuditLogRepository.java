package org.ggne.rc.domain.audit.repository;

import org.ggne.rc.domain.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // 특정 운영자가 수행한 행위 목록 — idx_audit_actor_created 인덱스 활용
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId);

    // 특정 대상에게 일어난 행위 목록 — idx_audit_target 인덱스 활용
    List<AuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId);
}
