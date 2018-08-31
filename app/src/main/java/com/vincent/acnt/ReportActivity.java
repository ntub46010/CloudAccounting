package com.vincent.acnt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.ReportPagerAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.ReportItem;
import com.vincent.acnt.entity.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private String activityTitle = "財務報告";

    private ViewPager vpgHome;
    private FloatingActionButton fabDate;

    private List<Entry> entries;
    private Map<String, ReportItem> mapReportItem = new TreeMap<>();
    private ReportFragment[] reportFragments = new ReportFragment[5];

    private Dialog dlgWaiting;

    private interface TaskListener { void onFinish();}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        context = this;
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(String.format("財務報告  %s/%s/%s",
                date.substring(0, 4),
                date.substring(4, 6),
                date.substring(6, 8)
        ));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        vpgHome = findViewById(R.id.vpgReport);
        fabDate = findViewById(R.id.fabSelectDate);
        TabLayout tabHome = findViewById(R.id.tabs);

        vpgHome.setOffscreenPageLimit(15);
        vpgHome.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabHome.setupWithViewPager(vpgHome);

        fabDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareRefreshReport();
            }
        });

        dlgWaiting = Utility.getWaitingDialog(context);

        collectReportItems(Integer.parseInt(date), null);
    }

    private void collectReportItems(final int endDate, final TaskListener listener) {
        dlgWaiting.show();
        fabDate.setVisibility(View.GONE);

        //儲存各個科目的初始餘額
        Subject subject;
        mapReportItem.clear();
        ReportItem item;

        for (int i = 0, len = MyApp.mapSubjectById.size(); i < len; i++) {
            subject = MyApp.mapSubjectById.valueAt(i);
            item = new ReportItem();

            item.setId(subject.getNo());
            item.setName(subject.getName());
            item.addCredit(subject.getCredit());
            item.addDebit(subject.getDebit());

            mapReportItem.put(subject.getNo(), item);
        }

        searchInEntry(endDate, listener);
    }

    private void searchInEntry(final int endDate, final TaskListener listener) {
        entries = new ArrayList<>(256);

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_ENTRIES)
                .orderBy(Constant.PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(Constant.PRO_MEMO, Query.Direction.ASCENDING)
                .whereLessThanOrEqualTo(Constant.PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();

                            //儲存分錄
                            for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                                entries.add(documentSnapshots.get(i).toObject(Entry.class));
                            }

                            //從各個分錄中將科目金額逐一儲存
                            ReportItem item;
                            for (int i = 0, len = entries.size(); i < len; i++) {
                                List<Subject> subjects = entries.get(i).getSubjects();

                                for (int j = 0, len2 = subjects.size(); j < len2; j++) {
                                    Subject subject = subjects.get(j);
                                    Subject s = MyApp.mapSubjectById.get(subject.getId());

                                    if (mapReportItem.containsKey(s.getNo())) {
                                        //科目已存在，取出累積金額，再放置回去；尚未實驗可否簡化為不需要宣告item物件
                                        item = mapReportItem.get(s.getNo());

                                        item.addCredit(subject.getCredit());
                                        item.addDebit(subject.getDebit());
                                    } else {
                                        item = new ReportItem();

                                        item.setId(s.getNo());
                                        item.setName(s.getName());
                                        item.addCredit(subject.getCredit());
                                        item.addDebit(subject.getDebit());
                                    }

                                    mapReportItem.put(s.getNo(), item);
                                }
                            } //各個科目金額計算完畢

                            if (listener == null) {
                                setupFragment(); //加入頁面
                            } else {
                                listener.onFinish();
                            }

                            String date = String.valueOf(endDate);
                            toolbar.setTitle(String.format("財務報告  %s/%s/%s",
                                    date.substring(0, 4),
                                    date.substring(4, 6),
                                    date.substring(6, 8)
                            ));

                            fabDate.setVisibility(View.VISIBLE);
                            dlgWaiting.dismiss();
                        } else {
                            Toast.makeText(context, "查詢分錄失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupFragment() {
        ReportPagerAdapter adapter = new ReportPagerAdapter(getSupportFragmentManager());

        for (int i = 0; i < 5; i++) {
            reportFragments[i] = new ReportFragment();
            reportFragments[i].setReportItems(getReportItems(Constant.CODE_TYPE[i]));
            reportFragments[i].setType(Constant.CODE_TYPE[i]);
        }

        adapter.addFragment(reportFragments[0], "資產");
        adapter.addFragment(reportFragments[1], "負債");
        adapter.addFragment(reportFragments[2], "權益");
        adapter.addFragment(reportFragments[3], "收入");
        adapter.addFragment(reportFragments[4], "支出");

        vpgHome.setAdapter(adapter);
    }

    private List<ReportItem> getReportItems(String type) {
        List<ReportItem> reportItems = new ArrayList<>(32);
        ReportItem item;

        for (String subjectNo : mapReportItem.keySet()) {
            if (subjectNo.substring(0, 1).equals(type)) {
                item = mapReportItem.get(subjectNo);
                item.calBalance();

                reportItems.add(item);
            }
        }

        return reportItems;
    }

    private void prepareRefreshReport() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dlgDate = new DatePickerDialog(
                context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        collectReportItems(Utility.getDateNumber(year, month, dayOfMonth), new TaskListener() {
                            @Override
                            public void onFinish() {
                                for (int i = 0; i < 5; i++) {
                                    reportFragments[i].setType(Constant.CODE_TYPE[i]);
                                    reportFragments[i].onResume();
                                }
                            }
                        });
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dlgDate.setTitle(activityTitle);
        dlgDate.setMessage("請選擇要查看的時間點");
        dlgDate.show();
    }
}
