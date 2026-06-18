package com.audit.aop;

/**
 * The kind of change being audited. Mirrors the action_type values
 * stored in the audit_log table.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE
}