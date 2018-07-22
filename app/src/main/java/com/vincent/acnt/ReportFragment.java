package com.vincent.acnt;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.acnt.adapter.ReportAdapter;
import com.vincent.acnt.data.ReportItem;

import java.util.ArrayList;

import static com.vincent.acnt.data.MyApp.KEY_REPORT_ITEMS;

public class ReportFragment extends Fragment {
    protected Context context;

    private TextView txtBalance;
    private ListView lstReport;

    private ArrayList<ReportItem> reportItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reportItems = (ArrayList<ReportItem>) getArguments().getSerializable(KEY_REPORT_ITEMS);

        //txtBalance = getView().findViewById(R.id.txtBalance);
        lstReport = getView().findViewById(R.id.lstReport);


        lstReport.setAdapter(new ReportAdapter(context, reportItems));
        lstReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, reportItems.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
