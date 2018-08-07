package com.vincent.acnt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vincent.acnt.R;

public class FeatureGridAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private int[] icons;
    private String[] titles;

    public FeatureGridAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);

        icons = new int[] {
                R.drawable.fun_subject,
                R.drawable.fun_journal,
                R.drawable.fun_ledger,
                R.drawable.fun_report,
                R.drawable.fun_profile,
                R.drawable.fun_logout
        };

        titles = new String[] {
                "會計科目",
                "日記簿",
                "分類帳",
                "財務報告",
                "個人檔案",
                "登出"
        };
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.grd_feature, parent, false);

        ImageView imgFunc = view.findViewById(R.id.imgFunc);
        TextView txtFunc = view.findViewById(R.id.txtFunc);
        imgFunc.setImageResource(icons[position]);
        txtFunc.setText(titles[position]);

        return view;
    }
}
