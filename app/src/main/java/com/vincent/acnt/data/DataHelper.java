package com.vincent.acnt.data;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

import com.vincent.acnt.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class DataHelper {

    public static AlertDialog.Builder getPlainDialog(Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("確定", null);
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

        //int first = 0, last = ary.size();
		/*
		 	假設first、mid, last為最後三個索引(5、6、7)，但未找到
			則first = mid = 6，得mid = (6 + 7) / 2，捨去小數點仍為6，造成無限尋找
			因此last應預設為元素個數，屆時mid = (6 + 8) / 2 = 7，可找到最後一個元素
		*/
    /*
        int mid = (last + first) / 2;
        Integer integer;

        while (first < last) {
            integer = ary.get(mid);
            if (integer.compareTo(target) == 0)
                return mid;
            else if (integer.compareTo(target) > 0)
                last = mid;
            else
                first = mid;

            mid = (last + first) / 2;
        }
        return -1;
        */
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

    public static int getSubjectColor(Context context, Subject subject) {
        MyApp app = (MyApp) context.getApplicationContext();
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
}
