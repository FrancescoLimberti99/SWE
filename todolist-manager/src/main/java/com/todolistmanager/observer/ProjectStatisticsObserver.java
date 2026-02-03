package com.todolistmanager.observer;

import com.todolistmanager.model.Task;

public class ProjectStatisticsObserver implements TaskObserver {
    
    @Override
    public void onTaskCreated(Task task) {
        System.out.println("📊 Statistiche progetto aggiornate (nuova task aggiunta)");
    }
    
    @Override
    public void onTaskUpdated(Task task) {
        System.out.println("📊 Statistiche progetto aggiornate (task modificata)");
    }
    
    @Override
    public void onTaskDeleted(Long taskId) {
        System.out.println("📊 Statistiche progetto aggiornate (task eliminata)");
    }
    
    @Override
    public void onTaskStatusChanged(Task task) {
        System.out.println("📊 Completamento progetto aggiornato");
    }
}