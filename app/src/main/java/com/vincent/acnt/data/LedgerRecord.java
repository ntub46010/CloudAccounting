package com.vincent.acnt.data;

public class LedgerRecord {
    private long date;
    private String memo;
    private int credit, debit, balance;

    public LedgerRecord(long date, String memo, int credit, int debit) {
        this.date = date;
        this.memo = memo;
        this.credit = credit;
        this.debit = debit;
    }

    public long getDate() {
        return date;
    }

    public String getMemo() {
        return memo;
    }

    public int getCredit() {
        return credit;
    }

    public int getDebit() {
        return debit;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

}
