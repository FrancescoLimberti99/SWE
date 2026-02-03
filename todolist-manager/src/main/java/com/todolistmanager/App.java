package com.todolistmanager;

import com.todolistmanager.db.DBConnection;

public class App {
    public static void main(String[] args) {
        System.out.println("Test connessione database...");
        
        //testa connessione
        DBConnection db = DBConnection.getInstance();
        
        System.out.println("✅ Connesione confermata");
    }
}