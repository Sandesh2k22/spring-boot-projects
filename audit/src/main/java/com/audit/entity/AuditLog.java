package com.audit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "action_type", nullable = false)
    private String actionType;  // CREATE, UPDATE, DELETE
    
    @Column(name = "entity_name", nullable = false)
    private String entityName;  // User, Product, etc.
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "changed_by", nullable = false)
    private String changedBy;  // Username
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;  // JSON format
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;  // JSON format
    
    @Column(name = "description")
    private String description;
    
    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditLog(String actionType, String entityName, Long entityId, 
                    String changedBy, String oldValues, String newValues) {
        this.actionType = actionType;
        this.entityName = entityName;
        this.entityId = entityId;
        this.changedBy = changedBy;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getOldValues() {
        return oldValues;
    }
    
    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }
    
    public String getNewValues() {
        return newValues;
    }
    
    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}