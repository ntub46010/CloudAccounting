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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.EntryCardAdapter;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.data.EntryContextMenuHandler;
import com.vincent.acnt.entity.Subject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import static com.vincent.acnt.data.EntryContextMenuHandler.MENU_DELETE;
import static com.vincent.acnt.data.EntryContextMenuHandler.MENU_UPDATE;
import static com.vincent.acnt.MyApp.CODE_QUIT_ACTIVITY;
import static com.vincent.acnt.MyApp.CODE_TYPE;
import static com.vincent.acnt.MyApp.KEY_BOOKS;
import static com.vincent.acnt.MyApp.KEY_BOOK_NAME;
import static com.vincent.acnt.MyApp.KEY_CREATOR;
import static com.vincent.acnt.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.MyApp.PRO_DATE;
import static com.vincent.acnt.MyApp.browsingBook;

public class BookHomeActivity extends AppCompatActivity {
    private Context context;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView txtLastMonthExpanse, txtThisMonthExpanse, txtBookName, txtBookCreator;
    private RecyclerView recyEntry;
    private ProgressBar prgBar;

    private ArrayList<Entry> entries;
    private EntryCardAdapter adapter;
    private int thisMonthStartDate, thisMonthEndDate,
            thisMonthExpanseCredit = 0, thisMonthExpanseDebit = 0, lastMonthExpanseCredit = 0, lastMonthExpanseDebit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_home);
        context = this;
        db = MyApp.getInstance().getFirestore();
        Bundle bundle = getIntent().getExtras();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(bundle.getString(KEY_BOOK_NAME));
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        FloatingActionButton fabCreateEntry = findViewById(R.id.fabCreateEntry);
        txtLastMonthExpanse = findViewById(R.id.txtLastMonthExpanse);
        txtThisMonthExpanse = findViewById(R.id.txtThisMonthExpanse);
        recyEntry = findViewById(R.id.recyEntry);
        prgBar = findViewById(R.id.prgBar);

        setupDrawer(bundle);

        fabCreateEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(context, EntryCreateActivity.class), 0);
            }
        });

        recyEntry.setHasFixedSize(true);
        recyEntry.setLayoutManager(new LinearLayoutManager(context));

        Date date = new Date();
        thisMonthStartDate = Integer.parseInt(new SimpleDateFormat("yyyyMM01").format(date));
        thisMonthEndDate = Integer.parseInt(new SimpleDateFormat("yyyyMM31").format(date));

        startQueryExpanse();
    }

    @Override
    public void onResume() {
        super.onResume();

        thisMonthStartDate = Integer.parseInt(new SimpleDateFormat("yyyyMM01").format(new Date()));
        thisMonthEndDate = Integer.parseInt(new SimpleDateFormat("yyyyMM31").format(new Date()));
    }

    private void startQueryExpanse() {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_ENTRIES)
                .whereGreaterThanOrEqualTo(PRO_DATE, thisMonthStartDate)
                .whereLessThanOrEqualTo(PRO_DATE, thisMonthEndDate)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        thisMonthExpanseCredit = 0;
                        thisMonthExpanseDebit = 0;
                        lastMonthExpanseCredit = 0;
                        lastMonthExpanseDebit = 0;

                        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        entries = new ArrayList<>();
                        Entry entry;

                        //取出分錄
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            entry = documentSnapshot.toObject(Entry.class);

                            //若為今日的分錄，則保存起來
                            if (entry.getDate() == Integer.parseInt(today)) {
                                entry.defineDocumentId(documentSnapshot.getId());
                                entries.add(entry);
                            }

                            //檢查分錄內的費用科目，進行總額累計
                            for (Subject subject : entry.getSubjects()) {
                                if (subject.getSubjectId().substring(0, 1).equals(CODE_TYPE[4])) {
                                    thisMonthExpanseCredit += subject.getCredit();
                                    thisMonthExpanseDebit += subject.getDebit();
                                }
                            }
                        } //本月費用計算完畢

                        //繼續計算上個月的費用
                        int lastMonthStartDate = thisMonthStartDate;

                        if (String.valueOf(lastMonthStartDate).substring(String.valueOf(lastMonthStartDate).length() - 4).equals("0101")) {
                            lastMonthStartDate -= 10000;
                            lastMonthStartDate = (lastMonthStartDate / 10000) * 10000 + 1231;
                        }else
                            lastMonthStartDate -= 100;

                        queryLastMonthExpanse(lastMonthStartDate, thisMonthStartDate);
                    }
                });
    }

    private void setupDrawer(Bundle bundle) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        navigationView.getMenu().findItem(R.id.nav_member).setTitle(String.format("成員（ %d ）", browsingBook.getMemberIds().size()));
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
        txtBookName.setText(bundle.getString(KEY_BOOK_NAME));
        txtBookCreator.setText("由" + bundle.getString(KEY_CREATOR) + "建立");

        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            Book book = documentSnapshot.toObject(Book.class);
                            toolbar.setTitle(book.getName());
                            txtBookName.setText(book.getName());
                            txtBookCreator.setText("由" + book.getCreator() + "建立");
                        }else
                            Toast.makeText(context, "該帳本不存在！", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void queryLastMonthExpanse(int startDate, int endDate) {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_ENTRIES)
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

                        showDashboard();
                    }
                });
    }

    private void showDashboard() {
        txtLastMonthExpanse.setText(NumberFormat.getNumberInstance(Locale.US).format(lastMonthExpanseCredit - lastMonthExpanseDebit));
        txtThisMonthExpanse.setText(NumberFormat.getNumberInstance(Locale.US).format(thisMonthExpanseCredit - thisMonthExpanseDebit));

        adapter = new EntryCardAdapter(context, entries);
        recyEntry.setAdapter(adapter);
        if (entries.isEmpty())
            Toast.makeText(context, "今日尚未記帳", Toast.LENGTH_SHORT).show();

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
        browsingBook = null;
        super.onDestroy();
    }

    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        EntryContextMenuHandler handler = new EntryContextMenuHandler(context, adapter.getItem(adapter.longClickPosition), db);
        switch (item.getItemId()) {
            case MENU_UPDATE:
                handler.updateEntry();
                break;

            case MENU_DELETE:
                handler.deleteEntry(toolbar.getTitle().toString(), prgBar, recyEntry);
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CODE_QUIT_ACTIVITY) {
            setResult(CODE_QUIT_ACTIVITY);
            finish();
        }
    }

}
