package com.library.exception;

public class BookNotAvailableException extends Exception {

    private static final long serialVersionUID = 1L;
    private final int bookId;

    public BookNotAvailableException(int bookId) {
        super("No copies available for book ID: " + bookId);
        this.bookId = bookId;
    }

    public int getBookId() {
        return bookId;
    }
}