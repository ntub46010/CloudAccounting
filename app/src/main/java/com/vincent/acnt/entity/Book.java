package com.vincent.acnt.entity;

import java.util.ArrayList;

public class Book {
    private String id;
    private String name, creator;
    private ArrayList<String> memberIds;
    private String documentId;

    public Book() {

    }

    public Book(String id, String name, String creator) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.memberIds = new ArrayList<>();
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

    public ArrayList<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        if (memberIds == null)
            memberIds = new ArrayList<>();
        this.memberIds = memberIds;
    }

    public void addMember(String userId) {
        if (memberIds == null)
            memberIds = new ArrayList<>();
        memberIds.add(userId);
    }

    public String obtainDocumentId() {
        return documentId;
    }

    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
