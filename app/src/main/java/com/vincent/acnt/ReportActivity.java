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
import com.vincent.acnt.data.Entry;
import com.vincent.acnt.data.MyApp;
import com.vincent.acnt.data.ReportItem;
import com.vincent.acnt.data.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.vincent.acnt.data.Utility.getDateNumber;
import static com.vincent.acnt.data.Utility.getWaitingDialog;
import static com.vincent.acnt.data.MyApp.CODE_TYPE;
import static com.vincent.acnt.data.MyApp.KEY_BOOKS;
import static com.vincent.acnt.data.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.data.MyApp.PRO_DATE;
import static com.vincent.acnt.data.MyApp.PRO_MEMO;
import static com.vincent.acnt.data.MyApp.PRO_SUBJECT_ID;
import static com.vincent.acnt.data.MyApp.browsingBookDocumentId;

public class ReportActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private String activityTitle = "財務報告";
    private FirebaseFirestore db;

    private ViewPager vpgHome;
    private FloatingActionButton fabDate;

    private ArrayList<Subject> subjects;
    private ArrayList<Entry> entries;
    private Map<String, ReportItem> mapReportItem;
    private ReportFragment[] reportFragments = new ReportFragment[5];

    private Dialog dlgWaiting;

    private interface TaskListener { void onFinish();}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        context = this;
        db = MyApp.getInstance().getFirestore();
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

        dlgWaiting = getWaitingDialog(context);

        collectReportItems(Integer.parseInt(date), null);
    }

    private void setupFragment() {
        ReportPagerAdapter adapter = new ReportPagerAdapter(getSupportFragmentManager());

        for (int i = 0; i < 5; i++) {
            reportFragments[i] = new ReportFragment();
            reportFragments[i].setReportItems(getReportItems(CODE_TYPE[i]));
            reportFragments[i].setType(CODE_TYPE[i]);
        }

        adapter.addFragment(reportFragments[0], "資產");
        adapter.addFragment(reportFragments[1], "負債");
        adapter.addFragment(reportFragments[2], "權益");
        adapter.addFragment(reportFragments[3], "收益");
        adapter.addFragment(reportFragments[4], "費損");
        vpgHome.setAdapter(adapter);
    }

    private void collectReportItems(final int endDate, final TaskListener listener) {
        dlgWaiting.show();
        fabDate.setVisibility(View.GONE);

        subjects = new ArrayList<>();
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_SUBJECTS)
                .orderBy(PRO_SUBJECT_ID, Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            //將科目依類別放入各自陣列
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments())
                                subjects.add(documentSnapshot.toObject(Subject.class));

                            //儲存各個科目的初始餘額
                            mapReportItem = new HashMap<>();
                            ReportItem item;
                            for (Subject subject : subjects) {
                                item = new ReportItem(
                                        subject.getSubjectId(),
                                        subject.getName(),
                                        subject.getCredit(),
                                        subject.getDebit()
                                );
                                mapReportItem.put(subject.getSubjectId(), item);
                            }
                            searchInEntry(endDate, listener);
                        }else
                            Toast.makeText(context, "取得科目資料失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchInEntry(final int endDate, final TaskListener listener) {
        entries = new ArrayList<>();
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_ENTRIES)
                .orderBy(PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(PRO_MEMO, Query.Direction.ASCENDING)
                .whereLessThanOrEqualTo(PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            //儲存分錄
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments())
                                entries.add(documentSnapshot.toObject(Entry.class));

                            //從各個分錄中將科目金額逐一儲存
                            ReportItem item;
                            for (Entry entry : entries) {
                                for (Subject subject : entry.getSubjects()) {
                                    if (mapReportItem.containsKey(subject.getSubjectId())) {
                                        //科目已存在，則取出累積金額，再放置回去
                                        item = mapReportItem.get(subject.getSubjectId());
                                        item.addCredit(subject.getCredit());
                                        item.addDebit(subject.getDebit());
                                        mapReportItem.put(subject.getSubjectId(), item);
                                    }else {
                                        item = new ReportItem(
                                                subject.getSubjectId(),
                                                subject.getName(),
                                                subject.getCredit(),
                                                subject.getDebit()
                                        );
                                        mapReportItem.put(subject.getSubjectId(), item);
                                    }
                                }
                            }//各個科目金額計算完畢

                            if (listener == null)
                                setupFragment(); //加入頁面
                            else
                                listener.onFinish();

                            String date = String.valueOf(endDate);
                            toolbar.setTitle(String.format("財務報告  %s/%s/%s",
                                    date.substring(0, 4),
                                    date.substring(4, 6),
                                    date.substring(6, 8)
                            ));
                            fabDate.setVisibility(View.VISIBLE);
                            dlgWaiting.dismiss();
                        }else
                            Toast.makeText(context, "取得分錄失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ArrayList<ReportItem> getReportItems(String type) {
        ArrayList<ReportItem> reportItems = new ArrayList<>();
        ReportItem item;

        for (Subject subject : subjects) {
            if (subject.getSubjectId().substring(0, 1).equals(type)) {
                item = mapReportItem.get(subject.getSubjectId());
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
                        collectReportItems(getDateNumber(year, month, dayOfMonth), new TaskListener() {
                            @Override
                            public void onFinish() {
                                for (int i = 0; i < 5; i++) {
                                    reportFragments[i].setType(CODE_TYPE[i]);
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
