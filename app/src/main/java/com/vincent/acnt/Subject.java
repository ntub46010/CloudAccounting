package com.vincent.acnt;

public class Subject {
    private String name;
    private int type, credit, debit;

    public Subject() {}

    public Subject(String name, int credit, int debit) {
        this.name = name;
        this.credit = credit;
        this.debit = debit;
    }

    public Subject(int type, String name, int credit, int debit) {
        this.type = type;
        this.name = name;
        this.credit = credit;
        this.debit = debit;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getCredit() {
        return credit;
    }

    public int getDebit() {
        return debit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public void setDebit(int debit) {
        this.debit = debit;
    }
}
