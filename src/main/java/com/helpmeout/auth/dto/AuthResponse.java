package com.helpmeout.auth.dto;

public class AuthResponse {

    private Long userId;
    private String phone;
    private String fullName;
    private String token;
    private String role;
    private String phoneVerificationStatus;

    // 🔧 Constructor
    public AuthResponse(Long userId, String phone, String fullName,
                        String token, String role, String phoneVerificationStatus) {
        this.userId = userId;
        this.phone = phone;
        this.fullName = fullName;
        this.token = token;
        this.role = role;
        this.phoneVerificationStatus = phoneVerificationStatus;
    }

    // 🔧 Empty constructor (optional but useful)
    public AuthResponse() {}

    // ✅ Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhoneVerificationStatus() { return phoneVerificationStatus; }
    public void setPhoneVerificationStatus(String phoneVerificationStatus) {
        this.phoneVerificationStatus = phoneVerificationStatus;
    }

    // 🔥 MANUAL BUILDER
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String phone;
        private String fullName;
        private String token;
        private String role;
        private String phoneVerificationStatus;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder phoneVerificationStatus(String status) {
            this.phoneVerificationStatus = status;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(
                    userId,
                    phone,
                    fullName,
                    token,
                    role,
                    phoneVerificationStatus
            );
        }
    }
}