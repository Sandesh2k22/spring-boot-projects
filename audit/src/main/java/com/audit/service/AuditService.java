package com.audit.service;

import com.audit.entity.AuditLog;
import com.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    /**
     * Create an audit log entry
     */
    public AuditLog createAuditLog(String actionType, String entityName, Long entityId,
                                   String changedBy, Object oldValues, Object newValues) {
        try {
            String oldValuesJson = oldValues != null ? objectMapper.writeValueAsString(oldValues) : null;
            String newValuesJson = newValues != null ? objectMapper.writeValueAsString(newValues) : null;
            
            AuditLog auditLog = new AuditLog();
            auditLog.setActionType(actionType);
            auditLog.setEntityName(entityName);
            auditLog.setEntityId(entityId);
            auditLog.setChangedBy(changedBy);
            auditLog.setOldValues(oldValuesJson);
            auditLog.setNewValues(newValuesJson);
            auditLog.setTimestamp(LocalDateTime.now());
            
            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Do not swallow: a failed audit write must roll back the operation
            // it belongs to, otherwise the audit trail silently loses entries.
            throw new RuntimeException("Failed to write audit log for "
                    + entityName + "/" + entityId, e);
        }
    }
    
    /**
     * Create CREATE action audit log
     */
    public AuditLog auditCreate(String entityName, Long entityId, String changedBy, Object newValues) {
        return createAuditLog("CREATE", entityName, entityId, changedBy, null, newValues);
    }
    
    /**
     * Create UPDATE action audit log
     */
    public AuditLog auditUpdate(String entityName, Long entityId, String changedBy, 
                                Object oldValues, Object newValues) {
        return createAuditLog("UPDATE", entityName, entityId, changedBy, oldValues, newValues);
    }
    
    /**
     * Create DELETE action audit log
     */
    public AuditLog auditDelete(String entityName, Long entityId, String changedBy, Object oldValues) {
        return createAuditLog("DELETE", entityName, entityId, changedBy, oldValues, null);
    }
    
    /**
     * Get all audit logs for a specific entity
     */
    public List<AuditLog> getAuditLogsForEntity(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId);
    }
    
    /**
     * Get all audit logs by action type
     */
    public List<AuditLog> getAuditLogsByActionType(String actionType) {
        return auditLogRepository.findByActionType(actionType);
    }
    
    /**
     * Get all audit logs by user
     */
    public List<AuditLog> getAuditLogsByUser(String changedBy) {
        return auditLogRepository.findByChangedBy(changedBy);
    }
    
    /**
     * Get all audit logs for an entity within a date range
     */
    public List<AuditLog> getAuditLogsWithinDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Get audit logs for specific entity within date range
     */
    public List<AuditLog> getAuditLogsForEntityInDateRange(String entityName, Long entityId,
                                                            LocalDateTime startDate, 
                                                            LocalDateTime endDate) {
        return auditLogRepository.findByEntityAndDateRange(entityName, entityId, startDate, endDate);
    }
    
    /**
     * Get latest audit log for an entity
     */
    public AuditLog getLatestAuditLog(String entityName, Long entityId) {
        return auditLogRepository.findLatestAuditLog(entityName, entityId);
    }
}