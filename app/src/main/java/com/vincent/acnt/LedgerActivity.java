package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.LedgerListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.LedgerRecord;
import com.vincent.acnt.entity.Subject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LedgerActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "分類帳";

    private LinearLayout layLedgerContainer;
    private Spinner spnYear, spnMonth;
    private AutoCompleteTextView actSubjectName;
    private ListView lstLedger;
    private ProgressBar prgBar;

    private int selectedYear, selectedMonth;
    private int queryFlag = -2;
    private boolean canQuery = true;

    private List<Entry> entries;
    private List<LedgerRecord> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);
        context = this;

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

        layLedgerContainer = findViewById(R.id.layLedgerContainer);
        spnYear = findViewById(R.id.spnYear);
        spnMonth = findViewById(R.id.spnMonth);
        actSubjectName = findViewById(R.id.actSubjectName);
        Button btnShow = findViewById(R.id.btnShow);
        lstLedger = findViewById(R.id.lstRecord);
        prgBar = findViewById(R.id.prgBar);

        if (getIntent().getExtras() != null) {
            actSubjectName.setText(getIntent().getExtras().getString(Constant.KEY_SUBJECT));
            queryFlag++;
        }

        setupSpinner();

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //點擊按鈕後收起鍵盤
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layLedgerContainer.getWindowToken(), 0);

                if (isSubjectExist(actSubjectName.getText().toString())) {
                    showLedger();
                }
            }
        });

        lstLedger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = entries.get(position);
                if (entry != null) {
                    Intent it = new Intent(context, EntryDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.KEY_ENTRY, entry);
                    it.putExtras(bundle);
                    startActivity(it);
                }
            }
        });

        loadSubjects();
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
                    showLedger();
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
                    showLedger();
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

    private void loadSubjects() {
        ArrayAdapter<String> adpSubjectName = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());

        for (int i = 0, len = MyApp.mapSubjectById.size(); i < len; i++) {
            adpSubjectName.add(MyApp.mapSubjectById.valueAt(i).getName());
        }

        actSubjectName.setAdapter(adpSubjectName);
        prgBar.setVisibility(View.GONE);
    }

    private boolean isSubjectExist(String subjectName) {
        if (!MyApp.mapSubjectByName.containsKey(subjectName)) {
            Utility.getPlainDialog(context, activityTitle, "會計科目不存在").show();
            return false;
        }

        return true;
    }

    private void showLedger() {
        String subjectName = actSubjectName.getText().toString();
        if (!canQuery || subjectName.equals("")) {
            return;
        }

        canQuery = false;
        prgBar.setVisibility(View.VISIBLE);
        lstLedger.setVisibility(View.INVISIBLE);

        int endYear = selectedYear;
        int endMonth = selectedMonth + 1;
        if (endMonth > 12) {
            endYear++;
            endMonth = 1;
        }

        entries = new ArrayList<>(256);
        records = new ArrayList<>(64);
        queryMonthlyRecord(
                MyApp.mapSubjectByName.get(subjectName).getId(),
                Utility.getDateNumber(selectedYear, selectedMonth, 1),
                Utility.getDateNumber(endYear, endMonth, 1)
        );
    }

    private void queryMonthlyRecord(final long subjectId, final int selectedDate, int endDate) {
        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_ENTRIES)
                .orderBy(Constant.PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(Constant.PRO_MEMO, Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo(Constant.PRO_DATE, selectedDate)
                .whereLessThan(Constant.PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "查詢本月明細失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                        Entry entry;
                        LedgerRecord record;

                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entry = documentSnapshots.get(i).toObject(Entry.class);
                            List<Subject> subjects = entry.getSubjects();

                            for (int j = 0, len2 = subjects.size(); j < len2; j++) {
                                Subject subject = subjects.get(j);

                                if (subject.getId() == subjectId) {
                                    //儲存分錄，供點擊清單後能顯示詳情，越前面越新
                                    entries.add(entry);

                                    //儲存明細，越前面是越新的紀錄
                                    record = new LedgerRecord();
                                    record.setDate(entry.getDate());
                                    record.setMemo(entry.getMemo());
                                    record.setCredit(subject.getCredit());
                                    record.setDebit(subject.getDebit());

                                    records.add(record);
                                }
                            }
                        }

                        if (records.isEmpty()) {
                            Toast.makeText(context, "本月沒有紀錄", Toast.LENGTH_SHORT).show();
                        }

                        //若選擇1月，則略過查詢歷史總額，否則繼續查詢
                        if (String.valueOf(selectedDate).substring(4).equals("0101")) {
                            queryOriginBalance(subjectId, selectedDate);
                        } else {
                            queryHistoryRecord(subjectId, selectedDate);
                        }
                    }
                });
    }

    private void queryHistoryRecord(final long subjectId, final int selectedDate) {
        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_ENTRIES)
                .whereLessThan(Constant.PRO_DATE, selectedDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "查詢歷史明細失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Entry entry;
                        int totalCredit = 0, totalDebit = 0;

                        //從歷史分錄中計算累積借貸總額
                        List<DocumentSnapshot > documentSnapshots = task.getResult().getDocuments();
                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entry = documentSnapshots.get(i).toObject(Entry.class);

                            for (Subject subject : entry.getSubjects()) {
                                if (subject.getId() == subjectId) {
                                    totalCredit += subject.getCredit();
                                    totalDebit += subject.getDebit();
                                }
                            }
                        }

                        LedgerRecord record = new LedgerRecord();
                        record.setDate(selectedDate);
                        record.setMemo("(歷史累積紀錄)");
                        record.setCredit(totalCredit);
                        record.setDebit(totalDebit);

                        records.add(record);
                        entries.add(null);

                        //繼續查詢初始餘額
                        queryOriginBalance(subjectId, selectedDate);
                    }
                });
    }

    private void queryOriginBalance(final long subjectId, final int selectedDate) {
        Subject subject = MyApp.mapSubjectById.get(subjectId);

        LedgerRecord record = new LedgerRecord();
        record.setDate((selectedDate / 10000) * 10000 + 101); //修正為當年1/1，如20180101
        record.setMemo("(初始餘額)");
        record.setCredit(subject.getCredit());
        record.setDebit(subject.getDebit());

        records.add(record);
        entries.add(null);

        //計算清單項目所要顯示的餘額
        LedgerRecord r = records.get(records.size() - 1);
        r.setBalance(r.getCredit() - r.getDebit());
        for (int i = records.size() - 2; i >= 0; i--) {
            r = records.get(i);
            r.setBalance(records.get(i + 1).getBalance() + r.getCredit() - r.getDebit());
        }

        //顯示清單
        lstLedger.setAdapter(new LedgerListAdapter(context, records));
        prgBar.setVisibility(View.GONE);
        lstLedger.setVisibility(View.VISIBLE);
        canQuery = true;
    }

}
