package com.library.servlet;

import com.library.books.Book;
import com.library.exception.BookNotFoundException;
import com.library.exception.BookNotAvailableException;
import com.library.service.LibraryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class BookServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private LibraryService service;

    @Override
    public void init() throws ServletException {
        service = new LibraryService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        String idParam = req.getParameter("id");
        try {
            if (idParam != null) {
                Book b = service.searchBook(Integer.parseInt(idParam));
                out.print(toJson(b));
            } else {
                out.print(toJsonArray(service.viewBooks()));
            }
        } catch (BookNotFoundException e) {
            resp.setStatus(404); out.print(error(e.getMessage()));
        } catch (NumberFormatException e) {
            resp.setStatus(400); out.print(error("Invalid book ID."));
        } catch (Exception e) {
            resp.setStatus(500); out.print(error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        try {
            int id        = Integer.parseInt(req.getParameter("bookId"));
            String title  = req.getParameter("title");
            String author = req.getParameter("author");
            int copies    = Integer.parseInt(req.getParameter("availableCopies"));
            if (title == null || title.isBlank() || author == null || author.isBlank()) {
                resp.setStatus(400); out.print(error("Title and author required.")); return;
            }
            Book b = new Book(id, title.trim(), author.trim(), copies);
            service.addBook(b);
            resp.setStatus(201);
            out.print("{\"success\":true,\"message\":\"Book added.\",\"book\":" + toJson(b) + "}");
        } catch (NumberFormatException e) {
            resp.setStatus(400); out.print(error("Invalid number format."));
        } catch (Exception e) {
            resp.setStatus(500); out.print(error(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();
        String idParam  = req.getParameter("id");
        if (idParam == null || pathInfo == null) {
            resp.setStatus(400); out.print(error("Missing id or action.")); return;
        }
        try {
            int id = Integer.parseInt(idParam);
            if (pathInfo.equals("/issue")) {
                service.issueBook(id);
                out.print("{\"success\":true,\"message\":\"Book " + id + " issued.\"}");
            } else if (pathInfo.equals("/return")) {
                service.returnBook(id);
                out.print("{\"success\":true,\"message\":\"Book " + id + " returned.\"}");
            } else {
                resp.setStatus(400); out.print(error("Unknown action."));
            }
        } catch (BookNotFoundException e) {
            resp.setStatus(404); out.print(error(e.getMessage()));
        } catch (BookNotAvailableException e) {
            resp.setStatus(409); out.print(error(e.getMessage()));
        } catch (NumberFormatException e) {
            resp.setStatus(400); out.print(error("Invalid book ID."));
        } catch (Exception e) {
            resp.setStatus(500); out.print(error(e.getMessage()));
        }
    }

    private String toJson(Book b) {
        return String.format("{\"bookId\":%d,\"title\":\"%s\",\"author\":\"%s\",\"availableCopies\":%d}",
                b.getBookId(), esc(b.getTitle()), esc(b.getAuthor()), b.getAvailableCopies());
    }

    private String toJsonArray(List<Book> books) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < books.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(books.get(i)));
        }
        return sb.append("]").toString();
    }

    private String error(String msg) {
        return "{\"success\":false,\"error\":\"" + esc(msg) + "\"}";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}