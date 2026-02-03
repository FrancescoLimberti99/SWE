package com.todolistmanager.dao;

import com.todolistmanager.model.Priority;
import com.todolistmanager.model.Task;
import com.todolistmanager.model.TaskStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskDAO {
    
    //salva task nel database
    Task save(Task task);
    
    //aggiorna task
    void update(Task task);
    
    //ricerca
    Optional<Task> findById(Long id);
    
    List<Task> findByProjectId(Long projectId);
    
    List<Task> findByStatus(TaskStatus status);
    
    List<Task> findByPriority(Priority priority);
    
    List<Task> findByDeadlineBefore(LocalDate date);
    
    List<Task> findOverdueTasks();
    
    List<Task> findAll();
    
    
    //elimina task
    void delete(Long id);
    
    //counter
    int countByProjectIdAndStatus(Long projectId, TaskStatus status);
}