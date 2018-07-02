package com.vincent.acnt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TransactionAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<Transaction> trans;

    public TransactionAdapter (Context context, ArrayList<Transaction> trans) {
        layoutInflater = LayoutInflater.from(context);
        this.trans = trans;
    }

    @Override
    public int getCount() {
        return trans.size();
    }

    @Override
    public Object getItem(int position) {
        return trans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.lst_tran, parent,false);

        TextView txtDate = view.findViewById(R.id.txtDate);
        TextView txtPs = view.findViewById(R.id.txtPs);
        TextView txtSubject = view.findViewById(R.id.txtSubject);

        Transaction tran = trans.get(position);
        txtDate.setText(tran.getDate());
        txtPs.setText(tran.getPs());
        txtSubject.setText(tran.showDetailText());

        return view;
    }
}