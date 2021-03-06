package com.vincent.acnt.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Entry implements Entity, Serializable {
    private int date;
    private List<Subject> subjects = new ArrayList<>();
    private String memo, ps, creator;
    private String documentId;

    public Entry() {

    }

    public int getDate() {
        return date;
    }

    public String getMemo() {
        return memo;
    }

    public String getPs() {
        return ps;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    @Override
    public String obtainDocumentId() {
        return documentId;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setPs(String ps) {
        this.ps = ps;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setSubjects(ArrayList<Subject> subjects) {
        this.subjects = subjects;
    }

    public void addSubject(Subject subject) {
        this.subjects.add(subject);
    }

    @Override
    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int calDifference() {
        return calTotalCredit() - calTotalDebit();
    }

    public int calTotalCredit() {
        int credit = 0;
        for (Subject subject : subjects) {
            credit += subject.getCredit();
        }

        return credit;
    }

    public int calTotalDebit() {
        int debit = 0;
        for (Subject subject : subjects) {
            debit += subject.getDebit();
        }

        return debit;
    }

}
