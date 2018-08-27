package com.vincent.acnt.entity;

import java.io.Serializable;

public class ReportItem implements Serializable {
    private String id, name;
    private int totalCredit, totalDebit, balance;

    public ReportItem() {

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

    public int getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(int totalCredit) {
        this.totalCredit = totalCredit;
    }

    public int getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(int totalDebit) {
        this.totalDebit = totalDebit;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
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
