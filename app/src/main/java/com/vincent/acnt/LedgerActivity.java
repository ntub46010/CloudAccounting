package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.acnt.accessor.EntryAccessor;
import com.vincent.acnt.adapter.LedgerListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.LedgerRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LedgerActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "分類帳";

    private Spinner spnYear, spnMonth;
    private AutoCompleteTextView actSubjectName;
    private ListView lstLedger;
    private ProgressBar prgBar;
    private RelativeLayout layHint;

    private int selectedYear, selectedMonth;
    private int queryFlag = -2;
    private boolean canQuery = true;

    private List<Entry> mEntries;
    private LedgerListAdapter adapter;

    private EntryAccessor accessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);
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
        actSubjectName = findViewById(R.id.actSubjectName);
        Button btnShow = findViewById(R.id.btnShow);
        lstLedger = findViewById(R.id.lstRecord);
        prgBar = findViewById(R.id.prgBar);
        layHint = findViewById(R.id.layContentHint);

        if (getIntent().getExtras() != null) {
            actSubjectName.setText(getIntent().getExtras().getString(Constant.KEY_SUBJECT));
            queryFlag++;
        }

        setupSpinner();

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //點擊按鈕後收起鍵盤
                //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(layLedgerContainer.getWindowToken(), 0);

                if (isSubjectExist(actSubjectName.getText().toString())) {
                    showLedger();
                }
            }
        });

        lstLedger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = mEntries.get(position);
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

        accessor.loadLedgerItems(MyApp.mapSubjectByName.get(subjectName).getId(),
                        Utility.getDateNumber(selectedYear, selectedMonth, 31),
                        new EntryAccessor.RetrieveLedgerRecordListener() {
                            @Override
                            public void onRetrieve(List<LedgerRecord> records, List<Entry> entries) {
                                if (records.isEmpty()) {
                                    TextView txtHint = findViewById(R.id.txtHint);
                                    txtHint.setText("該月沒有紀錄");
                                    layHint.setVisibility(View.VISIBLE);
                                } else {
                                    layHint.setVisibility(View.GONE);
                                }

                                //顯示清單
                                if (adapter == null) {
                                    adapter = new LedgerListAdapter(context, records);
                                    lstLedger.setAdapter(adapter);
                                } else {
                                    adapter.setRecords(records);
                                }

                                mEntries = entries;

                                prgBar.setVisibility(View.GONE);
                                lstLedger.setVisibility(View.VISIBLE);
                                canQuery = true;
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(context, "查詢明細失敗", Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}
