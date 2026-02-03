package com.todolistmanager.controller;

import com.todolistmanager.dao.UserDAO;
import com.todolistmanager.dao.UserDAOImpl;
import com.todolistmanager.model.User;
import java.util.List;
import java.util.Optional;

public class UserController {

    private final UserDAO userDAO;

    //costruttore per test
    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    //costruttore per produzione
    public UserController() {
        this(new UserDAOImpl());
    }

    //creazione utente
    public User createUser(String username, String email) {

    	//validazione input
        validateUsername(username);
        validateEmail(email);

        //controllo esistenza username
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username '" + username + "' già esistente");
        }

        //creazione e salvataggio utente
        User user = new User(username, email);
        return userDAO.save(user);
    }

    //metodi per ricerca utente
    public Optional<User> findUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID non valido");
        }
        return userDAO.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto");
        }
        return userDAO.findByUsername(username);
    }

    //getter
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    //elimina utente
    public boolean deleteUser(Long id) {

        //controllo esistenza
        Optional<User> user = userDAO.findById(id);
        if (user.isEmpty()) {
            return false;
        }

        //eliminazione
        userDAO.delete(id);
        return true;
    }

    //utili
    public int countUsers() {
        return userDAO.count();
    }

    protected UserDAO getUserDAO() {
        return userDAO;
    }

    //verifica esistenza
    public boolean usernameExists(String username) {
        return userDAO.findByUsername(username).isPresent();
    }


    //validazione input
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username deve essere di almeno 3 caratteri");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username troppo lungo (max 50 caratteri)");
        }
        //username può contenere solo lettere, numeri e underscore
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Username può contenere solo lettere, numeri e underscore");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email non può essere vuota");
        }

        if (email.length() > 255) {
            throw new IllegalArgumentException("Email troppo lunga (max 255 caratteri)");
        }

        //regex per validare email: la mail rispetta il pattern?
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Email non valida");
        }

    }
}