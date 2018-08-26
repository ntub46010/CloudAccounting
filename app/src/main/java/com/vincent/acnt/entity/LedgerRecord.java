package com.vincent.acnt.entity;

public class LedgerRecord {
    private int date;
    private String memo;
    private int credit, debit, balance;

    public LedgerRecord() {

    }

    public LedgerRecord(int date, String memo, int credit, int debit) {
        this.date = date;
        this.memo = memo;
        this.credit = credit;
        this.debit = debit;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getDebit() {
        return debit;
    }

    public void setDebit(int debit) {
        this.debit = debit;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
