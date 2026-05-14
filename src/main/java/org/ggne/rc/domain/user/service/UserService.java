package org.ggne.rc.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.repository.UserRepository;
import org.ggne.rc.global.exception.BusinessException;
import org.ggne.rc.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateNickname(Long id, String newNickname) {
        User user = findById(id);
        user.updateNickname(newNickname);
        // 더티 체킹(Dirty Checking): @Transactional 범위 안에서 엔티티 필드를 변경하면
        // 트랜잭션 종료 시점에 JPA가 자동으로 UPDATE 쿼리를 날린다. save() 호출 불필요.
        return user;
    }
}
