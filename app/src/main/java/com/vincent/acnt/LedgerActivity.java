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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.LedgerListAdapter;
import com.vincent.acnt.data.Entry;
import com.vincent.acnt.data.LedgerRecord;
import com.vincent.acnt.data.MyApp;
import com.vincent.acnt.data.Subject;

import java.util.ArrayList;
import java.util.Calendar;

import static com.vincent.acnt.data.Utility.getDateNumber;
import static com.vincent.acnt.data.Utility.getPlainDialog;
import static com.vincent.acnt.data.MyApp.KEY_BOOKS;
import static com.vincent.acnt.data.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.data.MyApp.KEY_ENTRY;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECT;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.data.MyApp.PRO_DATE;
import static com.vincent.acnt.data.MyApp.PRO_MEMO;
import static com.vincent.acnt.data.MyApp.PRO_NAME;
import static com.vincent.acnt.data.MyApp.PRO_SUBJECT_ID;
import static com.vincent.acnt.data.MyApp.browsingBookDocumentId;

public class LedgerActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "分類帳";
    private FirebaseFirestore db;

    private LinearLayout layLedger;
    private Spinner spnYear, spnMonth;
    private AutoCompleteTextView actSubject;
    private ListView lstLedger;
    private ProgressBar prgBar;

    private int selectedYear, selectedMonth;
    private int queryFlag = -2;
    private boolean canQuery = true;

    private ArrayList<String> subjectNames;
    private ArrayList<Entry> entries;
    private ArrayList<LedgerRecord> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);
        context = this;
        db = ((MyApp) getApplication()).getFirestore();

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

        layLedger = findViewById(R.id.layLedger);
        spnYear = findViewById(R.id.spnYear);
        spnMonth = findViewById(R.id.spnMonth);
        actSubject = findViewById(R.id.actSubject);
        Button btnShow = findViewById(R.id.btnShow);
        lstLedger = findViewById(R.id.lstRecord);
        prgBar = findViewById(R.id.prgBar);

        if (getIntent().getExtras() != null) {
            actSubject.setText(getIntent().getExtras().getString(KEY_SUBJECT));
            queryFlag++;
        }

        setupSpinner();

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //點擊按鈕後收起鍵盤
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(layLedger.getWindowToken(), 0);

                if (isSubjectExist(actSubject.getText().toString()))
                    showLedger();
            }
        });

        lstLedger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = entries.get(position);
                if (entry != null) {
                    Intent it = new Intent(context, EntryDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(KEY_ENTRY, entry);
                    it.putExtras(bundle);
                    startActivity(it);
                }
            }
        });

        layLedger.setVisibility(View.INVISIBLE);
        prgBar.setVisibility(View.VISIBLE);
        querySubject();
    }

    private void querySubject() {
        layLedger.setVisibility(View.INVISIBLE);
        prgBar.setVisibility(View.VISIBLE);
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_SUBJECTS)
                .orderBy(PRO_SUBJECT_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            subjectNames = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : querySnapshot)
                                subjectNames.add(documentSnapshot.toObject(Subject.class).getName());

                            actSubject.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, subjectNames));
                            prgBar.setVisibility(View.GONE);
                            layLedger.setVisibility(View.VISIBLE);
                        }else
                            Toast.makeText(context, "查詢會計科目失敗", Toast.LENGTH_SHORT).show();
                    }
                });
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
                    showLedger();
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
                    showLedger();
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

    private boolean isSubjectExist(String subjectName) {
        for (String name : subjectNames) {
            if (name.equals(subjectName))
                return true;
        }
        getPlainDialog(context, activityTitle, "會計科目不存在").show();
        return false;
    }

    private void showLedger() {
        final String subjectName = actSubject.getText().toString();
        if (!canQuery || subjectName.equals(""))
            return;

        canQuery = false;
        prgBar.setVisibility(View.VISIBLE);
        lstLedger.setVisibility(View.GONE);

        int endYear = selectedYear;
        int endMonth = selectedMonth + 1;
        if (endMonth > 12) {
            endYear++;
            endMonth = 1;
        }

        entries = new ArrayList<>();
        records = new ArrayList<>();
        queryMonthlyRecord(subjectName, getDateNumber(selectedYear, selectedMonth, 1), getDateNumber(endYear, endMonth, 1));
    }

    private void queryMonthlyRecord(final String subjectName, final int selectedDate, int endDate) {
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_ENTRIES)
                .orderBy(PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(PRO_MEMO, Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo(PRO_DATE, selectedDate)
                .whereLessThan(PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "查詢本月明細失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        QuerySnapshot querySnapshot = task.getResult();
                        Entry entry;
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            //儲存分錄，供點擊清單後能顯示詳情，越前面越新
                            entry = documentSnapshot.toObject(Entry.class);
                            entries.add(entry);

                            //儲存明細，越前面是越新的紀錄
                            for (Subject subject : entry.getSubjects()) {
                                if (subject.getName().equals(subjectName)) {
                                    records.add(new LedgerRecord(
                                            entry.getDate(),
                                            entry.getMemo(),
                                            subject.getCredit(),
                                            subject.getDebit()
                                    ));
                                }
                           }
                        }

                        if (records.isEmpty())
                            Toast.makeText(context, "本月沒有紀錄", Toast.LENGTH_SHORT).show();

                        //若選擇1月，則略過查詢歷史總額，否則繼續查詢
                        if (String.valueOf(selectedDate).substring(4).equals("0101"))
                            queryOriginBalance(subjectName, selectedDate);
                        else
                            queryHistoryRecord(subjectName, selectedDate);
                    }
                });
    }

    private void queryHistoryRecord(final String subjectName, final int selectedDate) {
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_ENTRIES)
                .whereLessThan(PRO_DATE, selectedDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "查詢歷史明細失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        QuerySnapshot querySnapshot = task.getResult();
                        Entry entry;
                        int totalCredit = 0, totalDebit = 0;

                        //從歷史分錄中計算累積借貸總額
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            entry = documentSnapshot.toObject(Entry.class);

                            for (Subject subject : entry.getSubjects()) {
                                if (subject.getName().equals(subjectName)) {
                                    totalCredit += subject.getCredit();
                                    totalDebit += subject.getDebit();
                                }
                            }
                        }

                        records.add(new LedgerRecord(
                                selectedDate,
                                "(歷史累積紀錄)",
                                totalCredit,
                                totalDebit
                        ));
                        entries.add(null);

                        //繼續查詢初始餘額
                        queryOriginBalance(subjectName, selectedDate);
                    }
                });
    }

    private void queryOriginBalance(final String subjectName, final int selectedDate) {
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_SUBJECTS)
                .whereEqualTo(PRO_NAME, subjectName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshots = task.getResult();
                            Subject subject = querySnapshots.getDocuments().get(0).toObject(Subject.class);

                            records.add(new LedgerRecord(
                                    (selectedDate / 10000) * 10000 + 101, //修正為當年1/1，如20180101
                                    "(初始餘額)",
                                    subject.getCredit(),
                                    subject.getDebit()
                            ));
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
                        }else
                            Toast.makeText(context, "查詢初始餘額失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
