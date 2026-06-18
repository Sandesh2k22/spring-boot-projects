package com.audit.controller;

import com.audit.entity.User;
import com.audit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user,
                                          @RequestHeader(value = "X-Changed-By", defaultValue = "SYSTEM") String changedBy) {
        try {
            User createdUser = userService.createUser(user, changedBy);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                          @RequestBody User userDetails,
                                          @RequestHeader(value = "X-Changed-By", defaultValue = "SYSTEM") String changedBy) {
        try {
            User updatedUser = userService.updateUser(id, userDetails, changedBy);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                          @RequestHeader(value = "X-Changed-By", defaultValue = "SYSTEM") String changedBy) {
        try {
            userService.deleteUser(id, changedBy);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}