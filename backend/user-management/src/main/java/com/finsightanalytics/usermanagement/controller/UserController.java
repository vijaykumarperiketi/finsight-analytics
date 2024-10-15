package com.finsightanalytics.usermanagement.controller;

import com.finsightanalytics.common.exception.UserNotFoundException;
import com.finsightanalytics.common.model.User;
import com.finsightanalytics.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('BANKER')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Financial Goals Endpoints
    @GetMapping("/{userId}/financialGoals")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER', 'CUSTOMER')")
    public ResponseEntity<List<FinancialGoal>> getUserFinancialGoals(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getFinancialGoalsByUserId(userId));
    }

    @GetMapping("/{userId}/financialGoals/{goalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER', 'CUSTOMER')")
    public ResponseEntity<FinancialGoal> getUserFinancialGoalById(@PathVariable Long userId, @PathVariable Long goalId) {
        return ResponseEntity.ok(userService.getUserFinancialGoalById(userId, goalId));
    }

    @PostMapping("/{userId}/financialGoals")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FinancialGoal> createFinancialGoal(@PathVariable Long userId, @RequestBody FinancialGoal goal) {
        return ResponseEntity.ok(userService.addFinancialGoal(userId, goal));
    }

    @PutMapping("/{userId}/financialGoals/{goalId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FinancialGoal> updateFinancialGoal(@PathVariable Long userId, @PathVariable Long goalId, @RequestBody FinancialGoal goal) {
        return ResponseEntity.ok(userService.updateFinancialGoal(userId, goalId, goal));
    }

    // Debt Management Endpoints
    @GetMapping("/{userId}/debts")
    @PreAuthorize("hasAnyRole('ADMIN', 'BANKER', 'CUSTOMER')")
    public ResponseEntity<List<Debt>> getUserDebts(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getDebtsByUserId(userId));
    }

    @PostMapping("/{userId}/debts")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Debt> createDebt(@PathVariable Long userId, @RequestBody Debt debt) {
        return ResponseEntity.ok(userService.addDebt(userId, debt));
    }

    @PutMapping("/{userId}/debts/{debtId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Debt> updateDebt(@PathVariable Long userId, @PathVariable Long debtId, @RequestBody Debt debt) {
        return ResponseEntity.ok(userService.updateDebt(userId, debtId, debt));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active/{active}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getActiveUsers(@PathVariable boolean active) {
        List<User> users = userService.getActiveUsers(active);
        return ResponseEntity.ok(users);
    }
}


