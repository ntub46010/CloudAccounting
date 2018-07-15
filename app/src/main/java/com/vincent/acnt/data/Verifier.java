package com.vincent.acnt.data;

import android.content.Context;

import com.vincent.acnt.R;

import java.util.regex.Pattern;

public class Verifier {
    private Context c;
    private String ptnChineseWord = "[\\u4e00-\\u9fa5a-zA-Z_0-9\\u3002\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\uff1f\\u300a\\u300b^\\x00-\\xff]{%s,%s}";
    private String ptnNumber = "[0-9]{%s,%s}";

    private String ptnSubjectId = String.format(ptnNumber, "3", "3");
    private String ptnSubjectName = String.format(ptnChineseWord, "1", "30");
    private String ptnAmount = String.format(ptnNumber, "1", "9");
    private String ptnMemo = String.format(ptnChineseWord, "0", "100");

    public Verifier(Context context) {
        this.c = context;
    }

    public String chkId(String s) {
        if (Pattern.matches(ptnSubjectId, s) && !s.equals("0"))
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

    public String chkAmount(String s) {
        if (Pattern.matches(ptnAmount, s) && !s.equals("0"))
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

}
