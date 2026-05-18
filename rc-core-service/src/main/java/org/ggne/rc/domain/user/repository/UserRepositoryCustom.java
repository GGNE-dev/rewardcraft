package org.ggne.rc.domain.user.repository;

import org.ggne.rc.domain.user.dto.UserSearchCondition;
import org.ggne.rc.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> search(UserSearchCondition condition, Pageable pageable);
}
