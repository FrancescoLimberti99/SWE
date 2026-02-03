package com.todolistmanager.model;

public class Project extends BaseModel {
    private Long userId; //foreign key
    private String name;
    private String description;

    //costruttori
    public Project() {
        super();
    }

    public Project(Long id, Long userId, String name, String description) {
        super(id);
        this.userId = userId;
        this.name = name;
        this.description = description;
    }

    public Project(Long userId, String name, String description) {
        super();
        this.userId = userId;
        this.name = name;
        this.description = description;
    }

    //getters-setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Project{id=" + getId() + ", userId=" + userId + ", name='" + name + 
               "', description='" + description + "', createdAt=" + getCreatedAt() + "}";
    }
}