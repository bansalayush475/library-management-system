package com.library.main;

import com.library.books.Book;
import com.library.exception.BookNotFoundException;
import com.library.exception.BookNotAvailableException;
import com.library.service.LibraryService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        LibraryService service = new LibraryService();

        System.out.println("=== Library Management System ===\n");

        System.out.println("Adding books...");
        try {
            service.addBook(new Book(101, "Clean Code",                 "Robert C. Martin", 3));
            service.addBook(new Book(102, "The Pragmatic Programmer",   "Andy Hunt",        2));
            service.addBook(new Book(103, "Design Patterns",            "Gang of Four",     1));
            service.addBook(new Book(104, "Effective Java",             "Joshua Bloch",     4));
            service.addBook(new Book(105, "Introduction to Algorithms", "CLRS",             0));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println("\nAll Books:");
        printBooks(service);

        System.out.println("\nIssuing Book ID 101:");
        try {
            service.issueBook(101);
            System.out.println("Book 101 issued.");
        } catch (BookNotFoundException | BookNotAvailableException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected: " + e.getMessage());
        }

        System.out.println("\nIssuing Book ID 105 (0 copies):");
        try {
            service.issueBook(105);
        } catch (BookNotAvailableException e) {
            System.err.println("BookNotAvailableException: " + e.getMessage());
        } catch (BookNotFoundException e) {
            System.err.println("BookNotFoundException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected: " + e.getMessage());
        }

        System.out.println("\nIssuing Book ID 999 (does not exist):");
        try {
            service.issueBook(999);
        } catch (BookNotFoundException e) {
            System.err.println("BookNotFoundException: " + e.getMessage());
        } catch (BookNotAvailableException e) {
            System.err.println("BookNotAvailableException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected: " + e.getMessage());
        }

        System.out.println("\nReturning Book ID 101:");
        try {
            service.returnBook(101);
            System.out.println("Book 101 returned.");
        } catch (BookNotFoundException e) {
            System.err.println("BookNotFoundException: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected: " + e.getMessage());
        }

        System.out.println("\nUpdated Catalogue:");
        printBooks(service);
    }

    private static void printBooks(LibraryService service) {
        try {
            List<Book> books = service.viewBooks();
            if (books.isEmpty()) { System.out.println("No books found."); return; }
            System.out.printf("| %-6s | %-30s | %-20s | %-16s |%n",
                    "ID", "Title", "Author", "Available Copies");
            System.out.println("|--------|--------------------------------|----------------------|------------------|");
            for (Book b : books) b.displayBook();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}