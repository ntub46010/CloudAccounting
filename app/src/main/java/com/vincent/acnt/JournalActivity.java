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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.EntryCardAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.data.EntryContextMenuHandler;
import com.vincent.acnt.entity.Subject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

public class JournalActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "日記簿";
    private FirebaseFirestore db;

    private Spinner spnYear, spnMonth;
    private RecyclerView recyEntry;
    private ProgressBar prgBar;

    private List<Entry> entries;
    private EntryCardAdapter adapter;

    private int selectedYear, selectedMonth;

    private int queryFlag = -1;
    private boolean canQuery = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        context = this;
        db = MyApp.getInstance().getFirestore();

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
        FloatingActionButton fabCreateEntry = findViewById(R.id.fabCreateEntry);
        prgBar = findViewById(R.id.prgBar);

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
                    showEntry();
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
                    showEntry();
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

    private void showEntry() {
        if (!canQuery) {
            return;
        }

        canQuery = false;
        prgBar.setVisibility(View.VISIBLE);
        recyEntry.setVisibility(View.GONE);

        int endYear = selectedYear;
        int endMonth = selectedMonth + 1;
        if (endMonth > 12) {
            endYear++;
            endMonth = 1;
        }

        db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_ENTRIES)
                .orderBy(Constant.PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(Constant.PRO_MEMO, Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo(Constant.PRO_DATE, Utility.getDateNumber(selectedYear, selectedMonth, 1))
                .whereLessThan(Constant.PRO_DATE, Utility.getDateNumber(endYear, endMonth, 1))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        entries = new ArrayList<>();
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        Entry entry;

                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entry = documentSnapshots.get(i).toObject(Entry.class);
                            entry.defineDocumentId(documentSnapshots.get(i).getId());

                            //將分錄中的科目補上名稱
                            for (Subject subject : entry.getSubjects()) {
                                subject.setName(MyApp.mapSubjectById.get(subject.getId()).getName());
                            }

                            entries.add(entry);
                        }

                        adapter = new EntryCardAdapter(context, entries);
                        recyEntry.setAdapter(adapter);

                        if (entries.isEmpty()) {
                            Toast.makeText(context, "該月沒有紀錄", Toast.LENGTH_SHORT).show();
                        }

                        prgBar.setVisibility(View.GONE);
                        recyEntry.setVisibility(View.VISIBLE);
                        canQuery = true;
                    }
                });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        EntryContextMenuHandler handler = new EntryContextMenuHandler(context, adapter.getItem(adapter.longClickPosition), db);
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
