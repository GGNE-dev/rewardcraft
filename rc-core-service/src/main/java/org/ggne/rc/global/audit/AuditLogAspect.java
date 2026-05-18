package org.ggne.rc.global.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.ggne.rc.domain.audit.entity.AuditLog;
import org.ggne.rc.domain.audit.repository.AuditLogRepository;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    // SpEL 파서 — targetIdSpEL 표현식("#userId" 등)을 실제 값으로 변환
    private final ExpressionParser parser = new SpelExpressionParser();

    // 메서드 파라미터 이름 추출 ("userId", "challengeId" 등)
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    // @Audited가 붙은 메서드 실행 전후를 가로챔
    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {

        // 1. SecurityContext에서 행위자 정보 수집
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long actorId = (Long) auth.getPrincipal();
        String actorRole = auth.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))   // "ROLE_ADMIN" → "ADMIN"
                .findFirst()
                .orElse("UNKNOWN");

        // 2. SpEL로 targetId 추출 — "#userId" 같은 표현식을 실제 인자 값으로 변환
        String targetId = resolveTargetId(joinPoint, audited.targetIdSpEL());

        // 3. 실제 메서드 실행
        Object result = joinPoint.proceed();

        // 4. 감사 로그 저장 — 실패해도 본 트랜잭션은 계속 진행 (가용성 우선 정책)
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes()).getRequest();

            AuditLog auditLog = AuditLog.builder()
                    .actorId(actorId)
                    .actorRole(actorRole)
                    .action(audited.action())
                    .targetType(audited.targetType())
                    .targetId(targetId)
                    .afterJson(objectMapper.writeValueAsString(result))
                    .ip(resolveClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            log.info("{}", auditLog);
            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            // 감사 로그 저장 실패가 실제 비즈니스 응답을 막아선 안 됨
            log.error("Audit log save failed: action={}, targetId={}", audited.action(), targetId, e);
        }

        return result;
    }

    // SpEL 표현식("#userId")을 메서드 인자 값(42L)으로 변환
    private String resolveTargetId(ProceedingJoinPoint joinPoint, String spel) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = nameDiscoverer.getParameterNames(signature.getMethod());
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return String.valueOf(parser.parseExpression(spel).getValue(context));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
