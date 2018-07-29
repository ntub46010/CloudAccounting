package com.vincent.acnt.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.vincent.acnt.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

public class DataHelper {

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

    public static String Comma(float num) {
        String strNum = String.valueOf(num);
        boolean negative = strNum.contains("-");
        if (negative) strNum = strNum.substring(1);
        String[] numPart = strNum.split("\\.");

        StringBuffer res = new StringBuffer(numPart[0]);
        int pos = 3;
        for (int i = 0; i < res.length(); i++) {
            if (pos + i >= res.length())
                break;

            res.insert(res.length() - pos - i, ",");
            pos += 3;
        }

        if (numPart.length == 2 && !numPart[1].equals("0"))
            res.append(".").append(numPart[1]);

        if (negative)
            res.insert(0, "-");

        return res.toString();
    }

    public static int binarySearchNumber(ArrayList<Integer> ary, Integer target) {
        int left = 0, right = ary.size() - 1;

        if (ary.isEmpty())
            return -1;

        if (ary.get(right).compareTo(target) == 0)
            return right;

        while (left <= right) {
            int middle = (right + left) / 2;

            if (ary.get(middle).compareTo(target) == 0)
                return middle;

            if (ary.get(middle) > target)
                right = middle - 1;
            else
                left = middle + 1;
        }

        return -1;
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

    public static int getSubjectColor(Subject subject) {
        MyApp app = MyApp.getInstance();
        switch (subject.getSubjectId().substring(0, 1)) {
            case "1":
                return app.getResource().getColor(R.color.type_asset);
            case "2":
                return app.getResource().getColor(R.color.type_liability);
            case "3":
                return app.getResource().getColor(R.color.type_capital);
            case "4":
                return app.getResource().getColor(R.color.type_revenue);
            case "5":
                return app.getResource().getColor(R.color.type_expense);
            default:
                return app.getResource().getColor(R.color.type_asset);
        }
    }

    public static int getWeekColor(Calendar calendar) {
        MyApp app =  MyApp.getInstance();
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                return app.getResource().getColor(R.color.week_sun);
            case 2:
                return app.getResource().getColor(R.color.week_mon);
            case 3:
                return app.getResource().getColor(R.color.week_tue);
            case 4:
                return app.getResource().getColor(R.color.week_wen);
            case 5:
                return app.getResource().getColor(R.color.week_thr);
            case 6:
                return app.getResource().getColor(R.color.week_fri);
            case 7:
                return app.getResource().getColor(R.color.week_sat);
            default:
                return app.getResource().getColor(R.color.week_sun);
        }
    }

    public static int getDateNumber(int year, int month, int day) {
        return year * 10000 + month * 100 + day;
    }
}
