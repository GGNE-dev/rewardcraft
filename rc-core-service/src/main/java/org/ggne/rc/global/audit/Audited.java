package org.ggne.rc.global.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    String action();        // 행위 종류 — 예: "USER_BAN", "CHALLENGE_DELETE"
    String targetType();    // 대상 도메인 — 예: "USER", "CHALLENGE"
    String targetIdSpEL();  // 메서드 인자에서 대상 ID를 꺼내는 SpEL 표현식 — 예: "#userId"
}