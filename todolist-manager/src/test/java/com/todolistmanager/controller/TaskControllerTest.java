package com.todolistmanager.controller;

import com.todolistmanager.dao.TaskDAO;
import com.todolistmanager.model.Priority;
import com.todolistmanager.model.Task;
import com.todolistmanager.model.TaskStatus;
import com.todolistmanager.observer.TaskObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//unit test TaskController Mockito
@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskDAO taskDAO;

    @Mock
    private TaskObserver mockObserver;

    private TaskController taskController;

    private Task testTask;

    @BeforeEach
    void setUp() {
        taskController = new TaskController(taskDAO); // PASSA IL MOCK!
        testTask = new Task(1L, 1L, "Test Task", "Description", 
                           TaskStatus.TODO, Priority.MEDIUM, LocalDate.now().plusDays(7));
        testTask.setId(1L);
    }

    
    //test observer
    @Test
    void testAddObserver() {
        //act
        taskController.addObserver(mockObserver);

        //assert - verifica che observer venga notificato quando creo task
        when(taskDAO.save(any(Task.class))).thenReturn(testTask);
        taskController.createTask(1L, "New Task", "Desc", Priority.HIGH, LocalDate.now());
        
        verify(mockObserver, times(1)).onTaskCreated(any(Task.class));
    }

    
    //test create task
    @Test
    void testCreateTask_Success() {
        //arrange
        when(taskDAO.save(any(Task.class))).thenReturn(testTask);

        //act
        Task result = taskController.createTask(1L, "Task", "Desc", Priority.HIGH, LocalDate.now().plusDays(5));

        //assert
        assertNotNull(result);
        verify(taskDAO, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTask_InvalidProjectId() {
        //act & assert
        assertThrows(IllegalArgumentException.class, 
            () -> taskController.createTask(null, "Task", "Desc", Priority.HIGH, null));
        assertThrows(IllegalArgumentException.class, 
            () -> taskController.createTask(0L, "Task", "Desc", Priority.HIGH, null));
    }

    @Test
    void testCreateTask_EmptyTitle() {
        //act & assert
        assertThrows(IllegalArgumentException.class,
            () -> taskController.createTask(1L, "", "Desc", Priority.HIGH, null));
    }

    @Test
    void testCreateTask_ShortTitle() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> taskController.createTask(1L, "ab", "Desc", Priority.HIGH, null)
        );
        
        assertTrue(exception.getMessage().contains("almeno 3 caratteri"));
    }

    @Test
    void testCreateTask_NullPriority() {
        //act & assert
        assertThrows(IllegalArgumentException.class,
            () -> taskController.createTask(1L, "Task", "Desc", null, null));
    }

    
    //test update status
    @Test
    void testUpdateTaskStatus_Success() {
        //arrange
        when(taskDAO.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskDAO).update(any(Task.class));
        taskController.addObserver(mockObserver);

        //act
        boolean result = taskController.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);

        //assert
        assertTrue(result);
        verify(taskDAO, times(1)).findById(1L);
        verify(taskDAO, times(1)).update(any(Task.class));
        verify(mockObserver, times(1)).onTaskStatusChanged(any(Task.class));
    }

    @Test
    void testUpdateTaskStatus_NotFound() {
        //arrange
        when(taskDAO.findById(99L)).thenReturn(Optional.empty());

        //act
        boolean result = taskController.updateTaskStatus(99L, TaskStatus.DONE);

        //assert
        assertFalse(result);
        verify(taskDAO, never()).update(any(Task.class));
    }

    
    //test update priority
    @Test
    void testUpdateTaskPriority_Success() {
        //arrange
        when(taskDAO.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskDAO).update(any(Task.class));

        //act
        boolean result = taskController.updateTaskPriority(1L, Priority.HIGH);

        //assert
        assertTrue(result);
        verify(taskDAO, times(1)).update(any(Task.class));
    }

    
    //test find task
    @Test
    void testFindTaskById_Found() {
        //arrange
        when(taskDAO.findById(1L)).thenReturn(Optional.of(testTask));

        //act
        Optional<Task> result = taskController.findTaskById(1L);

        //assert
        assertTrue(result.isPresent());
        assertEquals("Test Task", result.get().getTitle());
    }

    @Test
    void testFindTasksByProjectId() {
        //arrange
        Task task2 = new Task(2L, 1L, "Task 2", "Desc", TaskStatus.TODO, Priority.LOW, null);
        List<Task> tasks = Arrays.asList(testTask, task2);
        when(taskDAO.findByProjectId(1L)).thenReturn(tasks);

        //act
        List<Task> result = taskController.findTasksByProjectId(1L);

        //assert
        assertEquals(2, result.size());
        verify(taskDAO, times(1)).findByProjectId(1L);
    }

    @Test
    void testFindTasksByStatus() {
        //arrange
        List<Task> tasks = Arrays.asList(testTask);
        when(taskDAO.findByStatus(TaskStatus.TODO)).thenReturn(tasks);

        //act
        List<Task> result = taskController.findTasksByStatus(TaskStatus.TODO);

        //assert
        assertEquals(1, result.size());
    }

    
    //test delete task
    @Test
    void testDeleteTask_Success() {
        //arrange
        when(taskDAO.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskDAO).delete(1L);
        taskController.addObserver(mockObserver);

        //act
        boolean result = taskController.deleteTask(1L);

        //assert
        assertTrue(result);
        verify(taskDAO, times(1)).delete(1L);
        verify(mockObserver, times(1)).onTaskDeleted(1L);
    }

    
    //test calculate completion
    @Test
    void testCalculateProjectCompletion() {
        //arrange
        Task completedTask = new Task(2L, 1L, "Done Task", "Desc", TaskStatus.DONE, Priority.LOW, null);
        List<Task> tasks = Arrays.asList(testTask, completedTask);
        when(taskDAO.findByProjectId(1L)).thenReturn(tasks);

        //act
        int result = taskController.calculateProjectCompletion(1L);

        //assert
        assertEquals(50, result); // 1 su 2 task completate = 50%
    }

    @Test
    void testCalculateProjectCompletion_EmptyProject() {
        //arrange
        when(taskDAO.findByProjectId(1L)).thenReturn(Arrays.asList());

        //act
        int result = taskController.calculateProjectCompletion(1L);

        //assert
        assertEquals(0, result);
    }
}