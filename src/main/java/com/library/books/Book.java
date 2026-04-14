package com.library.books;

public class Book {

    private int bookId;
    private String title;
    private String author;
    private int availableCopies;

    public Book(int bookId, String title, String author, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.availableCopies = availableCopies;
    }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public void displayBook() {
        System.out.printf("| %-6d | %-30s | %-20s | %-16d |%n",
                bookId, title, author, availableCopies);
    }

    public String toFileLine() {
        return bookId + "|" + title + "|" + author + "|" + availableCopies;
    }

    public static Book fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4)
            throw new IllegalArgumentException("Bad record: " + line);
        return new Book(
                Integer.parseInt(parts[0].trim()),
                parts[1].trim(),
                parts[2].trim(),
                Integer.parseInt(parts[3].trim())
        );
    }

    @Override
    public String toString() {
        return "Book{id=" + bookId + ", title='" + title + "', author='" + author + "', copies=" + availableCopies + "}";
    }
}