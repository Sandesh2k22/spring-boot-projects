package com.audit.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the method parameter that holds the affected entity's id.
 *
 * <p>Needed for actions where the id is an input rather than part of the
 * return value — e.g. UPDATE and DELETE, where the id is passed in but the
 * method may return void. For CREATE the aspect instead reads {@code getId()}
 * from the returned entity, so this annotation is optional there.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditEntityId {
}