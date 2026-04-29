package org.ggne.rc.domain.user.dto;

import lombok.Getter;
import lombok.Setter;
import org.ggne.rc.domain.user.entity.UserRole;

import java.time.LocalDateTime;

/**
 *  검색 조건 DTO
 */
@Setter
@Getter
public class UserSearchCondition {

    private String email;
    private String nickname;
    private UserRole role;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
