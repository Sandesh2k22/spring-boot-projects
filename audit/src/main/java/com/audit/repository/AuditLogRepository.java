package com.audit.repository;

import com.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find all audit logs for a specific entity
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);
    
    // Find all audit logs by action type
    List<AuditLog> findByActionType(String actionType);
    
    // Find all audit logs by user
    List<AuditLog> findByChangedBy(String changedBy);
    
    // Find all audit logs for a specific entity name
    List<AuditLog> findByEntityName(String entityName);
    
    // Find audit logs within a date range
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    // Find audit logs by entity and date range
    @Query("SELECT a FROM AuditLog a WHERE a.entityName = :entityName " +
           "AND a.entityId = :entityId AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findByEntityAndDateRange(@Param("entityName") String entityName,
                                             @Param("entityId") Long entityId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    // Find latest audit log for an entity
    @Query(value = "SELECT * FROM audit_log WHERE entity_name = :entityName AND entity_id = :entityId " +
                   "ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    AuditLog findLatestAuditLog(@Param("entityName") String entityName, 
                                @Param("entityId") Long entityId);
}