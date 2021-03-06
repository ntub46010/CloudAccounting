package com.vincent.acnt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vincent.acnt.R;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.LedgerRecord;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LedgerListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<LedgerRecord> records;

    public LedgerListAdapter(Context context, List<LedgerRecord> records) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.records = records;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.lst_ledger_record, parent, false);
        }

        LedgerRecord record = records.get(position);
        String date = String.valueOf(record.getDate());

        TextView txtDate = view.findViewById(R.id.txtDate);
        TextView txtMemo = view.findViewById(R.id.txtMemo);
        TextView txtCredit = view.findViewById(R.id.txtCredit);
        TextView txtDebit = view.findViewById(R.id.txtDebit);
        TextView txtBalance = view.findViewById(R.id.txtBalance);

        txtDate.setText(String.format("%s. %s", Utility.getEngMonth(date.substring(4, 6)), date.substring(6, 8)));
        txtMemo.setText(record.getMemo());
        txtCredit.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getCredit()));
        txtDebit.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getDebit()));
        txtBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getBalance()));

        return view;
    }

    public void setRecords(List<LedgerRecord> records) {
        this.records.clear();
        this.records = records;
        notifyDataSetChanged();
    }
}
