package com.vincent.acnt.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable {
    private String id;
    private String name, creator;
    private List<String> approvedMembers = new ArrayList<>(),
            waitingMembers = new ArrayList<>(),
            adminMembers = new ArrayList<>();
    
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

    public List<String> getApprovedMembers() {
        return approvedMembers;
    }

    public void setApprovedMembers(List<String> approvedMembers) {
        if (approvedMembers == null) {
            approvedMembers = new ArrayList<>();
        }

        this.approvedMembers = approvedMembers;
    }

    public List<String> getWaitingMembers() {
        return waitingMembers;
    }

    public void setWaitingMembers(List<String> waitingMembers) {
        if (waitingMembers == null) {
            waitingMembers = new ArrayList<>();
        }
        this.waitingMembers = waitingMembers;
    }

    public List<String> getAdminMembers() {
        return adminMembers;
    }

    public void setAdminMembers(List<String> adminMembers) {
        this.adminMembers = adminMembers;
    }

    public void addApprovedMember(String member) {
        if (approvedMembers == null) {
            approvedMembers = new ArrayList<>();
        }
        approvedMembers.add(member);
    }

    public void addWaitingMember(String member) {
        if (waitingMembers == null) {
            waitingMembers = new ArrayList<>();
        }
        waitingMembers.add(member);
    }

    public void addAdminMember(String member) {
        if (adminMembers == null) {
            adminMembers = new ArrayList<>();
        }
        adminMembers.add(member);
    }

    public void removeApprovedMember(String userId) {
        approvedMembers.remove(userId);
    }

    public void removeWaitingMember(String userId) {
        waitingMembers.remove(userId);
    }

    public void removeAdminMember(String userId) {
        adminMembers.remove(userId);
    }

    public String obtainDocumentId() {
        return documentId;
    }

    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public boolean isAdminUser(String userId) {
        return adminMembers.contains(userId);
    }

    public boolean isApprovedUser(String userId) {
        return approvedMembers.contains(userId);
    }

    public boolean isWaitingUser(String userId) {
        return waitingMembers.contains(userId);
    }

    public boolean isLegalUser(String userId) {
        return isApprovedUser(userId) || isAdminUser(userId);
    }

}
