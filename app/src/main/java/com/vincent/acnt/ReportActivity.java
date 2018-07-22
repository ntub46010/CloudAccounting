package com.vincent.acnt;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.adapter.ReportPagerAdapter;
import com.vincent.acnt.data.MyApp;

public class ReportActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "財務報告";
    private FirebaseFirestore db;

    private TabLayout tabHome;
    private ViewPager vpgHome;
    private ReportPagerAdapter adapter;

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
        tabHome = findViewById(R.id.tabs);

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
        setupFragment(); //加入頁面

        tabHome.setupWithViewPager(vpgHome);
    }

    private void setupFragment() {
        adapter = new ReportPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AssetFragment(), "資產");
        adapter.addFragment(new LiabilityFragment(), "負債");
        adapter.addFragment(new CapitalFragment(), "權益");
        adapter.addFragment(new RevenueFragment(), "收益");
        adapter.addFragment(new ExpenseFragment(), "費損");
        vpgHome.setAdapter(adapter);
    }
}
