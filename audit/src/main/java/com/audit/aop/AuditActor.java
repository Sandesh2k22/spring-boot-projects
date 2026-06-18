package com.audit.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the method parameter that holds the user performing the change
 * (the "changed by" value). The aspect reads this parameter to populate
 * {@code changedBy} in the audit entry.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditActor {
}