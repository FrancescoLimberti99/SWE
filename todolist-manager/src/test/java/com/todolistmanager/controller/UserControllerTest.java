package com.todolistmanager.controller;

import com.todolistmanager.dao.UserDAO;
import com.todolistmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//unit test UserController Mockito
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserDAO userDAO;

    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        userController = new UserController(userDAO); // PASSA IL MOCK!
        testUser = new User(1L, "testuser", "test@email.com");
    }

    
    //test create user
    @Test
    void testCreateUser_Success() {
        //arrange
        when(userDAO.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenReturn(testUser);

        //act
        User result = userController.createUser("newuser", "new@email.com");

        //assert
        assertNotNull(result);
        verify(userDAO, times(1)).findByUsername("newuser");
        verify(userDAO, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists() {
        //arrange
        when(userDAO.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userController.createUser("testuser", "test@email.com")
        );
        
        assertEquals("Username 'testuser' già esistente", exception.getMessage());
        verify(userDAO, times(1)).findByUsername("testuser");
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_EmptyUsername() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userController.createUser("", "test@email.com")
        );
        
        assertEquals("Username non può essere vuoto", exception.getMessage());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_ShortUsername() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userController.createUser("ab", "test@email.com")
        );
        
        assertEquals("Username deve essere di almeno 3 caratteri", exception.getMessage());
    }

    @Test
    void testCreateUser_InvalidUsername() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userController.createUser("user@invalid", "test@email.com")
        );
        
        assertTrue(exception.getMessage().contains("Username può contenere solo"));
    }

    @Test
    void testCreateUser_EmptyEmail() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userController.createUser("testuser", "")
        );
        
        assertEquals("Email non può essere vuota", exception.getMessage());
    }

    @Test
    void testCreateUser_InvalidEmail() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userController.createUser("testuser", "invalid-email")
        );
        
        assertEquals("Email non valida", exception.getMessage());
    }

    
    //test find user
    @Test
    void testFindUserById_Found() {
        //arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));

        //act
        Optional<User> result = userController.findUserById(1L);

        //assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userDAO, times(1)).findById(1L);
    }

    @Test
    void testFindUserById_NotFound() {
        //arrange
        when(userDAO.findById(99L)).thenReturn(Optional.empty());

        //act
        Optional<User> result = userController.findUserById(99L);

        //assert
        assertFalse(result.isPresent());
        verify(userDAO, times(1)).findById(99L);
    }

    @Test
    void testFindUserById_InvalidId() {
        //act & assert
        assertThrows(IllegalArgumentException.class, () -> userController.findUserById(null));
        assertThrows(IllegalArgumentException.class, () -> userController.findUserById(0L));
        assertThrows(IllegalArgumentException.class, () -> userController.findUserById(-1L));
    }

    @Test
    void testFindUserByUsername_Found() {
        //arrange
        when(userDAO.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        //act
        Optional<User> result = userController.findUserByUsername("testuser");

        //assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userDAO, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindUserByUsername_NotFound() {
        //arrange
        when(userDAO.findByUsername("unknown")).thenReturn(Optional.empty());

        //act
        Optional<User> result = userController.findUserByUsername("unknown");

        //assert
        assertFalse(result.isPresent());
    }

    
    //test get all users
    @Test
    void testGetAllUsers() {
        //arrange
        User user2 = new User(2L, "user2", "user2@email.com");
        List<User> users = Arrays.asList(testUser, user2);
        when(userDAO.findAll()).thenReturn(users);

        //act
        List<User> result = userController.getAllUsers();

        //assert
        assertEquals(2, result.size());
        verify(userDAO, times(1)).findAll();
    }

    
    //test delete user
    @Test
    void testDeleteUser_Success() {
        //arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userDAO).delete(1L);

        //act
        boolean result = userController.deleteUser(1L);

        //assert
        assertTrue(result);
        verify(userDAO, times(1)).findById(1L);
        verify(userDAO, times(1)).delete(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        //arrange
        when(userDAO.findById(99L)).thenReturn(Optional.empty());

        //act
        boolean result = userController.deleteUser(99L);

        //assert
        assertFalse(result);
        verify(userDAO, times(1)).findById(99L);
        verify(userDAO, never()).delete(anyLong());
    }

    
    //test count
    @Test
    void testCountUsers() {
        //arrange
        when(userDAO.count()).thenReturn(5);

        //act
        int result = userController.countUsers();

        //assert
        assertEquals(5, result);
        verify(userDAO, times(1)).count();
    }

    
    //test username exists
    @Test
    void testUsernameExists_True() {
        //arrange
        when(userDAO.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        //act
        boolean result = userController.usernameExists("testuser");

        //assert
        assertTrue(result);
        verify(userDAO, times(1)).findByUsername("testuser");
    }

    @Test
    void testUsernameExists_False() {
        //arrange
        when(userDAO.findByUsername("unknown")).thenReturn(Optional.empty());

        //act
        boolean result = userController.usernameExists("unknown");

        //assert
        assertFalse(result);
        verify(userDAO, times(1)).findByUsername("unknown");
    }
}