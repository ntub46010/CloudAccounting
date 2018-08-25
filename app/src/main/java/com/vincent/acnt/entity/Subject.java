package com.vincent.acnt.entity;

import java.io.Serializable;

public class Subject implements Serializable {
    private long stamp;
    private int credit, debit;
    private String subjectId, name;
    private String documentId;

    public Subject() {}

    public Subject(String subjectId, String name, int credit, int debit, long stamp) {
        this.subjectId = subjectId;
        this.name = name;
        this.credit = credit;
        this.debit = debit;
        this.stamp = stamp;
    }

    public long getStamp() {
        return stamp;
    }

    public String getName() {
        return name;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public int getCredit() {
        return credit;
    }

    public int getDebit() {
        return debit;
    }

    public String obtainDocumentId() {
        return documentId;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public void setDebit(int debit) {
        this.debit = debit;
    }

    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
