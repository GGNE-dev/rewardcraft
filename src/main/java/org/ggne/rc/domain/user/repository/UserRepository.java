package org.ggne.rc.domain.user.repository;

import org.ggne.rc.domain.user.entity.OAuthProvider;
import org.ggne.rc.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // Ch 02 OAuth2 로그인: 소셜 플랫폼 + 해당 플랫폼의 사용자 ID로 회원 조회
    Optional<User> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    boolean existsByEmail(String email);
}
