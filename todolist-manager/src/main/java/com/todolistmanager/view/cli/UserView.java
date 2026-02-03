package com.todolistmanager.view.cli;

import com.todolistmanager.controller.UserController;
import com.todolistmanager.model.User;
import java.util.List;
import java.util.Optional;

//view per gestione utenti
public class UserView {
    
    private final UserController userController;

    public UserView(UserController userController) {
        this.userController = userController;
    }

    //selezionare/creare utente e restituirlo, null se voglio uscire
    public User selectOrCreateUser() {
        InputReader.clearScreen();
        InputReader.printHeader("SELEZIONE UTENTE");
        
        List<User> users = userController.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("\n📝 Nessun utente presente. Crea il primo utente!");
            return createNewUser();
        }
        
        System.out.println("\n👥 Utenti disponibili:\n");
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            System.out.println((i + 1) + ". 👤 " + user.getUsername() + 
                             " (" + user.getEmail() + ")");
        }
        System.out.println("\n" + (users.size() + 1) + ". ➕ Crea nuovo utente");
        System.out.println("0. 🚪 Esci");
        System.out.println();
        
        int choice = InputReader.readInt("Scegli un'opzione: ", 0, users.size() + 1);
        
        if (choice == 0) {
            return null;  //esce
        } else if (choice == users.size() + 1) {
            return createNewUser();  //crea
        } else {
            return users.get(choice - 1);  //seleziona
        }
    }

    //creare utente e restituirlo, null se voglio uscire
    public User createNewUser() {
        InputReader.clearScreen();
        InputReader.printHeader("CREA NUOVO UTENTE");
        
        System.out.println("\n📝 Inserisci i dati del nuovo utente:\n");
        
        try {
            String username = InputReader.readString("Username (min 3 caratteri, solo lettere/numeri/_): ");
            String email = InputReader.readString("Email: ");
            
            //conferma
            System.out.println("\n📋 Riepilogo:");
            System.out.println("   Username: " + username);
            System.out.println("   Email: " + email);
            System.out.println();
            
            boolean confirm = InputReader.readConfirmation("Confermi la creazione?");
            
            if (!confirm) {
                System.out.println("\n❌ Creazione annullata.");
                InputReader.pressEnterToContinue();
                return selectOrCreateUser();  //torna al menu selezione
            }
            
            //crea utente
            User newUser = userController.createUser(username, email);
            
            System.out.println("\n✅ Utente creato con successo!");
            System.out.println("   ID: " + newUser.getId());
            System.out.println("   Username: " + newUser.getUsername());
            System.out.println("   Creato il: " + newUser.getCreatedAt());
            
            InputReader.pressEnterToContinue();
            return newUser;
            
        } catch (IllegalArgumentException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
            InputReader.pressEnterToContinue();
            
            boolean retry = InputReader.readConfirmation("Vuoi riprovare?");
            if (retry) {
                return createNewUser();  //riprova
            } else {
                return selectOrCreateUser();  //torna al menu
            }
        }
    }

    //mostra utenti
    public void showAllUsers() {
        InputReader.clearScreen();
        InputReader.printHeader("LISTA UTENTI");
        
        List<User> users = userController.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("\n📝 Nessun utente presente.");
        } else {
            System.out.println("\n👥 Totale utenti: " + users.size() + "\n");
            InputReader.printSeparator();
            
            for (User user : users) {
                System.out.println("🆔 ID: " + user.getId());
                System.out.println("👤 Username: " + user.getUsername());
                System.out.println("📧 Email: " + user.getEmail());
                System.out.println("📅 Creato il: " + user.getCreatedAt());
                InputReader.printSeparator();
            }
        }
        
        InputReader.pressEnterToContinue();
    }

    //cerca per username
    public void searchUserByUsername() {
        InputReader.clearScreen();
        InputReader.printHeader("CERCA UTENTE");
        
        String username = InputReader.readString("\n🔍 Inserisci username da cercare: ");
        
        Optional<User> userOpt = userController.findUserByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("\n✅ Utente trovato!\n");
            InputReader.printSeparator();
            System.out.println("🆔 ID: " + user.getId());
            System.out.println("👤 Username: " + user.getUsername());
            System.out.println("📧 Email: " + user.getEmail());
            System.out.println("📅 Creato il: " + user.getCreatedAt());
            InputReader.printSeparator();
        } else {
            System.out.println("\n❌ Nessun utente trovato con username: " + username);
        }
        
        InputReader.pressEnterToContinue();
    }

    //elimina utente
    public void deleteUser() {
        InputReader.clearScreen();
        InputReader.printHeader("ELIMINA UTENTE");
        
        List<User> users = userController.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("\n📝 Nessun utente da eliminare.");
            InputReader.pressEnterToContinue();
            return;
        }
        
        System.out.println("\n👥 Seleziona utente da eliminare:\n");
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            System.out.println((i + 1) + ". " + user.getUsername() + " (" + user.getEmail() + ")");
        }
        System.out.println("0. Annulla");
        System.out.println();
        
        int choice = InputReader.readInt("Scegli un'opzione: ", 0, users.size());
        
        if (choice == 0) {
            return;  //annulla
        }
        
        User userToDelete = users.get(choice - 1);
        
        System.out.println("\n⚠️  ATTENZIONE: Eliminando l'utente verranno eliminati anche:");
        System.out.println("   - Tutti i suoi progetti");
        System.out.println("   - Tutte le task associate");
        System.out.println();
        
        boolean confirm = InputReader.readConfirmation("Sei sicuro di voler eliminare " + 
                                                       userToDelete.getUsername() + "?");
        
        if (confirm) {
            boolean deleted = userController.deleteUser(userToDelete.getId());
            if (deleted) {
                System.out.println("\n✅ Utente eliminato con successo!");
            } else {
                System.out.println("\n❌ Errore nell'eliminazione dell'utente.");
            }
        } else {
            System.out.println("\n❌ Eliminazione annullata.");
        }
        
        InputReader.pressEnterToContinue();
    }

    //menu gestione utenti
    public void manageUsers() {
        boolean running = true;
        
        while (running) {
            InputReader.clearScreen();
            InputReader.printHeader("GESTIONE UTENTI");
            
            System.out.println("\n1. 👥 Visualizza tutti gli utenti");
            System.out.println("2. 🔍 Cerca utente per username");
            System.out.println("3. ➕ Crea nuovo utente");
            System.out.println("4. 🗑️  Elimina utente");
            System.out.println("0. ↩️  Torna indietro");
            System.out.println();
            
            int choice = InputReader.readInt("Scegli un'opzione: ", 0, 4);
            
            switch (choice) {
                case 1 -> showAllUsers();
                case 2 -> searchUserByUsername();
                case 3 -> createNewUser();
                case 4 -> deleteUser();
                case 0 -> running = false;
            }
        }
    }

    //mostra info utente
    public void showUserInfo(User user) {
        InputReader.clearScreen();
        InputReader.printHeader("INFORMAZIONI UTENTE");
        
        System.out.println();
        System.out.println("🆔 ID: " + user.getId());
        System.out.println("👤 Username: " + user.getUsername());
        System.out.println("📧 Email: " + user.getEmail());
        System.out.println("📅 Creato il: " + user.getCreatedAt());
        System.out.println();
        
        InputReader.pressEnterToContinue();
    }
}