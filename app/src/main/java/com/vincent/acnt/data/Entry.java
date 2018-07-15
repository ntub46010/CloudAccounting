package com.vincent.acnt.data;

import java.util.ArrayList;

import static com.vincent.acnt.data.DataHelper.Comma;

public class Entry {
    private long date;
    private ArrayList<Subject> subjects;
    private String memo;
    private String documentId;

    public Entry() {}

    public Entry(int date, String memo) {
        this.subjects = new ArrayList<>();
        this.date = date;
        this.memo = memo;
    }

    public long getDate() {
        return date;
    }

    public String getMemo() {
        return memo;
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

    /*public String gainDetailText() {
        StringBuffer sb = new StringBuffer();
        for (Subject subject : subjects)
            sb.append(String.format("%s：%s／%s", subject.getName(), Comma(subject.getCredit()), Comma(subject.getDebit()))).append("\n");

        return sb.toString();
    }*/
}
