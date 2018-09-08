package com.vincent.acnt.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable {
    private String id;
    private String name, creator;
    private List<User> approvedMembers = new ArrayList<>(), waitingMembers = new ArrayList<>(), adminMembers = new ArrayList<>();
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

    public List<User> getAdminMembers() {
        return adminMembers;
    }

    public void setAdminMembers(List<User> adminMembers) {
        this.adminMembers = adminMembers;
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

    public void addAdminMember(User member) {
        if (adminMembers == null) {
            adminMembers = new ArrayList<>();
        }
        adminMembers.add(member);
    }

    public void removeApprovedMember(String userId) {
        for (int i = 0, len = approvedMembers.size(); i < len; i++) {
            if (approvedMembers.get(i).getId().equals(userId)) {
                approvedMembers.remove(i);
                return;
            }
        }
    }

    public void removeWaitingMember(String userId) {
        for (int i = 0, len = waitingMembers.size(); i < len; i++) {
            if (waitingMembers.get(i).getId().equals(userId)) {
                waitingMembers.remove(i);
                return;
            }
        }
    }

    public void removeAdminMember(String userId) {
        for (int i = 0, len = adminMembers.size(); i < len; i++) {
            if (adminMembers.get(i).getId().equals(userId)) {
                adminMembers.remove(i);
                return;
            }
        }
    }

    public String obtainDocumentId() {
        return documentId;
    }

    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public boolean isAdmin(String userId) {
        for (int i = 0, len = adminMembers.size(); i < len; i++) {
            if (adminMembers.get(i).getId().equals(userId)) {
                return true;
            }
        }

        return false;
    }
}
