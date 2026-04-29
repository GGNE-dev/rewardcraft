package org.ggne.rc.domain.user.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.dto.UserSearchCondition;
import org.ggne.rc.domain.user.entity.QUser;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> search(UserSearchCondition condition, Pageable pageable) {

        QUser user = QUser.user;

        List<User> content = queryFactory.selectFrom(user)
                .where(
                        emailContains(condition.getEmail()),
                        nicknameEq(condition.getNickname()),
                        roleEq(condition.getRole()),
                        createdAtBetween(condition.getCreatedFrom(), condition.getCreatedTo())
                )
                .orderBy(user.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(user.count())
                .from(user)
                .where(
                        emailContains(condition.getEmail()),
                        nicknameEq(condition.getNickname()),
                        roleEq(condition.getRole()),
                        createdAtBetween(condition.getCreatedFrom(), condition.getCreatedTo())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);       // 페이징 결과 조립
    }

    private BooleanExpression emailContains(String email) {
        return StringUtils.hasText(email) ? QUser.user.email.contains(email) : null;
    }

    private BooleanExpression nicknameEq(String nickname) {
        return StringUtils.hasText(nickname) ? QUser.user.nickname.eq(nickname) : null;
    }

    private BooleanExpression roleEq(UserRole role) {
        return role != null ? QUser.user.role.eq(role) : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) return null;
        if (from == null) return QUser.user.createdAt.loe(to);
        if (to == null) return QUser.user.createdAt.goe(from);
        return QUser.user.createdAt.between(from, to);
    }
}
