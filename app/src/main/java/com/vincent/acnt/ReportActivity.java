package com.vincent.acnt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.vincent.acnt.accessor.EntryAccessor;
import com.vincent.acnt.adapter.ReportPagerAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
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

    private ViewPager vpgReport;
    private FloatingActionButton fabDate;

    private Map<String, ReportItem> mapReportItem = new TreeMap<>();
    private ReportFragment[] reportFragments = new ReportFragment[5];

    private Dialog dlgWaiting;
    private EntryAccessor accessor;

    private interface TaskListener { void onFinish();}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        context = this;
        accessor = new EntryAccessor(MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .collection(Constant.KEY_ENTRIES));
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

        vpgReport = findViewById(R.id.vpgReport);
        fabDate = findViewById(R.id.fabSelectDate);
        TabLayout tabHome = findViewById(R.id.tabs);

        vpgReport.setOffscreenPageLimit(15);
        vpgReport.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        tabHome.setupWithViewPager(vpgReport);

        fabDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareRefreshReport();
            }
        });

        dlgWaiting = Utility.getWaitingDialog(context);

        //collectReportItems(Integer.parseInt(date), null);
        loadReportItems(Integer.parseInt(date), null);
    }

    private void loadReportItems(final int endDate, final TaskListener listener) {
        dlgWaiting.show();
        fabDate.setVisibility(View.INVISIBLE);

        //儲存各個科目的初始餘額
        mapReportItem.clear();
        Subject s;
        ReportItem item;

        List<Subject> subjects = MyApp.subjectTable.findAll();
        for (int i = 0, len = subjects.size(); i < len; i++) {
            s = subjects.get(i);

            item = new ReportItem();
            item.setId(s.getNo());
            item.setName(s.getName());
            item.addCredit(s.getCredit());
            item.addDebit(s.getDebit());

            mapReportItem.put(s.getNo(), item);
        }

        accessor.loadReportItems(endDate, mapReportItem, new EntryAccessor.RetrieveReportItemsListener() {
            @Override
            public void onRetrieve(Map<String, ReportItem> mapReportItem) {
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
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "查詢分錄失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFragment() {
        ReportPagerAdapter adapter = new ReportPagerAdapter(getSupportFragmentManager());

        for (int i = 0; i < 5; i++) {
            reportFragments[i] = new ReportFragment();
            reportFragments[i].setType(Constant.CODE_TYPE[i]);
            reportFragments[i].setReportItems(getReportItemsByType(Constant.CODE_TYPE[i]));
        }

        adapter.addFragment(reportFragments[0], "資產");
        adapter.addFragment(reportFragments[1], "負債");
        adapter.addFragment(reportFragments[2], "權益");
        adapter.addFragment(reportFragments[3], "收入");
        adapter.addFragment(reportFragments[4], "支出");

        vpgReport.setAdapter(adapter);
    }

    private List<ReportItem> getReportItemsByType(String type) {
        List<ReportItem> reportItems = new ArrayList<>(32);
        ReportItem item;

        // 由於mapReportItem使用TreeMap實作，因此取得的項目將會依照科目編號排列並依序存入List
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
                        loadReportItems(Utility.getDateNumber(year, month, dayOfMonth), new TaskListener() {
                            @Override
                            public void onFinish() {
                                for (int i = 0; i < 5; i++) {
                                    reportFragments[i].setType(Constant.CODE_TYPE[i]);
                                    reportFragments[i].setReportItems(getReportItemsByType(Constant.CODE_TYPE[i]));
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
