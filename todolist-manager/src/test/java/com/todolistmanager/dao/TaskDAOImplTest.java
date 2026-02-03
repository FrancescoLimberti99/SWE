package com.todolistmanager.dao;

import com.todolistmanager.db.DBConnection;
import com.todolistmanager.model.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//integration test (BLACK BOX) TaskDAOImpl - database PostgreSQL reale
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskDAOImplTest {

    private static TaskDAO taskDAO;
    private static ProjectDAO projectDAO;
    private static UserDAO userDAO;
    
    private User testUser;
    private Project testProject;
    private Task testTask;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        //inizializza connessione DB
        DBConnection.getInstance();
        taskDAO = new TaskDAOImpl();
        projectDAO = new ProjectDAOImpl();
        userDAO = new UserDAOImpl();
        
        //pulisci database
        cleanDatabase();
    }

    @BeforeEach
    void setUp() {
        //crea utente di test
        testUser = new User("taskuser", "task@test.com");
        testUser = userDAO.save(testUser);
        
        //crea progetto di test
        testProject = new Project(testUser.getId(), "Task Test Project", "Description");
        testProject = projectDAO.save(testProject);
        
        //crea task di test
        testTask = new Task(
            testProject.getId(),
            "Test Task",
            "Task Description",
            Priority.MEDIUM,
            LocalDate.now().plusDays(7)
        );
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanDatabase();
    }

    private static void cleanDatabase() throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        
        stmt.execute("SET session_replication_role = 'replica';");
        stmt.execute("DELETE FROM tasks");
        stmt.execute("DELETE FROM projects");
        stmt.execute("DELETE FROM users");
        stmt.execute("SET session_replication_role = 'origin';");
        
        stmt.close();
    }

    //test save
    @Test
    @Order(1)
    void testSave_Success() {
        //act
        Task savedTask = taskDAO.save(testTask);

        //assert
        assertNotNull(savedTask.getId(), "ID deve essere generato dal database");
        assertTrue(savedTask.getId() > 0);
        assertEquals(testProject.getId(), savedTask.getProjectId());
        assertEquals("Test Task", savedTask.getTitle());
        assertEquals("Task Description", savedTask.getDescription());
        assertEquals(TaskStatus.TODO, savedTask.getStatus());
        assertEquals(Priority.MEDIUM, savedTask.getPriority());
        assertNotNull(savedTask.getDeadline());
        assertNotNull(savedTask.getCreatedAt());
    }

    @Test
    @Order(2)
    void testSave_WithoutDeadline() {
        //arrange
        testTask = new Task(
            testProject.getId(),
            "Task No Deadline",
            "Description",
            Priority.LOW,
            null  //no deadline
        );

        //act
        Task savedTask = taskDAO.save(testTask);

        //assert
        assertNotNull(savedTask.getId());
        assertNull(savedTask.getDeadline());
    }

    @Test
    @Order(3)
    void testSave_AllPriorities() {
        //act & assert - verifica che Priority enum siano salvabili
        Task highTask = new Task(testProject.getId(), "High", "Desc", Priority.HIGH, null);
        Task savedHigh = taskDAO.save(highTask);
        assertEquals(Priority.HIGH, savedHigh.getPriority());

        Task mediumTask = new Task(testProject.getId(), "Medium", "Desc", Priority.MEDIUM, null);
        Task savedMedium = taskDAO.save(mediumTask);
        assertEquals(Priority.MEDIUM, savedMedium.getPriority());

        Task lowTask = new Task(testProject.getId(), "Low", "Desc", Priority.LOW, null);
        Task savedLow = taskDAO.save(lowTask);
        assertEquals(Priority.LOW, savedLow.getPriority());
    }

    //test update
    @Test
    @Order(4)
    void testUpdate_Success() {
        //arrange
        Task savedTask = taskDAO.save(testTask);
        Long taskId = savedTask.getId();

        //modifica task
        savedTask.setTitle("Updated Title");
        savedTask.setDescription("Updated Description");
        savedTask.setStatus(TaskStatus.IN_PROGRESS);
        savedTask.setPriority(Priority.HIGH);
        savedTask.setDeadline(LocalDate.now().plusDays(3));

        //act
        taskDAO.update(savedTask);

        //assert
        Optional<Task> updatedTask = taskDAO.findById(taskId);
        assertTrue(updatedTask.isPresent());
        assertEquals("Updated Title", updatedTask.get().getTitle());
        assertEquals("Updated Description", updatedTask.get().getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.get().getStatus());
        assertEquals(Priority.HIGH, updatedTask.get().getPriority());
    }

    @Test
    @Order(5)
    void testUpdate_CompleteTask() {
        //arrange
        Task savedTask = taskDAO.save(testTask);
        savedTask.setStatus(TaskStatus.DONE);
        savedTask.setCompletedAt(LocalDate.now());

        //act
        taskDAO.update(savedTask);

        //assert
        Optional<Task> completedTask = taskDAO.findById(savedTask.getId());
        assertTrue(completedTask.isPresent());
        assertEquals(TaskStatus.DONE, completedTask.get().getStatus());
        assertNotNull(completedTask.get().getCompletedAt());
    }

    
    //test find

    @Test
    @Order(6)
    void testFindById_Found() {
        //arrange
        Task savedTask = taskDAO.save(testTask);

        //act
        Optional<Task> result = taskDAO.findById(savedTask.getId());

        //assert
        assertTrue(result.isPresent());
        assertEquals(savedTask.getId(), result.get().getId());
        assertEquals("Test Task", result.get().getTitle());
    }

    @Test
    @Order(7)
    void testFindById_NotFound() {
        //act
        Optional<Task> result = taskDAO.findById(9999L);

        //assert
        assertFalse(result.isPresent());
    }


    @Test
    @Order(8)
    void testFindByProjectId_MultipleTasks() {
        // Arrange
        taskDAO.save(testTask);
        
        Task task2 = new Task(testProject.getId(), "Task 2", "Desc 2", Priority.HIGH, null);
        taskDAO.save(task2);
        
        Task task3 = new Task(testProject.getId(), "Task 3", "Desc 3", Priority.LOW, null);
        taskDAO.save(task3);

        //act
        List<Task> tasks = taskDAO.findByProjectId(testProject.getId());

        //assert
        assertEquals(3, tasks.size());
        
        //verifica che task appartengano al progetto corretto
        for (Task t : tasks) {
            assertEquals(testProject.getId(), t.getProjectId());
        }
    }

    @Test
    @Order(9)
    void testFindByProjectId_NoTasks() {
        //arrange - crea progetto senza task
        Project project2 = new Project(testUser.getId(), "Empty Project", "Desc");
        project2 = projectDAO.save(project2);

        //act
        List<Task> tasks = taskDAO.findByProjectId(project2.getId());

        //assert
        assertTrue(tasks.isEmpty());
    }


    @Test
    @Order(10)
    void testFindByStatus_AllStatuses() {
        //arrange - crea task con tutti gli stati
        Task todoTask = new Task(testProject.getId(), "TODO", "Desc", Priority.MEDIUM, null);
        todoTask.setStatus(TaskStatus.TODO);
        taskDAO.save(todoTask);

        Task inProgressTask = new Task(testProject.getId(), "In Progress", "Desc", Priority.MEDIUM, null);
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);
        taskDAO.save(inProgressTask);

        Task pausedTask = new Task(testProject.getId(), "Paused", "Desc", Priority.MEDIUM, null);
        pausedTask.setStatus(TaskStatus.PAUSED);
        taskDAO.save(pausedTask);

        Task doneTask = new Task(testProject.getId(), "Done", "Desc", Priority.MEDIUM, null);
        doneTask.setStatus(TaskStatus.DONE);
        taskDAO.save(doneTask);

        //act & assert
        assertEquals(1, taskDAO.findByStatus(TaskStatus.TODO).size());
        assertEquals(1, taskDAO.findByStatus(TaskStatus.IN_PROGRESS).size());
        assertEquals(1, taskDAO.findByStatus(TaskStatus.PAUSED).size());
        assertEquals(1, taskDAO.findByStatus(TaskStatus.DONE).size());
    }


    @Test
    @Order(11)
    void testFindByPriority_AllPriorities() {
    	//arrange - crea task con tutti le priorità
        Task highTask = new Task(testProject.getId(), "High", "Desc", Priority.HIGH, null);
        taskDAO.save(highTask);

        Task mediumTask = new Task(testProject.getId(), "Medium", "Desc", Priority.MEDIUM, null);
        taskDAO.save(mediumTask);

        Task lowTask = new Task(testProject.getId(), "Low", "Desc", Priority.LOW, null);
        taskDAO.save(lowTask);

        //act & assert
        assertEquals(1, taskDAO.findByPriority(Priority.HIGH).size());
        assertEquals(1, taskDAO.findByPriority(Priority.MEDIUM).size());
        assertEquals(1, taskDAO.findByPriority(Priority.LOW).size());
    }


    @Test
    @Order(12)
    void testFindByDeadlineBefore() {
        //arrange
        LocalDate today = LocalDate.now();
        
        Task task1 = new Task(testProject.getId(), "Due Tomorrow", "Desc", Priority.HIGH, today.plusDays(1));
        taskDAO.save(task1);
        
        Task task2 = new Task(testProject.getId(), "Due in 3 days", "Desc", Priority.HIGH, today.plusDays(3));
        taskDAO.save(task2);
        
        Task task3 = new Task(testProject.getId(), "Due in 10 days", "Desc", Priority.HIGH, today.plusDays(10));
        taskDAO.save(task3);

        //act - cerca task in scadenza entro 5 giorni
        List<Task> dueSoon = taskDAO.findByDeadlineBefore(today.plusDays(5));

        //assert
        assertEquals(2, dueSoon.size()); //solo le prime 2 task
    }

    @Test
    @Order(13)
    void testFindByDeadlineBefore_ExcludesDoneTasks() {
        //arrange
        LocalDate today = LocalDate.now();
        
        Task task1 = new Task(testProject.getId(), "Due Soon", "Desc", Priority.HIGH, today.plusDays(2));
        taskDAO.save(task1);
        
        Task task2 = new Task(testProject.getId(), "Done Task", "Desc", Priority.HIGH, today.plusDays(2));
        task2.setStatus(TaskStatus.DONE);
        taskDAO.save(task2);

        //act
        List<Task> dueSoon = taskDAO.findByDeadlineBefore(today.plusDays(5));

        //assert
        assertEquals(1, dueSoon.size()); // Solo la task non completata
        assertEquals("Due Soon", dueSoon.get(0).getTitle());
    }


    @Test
    @Order(14)
    void testFindOverdueTasks() {
        //arrange
        LocalDate today = LocalDate.now();
        
        //task in ritardo
        Task overdueTask1 = new Task(testProject.getId(), "Overdue 1", "Desc", Priority.HIGH, today.minusDays(2));
        taskDAO.save(overdueTask1);
        
        Task overdueTask2 = new Task(testProject.getId(), "Overdue 2", "Desc", Priority.HIGH, today.minusDays(5));
        taskDAO.save(overdueTask2);
        
        //task futura (non in ritardo)
        Task futureTask = new Task(testProject.getId(), "Future", "Desc", Priority.HIGH, today.plusDays(5));
        taskDAO.save(futureTask);
        
        //task completata in ritardo (non dovrebbe apparire)
        Task completedOverdue = new Task(testProject.getId(), "Done Overdue", "Desc", Priority.HIGH, today.minusDays(3));
        completedOverdue.setStatus(TaskStatus.DONE);
        taskDAO.save(completedOverdue);

        //act
        List<Task> overdueTasks = taskDAO.findOverdueTasks();

        //assert
        assertEquals(2, overdueTasks.size());
        
        //verifica che tutte abbiano deadline passata e non siano DONE
        for (Task t : overdueTasks) {
            assertTrue(t.getDeadline().isBefore(today));
            assertNotEquals(TaskStatus.DONE, t.getStatus());
        }
    }


    @Test
    @Order(15)
    void testFindAll() {
        //arrange
        taskDAO.save(testTask);
        
        Task task2 = new Task(testProject.getId(), "Task 2", "Desc", Priority.HIGH, null);
        taskDAO.save(task2);

        //act
        List<Task> allTasks = taskDAO.findAll();

        //assert
        assertEquals(2, allTasks.size());
    }

    //test delete
    @Test
    @Order(16)
    void testDelete_Success() {
        //arrange
        Task savedTask = taskDAO.save(testTask);
        Long taskId = savedTask.getId();

        //act
        taskDAO.delete(taskId);

        //assert
        Optional<Task> result = taskDAO.findById(taskId);
        assertFalse(result.isPresent(), "La task dovrebbe essere eliminata");
    }

    //test count by project and status
    @Test
    @Order(17)
    void testCountByProjectIdAndStatus() {
        //arrange
        Task task1 = new Task(testProject.getId(), "Task 1", "Desc", Priority.HIGH, null);
        task1.setStatus(TaskStatus.TODO);
        taskDAO.save(task1);
        
        Task task2 = new Task(testProject.getId(), "Task 2", "Desc", Priority.HIGH, null);
        task2.setStatus(TaskStatus.TODO);
        taskDAO.save(task2);
        
        Task task3 = new Task(testProject.getId(), "Task 3", "Desc", Priority.HIGH, null);
        task3.setStatus(TaskStatus.DONE);
        taskDAO.save(task3);

        //act
        int todoCount = taskDAO.countByProjectIdAndStatus(testProject.getId(), TaskStatus.TODO);
        int doneCount = taskDAO.countByProjectIdAndStatus(testProject.getId(), TaskStatus.DONE);

        //assert
        assertEquals(2, todoCount);
        assertEquals(1, doneCount);
    }

    //test cascade delete project - delete user
    @Test
    @Order(18)
    void testCascadeDelete_ProjectDeletesTasks() {
        //arrange
        Task savedTask = taskDAO.save(testTask);
        Long taskId = savedTask.getId();

        //act - elimina il progetto (dovrebbe eliminare anche le task per CASCADE)
        projectDAO.delete(testProject.getId());

        //assert
        Optional<Task> result = taskDAO.findById(taskId);
        assertFalse(result.isPresent(), "La task dovrebbe essere eliminata quando si elimina il progetto");
    }

    @Test
    @Order(19)
    void testCascadeDelete_UserDeletesEverything() {
        //arrange
        Task savedTask = taskDAO.save(testTask);
        Long taskId = savedTask.getId();
        Long projectId = testProject.getId();

        //act - elimina l'utente (dovrebbe eliminare progetto E task per CASCADE)
        userDAO.delete(testUser.getId());

        //assert
        Optional<Project> projectResult = projectDAO.findById(projectId);
        Optional<Task> taskResult = taskDAO.findById(taskId);
        
        assertFalse(projectResult.isPresent(), "Il progetto dovrebbe essere eliminato");
        assertFalse(taskResult.isPresent(), "La task dovrebbe essere eliminata");
    }
}