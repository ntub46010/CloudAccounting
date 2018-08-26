package com.vincent.acnt.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Entry implements Serializable {
    private int date;
    private List<Subject> subjects = new ArrayList<>();
    private String memo, ps;
    private String documentId;

    public Entry() {

    }

    public Entry(int date, String memo, String ps) {
        this.subjects = new ArrayList<>();
        this.date = date;
        this.memo = memo;
        this.ps = ps;
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

    public void setSubjects(ArrayList<Subject> subjects) {
        this.subjects = subjects;
    }

    public void addSubject(Subject subject) {
        this.subjects.add(subject);
    }

    public void defineDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int calDifference() {
        return calTotalCredit() - calTotalDebit();
    }

    public int calTotalCredit() {
        int credit = 0;
        for (Subject subject : subjects)
            credit += subject.getCredit();
        return credit;
    }

    public int calTotalDebit() {
        int debit = 0;
        for (Subject subject : subjects)
            debit += subject.getDebit();
        return debit;
    }

}
