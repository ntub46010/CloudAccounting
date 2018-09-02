package com.vincent.acnt.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable {
    private String id;
    private String name, creator;
    private List<User> approvedMembers = new ArrayList<>(), waitingMembers = new ArrayList<>();
    private String documentId;

    public Book() {

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

    public List<User> getApprovedMembers() {
        return approvedMembers;
    }

    public void setApprovedMembers(List<User> approvedMembers) {
        if (approvedMembers == null) {
            approvedMembers = new ArrayList<>();
        }

        this.approvedMembers = approvedMembers;
    }

    public List<User> getWaitingMembers() {
        return waitingMembers;
    }

    public void setWaitingMembers(List<User> waitingMembers) {
        if (waitingMembers == null) {
            waitingMembers = new ArrayList<>();
        }
        this.waitingMembers = waitingMembers;
    }

    public void addApprovedMember(User member) {
        if (approvedMembers == null) {
            approvedMembers = new ArrayList<>();
        }
        approvedMembers.add(member);
    }

    public void addWaitingMember(User member) {
        if (waitingMembers == null) {
            waitingMembers = new ArrayList<>();
        }
        waitingMembers.add(member);
    }

    public String obtainDocumentId() {
        return documentId;
    }

    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
