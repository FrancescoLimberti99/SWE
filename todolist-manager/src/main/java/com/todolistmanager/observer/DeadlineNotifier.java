package com.todolistmanager.observer;

import com.todolistmanager.model.Task;
import com.todolistmanager.model.TaskStatus;
import java.time.LocalDate;

public class DeadlineNotifier implements TaskObserver {
    
    @Override
    public void onTaskCreated(Task task) {
        checkDeadline(task);
    }
    
    @Override
    public void onTaskUpdated(Task task) {
        checkDeadline(task);
    }
    
    @Override
    public void onTaskDeleted(Long taskId) {
        //nessuna azione
    }
    
    @Override
    public void onTaskStatusChanged(Task task) {
        
        if (task.getStatus() == TaskStatus.DONE) {
            System.out.println("✅ Task completata: " + task.getTitle());
        }
    }
    
    private void checkDeadline(Task task) {
        if (task.getDeadline() == null) return;
        
        long daysUntilDeadline = LocalDate.now().until(task.getDeadline()).getDays();
        
        if (daysUntilDeadline < 0) {
            System.out.println("🚨 ATTENZIONE: Task '" + task.getTitle() + "' è in RITARDO di " + 
                             Math.abs(daysUntilDeadline) + " giorni!");
        } else if (daysUntilDeadline <= 3) {
            System.out.println("⚠️  Task '" + task.getTitle() + "' scade tra " + 
                             daysUntilDeadline + " giorni!");
        } else if (daysUntilDeadline <= 7) {
            System.out.println("📅 Task '" + task.getTitle() + "' scade tra " + 
                             daysUntilDeadline + " giorni");
        }
    }
}