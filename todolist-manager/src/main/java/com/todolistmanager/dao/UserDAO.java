package com.todolistmanager.dao;

import com.todolistmanager.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    
    //salva utente nel database
    User save(User user);
    
    //ricerca utente
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    
    //utili
    List<User> findAll();
    
    int count();
    
    
    //elimina utente dal database
    void delete(Long id);
}