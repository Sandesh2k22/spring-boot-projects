package com.audit.aop;

import com.audit.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * The audit aspect: cross-cutting logic that writes an audit log entry for every
 * method annotated with {@link Auditable}, without that method containing any
 * audit code itself.
 *
 * <p><b>Pointcut:</b> {@code @annotation(auditable)} — matches any join point
 * (method execution) whose method carries the {@code @Auditable} annotation, and
 * binds that annotation instance so we can read its {@code action} and
 * {@code entity}.
 *
 * <p><b>Advice:</b> {@code @Around} — wraps the target method. We call
 * {@code proceed()} to run the real business logic first, then build the audit
 * entry from the method's arguments and return value.
 *
 * <p><b>Ordering:</b> {@code @Order(LOWEST_PRECEDENCE)} makes this aspect the
 * <i>innermost</i> advice, so Spring's {@code @Transactional} advice wraps it.
 * The audit insert therefore runs inside the same transaction as the business
 * operation — if either fails, both roll back together.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    // Constructor injection — the aspect is a normal Spring bean.
    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // 1. Run the actual business method first.
        Object result = joinPoint.proceed();

        // 2. Gather audit metadata from the call.
        AuditAction action = auditable.action();
        String entityName = auditable.entity();
        String changedBy = resolveActor(joinPoint);
        Long entityId = resolveEntityId(joinPoint, result);

        // 3. The "new" snapshot is the method's return value for CREATE/UPDATE;
        //    a DELETE has no resulting state.
        Object newValues = action != AuditAction.DELETE ? result : null;

        // 4. Persist the audit entry. createAuditLog rethrows on failure, so a
        //    broken audit write rolls back the surrounding transaction.
        auditService.createAuditLog(action.name(), entityName, entityId, changedBy, null, newValues);
        log.debug("Audited {} on {}/{} by {}", action, entityName, entityId, changedBy);

        return result;
    }

    /** Reads the value of the parameter annotated with {@link AuditActor}. */
    private String resolveActor(ProceedingJoinPoint joinPoint) {
        Object value = findAnnotatedArg(joinPoint, AuditActor.class);
        return value != null ? value.toString() : "SYSTEM";
    }

    /**
     * Resolves the entity id: prefer the parameter annotated with
     * {@link AuditEntityId} (needed for UPDATE/DELETE), otherwise fall back to
     * {@code getId()} on the returned entity (the CREATE case).
     */
    private Long resolveEntityId(ProceedingJoinPoint joinPoint, Object result) {
        Object fromParam = findAnnotatedArg(joinPoint, AuditEntityId.class);
        if (fromParam instanceof Long id) {
            return id;
        }
        return invokeGetId(result);
    }

    /** Finds the argument whose parameter carries the given annotation. */
    private Object findAnnotatedArg(ProceedingJoinPoint joinPoint, Class<? extends Annotation> annotation) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (annotation.isInstance(a)) {
                    return args[i];
                }
            }
        }
        return null;
    }

    /** Reflectively calls getId() on an entity, returning null if unavailable. */
    private Long invokeGetId(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            Method getId = entity.getClass().getMethod("getId");
            Object id = getId.invoke(entity);
            return id instanceof Long value ? value : null;
        } catch (Exception e) {
            log.warn("Could not read getId() from {}", entity.getClass().getSimpleName());
            return null;
        }
    }
}