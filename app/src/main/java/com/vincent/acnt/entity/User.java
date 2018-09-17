package com.vincent.acnt.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String id;
    private String name, email;
    private List<String> books;
    private String documentId;

    public User() {

    }

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getBooks() {
        return books;
    }

    public void setBooks(List<String> books) {
        if (books == null) {
            books = new ArrayList<>();
        }

        this.books = books;
    }

    public String obtainDocumentId() {
        return documentId;
    }

    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void addBooks(String bookId) {
        if (books == null) {
            books = new ArrayList<>();
        }

        books.add(0, bookId);
    }
}
