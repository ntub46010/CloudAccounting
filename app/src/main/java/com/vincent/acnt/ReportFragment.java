package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.vincent.acnt.adapter.ReportAdapter;
import com.vincent.acnt.data.ReportItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.vincent.acnt.data.MyApp.CODE_ASSET;
import static com.vincent.acnt.data.MyApp.CODE_CAPITAL;
import static com.vincent.acnt.data.MyApp.CODE_EXPANSE;
import static com.vincent.acnt.data.MyApp.CODE_LIABILITY;
import static com.vincent.acnt.data.MyApp.CODE_REVENUE;
import static com.vincent.acnt.data.MyApp.KEY_REPORT_ITEMS;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECT;

public class ReportFragment extends Fragment {
    protected Context context;
    private int layout;

    private TextView txtBalance;
    private ListView lstReport;

    private String type;
    private ArrayList<ReportItem> reportItems;

    public ReportFragment() {

    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reportItems = (ArrayList<ReportItem>) getArguments().getSerializable(KEY_REPORT_ITEMS);

        txtBalance = getView().findViewById(R.id.txtBalance);
        lstReport = getView().findViewById(R.id.lstReport);

        showBalance();

        lstReport.setAdapter(new ReportAdapter(context, reportItems));
        lstReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(context, LedgerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_SUBJECT, reportItems.get(position).getName());
                it.putExtras(bundle);
                context.startActivity(it);
            }
        });
    }

    private void showBalance() {
        int balance = 0;
        for (ReportItem item : reportItems)
            balance += item.getBalance();

        String text = "";
        switch (type) {
            case CODE_ASSET:
                text = "資產餘額：";
                break;
            case CODE_LIABILITY:
                text = "負債餘額：";
                break;
            case CODE_CAPITAL:
                text = "權益餘額：";
                break;
            case CODE_REVENUE:
                text = "收益餘額：";
                break;
            case CODE_EXPANSE:
                text = "費損餘額：";
                break;
        }

        txtBalance.setText(text +  NumberFormat.getNumberInstance(Locale.US).format(balance));
    }

    public void setType(String type) {
        this.type = type;
    }
}
