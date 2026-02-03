package com.todolistmanager.model;

import java.time.LocalDateTime;

public abstract class BaseModel {
    private Long id;
    private LocalDateTime createdAt;

    //costruttori
    public BaseModel() {
        this.createdAt = LocalDateTime.now();
    }

    public BaseModel(Long id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public BaseModel(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    //getters-setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}