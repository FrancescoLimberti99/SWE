package com.todolistmanager.controller;

import com.todolistmanager.dao.ProjectDAO;
import com.todolistmanager.model.Project;
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

//unit test ProjectController Mockito
@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectDAO projectDAO;

    private ProjectController projectController;

    private Project testProject;

    @BeforeEach
    void setUp() {
        projectController = new ProjectController(projectDAO); // PASSA IL MOCK!
        testProject = new Project(1L, 1L, "Test Project", "Test Description");
    }

    
    //test create project
    @Test
    void testCreateProject_Success() {
        //arrange
        when(projectDAO.save(any(Project.class))).thenReturn(testProject);

        //act
        Project result = projectController.createProject(1L, "New Project", "Description");

        //assert
        assertNotNull(result);
        verify(projectDAO, times(1)).save(any(Project.class));
    }

    @Test
    void testCreateProject_InvalidUserId() {
        //act & assert
        assertThrows(IllegalArgumentException.class, 
            () -> projectController.createProject(null, "Project", "Desc"));
        assertThrows(IllegalArgumentException.class, 
            () -> projectController.createProject(0L, "Project", "Desc"));
        assertThrows(IllegalArgumentException.class, 
            () -> projectController.createProject(-1L, "Project", "Desc"));
    }

    @Test
    void testCreateProject_EmptyName() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectController.createProject(1L, "", "Description")
        );
        
        assertEquals("Nome progetto non può essere vuoto", exception.getMessage());
    }

    @Test
    void testCreateProject_ShortName() {
        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectController.createProject(1L, "ab", "Description")
        );
        
        assertEquals("Nome progetto deve essere di almeno 3 caratteri", exception.getMessage());
    }

    @Test
    void testCreateProject_LongName() {
        //arrange
        String longName = "a".repeat(201);

        //act & assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectController.createProject(1L, longName, "Description")
        );
        
        assertTrue(exception.getMessage().contains("troppo lungo"));
    }

    
    //test update project
    @Test
    void testUpdateProject_Success() {
        //arrange
        when(projectDAO.findById(1L)).thenReturn(Optional.of(testProject));
        doNothing().when(projectDAO).update(any(Project.class));

        //act
        boolean result = projectController.updateProject(1L, "Updated Name", "Updated Desc");

        //assert
        assertTrue(result);
        verify(projectDAO, times(1)).findById(1L);
        verify(projectDAO, times(1)).update(any(Project.class));
    }

    @Test
    void testUpdateProject_NotFound() {
        //arrange
        when(projectDAO.findById(99L)).thenReturn(Optional.empty());

        //act
        boolean result = projectController.updateProject(99L, "Name", "Desc");

        //assert
        assertFalse(result);
        verify(projectDAO, times(1)).findById(99L);
        verify(projectDAO, never()).update(any(Project.class));
    }

    
    //test find project
    @Test
    void testFindProjectById_Found() {
        //arrange
        when(projectDAO.findById(1L)).thenReturn(Optional.of(testProject));

        //act
        Optional<Project> result = projectController.findProjectById(1L);

        //assert
        assertTrue(result.isPresent());
        assertEquals("Test Project", result.get().getName());
        verify(projectDAO, times(1)).findById(1L);
    }

    @Test
    void testFindProjectById_NotFound() {
        //arrange
        when(projectDAO.findById(99L)).thenReturn(Optional.empty());

        //act
        Optional<Project> result = projectController.findProjectById(99L);

        //assert
        assertFalse(result.isPresent());
    }

    @Test
    void testFindProjectsByUserId() {
        //arrange
        Project project2 = new Project(2L, 1L, "Project 2", "Desc 2");
        List<Project> projects = Arrays.asList(testProject, project2);
        when(projectDAO.findByUserId(1L)).thenReturn(projects);

        //act
        List<Project> result = projectController.findProjectsByUserId(1L);

        //assert
        assertEquals(2, result.size());
        verify(projectDAO, times(1)).findByUserId(1L);
    }

    
    //test delete project
    @Test
    void testDeleteProject_Success() {
        //arrange
        when(projectDAO.findById(1L)).thenReturn(Optional.of(testProject));
        doNothing().when(projectDAO).delete(1L);

        //act
        boolean result = projectController.deleteProject(1L);

        //assert
        assertTrue(result);
        verify(projectDAO, times(1)).findById(1L);
        verify(projectDAO, times(1)).delete(1L);
    }

    @Test
    void testDeleteProject_NotFound() {
        //arrange
        when(projectDAO.findById(99L)).thenReturn(Optional.empty());

        //act
        boolean result = projectController.deleteProject(99L);

        //assert
        assertFalse(result);
        verify(projectDAO, never()).delete(anyLong());
    }

    
    //test count
    @Test
    void testCountProjectsByUserId() {
        //arrange
        when(projectDAO.countByUserId(1L)).thenReturn(3);

        //act
        int result = projectController.countProjectsByUserId(1L);

        //assert
        assertEquals(3, result);
        verify(projectDAO, times(1)).countByUserId(1L);
    }

    
    //test hasProjects
    @Test
    void testUserHasProjects_True() {
        //arrange
        when(projectDAO.countByUserId(1L)).thenReturn(2);

        //act
        boolean result = projectController.userHasProjects(1L);

        //assert
        assertTrue(result);
    }

    @Test
    void testUserHasProjects_False() {
        //arrange
        when(projectDAO.countByUserId(1L)).thenReturn(0);

        //act
        boolean result = projectController.userHasProjects(1L);

        //assert
        assertFalse(result);
    }
}