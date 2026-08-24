package dev.learning.springcore.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
final class AuditAspect {

    private final AuditTrail trail;

    AuditAspect(AuditTrail trail) {
        this.trail = trail;
    }

    @AfterReturning("@annotation(dev.learning.springcore.audit.Audited)")
    void afterSuccess(JoinPoint joinPoint) {
        trail.record(joinPoint.getSignature().toShortString());
    }
}

