package org.ggne.rc.domain.reward.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ggne.rc.global.entity.BaseEntity;

@Entity
@Table(name = "rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "required_points", nullable = false)
    private Long requiredPoints;

    @Column(name = "total_stock", nullable = false)
    private Long totalStock;

    @Column(name = "remaining_stock", nullable = false)
    private Long remainingStock;

    @Builder
    public Reward(String name, Long requiredPoints, Long totalStock) {
        this.name = name;
        this.requiredPoints = requiredPoints;
        this.totalStock = totalStock;
        this.remainingStock = totalStock;   // 초기 재고 = 전체 수량
    }

    public void decreaseStock() {
        if (this.remainingStock <= 0) {
            throw new IllegalStateException("재고가 없습니다.");
        }
        this.remainingStock--;
    }

    // 관리자 재고 조정 — 양수면 추가, 음수면 차감. remainingStock는 0 이상, totalStock 이하
    public void adjustStock(long delta) {
        long next = this.remainingStock + delta;
        if (next < 0) throw new IllegalArgumentException("재고는 0 미만이 될 수 없습니다.");
        this.remainingStock = next;
        if (delta > 0) this.totalStock += delta; // 재고 추가 시 전체 수량도 늘림
    }
}
