package com.vincent.acnt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.EntryCardAdapter;
import com.vincent.acnt.data.Entry;
import com.vincent.acnt.data.MyApp;

import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.Nullable;

import static com.vincent.acnt.data.DataHelper.getDateNumber;
import static com.vincent.acnt.data.DataHelper.getPlainDialog;
import static com.vincent.acnt.data.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.data.MyApp.KEY_USERS;
import static com.vincent.acnt.data.MyApp.PRO_DATE;
import static com.vincent.acnt.data.MyApp.PRO_DOCUMENT_ID;
import static com.vincent.acnt.data.MyApp.PRO_MEMO;

public class JournalActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "日記簿";
    private FirebaseFirestore db;

    private Spinner spnYear, spnMonth;
    private RecyclerView recyEntry;
    private ProgressBar prgBar;

    private ArrayList<Entry> entries;
    private EntryCardAdapter adapter;

    private int selectedYear, selectedMonth;
    private final int mnuEditEntry = Menu.FIRST, mnuDelEntry = Menu.FIRST + 1;
    private Entry entry;

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
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        prgBar = findViewById(R.id.prgBar);

        setupSpinner();

        recyEntry.setHasFixedSize(true);
        recyEntry.setLayoutManager(new LinearLayoutManager(context));

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(context, EntryCreateActivity.class), 0);
            }
        });
    }

    private void showEntry() {
        if (!canQuery)
            return;

        canQuery = false;
        prgBar.setVisibility(View.VISIBLE);
        recyEntry.setVisibility(View.GONE);

        int endYear = selectedYear;
        int endMonth = selectedMonth + 1;
        if (endMonth > 12) {
            endYear++;
            endMonth = 1;
        }

        entries = new ArrayList<>();
        db.collection(KEY_USERS).document(MyApp.getInstance().getUser().gainDocumentId()).collection(KEY_ENTRIES)
                .orderBy(PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(PRO_MEMO, Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo(PRO_DATE, getDateNumber(selectedYear, selectedMonth, 1))
                .whereLessThan(PRO_DATE, getDateNumber(endYear, endMonth, 1))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Entry entry;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            entry = documentSnapshot.toObject(Entry.class);
                            entry.giveDocumentId(documentSnapshot.getId());
                            entries.add(entry);
                        }

                        adapter = new EntryCardAdapter(context, entries);
                        recyEntry.setAdapter(adapter);

                        if (entries.isEmpty())
                            Toast.makeText(context, "沒有紀錄", Toast.LENGTH_SHORT).show();

                        prgBar.setVisibility(View.GONE);
                        recyEntry.setVisibility(View.VISIBLE);
                        canQuery = true;
                    }
                });
    }

    private void deleteEntry() {
        getPlainDialog(context, activityTitle, "確定要刪除分錄「" + entry.getMemo() + "」嗎？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prgBar.setVisibility(View.VISIBLE);
                        recyEntry.setVisibility(View.GONE);

                        db.collection(KEY_USERS).document(MyApp.getInstance().getUser().gainDocumentId()).collection(KEY_ENTRIES).document(entry.gainDocumentId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "分錄刪除成功", Toast.LENGTH_SHORT).show();
                                            showEntry();
                                        }else
                                            Toast.makeText(context, "分錄刪除失敗", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }

    private void setupSpinner() {
        //取得日期資料
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);

        //建立年份清單
        ArrayList<Integer> years = new ArrayList<>(), months = new ArrayList<>();
        for (int i = 2017; i <= year; i++)
            years.add(i);
        spnYear.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, years));

        for (int i = 1; i<= 12; i++)
            months.add(i);
        spnMonth.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, months));

        //定義清單點擊事件
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(parent.getSelectedItem().toString());

                queryFlag++;
                if (queryFlag > 0)
                    showEntry();
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
                if (queryFlag > 0)
                    showEntry();
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        entry = adapter.getItem(adapter.longClickPosition);
        switch (item.getItemId()) {
            case mnuEditEntry:
                //調整日期格式
                StringBuffer date = new StringBuffer(String.valueOf(entry.getDate()));
                date.insert(4, "/");
                date.insert(7, "/");

                Intent it = new Intent(context, EntryUpdateActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString(PRO_DATE, date.toString());
                bundle.putString(PRO_MEMO, entry.getMemo());
                bundle.putString(PRO_DOCUMENT_ID, entry.gainDocumentId());
                it.putExtras(bundle);
                it.putExtra(KEY_SUBJECTS, entry.getSubjects());
                startActivityForResult(it, 0);
                break;

            case mnuDelEntry:
                deleteEntry();
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 0:
                break;
            case 1:
                showEntry();
                break;
            default:
                Toast.makeText(context, "不明錯誤", Toast.LENGTH_SHORT).show();
        }
    }

}
