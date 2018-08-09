package com.vincent.acnt.data;

public class Book {
    private String id;
    private String name, creator;
    private String documentId;

    public Book() {

    }

    public Book(String id, String name, String creator) {
        this.id = id;
        this.name = name;
        this.creator = creator;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String gainDocumentId() {
        return documentId;
    }

    public void giveDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
