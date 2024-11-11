package com.finsightanalytics.usermanagement.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finsightanalytics.common.exception.DebtNotFoundException;
import com.finsightanalytics.common.exception.FinancialGoalNotFoundException;
import com.finsightanalytics.common.exception.InsufficientBalanceException;
import com.finsightanalytics.common.exception.InvalidTransactionException;
import com.finsightanalytics.common.exception.UserNotFoundException;
import com.finsightanalytics.common.model.Debt;
import com.finsightanalytics.common.model.FinancialGoal;
import com.finsightanalytics.common.model.User;
import com.finsightanalytics.usermanagement.repository.DebtRepository;
import com.finsightanalytics.usermanagement.repository.FinancialGoalRepository;
import com.finsightanalytics.usermanagement.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialGoalRepository financialGoalRepository;

    @Autowired
    private DebtRepository debtRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }

    public User updateUser(User user) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new UserNotFoundException("User not found with id: " + user.getUserId());
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }


    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public List<User> getActiveUsers(boolean active) {
        return userRepository.findAllByActive(active);
    }

    public void updateFinancialGoalsAndDebtsBasedOnTransaction(Long userId, Long financialGoalId, Long debtId, String transactionType, BigDecimal amount) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        User user = userOptional.get();

        // Validate that debit transactions don't have financialGoalId or debtId
        if ("debit".equalsIgnoreCase(transactionType)) {
            if (financialGoalId != null || debtId != null) {
                throw new InvalidTransactionException("Debits cannot be associated with a financial goal or debt.");
            }
        }

        // Handle financial goals for credit transactions only
        if (financialGoalId != null) {
            Optional<FinancialGoal> financialGoalOptional = financialGoalRepository.findById(financialGoalId);
            if (financialGoalOptional.isPresent()) {
                FinancialGoal financialGoal = financialGoalOptional.get();
                if ("credit".equalsIgnoreCase(transactionType)) {
                    financialGoal.setCurrentAmount(financialGoal.getCurrentAmount().add(amount));
                    user.setTotalSavings(user.getTotalSavings().add(amount));
                }
                financialGoalRepository.save(financialGoal);
            } else {
                throw new FinancialGoalNotFoundException("Financial goal not found with ID: " + financialGoalId);
            }
        }

        // Handle debts for credit transactions only
        if (debtId != null) {
            Optional<Debt> debtOptional = debtRepository.findById(debtId);
            if (debtOptional.isPresent()) {
                Debt debt = debtOptional.get();
                if ("credit".equalsIgnoreCase(transactionType)) {
                    debt.setPaidAmount(debt.getPaidAmount().add(amount));
                }
                debtRepository.save(debt);
            } else {
                throw new DebtNotFoundException("Debt not found with ID: " + debtId);
            }
        }

        // If no financialGoalId or debtId, update user's balance only
        if (financialGoalId == null && debtId == null) {
            if ("credit".equalsIgnoreCase(transactionType)) {
                user.setBalance(user.getBalance().add(amount));
            } else if ("debit".equalsIgnoreCase(transactionType)) {
                if (user.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException("Insufficient balance for user with ID: " + userId);
                }
                user.setBalance(user.getBalance().subtract(amount));
            }
            userRepository.save(user);
        }
    }

    // Fetch financial goals by user ID
    public List<FinancialGoal> getFinancialGoalsByUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return financialGoalRepository.findByUserId(userId);
    }

    // Fetch a specific financial goal by user ID and goal ID
    public FinancialGoal getUserFinancialGoalById(Long userId, Long goalId) {
        Optional<FinancialGoal> financialGoalOptional = Optional.ofNullable(financialGoalRepository.findByIdAndUserId(goalId, userId));
        if (!financialGoalOptional.isPresent()) {
            throw new FinancialGoalNotFoundException("Financial goal not found with ID: " + goalId);
        }
        return financialGoalOptional.get();
    }

    // Add a new financial goal for a user
    public FinancialGoal addFinancialGoal(Long userId, FinancialGoal goal) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        goal.setUserId(userOptional.get().getUserId());
        return financialGoalRepository.save(goal);
    }

    // Update an existing financial goal for a user
    public FinancialGoal updateFinancialGoal(Long userId, Long goalId, FinancialGoal goal) {
        Optional<FinancialGoal> financialGoalOptional = Optional.ofNullable(financialGoalRepository.findByIdAndUserId(goalId, userId));
        if (!financialGoalOptional.isPresent()) {
            throw new FinancialGoalNotFoundException("Financial goal not found with ID: " + goalId);
        }
        FinancialGoal existingGoal = financialGoalOptional.get();
        existingGoal.setTargetAmount(goal.getTargetAmount());
        existingGoal.setCurrentAmount(goal.getCurrentAmount());
        return financialGoalRepository.save(existingGoal);
    }

    // Fetch debts by user ID
    public List<Debt> getDebtsByUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return debtRepository.findByUserId(userId);
    }

    // Fetch a specific debt by user ID and debt ID
    public Debt getUserDebtById(Long userId, Long debtId) {
        Optional<Debt> debtOptional = Optional.ofNullable(debtRepository.findByIdAndUserId(debtId, userId));
        if (!debtOptional.isPresent()) {
            throw new DebtNotFoundException("Debt not found with ID: " + debtId);
        }
        return debtOptional.get();
    }

    // Add a new debt for a user
    public Debt addDebt(Long userId, Debt debt) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        debt.setUserId(userOptional.get().getUserId());
        return debtRepository.save(debt);
    }

    // Update an existing debt for a user
    public Debt updateDebt(Long userId, Long debtId, Debt debt) {
        Optional<Debt> debtOptional = Optional.ofNullable(debtRepository.findByIdAndUserId(debtId, userId));
        if (!debtOptional.isPresent()) {
            throw new DebtNotFoundException("Debt not found with ID: " + debtId);
        }
        Debt existingDebt = debtOptional.get();
        existingDebt.setTotalAmount(debt.getTotalAmount());
        existingDebt.setPaidAmount(debt.getPaidAmount());
        return debtRepository.save(existingDebt);
    }
}
