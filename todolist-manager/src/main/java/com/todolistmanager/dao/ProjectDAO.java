package com.todolistmanager.dao;

import com.todolistmanager.model.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectDAO {
    
	//salva progetto nel database
    Project save(Project project);
    
    //aggiorna progetto
    void update(Project project);
    
    //ricerca
    Optional<Project> findById(Long id);
    
    List<Project> findByUserId(Long userId);
    
    List<Project> findAll();
    
    
    //elimina progetto
    void delete(Long id);
    
    //counter
    int countByUserId(Long userId);
}