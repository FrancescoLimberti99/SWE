package com.todolistmanager.controller;

import com.todolistmanager.dao.TaskDAO;
import com.todolistmanager.dao.TaskDAOImpl;
import com.todolistmanager.model.Priority;
import com.todolistmanager.model.Task;
import com.todolistmanager.model.TaskStatus;
import com.todolistmanager.observer.TaskObserver;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskController {
    
    private final TaskDAO taskDAO;
    private final List<TaskObserver> observers;

    //costruttore per test
    public TaskController(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
        this.observers = new ArrayList<>();
    }

    //costruttore per produzione
    public TaskController() {
        this(new TaskDAOImpl());
    }

    
    //gestione observer
    public void addObserver(TaskObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    private void notifyTaskCreated(Task task) {
        for (TaskObserver observer : observers) {
            observer.onTaskCreated(task);
        }
    }

    private void notifyTaskUpdated(Task task) {
        for (TaskObserver observer : observers) {
            observer.onTaskUpdated(task);
        }
    }

    private void notifyTaskDeleted(Long taskId) {
        for (TaskObserver observer : observers) {
            observer.onTaskDeleted(taskId);
        }
    }

    private void notifyTaskStatusChanged(Task task) {
        for (TaskObserver observer : observers) {
            observer.onTaskStatusChanged(task);
        }
    }

    
    //operazioni CRUD
    public Task createTask(Long projectId, String title, String description, 
    		Priority priority, LocalDate deadline) {
        
    	//validazione input
        validateProjectId(projectId);
        validateTitle(title);
        validateDescription(description);
        validatePriority(priority);
        validateDeadline(deadline);

        //creazione e salvataggio task
        Task task = new Task(projectId, title, description, priority, deadline);
        Task savedTask = taskDAO.save(task);
        
        //notifica observer
        notifyTaskCreated(savedTask);
        
        return savedTask;
    }

    public boolean updateTaskStatus(Long taskId, TaskStatus newStatus) {
        validateTaskStatus(newStatus);

        Optional<Task> existingTask = taskDAO.findById(taskId);
        if (existingTask.isEmpty()) {
            return false;
        }

        Task task = existingTask.get();
        task.setStatus(newStatus);
        taskDAO.update(task);
        
        //notifica observer
        notifyTaskStatusChanged(task);
        
        return true;
    }

    public boolean updateTaskPriority(Long taskId, Priority newPriority) {
        validatePriority(newPriority);

        Optional<Task> existingTask = taskDAO.findById(taskId);
        if (existingTask.isEmpty()) {
            return false;
        }

        Task task = existingTask.get();
        task.setPriority(newPriority);
        taskDAO.update(task);
        
        //notifica observer
        notifyTaskUpdated(task);
        
        return true;
    }

    public boolean updateTaskDetails(Long taskId, String newTitle, String newDescription) {
        validateTitle(newTitle);
        validateDescription(newDescription);

        Optional<Task> existingTask = taskDAO.findById(taskId);
        if (existingTask.isEmpty()) {
            return false;
        }

        Task task = existingTask.get();
        task.setTitle(newTitle);
        task.setDescription(newDescription);
        taskDAO.update(task);
        
        //notifica observer
        notifyTaskUpdated(task);
        
        return true;
    }

    public boolean updateTaskDeadline(Long taskId, LocalDate newDeadline) {
        validateDeadline(newDeadline);

        Optional<Task> existingTask = taskDAO.findById(taskId);
        if (existingTask.isEmpty()) {
            return false;
        }

        Task task = existingTask.get();
        task.setDeadline(newDeadline);
        taskDAO.update(task);
        
        //notifica observer
        notifyTaskUpdated(task);
        
        return true;
    }

    public boolean deleteTask(Long id) {
        Optional<Task> task = taskDAO.findById(id);
        if (task.isEmpty()) {
            return false;
        }

        taskDAO.delete(id);
        
        //notifica observer
        notifyTaskDeleted(id);
        
        return true;
    }

    
    //metodi per ricerca task
    public Optional<Task> findTaskById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID non valido");
        }
        return taskDAO.findById(id);
    }

    public List<Task> findTasksByProjectId(Long projectId) {
        validateProjectId(projectId);
        return taskDAO.findByProjectId(projectId);
    }

    public List<Task> findTasksByStatus(TaskStatus status) {
        validateTaskStatus(status);
        return taskDAO.findByStatus(status);
    }

    public List<Task> findTasksByPriority(Priority priority) {
        validatePriority(priority);
        return taskDAO.findByPriority(priority);
    }

    public List<Task> findTasksDueBy(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data non può essere null");
        }
        return taskDAO.findByDeadlineBefore(date);
    }

    public List<Task> findOverdueTasks() {
        return taskDAO.findOverdueTasks();
    }

    public List<Task> findTasksDueToday() {
        return findTasksDueBy(LocalDate.now());
    }

    public List<Task> findTasksDueInNextDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Giorni non può essere negativo");
        }
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return findTasksDueBy(futureDate);
    }

    public List<Task> getAllTasks() {
        return taskDAO.findAll();
    }

    //utili
    public int countTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        validateProjectId(projectId);
        validateTaskStatus(status);
        return taskDAO.countByProjectIdAndStatus(projectId, status);
    }

    public int calculateProjectCompletion(Long projectId) {
        List<Task> allTasks = findTasksByProjectId(projectId);
        if (allTasks.isEmpty()) {
            return 0;
        }

        long completedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();

        return (int) ((completedTasks * 100) / allTasks.size());
    }

    public boolean projectHasTasks(Long projectId) {
        return !findTasksByProjectId(projectId).isEmpty();
    }

    protected TaskDAO getTaskDAO() {
        return taskDAO;
    }

    //validazione input
    private void validateProjectId(Long projectId) {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID non valido");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Titolo task non può essere vuoto");
        }
        if (title.length() < 3) {
            throw new IllegalArgumentException("Titolo deve essere di almeno 3 caratteri");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Titolo troppo lungo (max 200 caratteri)");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Descrizione troppo lunga (max 2000 caratteri)");
        }
    }

    private void validatePriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priorità non può essere null");
        }
    }

    private void validateTaskStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Stato non può essere null");
        }
    }

    private void validateDeadline(LocalDate deadline) {
        //può essere null (nessuna scadenza), ma non nel passato
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline non può essere nel passato");
        }
    }
}