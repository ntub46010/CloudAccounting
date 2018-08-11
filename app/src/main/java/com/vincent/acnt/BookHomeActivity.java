package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.Entry;
import com.vincent.acnt.data.MyApp;
import com.vincent.acnt.data.Subject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.vincent.acnt.data.MyApp.CODE_TYPE;
import static com.vincent.acnt.data.MyApp.KEY_BOOKS;
import static com.vincent.acnt.data.MyApp.KEY_BOOK_DOCUMENT_ID;
import static com.vincent.acnt.data.MyApp.KEY_BOOK_NAME;
import static com.vincent.acnt.data.MyApp.KEY_CREATOR;
import static com.vincent.acnt.data.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.data.MyApp.PRO_DATE;
import static com.vincent.acnt.data.MyApp.browsingBookDocumentId;

public class BookHomeActivity extends AppCompatActivity {
    private Context context;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigation_view;

    private TextView txtLastMonthExpanse, txtThisMonthExpanse;
    private ProgressBar prgBar;

    private int thisMonthExpanseCredit = 0, thisMonthExpanseDebit = 0, lastMonthExpanseCredit = 0, lastMonthExpanseDebit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_home);
        context = this;
        db = MyApp.getInstance().getFirestore();
        Bundle bundle = getIntent().getExtras();
        browsingBookDocumentId = bundle.getString(KEY_BOOK_DOCUMENT_ID);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(bundle.getString(KEY_BOOK_NAME));
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigation_view = findViewById(R.id.navigation_view);
        FloatingActionButton fabCreateEntry = findViewById(R.id.fabCreateEntry);
        txtLastMonthExpanse = findViewById(R.id.txtLastMonthExpanse);
        txtThisMonthExpanse = findViewById(R.id.txtThisMonthExpanse);
        prgBar = findViewById(R.id.prgBar);

        setupDrawer(bundle);

        fabCreateEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, EntryCreateActivity.class));
            }
        });

        startQueryExpanse(true);
    }

    private void setupDrawer(Bundle bundle) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_journal:
                        startActivityForResult(new Intent(context, JournalActivity.class), 0);
                        break;
                    case R.id.nav_ledger:
                        startActivityForResult(new Intent(context, LedgerActivity.class), 0);
                        break;
                    case R.id.nav_report:
                        startActivityForResult(new Intent(context, ReportActivity.class), 0);
                        break;
                    case R.id.nav_subject:
                        startActivityForResult(new Intent(context, SubjectActivity.class), 0);
                        break;
                    case R.id.nav_member:
                        break;
                    case R.id.nav_options:
                        break;
                }

                return false;
            }
        });

        View header = navigation_view.getHeaderView(0);
        TextView txtBookName = header.findViewById(R.id.txtBookName);
        TextView txtCreator = header.findViewById(R.id.txtCreator);
        txtBookName.setText(bundle.getString(KEY_BOOK_NAME));
        txtCreator.setText("由" + bundle.getString(KEY_CREATOR) + "建立");
    }

    private void startQueryExpanse(boolean showPrgBar) {
        if (showPrgBar)
            prgBar.setVisibility(View.VISIBLE);

        int thisMonthStart = Integer.parseInt(new SimpleDateFormat("yyyyMM01").format(new Date()));
        int thisMonthEnd = Integer.parseInt(new SimpleDateFormat("yyyyMM31").format(new Date()));
        queryThisMonthExpanse(thisMonthStart, thisMonthEnd);
    }

    private void queryThisMonthExpanse(final int startDate, int endDate) {
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_ENTRIES)
                .whereGreaterThanOrEqualTo(PRO_DATE, startDate)
                .whereLessThanOrEqualTo(PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "查詢本月交易失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                            return;
                        }

                        QuerySnapshot querySnapshot = task.getResult();
                        Entry entry;
                        //取出分錄
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            entry = documentSnapshot.toObject(Entry.class);

                            //檢查分錄內的費用科目，進行總額累計
                            for (Subject subject : entry.getSubjects()) {
                                if (subject.getSubjectId().substring(0, 1).equals(CODE_TYPE[4])) {
                                    thisMonthExpanseCredit += subject.getCredit();
                                    thisMonthExpanseDebit += subject.getDebit();
                                }
                            }
                        } //本月費用計算完畢

                        //繼續計算上個月的費用
                        int lastMonthStartDate = startDate;

                        if (String.valueOf(lastMonthStartDate).substring(String.valueOf(lastMonthStartDate).length() - 4).equals("0101")) {
                            lastMonthStartDate -= 10000;
                            lastMonthStartDate = (lastMonthStartDate / 10000) * 10000 + 1231;
                        }else
                            lastMonthStartDate -= 100;

                        queryLastMonthExpanse(lastMonthStartDate, startDate);
                    }
                });
    }

    private void queryLastMonthExpanse(int startDate, int endDate) {
        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_ENTRIES)
                .whereGreaterThanOrEqualTo(PRO_DATE, startDate)
                .whereLessThan(PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "查詢上月交易失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                            return;
                        }

                        QuerySnapshot querySnapshot = task.getResult();
                        Entry entry;
                        //取出分錄
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            entry = documentSnapshot.toObject(Entry.class);

                            //檢查分錄內的費用科目，進行總額累計
                            for (Subject subject : entry.getSubjects()) {
                                if (subject.getSubjectId().substring(0, 1).equals(CODE_TYPE[4])) {
                                    lastMonthExpanseCredit += subject.getCredit();
                                    lastMonthExpanseDebit += subject.getDebit();
                                }
                            }
                        } //上月費用計算完畢

                        showExpanseDashboard();
                    }
                });
    }

    private void showExpanseDashboard() {
        txtLastMonthExpanse.setText(NumberFormat.getNumberInstance(Locale.US).format(lastMonthExpanseCredit - lastMonthExpanseDebit));
        txtThisMonthExpanse.setText(NumberFormat.getNumberInstance(Locale.US).format(thisMonthExpanseCredit - thisMonthExpanseDebit));
        prgBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) //收起選單
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        browsingBookDocumentId = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startQueryExpanse(false);
    }
}
