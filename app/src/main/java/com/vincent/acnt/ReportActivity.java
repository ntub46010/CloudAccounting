package com.vincent.acnt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.vincent.acnt.data.MyApp.CODE_ASSET;
import static com.vincent.acnt.data.MyApp.CODE_CAPITAL;
import static com.vincent.acnt.data.MyApp.CODE_EXPANSE;
import static com.vincent.acnt.data.MyApp.CODE_LIABILITY;
import static com.vincent.acnt.data.MyApp.CODE_REVENUE;
import static com.vincent.acnt.data.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.data.MyApp.KEY_REPORT_ITEMS;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.data.MyApp.PRO_DATE;
import static com.vincent.acnt.data.MyApp.PRO_MEMO;
import static com.vincent.acnt.data.MyApp.PRO_SUBJECT_ID;

public class ReportActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "財務報告";
    private FirebaseFirestore db;

    private ViewPager vpgHome;

    private ArrayList<Subject> subjects;
    private ArrayList<Entry> entries;
    private Map<String, ReportItem> mapReportItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
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

        vpgHome = findViewById(R.id.vpgHome);
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

        collectReportItems();
    }

    private void setupFragment() {
        ReportPagerAdapter adapter = new ReportPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();

        AssetFragment assetFragment = new AssetFragment();
        bundle.putSerializable(KEY_REPORT_ITEMS, getReportItems(CODE_ASSET));
        assetFragment.setArguments(bundle);
        adapter.addFragment(assetFragment, "資產");
/*
        LiabilityFragment liabilityFragment = new LiabilityFragment();
        bundle.putSerializable(KEY_REPORT_ITEMS, getReportItems(CODE_LIABILITY));
        liabilityFragment.setArguments(bundle);
        adapter.addFragment(liabilityFragment, "負債");

        CapitalFragment capitalFragment = new CapitalFragment();
        bundle.putSerializable(KEY_REPORT_ITEMS, getReportItems(CODE_CAPITAL));
        capitalFragment.setArguments(bundle);
        adapter.addFragment(capitalFragment, "權益");

        RevenueFragment revenueFragment = new RevenueFragment();
        bundle.putSerializable(KEY_REPORT_ITEMS, getReportItems(CODE_REVENUE));
        revenueFragment.setArguments(bundle);
        adapter.addFragment(revenueFragment, "收益");
        ExpenseFragment expenseFragment = new ExpenseFragment();
        bundle.putSerializable(KEY_REPORT_ITEMS, getReportItems(CODE_EXPANSE));
        expenseFragment.setArguments(bundle);
        adapter.addFragment(expenseFragment, "費損");
*/
        vpgHome.setAdapter(adapter);
    }

    private void collectReportItems() {
        subjects = new ArrayList<>();

        db.collection(KEY_SUBJECTS)
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

                            searchInEntry(20180722);
                        }else
                            Toast.makeText(context, "取得科目資料失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchInEntry(int endDate) {
        entries = new ArrayList<>();
        db.collection(KEY_ENTRIES)
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
                            }

                            //各個科目金額計算完畢
                            setupFragment(); //加入頁面
                        }else
                            Toast.makeText(context, "取得分錄失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public ArrayList<ReportItem> getReportItems(String type) {
        ArrayList<ReportItem> reportItems = new ArrayList<>();
        ReportItem item;

        for (Subject subject : subjects) {
            if (subject.getSubjectId().substring(0, 1).equals(type)) {
                item = mapReportItem.get(subject.getSubjectId());
                item.calBalance();
                reportItems.add(item);
            }else
                break;
        }
        return reportItems;
    }
}
