package com.example.lotterypatentpending;

public class User {

    private String userId;
    private String name;
    private String email;
    private String role; // Entrant or Organizer
    private String contactInfo;



    public User(String userId, String name, String email, String contactInfo, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.contactInfo = contactInfo;
        this.role = role;
    }

    // --- Getters & Setters ---
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}