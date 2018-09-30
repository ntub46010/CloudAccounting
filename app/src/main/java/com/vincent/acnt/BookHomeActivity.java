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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;
import com.vincent.acnt.accessor.EntryAccessor;
import com.vincent.acnt.accessor.RetrieveEntitiesListener;
import com.vincent.acnt.accessor.SubjectAccessor;
import com.vincent.acnt.adapter.EntryCardAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Entity;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.data.EntryContextMenuHandler;
import com.vincent.acnt.entity.Subject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BookHomeActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView txtLastMonthExpanse, txtThisMonthExpanse, txtBookName, txtBookCreator;
    private RecyclerView recyEntry;
    private FloatingActionButton fabCreateEntry;
    private ProgressBar prgBar;
    private RelativeLayout layHint;

    private EntryCardAdapter adapter;

    private SubjectAccessor asrSubject;
    private EntryAccessor asrEntry;
    private ListenerRegistration regSubject, regEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_home);
        context = this;
        Bundle bundle = getIntent().getExtras();
        asrSubject = new SubjectAccessor(MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .collection(Constant.KEY_SUBJECTS));
        asrEntry = new EntryAccessor(MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .collection(Constant.KEY_ENTRIES));

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(bundle.getString(Constant.KEY_BOOK_NAME));
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        fabCreateEntry = findViewById(R.id.fabCreateEntry);
        txtLastMonthExpanse = findViewById(R.id.txtLastMonthExpanse);
        txtThisMonthExpanse = findViewById(R.id.txtThisMonthExpanse);
        recyEntry = findViewById(R.id.recyEntry);
        prgBar = findViewById(R.id.prgBar);
        layHint = findViewById(R.id.layContentHint);

        setupDrawer(bundle);

        fabCreateEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(context, EntryEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.KEY_MODE, Constant.MODE_CREATE);
                it.putExtras(bundle);
                startActivity(it);
            }
        });

        recyEntry.setHasFixedSize(true);
        recyEntry.setLayoutManager(new LinearLayoutManager(context));

        loadSubjects();
    }

    private void setupDrawer(Bundle bundle) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_journal:
                        startActivity(new Intent(context, JournalActivity.class));
                        break;
                    case R.id.nav_ledger:
                        startActivity(new Intent(context, LedgerActivity.class));
                        break;
                    case R.id.nav_report:
                        startActivity(new Intent(context, ReportActivity.class));
                        break;
                    case R.id.nav_subject:
                        startActivity(new Intent(context, SubjectActivity.class));
                        break;
                    case R.id.nav_member:
                        startActivity(new Intent(context, BookMemberActivity.class));
                        break;
                    case R.id.nav_options:
                        startActivityForResult(new Intent(context, BookOptionActivity.class), 0);
                        break;
                }

                return false;
            }
        });

        View header = navigationView.getHeaderView(0);
        txtBookName = header.findViewById(R.id.txtBookName);
        txtBookCreator = header.findViewById(R.id.txtCreator);
        txtBookName.setText(bundle.getString(Constant.KEY_BOOK_NAME));
        //txtBookCreator.setText("由" + bundle.getString(Constant.KEY_CREATOR) + "建立");
    }

    private void loadSubjects() {
        fabCreateEntry.setVisibility(View.INVISIBLE);

        regSubject = asrSubject.observeSubjects(new RetrieveEntitiesListener() {
            @Override
            public void onRetrieve(List<? extends Entity> entities) {
                List<Subject> subjects = (List<Subject>) entities;
                //Subject subject;

                /*
                MyApp.mapSubjectById.clear();
                MyApp.mapSubjectByName.clear();
                MyApp.mapSubjectByNo.clear();
                */

                MyApp.subjectTable.clear();
                for (int i = 0, len = subjects.size(); i < len; i++) {
                    MyApp.subjectTable.add(subjects.get(i));
                    /*
                    subject = subjects.get(i);

                    MyApp.mapSubjectById.put(subject.getId(), subject);
                    MyApp.mapSubjectByNo.put(subject.getNo(), subject);
                    MyApp.mapSubjectByName.put(subject.getName(), subject);
                    */
                }

                observeTodayStatement();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void observeTodayStatement() {
        regEntry = asrEntry.observeTodayStatement(new EntryAccessor.RetrieveTodayStatementListener() {
            @Override
            public void onRetrieve(int lastMonthExpanse, int thisMonthExpanse, List<Entry> thisMonthEntries, List<Entry> todayEntries) {
                txtLastMonthExpanse.setText(NumberFormat.getNumberInstance(Locale.US).format(lastMonthExpanse));
                txtThisMonthExpanse.setText(NumberFormat.getNumberInstance(Locale.US).format(thisMonthExpanse));

                MyApp.thisMonthEntries.clear();
                MyApp.thisMonthEntries = thisMonthEntries;

                if (todayEntries.isEmpty()) {
                    TextView txtHint = findViewById(R.id.txtHint);
                    txtHint.setText("今日尚未記帳，點擊右下方按鈕進行記帳");
                    layHint.setVisibility(View.VISIBLE);
                } else {
                    layHint.setVisibility(View.GONE);
                }

                if (adapter == null) {
                    adapter = new EntryCardAdapter(context, todayEntries);
                    recyEntry.setAdapter(adapter);
                    prgBar.setVisibility(View.GONE);
                    fabCreateEntry.setVisibility(View.VISIBLE);
                } else {
                    adapter.setEntries(todayEntries);
                }
            }
        });
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
        MyApp.browsingBook = null;
        /*
        MyApp.mapSubjectById.clear();
        MyApp.mapSubjectByNo.clear();
        MyApp.mapSubjectByName.clear();
        */
        MyApp.subjectTable.clear();
        MyApp.thisMonthEntries.clear();
        regSubject.remove();
        regEntry.remove();
        super.onDestroy();
    }

    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        EntryContextMenuHandler handler = new EntryContextMenuHandler(context, adapter.getItem(adapter.longClickPosition));
        switch (item.getItemId()) {
            case Constant.MODE_UPDATE:
                handler.updateEntry();
                break;

            case Constant.MODE_DELETE:
                handler.deleteEntry(toolbar.getTitle().toString(), prgBar, recyEntry);
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constant.MODE_QUIT) {
            setResult(Constant.MODE_QUIT);
            finish();
        }
    }

}
