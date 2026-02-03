package com.todolistmanager.dao;

import com.todolistmanager.db.DBConnection;
import com.todolistmanager.model.Project;
import com.todolistmanager.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//integration test (BLACK BOX) ProjectDAOImpl - database PostgreSQL reale
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectDAOImplTest {

    private static ProjectDAO projectDAO;
    private static UserDAO userDAO;
    private User testUser;
    private Project testProject;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        //inizializza connessione DB
        DBConnection.getInstance();
        projectDAO = new ProjectDAOImpl();
        userDAO = new UserDAOImpl();
        
        //pulisci database
        cleanDatabase();
    }

    @BeforeEach
    void setUp() {
        //crea utente test (necessario per foreign key)
        testUser = new User("projectuser", "project@test.com");
        testUser = userDAO.save(testUser);
        
        //crea progetto di test
        testProject = new Project(testUser.getId(), "Test Project", "Test Description");
    }

    @AfterEach
    void tearDown() throws SQLException {
        //pulisci dopo test
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
        Project savedProject = projectDAO.save(testProject);

        //assert
        assertNotNull(savedProject.getId(), "ID deve essere generato dal database");
        assertTrue(savedProject.getId() > 0);
        assertEquals(testUser.getId(), savedProject.getUserId());
        assertEquals("Test Project", savedProject.getName());
        assertEquals("Test Description", savedProject.getDescription());
        assertNotNull(savedProject.getCreatedAt());
    }

    @Test
    @Order(2)
    void testSave_WithNullDescription() {
        //arrange
        testProject = new Project(testUser.getId(), "Project Without Desc", null);

        //act
        Project savedProject = projectDAO.save(testProject);

        //assert
        assertNotNull(savedProject.getId());
        assertNull(savedProject.getDescription());
    }

    //test update
    @Test
    @Order(3)
    void testUpdate_Success() {
        //arrange
        Project savedProject = projectDAO.save(testProject);
        Long projectId = savedProject.getId();

        //modifica progetto
        savedProject.setName("Updated Project Name");
        savedProject.setDescription("Updated Description");

        //act
        projectDAO.update(savedProject);

        //assert - verifica che modifiche siano salvate
        Optional<Project> updatedProject = projectDAO.findById(projectId);
        assertTrue(updatedProject.isPresent());
        assertEquals("Updated Project Name", updatedProject.get().getName());
        assertEquals("Updated Description", updatedProject.get().getDescription());
    }

    
    //test find project
    
    @Test
    @Order(4)
    void testFindById_Found() {
        //arrange
        Project savedProject = projectDAO.save(testProject);

        //act
        Optional<Project> result = projectDAO.findById(savedProject.getId());

        //assert
        assertTrue(result.isPresent());
        assertEquals(savedProject.getId(), result.get().getId());
        assertEquals("Test Project", result.get().getName());
    }

    @Test
    @Order(5)
    void testFindById_NotFound() {
        //act
        Optional<Project> result = projectDAO.findById(9999L);

        //assert
        assertFalse(result.isPresent());
    }


    @Test
    @Order(6)
    void testFindByUserId_MultipleProjects() {
        //arrange
        projectDAO.save(testProject);
        
        Project project2 = new Project(testUser.getId(), "Project 2", "Desc 2");
        projectDAO.save(project2);
        
        Project project3 = new Project(testUser.getId(), "Project 3", "Desc 3");
        projectDAO.save(project3);

        //act
        List<Project> projects = projectDAO.findByUserId(testUser.getId());

        //assert
        assertEquals(3, projects.size());
        
        //verifica che tutti i progetti appartengano all'utente corretto
        for (Project p : projects) {
            assertEquals(testUser.getId(), p.getUserId());
        }
    }

    @Test
    @Order(7)
    void testFindByUserId_NoProjects() {
        //arrange - crea altro utente senza progetti
        User user2 = new User("user2", "user2@test.com");
        user2 = userDAO.save(user2);

        //act
        List<Project> projects = projectDAO.findByUserId(user2.getId());

        //assert
        assertTrue(projects.isEmpty());
    }

    @Test
    @Order(8)
    void testFindByUserId_OnlyUserProjects() {
        //arrange - crea 2 utenti con progetti diversi
        projectDAO.save(testProject); //progetto di testUser
        
        User user2 = new User("user2", "user2@test.com");
        user2 = userDAO.save(user2);
        
        Project user2Project = new Project(user2.getId(), "User2 Project", "Desc");
        projectDAO.save(user2Project);

        //act
        List<Project> testUserProjects = projectDAO.findByUserId(testUser.getId());
        List<Project> user2Projects = projectDAO.findByUserId(user2.getId());

        //assert
        assertEquals(1, testUserProjects.size());
        assertEquals(1, user2Projects.size());
        assertEquals("Test Project", testUserProjects.get(0).getName());
        assertEquals("User2 Project", user2Projects.get(0).getName());
    }


    @Test
    @Order(9)
    void testFindAll() {
        //arrange
        projectDAO.save(testProject);
        
        Project project2 = new Project(testUser.getId(), "Project 2", "Desc 2");
        projectDAO.save(project2);

        //act
        List<Project> projects = projectDAO.findAll();

        //assert
        assertEquals(2, projects.size());
    }

    @Test
    @Order(10)
    void testFindAll_EmptyDatabase() {
        //act
        List<Project> projects = projectDAO.findAll();

        //assert
        assertTrue(projects.isEmpty());
    }

    //test delete
    @Test
    @Order(11)
    void testDelete_Success() {
        //arrange
        Project savedProject = projectDAO.save(testProject);
        Long projectId = savedProject.getId();

        //act
        projectDAO.delete(projectId);

        //assert
        Optional<Project> result = projectDAO.findById(projectId);
        assertFalse(result.isPresent(), "Il progetto dovrebbe essere eliminato");
    }

    @Test
    @Order(12)
    void testDelete_CascadeToTasks() {
        //arrange
        Project savedProject = projectDAO.save(testProject);
        Long projectId = savedProject.getId();
        
        //crea tasks associate al progetto
        TaskDAO taskDAO = new TaskDAOImpl();
        com.todolistmanager.model.Task task1 = new com.todolistmanager.model.Task(
            projectId, "Task 1", "Desc", 
            com.todolistmanager.model.Priority.HIGH, 
            java.time.LocalDate.now().plusDays(5)
        );
        taskDAO.save(task1);

        //act - elimina il progetto
        projectDAO.delete(projectId);

        //assert - verifica che task siano eliminate (CASCADE)
        List<com.todolistmanager.model.Task> tasks = taskDAO.findByProjectId(projectId);
        assertTrue(tasks.isEmpty(), "Le task dovrebbero essere eliminate per CASCADE");
    }

    //test count by user id
    @Test
    @Order(13)
    void testCountByUserId() {
        //arrange
        projectDAO.save(testProject);
        
        Project project2 = new Project(testUser.getId(), "Project 2", "Desc 2");
        projectDAO.save(project2);
        
        Project project3 = new Project(testUser.getId(), "Project 3", "Desc 3");
        projectDAO.save(project3);

        //act
        int count = projectDAO.countByUserId(testUser.getId());

        //assert
        assertEquals(3, count);
    }

    @Test
    @Order(14)
    void testCountByUserId_NoProjects() {
        //arrange
        User user2 = new User("user2", "user2@test.com");
        user2 = userDAO.save(user2);

        //act
        int count = projectDAO.countByUserId(user2.getId());

        //assert
        assertEquals(0, count);
    }

    //test cascade delete user
    @Test
    @Order(15)
    void testCascadeDelete_UserDeletesProjects() {
        //arrange
        Project savedProject = projectDAO.save(testProject);
        Long projectId = savedProject.getId();

        //act - elimina utente (dovrebbe eliminare progetti per CASCADE)
        userDAO.delete(testUser.getId());

        //assert
        Optional<Project> result = projectDAO.findById(projectId);
        assertFalse(result.isPresent(), "Il progetto dovrebbe essere eliminato quando si elimina l'utente");
    }
}