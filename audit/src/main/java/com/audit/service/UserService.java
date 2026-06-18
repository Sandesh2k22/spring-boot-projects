package com.audit.service;

import com.audit.aop.AuditAction;
import com.audit.aop.AuditActor;
import com.audit.aop.AuditEntityId;
import com.audit.aop.Auditable;
import com.audit.entity.User;
import com.audit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new user. The audit entry is written automatically by
     * AuditAspect because of the @Auditable annotation — no audit code here.
     */
    @Transactional
    @Auditable(action = AuditAction.CREATE, entity = "User")
    public User createUser(User user, @AuditActor String changedBy) {
        return userRepository.save(user);
    }

    /**
     * Update user. The aspect reads the id from the @AuditEntityId parameter
     * and the new state from the returned User.
     */
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "User")
    public User updateUser(@AuditEntityId Long userId, User userDetails, @AuditActor String changedBy) {
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update user fields
        oldUser.setUsername(userDetails.getUsername());
        oldUser.setEmail(userDetails.getEmail());
        oldUser.setFirstName(userDetails.getFirstName());
        oldUser.setLastName(userDetails.getLastName());
        oldUser.setIsActive(userDetails.getIsActive());

        return userRepository.save(oldUser);
    }

    /**
     * Delete user. The aspect records a DELETE using the @AuditEntityId
     * parameter; the method returns void so there is no "new" state.
     */
    @Transactional
    @Auditable(action = AuditAction.DELETE, entity = "User")
    public void deleteUser(@AuditEntityId Long userId, @AuditActor String changedBy) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}