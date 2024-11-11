package com.finsightanalytics.recommendationengine.model;

import java.math.BigDecimal;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import com.finsightanalytics.common.model.Role;

@Node("User")
public class Neo4jUser {

    @Id
    private Long userId; // Maps to SQL user ID for direct reference

    private String name;
    private String email;
    private Long phoneNumber;
    private Role role;
    private BigDecimal totalSavings;
    private String ifscCode;

    // Constructors, getters, and setters

    public Neo4jUser() {}

    public Neo4jUser(Long userId, String name, String email, Long phoneNumber, Role role, BigDecimal totalSavings, String ifscCode) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.totalSavings = totalSavings;
        this.ifscCode = ifscCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }
}
