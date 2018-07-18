package com.vincent.acnt.data;

import java.io.Serializable;
import java.util.ArrayList;

import static com.vincent.acnt.data.DataHelper.Comma;

public class Entry implements Serializable {
    private long date;
    private ArrayList<Subject> subjects;
    private String memo, ps;
    private String documentId;

    public Entry() {}

    public Entry(int date, String memo, String ps) {
        this.subjects = new ArrayList<>();
        this.date = date;
        this.memo = memo;
        this.ps = ps;
    }

    public long getDate() {
        return date;
    }

    public String getMemo() {
        return memo;
    }

    public String getPs() {
        return ps;
    }

    public ArrayList<Subject> getSubjects() {
        return subjects;
    }

    public String gainDocumentId() {
        return documentId;
    }

    public void setDate(long date) {
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

    public void giveDocumentId(String documentId) {
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
