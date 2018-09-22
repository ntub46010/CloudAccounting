package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;
import com.vincent.acnt.accessor.EntryAccessor;
import com.vincent.acnt.accessor.RetrieveEntitiesListener;
import com.vincent.acnt.adapter.EntryCardAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Entity;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.data.EntryContextMenuHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JournalActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "日記簿";

    private Spinner spnYear, spnMonth;
    private RecyclerView recyEntry;
    private FloatingActionButton fabCreateEntry;
    private ProgressBar prgBar;
    private RelativeLayout layHint;

    private EntryCardAdapter adapter;

    private int selectedYear, selectedMonth;

    private int queryFlag = -2;

    private ListenerRegistration regEntry;
    private EntryAccessor accessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        context = this;
        accessor = new EntryAccessor(MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .collection(Constant.KEY_ENTRIES));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(activityTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        spnYear = findViewById(R.id.spnYear);
        spnMonth = findViewById(R.id.spnMonth);
        recyEntry = findViewById(R.id.recyEntry);
        fabCreateEntry = findViewById(R.id.fabCreateEntry);
        prgBar = findViewById(R.id.prgBar);
        layHint = findViewById(R.id.layContentHint);

        recyEntry.setHasFixedSize(true);
        recyEntry.setLayoutManager(new LinearLayoutManager(context));

        fabCreateEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(context, EntryEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.KEY_MODE, Constant.MODE_CREATE);
                it.putExtras(bundle);
                startActivity(it);
            }
        });

        setupSpinner();

        adapter = new EntryCardAdapter(context, MyApp.thisMonthEntries);
        recyEntry.setAdapter(adapter);

        prgBar.setVisibility(View.GONE);
        fabCreateEntry.setVisibility(View.VISIBLE);
    }

    private void setupSpinner() {
        //取得日期資料
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);

        //建立年份清單
        List<Integer> years = new ArrayList<>(), months = new ArrayList<>();
        for (int i = 2017; i <= year; i++) {
            years.add(i);
        }
        spnYear.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, years));

        for (int i = 1; i<= 12; i++) {
            months.add(i);
        }
        spnMonth.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, months));

        //定義清單點擊事件
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(parent.getSelectedItem().toString());

                queryFlag++;
                if (queryFlag > 0) {
                    loadEntries();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position + 1;

                queryFlag++;
                if (queryFlag > 0) {
                    loadEntries();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //預設清單選項
        for (int i = 0; i < years.size(); i++) {
            if (years.get(i) == year) {
                spnYear.setSelection(i);
                break;
            }
        }
        spnMonth.setSelection(month);
    }

    private void loadEntries() {
        prgBar.setVisibility(View.VISIBLE);
        recyEntry.setVisibility(View.INVISIBLE);
        fabCreateEntry.setVisibility(View.INVISIBLE);

        regEntry = accessor.observeEntriesByMonth(selectedYear, selectedMonth, new RetrieveEntitiesListener() {
            @Override
            public void onRetrieve(List<? extends Entity> entities) {
                if (queryFlag < 1) {
                    return;
                }

                List<Entry> entries = (List<Entry>) entities;

                if (entries.isEmpty()) {
                    TextView txtHint = findViewById(R.id.txtHint);
                    txtHint.setText("該月沒有紀錄，可點擊右下方按鈕進行記帳");
                    layHint.setVisibility(View.VISIBLE);
                } else {
                    layHint.setVisibility(View.GONE);
                }

                adapter.setEntries(entries);

                prgBar.setVisibility(View.GONE);
                recyEntry.setVisibility(View.VISIBLE);
                fabCreateEntry.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    @Override
    public void onDestroy() {
        if (regEntry != null) {
            regEntry.remove();
        }
        super.onDestroy();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        EntryContextMenuHandler handler = new EntryContextMenuHandler(context, adapter.getItem(adapter.longClickPosition));
        switch (item.getItemId()) {
            case Constant.MODE_UPDATE:
                handler.updateEntry();
                break;

            case Constant.MODE_DELETE:
                handler.deleteEntry(activityTitle, prgBar, recyEntry);
                break;
        }

        return super.onContextItemSelected(item);
    }

}
