package com.todolistmanager.view.cli;

import com.todolistmanager.controller.ProjectController;
import com.todolistmanager.controller.TaskController;
import com.todolistmanager.model.Project;
//import com.todolistmanager.model.Task;
import com.todolistmanager.model.User;
import java.util.List;
import java.util.Optional;

//view per gestione progetti
public class ProjectView {
    
    private final ProjectController projectController;
    private final TaskController taskController;

    public ProjectView(ProjectController projectController, TaskController taskController) {
        this.projectController = projectController;
        this.taskController = taskController;
    }

    //menu per gestione progetti
    public void manageProjects(User user) {
        boolean running = true;
        
        while (running) {
            InputReader.clearScreen();
            InputReader.printHeader("GESTIONE PROGETTI");
            System.out.println("\n👤 Utente: " + user.getUsername());
            System.out.println();
            
            System.out.println("1. 📋 Visualizza i miei progetti");
            System.out.println("2. ➕ Crea nuovo progetto");
            System.out.println("3. ✏️  Modifica progetto");
            System.out.println("4. 🗑️  Elimina progetto");
            System.out.println("5. 🔍 Cerca progetto per ID");
            System.out.println("0. ↩️  Torna indietro");
            System.out.println();
            
            int choice = InputReader.readInt("Scegli un'opzione: ", 0, 5);
            
            switch (choice) {
                case 1 -> showUserProjects(user);
                case 2 -> createProject(user);
                case 3 -> updateProject(user);
                case 4 -> deleteProject(user);
                case 5 -> searchProjectById();
                case 0 -> running = false;
            }
        }
    }

    //visualizza progetti utente
    public void showUserProjects(User user) {
        InputReader.clearScreen();
        InputReader.printHeader("I MIEI PROGETTI");
        
        List<Project> projects = projectController.findProjectsByUserId(user.getId());
        
        if (projects.isEmpty()) {
            System.out.println("\n📝 Non hai ancora progetti.");
            System.out.println("   Crea il tuo primo progetto!");
        } else {
            System.out.println("\n📁 Totale progetti: " + projects.size() + "\n");
            
            for (Project project : projects) {
                printProjectCard(project);
            }
        }
        
        InputReader.pressEnterToContinue();
    }

    //crea progetto
    public void createProject(User user) {
        InputReader.clearScreen();
        InputReader.printHeader("CREA NUOVO PROGETTO");
        
        System.out.println("\n📝 Inserisci i dati del progetto:\n");
        
        try {
            String name = InputReader.readString("Nome progetto (min 3 caratteri): ");
            String description = InputReader.readStringOptional("Descrizione (opzionale): ");
            
            if (description.isEmpty()) {
                description = null;
            }
            
            //conferma
            System.out.println("\n📋 Riepilogo:");
            System.out.println("   Nome: " + name);
            System.out.println("   Descrizione: " + (description != null ? description : "Nessuna"));
            System.out.println();
            
            boolean confirm = InputReader.readConfirmation("Confermi la creazione?");
            
            if (!confirm) {
                System.out.println("\n❌ Creazione annullata.");
                InputReader.pressEnterToContinue();
                return;
            }
            
            //crea progetto
            Project project = projectController.createProject(user.getId(), name, description);
            
            System.out.println("\n✅ Progetto creato con successo!");
            System.out.println("   ID: " + project.getId());
            System.out.println("   Nome: " + project.getName());
            System.out.println("   Creato il: " + project.getCreatedAt());
            
            InputReader.pressEnterToContinue();
            
        } catch (IllegalArgumentException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
            InputReader.pressEnterToContinue();
        }
    }

    //modifica progetto
    public void updateProject(User user) {
        InputReader.clearScreen();
        InputReader.printHeader("MODIFICA PROGETTO");
        
        List<Project> projects = projectController.findProjectsByUserId(user.getId());
        
        if (projects.isEmpty()) {
            System.out.println("\n📝 Non hai progetti da modificare.");
            InputReader.pressEnterToContinue();
            return;
        }
        
        //mostra progetti
        System.out.println("\n📁 Seleziona il progetto da modificare:\n");
        for (int i = 0; i < projects.size(); i++) {
            System.out.println((i + 1) + ". " + projects.get(i).getName());
        }
        System.out.println("0. Annulla");
        System.out.println();
        
        int choice = InputReader.readInt("Scegli un'opzione: ", 0, projects.size());
        
        if (choice == 0) {
            return;  //annulla
        }
        
        Project project = projects.get(choice - 1);
        
        System.out.println("\n📝 Dati attuali:");
        System.out.println("   Nome: " + project.getName());
        System.out.println("   Descrizione: " + (project.getDescription() != null ? project.getDescription() : "Nessuna"));
        System.out.println();
        
        try {
            String newName = InputReader.readString("Nuovo nome (Enter per mantenere '" + project.getName() + "'): ");
            if (newName.isEmpty()) {
                newName = project.getName();
            }
            
            String newDescription = InputReader.readStringOptional("Nuova descrizione (Enter per mantenere attuale): ");
            if (newDescription.isEmpty()) {
                newDescription = project.getDescription();
            }
            
            boolean confirm = InputReader.readConfirmation("Confermi le modifiche?");
            
            if (confirm) {
                boolean updated = projectController.updateProject(project.getId(), newName, newDescription);
                if (updated) {
                    System.out.println("\n✅ Progetto aggiornato con successo!");
                } else {
                    System.out.println("\n❌ Errore nell'aggiornamento del progetto.");
                }
            } else {
                System.out.println("\n❌ Modifica annullata.");
            }
            
            InputReader.pressEnterToContinue();
            
        } catch (IllegalArgumentException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
            InputReader.pressEnterToContinue();
        }
    }

    //elimina progetto
    public void deleteProject(User user) {
        InputReader.clearScreen();
        InputReader.printHeader("ELIMINA PROGETTO");
        
        List<Project> projects = projectController.findProjectsByUserId(user.getId());
        
        if (projects.isEmpty()) {
            System.out.println("\n📝 Non hai progetti da eliminare.");
            InputReader.pressEnterToContinue();
            return;
        }
        
        //mostra progetti
        System.out.println("\n📁 Seleziona il progetto da eliminare:\n");
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            int taskCount = taskController.findTasksByProjectId(project.getId()).size();
            System.out.println((i + 1) + ". " + project.getName() + " (" + taskCount + " task)");
        }
        System.out.println("0. Annulla");
        System.out.println();
        
        int choice = InputReader.readInt("Scegli un'opzione: ", 0, projects.size());
        
        if (choice == 0) {
            return;  //annulla
        }
        
        Project project = projects.get(choice - 1);
        int taskCount = taskController.findTasksByProjectId(project.getId()).size();
        
        System.out.println("\n⚠️  ATTENZIONE:");
        System.out.println("   Eliminando questo progetto verranno eliminate anche");
        System.out.println("   tutte le " + taskCount + " task associate!");
        System.out.println();
        
        boolean confirm = InputReader.readConfirmation("Sei sicuro di voler eliminare '" + project.getName() + "'?");
        
        if (confirm) {
            boolean deleted = projectController.deleteProject(project.getId());
            if (deleted) {
                System.out.println("\n✅ Progetto eliminato con successo!");
            } else {
                System.out.println("\n❌ Errore nell'eliminazione del progetto.");
            }
        } else {
            System.out.println("\n❌ Eliminazione annullata.");
        }
        
        InputReader.pressEnterToContinue();
    }

    //cerca progetto per ID
    public void searchProjectById() {
        InputReader.clearScreen();
        InputReader.printHeader("CERCA PROGETTO");
        
        Long id = InputReader.readLong("\n🔍 Inserisci ID del progetto: ");
        
        Optional<Project> projectOpt = projectController.findProjectById(id);
        
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            System.out.println("\n✅ Progetto trovato!\n");
            printProjectCard(project);
        } else {
            System.out.println("\n❌ Nessun progetto trovato con ID: " + id);
        }
        
        InputReader.pressEnterToContinue();
    }

    //gestione task di un progetto
    public void manageTasksFromProjects(User user) {
        InputReader.clearScreen();
        InputReader.printHeader("GESTIONE TASK");
        
        List<Project> projects = projectController.findProjectsByUserId(user.getId());
        
        if (projects.isEmpty()) {
            System.out.println("\n📝 Crea prima un progetto per gestire le task!");
            InputReader.pressEnterToContinue();
            return;
        }
        
        //seleziona progetto
        System.out.println("\n📁 Seleziona il progetto:\n");
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            int taskCount = taskController.findTasksByProjectId(project.getId()).size();
            System.out.println((i + 1) + ". " + project.getName() + " (" + taskCount + " task)");
        }
        System.out.println("0. Annulla");
        System.out.println();
        
        int choice = InputReader.readInt("Scegli un'opzione: ", 0, projects.size());
        
        if (choice == 0) {
            return;
        }
        
        Project selectedProject = projects.get(choice - 1);
        
        //passa a TaskView per gestire le task del progetto
        TaskView taskView = new TaskView(taskController);
        taskView.manageTasksForProject(selectedProject);
    }

    //stampa info progetto
    private void printProjectCard(Project project) {
        int taskCount = taskController.findTasksByProjectId(project.getId()).size();
        int completion = taskController.calculateProjectCompletion(project.getId());
        
        InputReader.printSeparator();
        System.out.println("📁 " + project.getName());
        System.out.println("🆔 ID: " + project.getId());
        if (project.getDescription() != null && !project.getDescription().isEmpty()) {
            System.out.println("📝 Descrizione: " + project.getDescription());
        }
        System.out.println("✅ Task: " + taskCount);
        System.out.println("📈 Completamento: " + completion + "%");
        System.out.println("📅 Creato il: " + project.getCreatedAt());
        InputReader.printSeparator();
        System.out.println();
    }
}