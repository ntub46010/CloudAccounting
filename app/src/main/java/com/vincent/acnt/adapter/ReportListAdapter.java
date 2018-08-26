package com.vincent.acnt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vincent.acnt.R;
import com.vincent.acnt.entity.ReportItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReportListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<ReportItem> items;

    public ReportListAdapter(Context context, List<ReportItem> items) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.lst_report_item, parent,false);

        TextView txtId = view.findViewById(R.id.txtSubjectId);
        TextView txtName = view.findViewById(R.id.txtSubjectName);
        TextView txtCredit = view.findViewById(R.id.txtCredit);
        TextView txtDebit = view.findViewById(R.id.txtDebit);
        TextView txtBalance = view.findViewById(R.id.txtBalance);

        ReportItem item = items.get(position);
        txtId.setText(String.valueOf(item.getId()));
        txtName.setText(item.getName());
        txtCredit.setText("借：" + NumberFormat.getNumberInstance(Locale.US).format(item.getTotalCredit()));
        txtDebit.setText("貸：" + NumberFormat.getNumberInstance(Locale.US).format(item.getTotalDebit()));
        txtBalance.setText("餘：" + NumberFormat.getNumberInstance(Locale.US).format(item.getBalance()));

        //txtId.setTextColor(getSubjectColor(context, subject));


        return view;
    }
}
