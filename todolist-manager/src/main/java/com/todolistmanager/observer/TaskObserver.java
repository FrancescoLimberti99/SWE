package com.todolistmanager.observer;

import com.todolistmanager.model.Task;

public interface TaskObserver {
    void onTaskCreated(Task task);
    void onTaskUpdated(Task task);
    void onTaskDeleted(Long taskId);
    void onTaskStatusChanged(Task task);
}