package org.ggne.rc.global.security.oauth2;

public interface OAuthUserInfo {

    String getProviderUserId();
    String getEmail();
    String getNickname();
}
