package com.todolistmanager.view.cli;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

//legge input da console
public class InputReader {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    //legge stringa non vuota
    public static String readString(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        while (input.isEmpty()) {
            System.out.print("❌ Input non può essere vuoto. " + prompt);
            input = scanner.nextLine().trim();
        }
        return input;
    }

    //legge stringa (anche vuota)
    public static String readStringOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    //legge numero intero
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
            }
        }
    }

    //legge numero intero con range min-max
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("❌ Il numero deve essere tra " + min + " e " + max);
        }
    }

    //legge Long
    public static Long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
            }
        }
    }

    //legge data nel formato dd/MM/yyyy
    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (formato: gg/mm/aaaa): ");
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    return null;  // Nessuna deadline
                }
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Data non valida! Usa il formato gg/mm/aaaa (es. 25/12/2024)");
            }
        }
    }

    //legge conferma (s/n)
    public static boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (s/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("s") || input.equals("si") || input.equals("yes") || input.equals("y")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("❌ Rispondi con 's' o 'n'");
        }
    }

    //utente deve premere Enter per continuare
    public static void pressEnterToContinue() {
        System.out.print("\n⏎ Premi ENTER per continuare...");
        scanner.nextLine();
    }

    //pulisce schermo (simula clear console)
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    //stampa linea separatrice
    public static void printSeparator() {
        System.out.println("═".repeat(60));
    }

    //stampa intestazione
    public static void printHeader(String title) {
        printSeparator();
        System.out.println("  " + title);
        printSeparator();
    }

    //chiude scanner (chiamare alla fine del programma)
    public static void close() {
        scanner.close();
    }
}