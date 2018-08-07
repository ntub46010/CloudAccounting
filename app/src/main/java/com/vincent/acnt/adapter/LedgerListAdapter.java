package com.vincent.acnt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.acnt.R;
import com.vincent.acnt.data.LedgerRecord;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.vincent.acnt.data.DataHelper.Comma;
import static com.vincent.acnt.data.DataHelper.getEngMonth;

public class LedgerListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<LedgerRecord> records;

    public LedgerListAdapter(Context context, ArrayList<LedgerRecord> records) {
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
        if (view == null)
            view = layoutInflater.inflate(R.layout.lst_ledger_record, parent,false);

        LedgerRecord record = records.get(position);
        String date = String.valueOf(record.getDate());

        TextView txtDate = view.findViewById(R.id.txtDate);
        TextView txtMemo = view.findViewById(R.id.txtMemo);
        TextView txtCredit = view.findViewById(R.id.txtCredit);
        TextView txtDebit = view.findViewById(R.id.txtDebit);
        TextView txtBalance = view.findViewById(R.id.txtBalance);

        txtDate.setText(String.format("%s. %s", getEngMonth(date.substring(4, 6)), date.substring(6, 8)));
        txtMemo.setText(record.getMemo());
        txtCredit.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getCredit()));
        txtDebit.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getDebit()));
        txtBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getBalance()));

        return view;
    }
}
