package com.vincent.acnt.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.vincent.acnt.MyApp;
import com.vincent.acnt.R;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;

public class Utility {
    private static String symbols = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static AlertDialog.Builder getPlainDialog(Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("確定", null);
    }

    public static Dialog getWaitingDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dlg_waiting);
        dialog.setCancelable(false);
        return dialog;
    }

    public static View[] getEditTextLayout(Context context, String defaultContentText) {
        LinearLayout container = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(40, 0, 40, 0);
        container.setOrientation(LinearLayout.VERTICAL);

        final EditText editText = new EditText(context);
        editText.setLayoutParams(lp);
        editText.setMaxLines(1);
        editText.setSingleLine(true);
        editText.setText(defaultContentText);
        container.addView(editText);

        return new View[] {container, editText};
    }

    public static String getEngMonth(String month) {
        switch (month){
            case "01":
                return "Jan";
            case "02":
                return "Feb";
            case "03":
                return "Mar";
            case "04":
                return "Apr";
            case "05":
                return "May";
            case "06":
                return "Jun";
            case "07":
                return "Jul";
            case "08":
                return "Aug";
            case "09":
                return "Sep";
            case "10":
                return "Oct";
            case "11":
                return "Nov";
            case "12":
                return "Dec";
            default:
                return "";
        }
    }

    public static int getWeekColor(Calendar calendar) {
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                return MyApp.res.getColor(R.color.week_sun);
            case 2:
                return MyApp.res.getColor(R.color.week_mon);
            case 3:
                return MyApp.res.getColor(R.color.week_tue);
            case 4:
                return MyApp.res.getColor(R.color.week_wen);
            case 5:
                return MyApp.res.getColor(R.color.week_thr);
            case 6:
                return MyApp.res.getColor(R.color.week_fri);
            case 7:
                return MyApp.res.getColor(R.color.week_sat);
            default:
                return MyApp.res.getColor(R.color.week_sun);
        }
    }

    public static int getDateNumber(int year, int month, int day) {
        return year * 10000 + month * 100 + day;
    }

    public static boolean isEmptyString(String s) {
        return s == null || s.equals("");
    }

    public static String encrypt(String text) {
        text = URLEncoder.encode(text);

        StringBuilder sb = new StringBuilder();
        String str;
        for (int i = 0; i < text.length(); i++) {
            str = String.valueOf((int) text.charAt(i));
            if (str.length() < 3)
                sb.append("0").append(str);
            else
                sb.append(str);
        }
        return convertTo62Notation(sb.toString());
    }

    public static String decrypt(String text) {
        text = convertTo10Notation(text);

        StringBuilder sb = new StringBuilder();
        int ascCode;
        for (int i = 0; i < text.length() - 2; i = i + 3) {
            ascCode = Integer.parseInt(text.substring(i, i + 3));
            sb.append((char) ascCode);
        }

        return URLDecoder.decode(sb.toString());
    }

    public static String convertTo62Notation(String text) {
        StringBuilder sb = new StringBuilder();
        BigDecimal multiple = new BigDecimal(symbols.length());
        BigDecimal dec = new BigDecimal(text);
        BigDecimal r;

        r = dec.remainder(multiple);
        dec = dec.divide(multiple, BigDecimal.ROUND_DOWN);
        while (dec.compareTo(multiple) >= 0) {
            sb.insert(0, symbols.substring(r.intValue(), r.intValue() + 1));
            r = dec.remainder(multiple);
            dec = dec.divide(multiple, BigDecimal.ROUND_DOWN);
        }
        sb.insert(0, symbols.substring(r.intValue(), r.intValue() + 1));
        sb.insert(0, symbols.substring(dec.intValue(), dec.intValue() + 1));

        String result = sb.toString();
        return result.substring(0, 1).equals("0") ? result.substring(1, 2) : result;
    }

    public static String convertTo10Notation(String text) {
        BigDecimal multiple = new BigDecimal(symbols.length());
        BigDecimal dec, total = new BigDecimal("0");
        for (int i = 0; i < text.length(); i++) {
            dec = new BigDecimal(symbols.indexOf(text.substring(i, i + 1)));
            dec = dec.multiply(multiple.pow(text.length() - i - 1));
            total = total.add(dec);
        }

        StringBuilder result = new StringBuilder(total.toString());
        for (int i = 3 - result.length() % 3; i > 0; i--)
            result.insert(0, "0");

        return result.toString();
    }

}
