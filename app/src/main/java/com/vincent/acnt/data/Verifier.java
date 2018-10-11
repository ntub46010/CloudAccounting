package com.vincent.acnt.data;

import android.content.Context;

import com.vincent.acnt.R;

import java.util.regex.Pattern;

public class Verifier {
    private Context c;
    private String ptnWord = "[a-zA-Z0-9]{%s,%s}";
    private String ptnChineseWord = "[\\u4e00-\\u9fa5a-zA-Z_0-9\\u3002\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\uff1f\\u300a\\u300b^\\x00-\\xff]{%s,%s}";
    private String ptnNumber = "[0-9]{%s,%s}";
    private String ptnEmail = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$";

    private String ptnPassword = String.format(ptnWord, "8", "20");
    private String ptnSubjectNo = String.format(ptnNumber, "3", "3");
    private String ptnSubjectName = String.format(ptnChineseWord, "1", "30");
    private String ptnAmount = String.format(ptnNumber, "1", "9");
    private String ptnMemo = String.format(ptnChineseWord, "0", "100");

    public Verifier(Context context) {
        this.c = context;
    }

    public String chkNickName(String s) {
        if (s.equals(""))
            return "暱稱未填寫";
        else if (s.length() > 20)
            return "暱稱不可超過20字";
        else
            return null;
    }

    public String chkPassword(String s) {
        if (Pattern.matches(ptnPassword, s))
            return null;
        else
            return "密碼需為8~20位英數字";
    }

    public String chkPasswordEqual(String pwd1, String pwd2) {
        if (pwd1.equals(pwd2))
            return null;
        else
            return "確認密碼不相符";
    }

    public String chkEmail(String s) {
        if (Pattern.matches(ptnEmail, s))
            return null;
        else
            return c.getString(R.string.chk_format_wrong, "Email");
    }

    public String chkSubjectNo(String s) {
        if (Pattern.matches(ptnSubjectNo, s) && !s.substring(2, 3).equals("0"))
            return "";
        else
            return c.getString(R.string.chk_format_wrong, "編號");
    }

    public String chkSubjectName(String s) {
        if (Pattern.matches(ptnSubjectName, s))
            return "";
        else
            return c.getString(R.string.chk_format_wrong, "科目名稱");
    }

    public String chkSubjectAmount(String s) {
        if (Pattern.matches(ptnAmount, s))
            return "";
        else
            return c.getString(R.string.chk_format_wrong, "金額");
    }

    public String chkMemo(String s) {
        if (Pattern.matches(ptnMemo, s))
            return "";
        else
            return c.getString(R.string.chk_max_words, "摘要", "100");
    }

    public String chkPs(String s) {
        if (Pattern.matches(ptnMemo, s))
            return "";
        else
            return c.getString(R.string.chk_max_words, "備註", "100");
    }

}
