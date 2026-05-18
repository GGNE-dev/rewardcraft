package org.ggne.rc.global.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.entity.OAuthProvider;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.entity.UserRole;
import org.ggne.rc.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. 부모 클래스가 user-info-uri를 호출해서 소셜 사용자 정보(attributes)를 받아옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. "kakao" or "google" — application.yaml의 registration 키와 일치
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        // 3. provider별로 다른 응답 구조를 동일한 인터페이스로 추상화
        OAuthUserInfo userInfo = OAuthUserInfoFactory.from(provider, oAuth2User.getAttributes());

        // 4. DB 조회 → 없으면 최초 로그인이므로 자동 가입 (트랜잭션 묶음 처리를 위해 @Transactional 필요)
        User user = userRepository.findByProviderAndProviderUserId(provider, userInfo.getProviderUserId())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(userInfo.getEmail())
                                .nickname(userInfo.getNickname())
                                .provider(provider)
                                .providerUserId(userInfo.getProviderUserId())
                                .role(UserRole.USER)
                                .build()
                ));

        // 5. Spring Security가 인식하는 타입으로 감싸서 반환
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
