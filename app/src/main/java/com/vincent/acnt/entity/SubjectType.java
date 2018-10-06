package com.vincent.acnt.entity;

import com.vincent.acnt.MyApp;
import com.vincent.acnt.R;

import java.util.NoSuchElementException;

public enum SubjectType {
    ASSET("1", MyApp.res.getColor(R.color.type_asset)),
    LIABILITY("2", MyApp.res.getColor(R.color.type_liability)),
    CAPITAL("3", MyApp.res.getColor(R.color.type_capital)),
    REVENUE("4", MyApp.res.getColor(R.color.type_revenue)),
    EXPENSE("5", MyApp.res.getColor(R.color.type_expense));

    private String code;

    private int color;

    SubjectType(String code, int color) {
        this.code = code;
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public int getColor() {
        return color;
    }

    public static SubjectType getType(String code) {
        switch (code) {
            case "1":
                return ASSET;

            case "2":
                return LIABILITY;

            case "3":
                return CAPITAL;

            case "4":
                return REVENUE;

            case "5":
                return EXPENSE;
        }

        throw new NoSuchElementException("No such subject type which code prefix is " + code);
    }
}
