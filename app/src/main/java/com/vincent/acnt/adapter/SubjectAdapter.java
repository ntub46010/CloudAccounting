package com.vincent.acnt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vincent.acnt.R;
import com.vincent.acnt.data.Subject;

import java.util.ArrayList;

import static com.vincent.acnt.data.DataHelper.Comma;

public class SubjectAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<Subject> subjects;

    public SubjectAdapter (Context context, ArrayList<Subject> subjects) {
        layoutInflater = LayoutInflater.from(context);
        this.subjects = subjects;
    }

    @Override
    public int getCount() {
        return subjects.size();
    }

    @Override
    public Object getItem(int position) {
        return subjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.lst_subject, parent,false);

        TextView txtId = view.findViewById(R.id.txtSubjectId);
        TextView txtName = view.findViewById(R.id.txtSubjectName);
        TextView txtCredit = view.findViewById(R.id.txtCredit);
        TextView txtDebit = view.findViewById(R.id.txtDebit);

        Subject subject = subjects.get(position);
        txtId.setText(String.valueOf(subject.getSubjectId()));
        txtName.setText(subject.getName());
        txtCredit.setText("借：" + Comma(subject.getCredit()));
        txtDebit.setText("貸：" + Comma(subject.getDebit()));

        switch (subject.getSubjectId().substring(0, 1)) {
            case "1":
                txtId.setTextColor(Color.parseColor("#F56800"));
                break;
            case "2":
                txtId.setTextColor(Color.parseColor("#0D8F06"));
                break;
            case "3":
                txtId.setTextColor(Color.parseColor("#F02A2A"));//D53756
                break;
            case "4":
                txtId.setTextColor(Color.parseColor("#5462C7"));
                break;
            case "5":
                txtId.setTextColor(Color.parseColor("#883DAE"));
                break;
        }

        return view;
    }
}
