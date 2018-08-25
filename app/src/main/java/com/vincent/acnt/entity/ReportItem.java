package com.vincent.acnt.entity;

import java.io.Serializable;

public class ReportItem implements Serializable {
    private String id, name;
    private int totalCredit, totalDebit, balance;

    public ReportItem(String id, String name, int totalCredit, int totalDebit) {
        this.id = id;
        this.name = name;
        this.totalCredit = totalCredit;
        this.totalDebit = totalDebit;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalCredit() {
        return totalCredit;
    }

    public int getTotalDebit() {
        return totalDebit;
    }

    public int getBalance() {
        return balance;
    }

    public void addCredit(int credit) {
        totalCredit += credit;
    }

    public void addDebit(int debit) {
        totalDebit += debit;
    }

    public void calBalance() {
        balance = totalCredit - totalDebit;
    }
}
