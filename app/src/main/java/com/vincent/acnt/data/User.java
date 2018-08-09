package com.vincent.acnt.data;

import java.util.ArrayList;

public class User {
    private String uid, name, email;
    private ArrayList<String> books;
    private String documentId;

    public User() {

    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
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

    public ArrayList<String> getBooks() {
        return books;
    }

    public void setBooks(ArrayList<String> books) {
        this.books = books;
    }

    public String gainDocumentId() {
        return documentId;
    }

    public void giveDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void addBooks(String bookId) {
        if (books == null)
            books = new ArrayList<>();
        books.add(0, bookId);
    }
}
