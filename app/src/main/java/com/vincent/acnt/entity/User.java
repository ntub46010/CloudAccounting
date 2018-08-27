package com.vincent.acnt.entity;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid, name, email;
    private List<String> books;
    private String documentId;

    public User() {

    }

    public User(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
