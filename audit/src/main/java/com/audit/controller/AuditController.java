package com.audit.controller;

import com.audit.entity.AuditLog;
import com.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Get all audit logs for a specific entity
     */
    @GetMapping("/entity")
    public ResponseEntity<List<AuditLog>> getAuditLogsForEntity(
            @RequestParam String entityName,
            @RequestParam Long entityId) {
        List<AuditLog> auditLogs = auditService.getAuditLogsForEntity(entityName, entityId);
        return new ResponseEntity<>(auditLogs, HttpStatus.OK);
    }
    
    /**
     * Get all audit logs by action type
     */
    @GetMapping("/action")
    public ResponseEntity<List<AuditLog>> getAuditLogsByActionType(@RequestParam String actionType) {
        List<AuditLog> auditLogs = auditService.getAuditLogsByActionType(actionType);
        return new ResponseEntity<>(auditLogs, HttpStatus.OK);
    }
    
    /**
     * Get all audit logs by user
     */
    @GetMapping("/user")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@RequestParam String changedBy) {
        List<AuditLog> auditLogs = auditService.getAuditLogsByUser(changedBy);
        return new ResponseEntity<>(auditLogs, HttpStatus.OK);
    }
    
    /**
     * Get audit logs within date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsWithinDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> auditLogs = auditService.getAuditLogsWithinDateRange(startDate, endDate);
        return new ResponseEntity<>(auditLogs, HttpStatus.OK);
    }
    
    /**
     * Get audit logs for entity within date range
     */
    @GetMapping("/entity-date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsForEntityInDateRange(
            @RequestParam String entityName,
            @RequestParam Long entityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> auditLogs = auditService.getAuditLogsForEntityInDateRange(entityName, entityId, startDate, endDate);
        return new ResponseEntity<>(auditLogs, HttpStatus.OK);
    }
    
    /**
     * Get latest audit log for an entity
     */
    @GetMapping("/latest")
    public ResponseEntity<AuditLog> getLatestAuditLog(
            @RequestParam String entityName,
            @RequestParam Long entityId) {
        AuditLog auditLog = auditService.getLatestAuditLog(entityName, entityId);
        if (auditLog != null) {
            return new ResponseEntity<>(auditLog, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}