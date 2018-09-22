package com.vincent.acnt.entity;

import java.io.Serializable;

public class Subject implements Entity, Serializable {
    private long id;
    private int credit, debit;
    private String no, name;
    private String documentId;

    public Subject() {

    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNo() {
        return no;
    }

    public int getCredit() {
        return credit;
    }

    public int getDebit() {
        return debit;
    }

    @Override
    public String obtainDocumentId() {
        return documentId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public void setDebit(int debit) {
        this.debit = debit;
    }

    @Override
    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
