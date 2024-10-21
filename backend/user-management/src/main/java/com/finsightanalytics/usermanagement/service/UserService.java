package com.finsightanalytics.usermanagement.service;

import com.finsightanalytics.common.exception.UserNotFoundException;
import com.finsightanalytics.common.exception.InsufficientBalanceException;
import com.finsightanalytics.common.model.User;
import com.finsightanalytics.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException("User not found with id: " + user.getId());
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
                    financialGoal.setSavedAmount(financialGoal.getSavedAmount().add(amount));
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
}
