package com.library.service;

import com.library.books.Book;
import com.library.exception.BookNotFoundException;
import com.library.exception.BookNotAvailableException;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {

    private static final String FILE_PATH = "books.txt";
    private static final String DB_URL  = System.getProperty("db.url",  "jdbc:mysql://localhost:3306/library_db");
    private static final String DB_USER = System.getProperty("db.user", "root");
    private static final String DB_PASS = System.getProperty("db.pass", "");

    private final boolean useDatabase;

    public LibraryService() {
        boolean db = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = getConnection()) {
                db = c != null && !c.isClosed();
            }
        } catch (Exception e) {
            System.out.println("DB not available, using file storage.");
        }
        this.useDatabase = db;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public void addBook(Book b) throws Exception {
        if (useDatabase) addBookDB(b);
        else addBookFile(b);
    }

    public List<Book> viewBooks() throws Exception {
        return useDatabase ? viewBooksDB() : viewBooksFile();
    }

    public void issueBook(int bookId) throws BookNotFoundException, BookNotAvailableException, Exception {
        if (useDatabase) issueBookDB(bookId);
        else issueBookFile(bookId);
    }

    public void returnBook(int bookId) throws BookNotFoundException, Exception {
        if (useDatabase) returnBookDB(bookId);
        else returnBookFile(bookId);
    }

    public Book searchBook(int bookId) throws BookNotFoundException, Exception {
        return viewBooks().stream()
                .filter(b -> b.getBookId() == bookId)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private void addBookFile(Book b) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(b.toFileLine());
            writer.newLine();
        }
    }

    private List<Book> viewBooksFile() throws IOException {
        List<Book> list = new ArrayList<>();
        File f = new File(FILE_PATH);
        if (!f.exists()) return list;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty())
                    list.add(Book.fromFileLine(line));
            }
        }
        return list;
    }

    private void issueBookFile(int bookId) throws BookNotFoundException, BookNotAvailableException, IOException {
        List<Book> books = viewBooksFile();
        Book book = books.stream()
                .filter(b -> b.getBookId() == bookId)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(bookId));
        if (book.getAvailableCopies() == 0)
            throw new BookNotAvailableException(bookId);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        saveBooksToFile(books);
    }

    private void returnBookFile(int bookId) throws BookNotFoundException, IOException {
        List<Book> books = viewBooksFile();
        Book book = books.stream()
                .filter(b -> b.getBookId() == bookId)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(bookId));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        saveBooksToFile(books);
    }

    private void saveBooksToFile(List<Book> books) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Book b : books) {
                writer.write(b.toFileLine());
                writer.newLine();
            }
        }
    }

    private void addBookDB(Book b) throws SQLException {
        String sql = "INSERT INTO books (book_id, title, author, available_copies) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE title=VALUES(title), author=VALUES(author), available_copies=VALUES(available_copies)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getBookId());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setInt(4, b.getAvailableCopies());
            ps.executeUpdate();
        }
    }

    private List<Book> viewBooksDB() throws SQLException {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT book_id, title, author, available_copies FROM books ORDER BY book_id";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("available_copies")
                ));
            }
        }
        return list;
    }

    private void issueBookDB(int bookId) throws BookNotFoundException, BookNotAvailableException, SQLException {
        try (Connection c = getConnection()) {
            int copies;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT available_copies FROM books WHERE book_id = ?")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new BookNotFoundException(bookId);
                    copies = rs.getInt("available_copies");
                }
            }
            if (copies == 0) throw new BookNotAvailableException(bookId);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }
            logTransaction(c, bookId, "ISSUE");
        }
    }

    private void returnBookDB(int bookId) throws BookNotFoundException, SQLException {
        try (Connection c = getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT book_id FROM books WHERE book_id = ?")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new BookNotFoundException(bookId);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }
            logTransaction(c, bookId, "RETURN");
        }
    }

    private void logTransaction(Connection c, int bookId, String type) {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO transactions (book_id, transaction_type) VALUES (?, ?)")) {
            ps.setInt(1, bookId);
            ps.setString(2, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not log transaction: " + e.getMessage());
        }
    }
}