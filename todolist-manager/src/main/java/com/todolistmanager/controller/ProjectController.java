package com.todolistmanager.controller;

import com.todolistmanager.dao.ProjectDAO;
import com.todolistmanager.dao.ProjectDAOImpl;
import com.todolistmanager.model.Project;
import java.util.List;
import java.util.Optional;

public class ProjectController {
    
    private final ProjectDAO projectDAO;

    //costruttore per test
    public ProjectController(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    //costruttore per produzione
    public ProjectController() {
        this(new ProjectDAOImpl());
    }

    //creazione progetto
    public Project createProject(Long userId, String name, String description) {
    	
        //validazione input
        validateUserId(userId);
        validateProjectName(name);
        validateDescription(description);

        //creazione e salvataggio progetto
        Project project = new Project(userId, name, description);
        return projectDAO.save(project);
    }

    //aggiorna progetto
    public boolean updateProject(Long projectId, String newName, String newDescription) {
        
    	//validazione
        validateProjectName(newName);
        validateDescription(newDescription);

        //controllo esistenza progetto
        Optional<Project> existingProject = findProjectById(projectId);
        if (existingProject.isEmpty()) {
            return false;
        }

        //aggiorna
        Project project = existingProject.get();
        project.setName(newName);
        project.setDescription(newDescription);
        projectDAO.update(project);
        return true;
    }

    //metodi per ricerca progetto
    public Optional<Project> findProjectById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID non valido");
        }
        return projectDAO.findById(id);
    }

    public List<Project> findProjectsByUserId(Long userId) {
        validateUserId(userId);
        return projectDAO.findByUserId(userId);
    }

    //getter
    public List<Project> getAllProjects() {
        return projectDAO.findAll();
    }

    //elimina utente
    public boolean deleteProject(Long id) {
    	
        //controllo esistenza
        Optional<Project> project = projectDAO.findById(id);
        if (project.isEmpty()) {
            return false;
        }

        //eliminazione
        projectDAO.delete(id);
        return true;
    }

    //utili
    public int countProjectsByUserId(Long userId) {
        validateUserId(userId);
        return projectDAO.countByUserId(userId);
    }

    public boolean userHasProjects(Long userId) {
        return countProjectsByUserId(userId) > 0;
    }

    protected ProjectDAO getProjectDAO() {
        return projectDAO;
    }

    
    //validazione input
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID non valido");
        }
    }

    private void validateProjectName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome progetto non può essere vuoto");
        }
        if (name.length() < 3) {
            throw new IllegalArgumentException("Nome progetto deve essere di almeno 3 caratteri");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Nome progetto troppo lungo (max 200 caratteri)");
        }
    }

    private void validateDescription(String description) {
        //opzionale, massimo 1000 caratteri
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("Descrizione troppo lunga (max 1000 caratteri)");
        }
    }
}