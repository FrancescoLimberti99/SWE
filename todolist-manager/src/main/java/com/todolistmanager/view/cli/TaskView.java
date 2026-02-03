package com.todolistmanager.view.cli;

import com.todolistmanager.controller.TaskController;
import com.todolistmanager.model.Priority;
import com.todolistmanager.model.Project;
import com.todolistmanager.model.Task;
import com.todolistmanager.model.TaskStatus;
import java.time.LocalDate;
import java.util.List;
//import java.util.Optional;

//view per gestione task
public class TaskView {
    
    private final TaskController taskController;

    public TaskView(TaskController taskController) {
        this.taskController = taskController;
    }

    //menu per gestire task di un progetto
    public void manageTasksForProject(Project project) {
        boolean running = true;
        
        while (running) {
            InputReader.clearScreen();
            InputReader.printHeader("GESTIONE TASK - " + project.getName());
            System.out.println();
            
            System.out.println("1. 📋 Visualizza tutte le task");
            System.out.println("2. ➕ Crea nuova task");
            System.out.println("3. ✏️  Modifica task");
            System.out.println("4. 🗑️  Elimina task");
            System.out.println("5. 🔍 Filtra task");
            System.out.println("6. ⚠️  Task in ritardo");
            System.out.println("0. ↩️  Torna indietro");
            System.out.println();
            
            int choice = InputReader.readInt("Scegli un'opzione: ", 0, 9);
            
            switch (choice) {
            	case 1 -> showAllTasks(project);
            	case 2 -> createTask(project);
            	case 3 -> modifyTask(project);
            	case 4 -> deleteTask(project);
            	case 5 -> filterTasks(project);
            	case 6 -> showOverdueTasks(project);
            	case 0 -> running = false;
            }
        }
    }

    //visualizza task di un progetto
    public void showAllTasks(Project project) {
        InputReader.clearScreen();
        InputReader.printHeader("TASK - " + project.getName());
        
        List<Task> tasks = taskController.findTasksByProjectId(project.getId());
        
        if (tasks.isEmpty()) {
            System.out.println("\n📝 Nessuna task presente in questo progetto.");
        } else {
            System.out.println("\n✅ Totale task: " + tasks.size());
            int completion = taskController.calculateProjectCompletion(project.getId());
            System.out.println("📈 Completamento: " + completion + "%\n");
            
            for (Task task : tasks) {
                printTaskCard(task);
            }
        }
        
        InputReader.pressEnterToContinue();
    }

    //crea task
    public void createTask(Project project) {
        InputReader.clearScreen();
        InputReader.printHeader("CREA NUOVA TASK");
        
        System.out.println("\n📝 Progetto: " + project.getName());
        System.out.println("\nInserisci i dati della task:\n");
        
        try {
            String title = InputReader.readString("Titolo task (min 3 caratteri): ");
            String description = InputReader.readStringOptional("Descrizione (opzionale): ");
            
            if (description.isEmpty()) {
                description = null;
            }
            
            //seleziona priorità
            System.out.println("\nSeleziona priorità:");
            System.out.println("1. 🔴 HIGH (Alta)");
            System.out.println("2. 🟡 MEDIUM (Media)");
            System.out.println("3. 🟢 LOW (Bassa)");
            int priorityChoice = InputReader.readInt("Scelta: ", 1, 3);
            
            Priority priority = switch (priorityChoice) {
                case 1 -> Priority.HIGH;
                case 2 -> Priority.MEDIUM;
                case 3 -> Priority.LOW;
                default -> Priority.MEDIUM;
            };
            
            //deadline (opzionale)
            boolean hasDeadline = InputReader.readConfirmation("\nVuoi impostare una deadline?");
            LocalDate deadline = null;
            if (hasDeadline) {
                deadline = InputReader.readDate("Deadline");
            }
            
            //conferma
            System.out.println("\n📋 Riepilogo:");
            System.out.println("   Titolo: " + title);
            System.out.println("   Descrizione: " + (description != null ? description : "Nessuna"));
            System.out.println("   Priorità: " + getPriorityIcon(priority) + " " + priority);
            System.out.println("   Deadline: " + (deadline != null ? deadline : "Nessuna"));
            System.out.println();
            
            boolean confirm = InputReader.readConfirmation("Confermi la creazione?");
            
            if (!confirm) {
                System.out.println("\n❌ Creazione annullata.");
                InputReader.pressEnterToContinue();
                return;
            }
            
            //crea task
            Task task = taskController.createTask(project.getId(), title, description, priority, deadline);
            
            System.out.println("\n✅ Task creata con successo!");
            System.out.println("   ID: " + task.getId());
            System.out.println("   Titolo: " + task.getTitle());
            System.out.println("   Stato: " + getStatusIcon(task.getStatus()) + " " + task.getStatus());
            
            InputReader.pressEnterToContinue();
            
        } catch (IllegalArgumentException e) {
            System.out.println("\n❌ Errore: " + e.getMessage());
            InputReader.pressEnterToContinue();
        }
    }

    //menù modifiche task
    public void modifyTask(Project project) {
    	
        //seleziona task per modifiche
        Task task = selectTask(project, "MODIFICA TASK");
        if (task == null) return;
        
        //mostra info attuali
        InputReader.clearScreen();
        InputReader.printHeader("MODIFICA TASK");
        System.out.println("\n📝 Task selezionata:");
        printTaskCard(task);
        
        //opzioni modifica
        System.out.println("\nCosa vuoi modificare?");
        System.out.println("1. ✏️  Informazioni (titolo/descrizione)");
        System.out.println("2. 📊 Stato (TODO/IN_PROGRESS/PAUSED/DONE)");
        System.out.println("3. 🎯 Priorità (LOW/MEDIUM/HIGH)");
        System.out.println("4. 📅 Deadline");
        System.out.println("0. ↩️  Annulla");
        System.out.println();
        
        int choice = InputReader.readInt("Scelta: ", 0, 4);
        
        switch (choice) {
            case 1 -> modifyTaskInfo(task);
            case 2 -> modifyTaskStatus(task);
            case 3 -> modifyTaskPriority(task);
            case 4 -> modifyTaskDeadline(task);
            case 0 -> System.out.println("\n❌ Modifica annullata.");
        }
    }

    //modifica titolo e descrizione
    private void modifyTaskInfo(Task task) {
        System.out.println("\n📝 Dati attuali:");
        System.out.println("   Titolo: " + task.getTitle());
        System.out.println("   Descrizione: " + (task.getDescription() != null ? task.getDescription() : "Nessuna"));
        System.out.println();
        
        try {
            String newTitle = InputReader.readStringOptional("Nuovo titolo (Enter per mantenere): ");
            if (newTitle.isEmpty()) {
                newTitle = task.getTitle();
            }
            
            String newDescription = InputReader.readStringOptional("Nuova descrizione (Enter per mantenere): ");
            if (newDescription.isEmpty()) {
                newDescription = task.getDescription();
            }
            
            boolean confirm = InputReader.readConfirmation("Confermi le modifiche?");
            
            if (confirm) {
                boolean updated = taskController.updateTaskDetails(task.getId(), newTitle, newDescription);
                if (updated) {
                    System.out.println("\n✅ Task aggiornata con successo!");
                } else {
                    System.out.println("\n❌ Errore nell'aggiornamento della task.");
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

    //modifica stato
    private void modifyTaskStatus(Task task) {
        System.out.println("\n📝 Stato attuale: " + getStatusIcon(task.getStatus()) + " " + task.getStatus());
        System.out.println("\nSeleziona nuovo stato:");
        System.out.println("1. 📝 TODO");
        System.out.println("2. 🔄 IN_PROGRESS");
        System.out.println("3. ⏸️  PAUSED"); 
        System.out.println("4. ✅ DONE");
        System.out.println("0. Annulla");
        
        int choice = InputReader.readInt("\nScelta: ", 0, 4);
        
        if (choice == 0) return;
        
        TaskStatus newStatus = switch (choice) {
            case 1 -> TaskStatus.TODO;
            case 2 -> TaskStatus.IN_PROGRESS;
            case 3 -> TaskStatus.PAUSED;
            case 4 -> TaskStatus.DONE;
            default -> task.getStatus();
        };
        
        boolean updated = taskController.updateTaskStatus(task.getId(), newStatus);
        if (updated) {
            System.out.println("\n✅ Stato aggiornato: " + getStatusIcon(newStatus) + " " + newStatus);
        } else {
            System.out.println("\n❌ Errore nell'aggiornamento dello stato.");
        }
        
        InputReader.pressEnterToContinue();
    }

    //modifica priorità
    private void modifyTaskPriority(Task task) {
        System.out.println("\n📝 Priorità attuale: " + getPriorityIcon(task.getPriority()) + " " + task.getPriority());
        System.out.println("\nSeleziona nuova priorità:");
        System.out.println("1. 🔴 HIGH");
        System.out.println("2. 🟡 MEDIUM");
        System.out.println("3. 🟢 LOW");
        System.out.println("0. Annulla");
        
        int choice = InputReader.readInt("\nScelta: ", 0, 3);
        
        if (choice == 0) return;
        
        Priority newPriority = switch (choice) {
            case 1 -> Priority.HIGH;
            case 2 -> Priority.MEDIUM;
            case 3 -> Priority.LOW;
            default -> task.getPriority();
        };
        
        boolean updated = taskController.updateTaskPriority(task.getId(), newPriority);
        if (updated) {
            System.out.println("\n✅ Priorità aggiornata: " + getPriorityIcon(newPriority) + " " + newPriority);
        } else {
            System.out.println("\n❌ Errore nell'aggiornamento della priorità.");
        }
        
        InputReader.pressEnterToContinue();
    }

    //modifica deadline
    private void modifyTaskDeadline(Task task) {
        System.out.println("\n📝 Deadline attuale: " + (task.getDeadline() != null ? task.getDeadline() : "Nessuna"));
        
        LocalDate newDeadline = InputReader.readDate("\nNuova deadline (Enter per nessuna deadline)");
        
        boolean updated = taskController.updateTaskDeadline(task.getId(), newDeadline);
        if (updated) {
            System.out.println("\n✅ Deadline aggiornata: " + (newDeadline != null ? newDeadline : "Nessuna"));
        } else {
            System.out.println("\n❌ Errore nell'aggiornamento della deadline.");
        }
        
        InputReader.pressEnterToContinue();
    }

    //elimina task
    public void deleteTask(Project project) {
        Task task = selectTask(project, "ELIMINA TASK");
        if (task == null) return;
        
        System.out.println("\n⚠️  Stai per eliminare:");
        printTaskCard(task);
        
        boolean confirm = InputReader.readConfirmation("Confermi l'eliminazione?");
        
        if (confirm) {
            boolean deleted = taskController.deleteTask(task.getId());
            if (deleted) {
                System.out.println("\n✅ Task eliminata con successo!");
            } else {
                System.out.println("\n❌ Errore nell'eliminazione della task.");
            }
        } else {
            System.out.println("\n❌ Eliminazione annullata.");
        }
        
        InputReader.pressEnterToContinue();
    }

    //filtra per stato/priorità
    public void filterTasks(Project project) {
        InputReader.clearScreen();
        InputReader.printHeader("FILTRA TASK");
        
        System.out.println("\nFiltra per:");
        System.out.println("1. Stato");
        System.out.println("2. Priorità");
        System.out.println("3. Task in scadenza (prossimi 7 giorni)");
        System.out.println("0. Annulla");
        
        int choice = InputReader.readInt("\nScelta: ", 0, 3);
        
        List<Task> filteredTasks = null;
        
        switch (choice) {
            case 1 -> {
                System.out.println("\nFiltra per stato:");
                System.out.println("1. TODO");
                System.out.println("2. IN_PROGRESS");
                System.out.println("3. PAUSED");
                System.out.println("4. DONE");
                int statusChoice = InputReader.readInt("Scelta: ", 1, 4);
                TaskStatus status = switch (statusChoice) {
                    case 1 -> TaskStatus.TODO;
                    case 2 -> TaskStatus.IN_PROGRESS;
                    case 3 -> TaskStatus.PAUSED;
                    case 4 -> TaskStatus.DONE;
                    default -> null;
                };
                if (status != null) {
                    filteredTasks = taskController.findTasksByStatus(status).stream()
                            .filter(t -> t.getProjectId().equals(project.getId()))
                            .toList();
                }
            }
            case 2 -> {
                System.out.println("\nFiltra per priorità:");
                System.out.println("1. HIGH");
                System.out.println("2. MEDIUM");
                System.out.println("3. LOW");
                int priorityChoice = InputReader.readInt("Scelta: ", 1, 3);
                Priority priority = switch (priorityChoice) {
                    case 1 -> Priority.HIGH;
                    case 2 -> Priority.MEDIUM;
                    case 3 -> Priority.LOW;
                    default -> null;
                };
                if (priority != null) {
                    filteredTasks = taskController.findTasksByPriority(priority).stream()
                            .filter(t -> t.getProjectId().equals(project.getId()))
                            .toList();
                }
            }
            case 3 -> {
                filteredTasks = taskController.findTasksDueInNextDays(7).stream()
                        .filter(t -> t.getProjectId().equals(project.getId()))
                        .toList();
            }
            case 0 -> {
                return;
            }
        }
        
        if (filteredTasks != null) {
            System.out.println("\n📋 Risultati: " + filteredTasks.size() + " task trovate\n");
            if (filteredTasks.isEmpty()) {
                System.out.println("Nessuna task trovata con questi criteri.");
            } else {
                for (Task task : filteredTasks) {
                    printTaskCard(task);
                }
            }
        }
        
        InputReader.pressEnterToContinue();
    }

    //mostra task in ritardo
    public void showOverdueTasks(Project project) {
        InputReader.clearScreen();
        InputReader.printHeader("TASK IN RITARDO");
        
        List<Task> overdueTasks = taskController.findOverdueTasks().stream()
                .filter(t -> t.getProjectId().equals(project.getId()))
                .toList();
        
        if (overdueTasks.isEmpty()) {
            System.out.println("\n✅ Nessuna task in ritardo! Ottimo lavoro!");
        } else {
            System.out.println("\n⚠️  Hai " + overdueTasks.size() + " task in ritardo:\n");
            for (Task task : overdueTasks) {
                printTaskCard(task);
            }
        }
        
        InputReader.pressEnterToContinue();
    }

    //seleziona task da lista
    private Task selectTask(Project project, String title) {
        InputReader.clearScreen();
        InputReader.printHeader(title);
        
        List<Task> tasks = taskController.findTasksByProjectId(project.getId());
        
        if (tasks.isEmpty()) {
            System.out.println("\n📝 Nessuna task presente in questo progetto.");
            InputReader.pressEnterToContinue();
            return null;
        }
        
        System.out.println("\n📋 Seleziona una task:\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            System.out.println((i + 1) + ". " + getStatusIcon(task.getStatus()) + " " + 
                             task.getTitle() + " [" + getPriorityIcon(task.getPriority()) + "]");
        }
        System.out.println("0. Annulla");
        System.out.println();
        int choice = InputReader.readInt("Scelta: ", 0, tasks.size());
        
        if (choice == 0) {
            return null;
        }
        
        return tasks.get(choice - 1);
    }
    
    //stampa info task
    private void printTaskCard(Task task) {
        InputReader.printSeparator();
        System.out.println(getStatusIcon(task.getStatus()) + " " + task.getTitle());
        System.out.println("🆔 ID: " + task.getId());
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            System.out.println("📝 Descrizione: " + task.getDescription());
        }
        System.out.println("📊 Stato: " + task.getStatus());
        System.out.println("🎯 Priorità: " + getPriorityIcon(task.getPriority()) + " " + task.getPriority());
        if (task.getDeadline() != null) {
            System.out.println("📅 Deadline: " + task.getDeadline());
            if (task.getDeadline().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.DONE) {
                System.out.println("⚠️  IN RITARDO!");
            }
        }
        if (task.getCompletedAt() != null) {
            System.out.println("✅ Completata il: " + task.getCompletedAt());
        }
        System.out.println("📅 Creata il: " + task.getCreatedAt());
        InputReader.printSeparator();
        System.out.println();
    }

    //ritorna icona stato
    private String getStatusIcon(TaskStatus status) {
        return switch (status) {
            case TODO -> "📝";
            case IN_PROGRESS -> "🔄";
            case PAUSED -> "⏸️";
            case DONE -> "✅";
        };
    }

    //ritorna icona priorità
    private String getPriorityIcon(Priority priority) {
        return switch (priority) {
            case HIGH -> "🔴";
            case MEDIUM -> "🟡";
            case LOW -> "🟢";
        };
    }
}