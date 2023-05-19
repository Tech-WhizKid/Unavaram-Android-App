package com.example.aaharapp;

public class User {
    private String name;
    private String email;
    private String phone;
    private String DOB;
    private String profilePicture;

    public User() {}

    public User(String name, String email, String phone, String DOB, String profilePicture) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.DOB = DOB;
        this.profilePicture = profilePicture;
    }

    public String getUserName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phone;
    }

    public String getDateOfBirth() {
        return DOB;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
