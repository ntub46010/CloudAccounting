package com.vincent.acnt;

import java.util.ArrayList;

public class Transaction {
    private ArrayList<Subject> subjects;
    private String date, ps;

    public Transaction() {}

    public Transaction(String date, String ps) {
        this.subjects = new ArrayList<>();
        this.date = date;
        this.ps = ps;
    }

    public ArrayList<Subject> getSubjects() {
        return subjects;
    }

    public String getDate() {
        return date;
    }

    public String getPs() {
        return ps;
    }

    public void add(Subject subject) {
        subjects.add(subject);
    }

    public void setPs(String ps) {
        this.ps = ps;
    }

    public void setSubjects(ArrayList<Subject> subjects) {
        this.subjects = subjects;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String showDetailText() {
        StringBuffer sb = new StringBuffer();
        for (Subject subject : subjects)
            sb.append(String.format("%s：%d／%d", subject.getName(), subject.getCredit(), subject.getDebit())).append("\n");

        return sb.toString();
    }
}
