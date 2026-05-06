package org.ggne.rc.global.security.oauth2;

import org.ggne.rc.domain.user.entity.OAuthProvider;

import java.util.Map;

public class OAuthUserInfoFactory {
    // static 유틸 — 인스턴스 생성 불필요
    private OAuthUserInfoFactory() {}

    public static OAuthUserInfo from(OAuthProvider provider, Map<String, Object> attributes) {
        return switch (provider) {
            case KAKAO -> new KakaoUserInfo(attributes);
            case GOOGLE -> new GoogleUserInfo(attributes);
        };
    }
}
