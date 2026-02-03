package com.todolistmanager.view.cli;

import com.todolistmanager.controller.ProjectController;
import com.todolistmanager.controller.TaskController;
import com.todolistmanager.controller.UserController;
import com.todolistmanager.model.User;
import com.todolistmanager.model.TaskStatus;
import com.todolistmanager.observer.AuditLogger;            
import com.todolistmanager.observer.DeadlineNotifier;        
import com.todolistmanager.observer.ProjectStatisticsObserver;

//view principale dell'applicazione
public class MainView {
    
    private final UserController userController;
    private final ProjectController projectController;
    private final TaskController taskController;
    
    private final UserView userView;
    private final ProjectView projectView;
    //private final TaskView taskView;
    
    private User currentUser;

    public MainView() {
        //inizializza controller
        this.userController = new UserController();
        this.projectController = new ProjectController();
        this.taskController = new TaskController();
        
        //registra observer
        taskController.addObserver(new DeadlineNotifier());
        taskController.addObserver(new ProjectStatisticsObserver());
        taskController.addObserver(new AuditLogger());
        
        //inizializza view
        this.userView = new UserView(userController);
        this.projectView = new ProjectView(projectController, taskController);
        //this.taskView = new TaskView(taskController);
    }

    //avvia applicazione
    public void start() {
        
    	//messaggio di benvenuto
    	showWelcome();
        
        //selezione/creazione utente
        currentUser = userView.selectOrCreateUser();
        
        if (currentUser == null) {
            System.out.println("\n👋 Arrivederci!");
            return;
        }
        
        //menu principale
        mainMenu();
        
        System.out.println("\n👋 Arrivederci, " + currentUser.getUsername() + "!");
    }

    //messaggio di benvenuto
    private void showWelcome() {
        InputReader.clearScreen();
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                                                        ║");
        System.out.println("║          📋 TODO LIST MANAGER 📋                        ║");
        System.out.println("║                                                        ║");
        System.out.println("║          Gestisci i tuoi progetti e task!              ║");
        System.out.println("║                                                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    //menu principale
    private void mainMenu() {
        boolean running = true;
        
        while (running) {
            InputReader.clearScreen();
            InputReader.printHeader("MENU PRINCIPALE");
            System.out.println("\n👤 Utente: " + currentUser.getUsername());
            System.out.println();
            System.out.println("1. 📁 Gestisci Progetti");
            System.out.println("2. ✅ Gestisci Task");
            System.out.println("3. 👤 Cambia Utente");
            System.out.println("4. 📊 Statistiche");
            System.out.println("5. 👥 Gestisci Utenti");
            System.out.println("0. 🚪 Esci");
            System.out.println();
            
            int choice = InputReader.readInt("Scegli un'opzione: ", 0, 5);
            
            switch (choice) {
                case 1 -> projectView.manageProjects(currentUser);
                case 2 -> projectView.manageTasksFromProjects(currentUser);
                case 3 -> {
                    currentUser = userView.selectOrCreateUser();
                    if (currentUser == null) {
                        running = false;
                    }
                }
                case 4 -> showStatistics();
                case 5 -> userView.manageUsers();
                case 0 -> running = false;
            }
        }
    }

    //mostra statistiche generali
    private void showStatistics() {
        InputReader.clearScreen();
        InputReader.printHeader("STATISTICHE");

        int totalProjects = projectController.countProjectsByUserId(currentUser.getId());
        var projects = projectController.findProjectsByUserId(currentUser.getId());

        int totalTasks = 0;
        int todoTasks = 0;
        int inProgressTasks = 0;
        int pausedTasks = 0;
        int completedTasks = 0;
        int overdueTasks = 0;

        for (var project : projects) {
            var tasks = taskController.findTasksByProjectId(project.getId());
            totalTasks += tasks.size();
            
            todoTasks += (int) tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.TODO)
                    .count();
            
            inProgressTasks += (int) tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS)
                    .count();
            
            pausedTasks += (int) tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.PAUSED)
                    .count();
            
            completedTasks += (int) tasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .count();
        }

        var allOverdue = taskController.findOverdueTasks();
        for (var task : allOverdue) {
            for (var project : projects) {
                if (task.getProjectId().equals(project.getId())) {
                    overdueTasks++;
                    break;
                }
            }
        }

        System.out.println("\n📊 Le tue statistiche:");
        System.out.println("   📁 Progetti totali: " + totalProjects);
        System.out.println("   ✅ Task totali: " + totalTasks);
        System.out.println();
        System.out.println("   📝 TODO: " + todoTasks);
        System.out.println("   🔄 In Progress: " + inProgressTasks);
        System.out.println("   ⏸️  In Pausa: " + pausedTasks);
        System.out.println("   ✔️  Completate: " + completedTasks);
        System.out.println();
        System.out.println("   ⚠️  Task in ritardo: " + overdueTasks);

        if (totalTasks > 0) {
            int percentage = (completedTasks * 100) / totalTasks;
            System.out.println("   📈 Percentuale completamento: " + percentage + "%");
        }

        InputReader.pressEnterToContinue();
    }

    //punto di ingresso
    public static void main(String[] args) {
        MainView app = new MainView();
        app.start();
        InputReader.close();
    }
}