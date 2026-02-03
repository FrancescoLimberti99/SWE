package com.todolistmanager.observer;

import com.todolistmanager.model.Task;
import java.time.LocalDateTime;

public class AuditLogger implements TaskObserver {
    
    @Override
    public void onTaskCreated(Task task) {
        log("CREATE", "Task creata: " + task.getTitle());
    }
    
    @Override
    public void onTaskUpdated(Task task) {
        log("UPDATE", "Task modificata: " + task.getTitle());
    }
    
    @Override
    public void onTaskDeleted(Long taskId) {
        log("DELETE", "Task eliminata: ID=" + taskId);
    }
    
    @Override
    public void onTaskStatusChanged(Task task) {
        log("STATUS_CHANGE", "Task '" + task.getTitle() + "' -> " + task.getStatus());
    }
    
    private void log(String action, String message) {
        System.out.println("[" + LocalDateTime.now() + "] [" + action + "] " + message);
    }
}