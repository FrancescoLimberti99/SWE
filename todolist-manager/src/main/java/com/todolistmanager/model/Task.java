package com.todolistmanager.model;

import java.time.LocalDate;

public class Task extends BaseModel {
    private Long projectId; //foreign key
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate deadline;
    private LocalDate completedAt;

    //costruttori
    public Task() {
        super();
        this.status = TaskStatus.TODO;
        this.priority = Priority.MEDIUM;
    }

    public Task(Long id, Long projectId, String title, String description, 
                TaskStatus status, Priority priority, LocalDate deadline) {
        super(id);
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
    }

    public Task(Long projectId, String title, String description, 
                Priority priority, LocalDate deadline) {
        super();
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;
        this.priority = priority;
        this.deadline = deadline;
    }

    //getters-setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        if (status == TaskStatus.DONE && this.completedAt == null) {
            this.completedAt = LocalDate.now();
        } else if (status != TaskStatus.DONE) {
            this.completedAt = null; 
        }
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDate completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "Task{id=" + getId() + ", projectId=" + projectId + ", title='" + title + 
               "', status=" + status + ", priority=" + priority + 
               ", deadline=" + deadline + ", createdAt=" + getCreatedAt() + "}";
    }
}