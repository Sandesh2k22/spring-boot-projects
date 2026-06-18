package com.audit.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method whose execution should produce an audit log entry.
 *
 * <p>This annotation is the <b>pointcut marker</b>: {@code AuditAspect} advises
 * every method annotated with it. Put it on a method and auditing happens
 * automatically — no audit code inside the method body.
 *
 * <pre>
 * &#64;Auditable(action = AuditAction.CREATE, entity = "User")
 * public User createUser(User user, &#64;AuditActor String changedBy) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** The action type recorded in the audit entry. */
    AuditAction action();

    /** The entity name recorded in the audit entry, e.g. "User". */
    String entity();
}