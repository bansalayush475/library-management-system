package com.library.exception;

public class BookNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;
    private final int bookId;

    public BookNotFoundException(int bookId) {
        super("Book not found with ID: " + bookId);
        this.bookId = bookId;
    }

    public int getBookId() {
        return bookId;
    }
}