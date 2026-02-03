package com.todolistmanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;
    
    //configurazione database
    private static final String URL = "jdbc:postgresql://localhost:5434/exam";
    private static final String USER = "postgres";
    private static final String PASSWORD = "**************"; //password oscurata

    //costruttore privato (Singleton)
    private DBConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connessione al database riuscita!");
        } catch (SQLException e) {
            System.err.println("❌ Errore connessione database: " + e.getMessage());
            throw new RuntimeException("Impossibile connettersi al database", e);
        }
    }

    //restituisce istanza Singleton
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    //restituisce connessione
    public Connection getConnection() {
        try {
            //connessione chiusa, la ricrea
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero della connessione", e);
        }
        return connection;
    }

    //chiude connessione
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Connessione chiusa");
            } catch (SQLException e) {
                System.err.println("❌ Errore chiusura connessione: " + e.getMessage());
            }
        }
    }
}