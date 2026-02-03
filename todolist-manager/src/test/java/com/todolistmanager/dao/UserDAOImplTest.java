package com.todolistmanager.dao;

import com.todolistmanager.db.DBConnection;
import com.todolistmanager.model.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

//integration test (BLACK BOX) UserDAOImpl - database PostgreSQL reale
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOImplTest {

    private static UserDAO userDAO;
    private User testUser;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        //inizializza connessione DB
        DBConnection.getInstance();
        
        userDAO = new UserDAOImpl();
        
        //pulisci database
        cleanDatabase();
    }

    @BeforeEach
    void setUp() {
    	//crea utente test
        testUser = new User("testuser123", "test@example.com");
    }

    @AfterEach
    void tearDown() throws SQLException {
        //pulisci dopo test
        cleanDatabase();
    }

    private static void cleanDatabase() throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        Statement stmt = conn.createStatement();
        
        //disabilita temporaneamente i foreign key constraints
        stmt.execute("SET session_replication_role = 'replica';");
        
        //elimina tutti i dati
        stmt.execute("DELETE FROM tasks");
        stmt.execute("DELETE FROM projects");
        stmt.execute("DELETE FROM users");
        
        //riabilita i foreign key constraints
        stmt.execute("SET session_replication_role = 'origin';");
        
        stmt.close();
    }

    
    //test save
    @Test
    @Order(1)
    void testSave_Success() {
        //act
        User savedUser = userDAO.save(testUser);

        //assert
        assertNotNull(savedUser.getId(), "ID deve essere generato dal database");
        assertTrue(savedUser.getId() > 0);
        assertEquals("testuser123", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @Order(2)
    void testSave_DuplicateUsername_ThrowsException() {
        //arrange
        userDAO.save(testUser);

        //act & assert
        User duplicateUser = new User("testuser123", "different@email.com");
        
        assertThrows(RuntimeException.class, () -> {
            userDAO.save(duplicateUser);
        }, "Dovrebbe lanciare eccezione per username duplicato");
    }

    
    //test find user
    
    @Test
    @Order(3)
    void testFindById_Found() {
        //arrange
        User savedUser = userDAO.save(testUser);

        //act
        Optional<User> result = userDAO.findById(savedUser.getId());

        //assert
        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
        assertEquals("testuser123", result.get().getUsername());
    }

    @Test
    @Order(4)
    void testFindById_NotFound() {
        //act
        Optional<User> result = userDAO.findById(9999L);

        //assert
        assertFalse(result.isPresent());
    }

    
    @Test
    @Order(5)
    void testFindByUsername_Found() {
        //arrange
        userDAO.save(testUser);

        //act
        Optional<User> result = userDAO.findByUsername("testuser123");

        //assert
        assertTrue(result.isPresent());
        assertEquals("testuser123", result.get().getUsername());
    }

    @Test
    @Order(6)
    void testFindByUsername_NotFound() {
        //act
        Optional<User> result = userDAO.findByUsername("nonexistent");

        //assert
        assertFalse(result.isPresent());
    }

    
    @Test
    @Order(7)
    void testFindAll() {
        //arrange
        userDAO.save(testUser);
        User user2 = new User("user2", "user2@email.com");
        userDAO.save(user2);

        //act
        List<User> users = userDAO.findAll();

        //assert
        assertEquals(2, users.size());
    }

    @Test
    @Order(8)
    void testFindAll_EmptyDatabase() {
        //act
        List<User> users = userDAO.findAll();

        //assert
        assertTrue(users.isEmpty());
    }

    //test delete
    @Test
    @Order(9)
    void testDelete_Success() {
        //arrange
        User savedUser = userDAO.save(testUser);
        Long userId = savedUser.getId();

        //act
        userDAO.delete(userId);

        //assert
        Optional<User> result = userDAO.findById(userId);
        assertFalse(result.isPresent(), "L'utente dovrebbe essere eliminato");
    }

    //test count
    @Test
    @Order(10)
    void testCount() {
        //arrange
        userDAO.save(testUser);
        User user2 = new User("user2", "user2@email.com");
        userDAO.save(user2);

        //act
        int count = userDAO.count();

        //assert
        assertEquals(2, count);
    }

    @Test
    @Order(11)
    void testCount_EmptyDatabase() {
        //act
        int count = userDAO.count();

        //assert
        assertEquals(0, count);
    }
}