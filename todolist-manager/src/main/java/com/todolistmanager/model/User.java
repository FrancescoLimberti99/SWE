package com.todolistmanager.model;

public class User extends BaseModel {
    private String username;
    private String email;

    //costruttori
    public User() {
        super();
    }

    public User(Long id, String username, String email) {
        super(id);
        this.username = username;
        this.email = email;
    }

    public User(String username, String email) {
        super();
        this.username = username;
        this.email = email;
    }

    //getters-setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{id=" + getId() + ", username='" + username + "', email='" + email + "', createdAt=" + getCreatedAt() + "}";
    }
}