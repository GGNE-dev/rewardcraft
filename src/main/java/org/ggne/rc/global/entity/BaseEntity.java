package org.ggne.rc.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 *  생성, 수정일자를 관리하는 공통 클래스
 *  - 추상 클래스로 선언하여 직접 인스터스 되는 것을 방지
 */
@Getter
@MappedSuperclass                                   // 자식 엔티티의 테이블에 컬럼으로 포함
@EntityListeners(AuditingEntityListener.class)      // 스프링 JPA가 이 클래스의 날짜 필드 자동으로 채우도록 등록
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
