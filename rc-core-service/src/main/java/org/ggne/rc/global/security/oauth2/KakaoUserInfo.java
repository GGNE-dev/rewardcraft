package org.ggne.rc.global.security.oauth2;

import java.util.Map;

public class KakaoUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;       // 카카오 응답 최상위
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    /**
     *   {
     *     "id": 123456789,
     *     "kakao_account": {
     *       "profile": {          // profile은 kakao_account 안에 있음
     *         "nickname": "홍길동"
     *       }
     *     }
     *   }
     */
    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getProviderUserId() {
        // 카카오 ID는 Long 타입으로 전달됨 -> DB엔 String으로 저장
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getNickname() {
        return (String) profile.get("nickname");
    }
}
