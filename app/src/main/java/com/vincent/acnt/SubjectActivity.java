package com.vincent.acnt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.SubjectListAdapter;
import com.vincent.acnt.entity.Subject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.vincent.acnt.MyApp.MODE_CREATE;
import static com.vincent.acnt.MyApp.MODE_UPDATE;
import static com.vincent.acnt.MyApp.KEY_MODE;
import static com.vincent.acnt.MyApp.KEY_SUBJECT;
import static com.vincent.acnt.MyApp.browsingBook;
import static com.vincent.acnt.data.Utility.getPlainDialog;
import static com.vincent.acnt.MyApp.KEY_BOOKS;
import static com.vincent.acnt.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.MyApp.PRO_SUBJECT_NO;

public class SubjectActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "會計科目";
    private FirebaseFirestore db;

    private ListView lstSubject;
    private ProgressBar prgBar;

    private SubjectListAdapter adapter;
    private final int mnuUpdateSubject = Menu.FIRST, mnuDeleteSubject = Menu.FIRST + 1;
    private int longClickPosition;

    private Subject subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        context = this;
        db = MyApp.getInstance().getFirestore();

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

        FloatingActionButton fabCreateSubject = findViewById(R.id.fabCreateSubject);
        lstSubject = findViewById(R.id.lstSubject);
        prgBar = findViewById(R.id.prgBar);

        fabCreateSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(context, SubjectEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_MODE, MODE_CREATE);
                it.putExtras(bundle);
                startActivity(it);
            }
        });

        registerForContextMenu(lstSubject);

        prgBar.setVisibility(View.VISIBLE);
        lstSubject.setVisibility(View.GONE);

        loadSubjects();
    }

    private void loadSubjects() {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_SUBJECTS)
                .orderBy(PRO_SUBJECT_NO, Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        ArrayList<Subject> subjects = new ArrayList<>();
                        Subject subject;

                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            subject = documentSnapshots.get(i).toObject(Subject.class);
                            subject.defineDocumentId(documentSnapshots.get(i).getId());
                            subjects.add(subject);
                        }

                        adapter = new SubjectListAdapter(context, subjects);
                        lstSubject.setAdapter(adapter);

                        prgBar.setVisibility(View.GONE);
                        lstSubject.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void deleteSubject() {
        getPlainDialog(context, activityTitle, "確定要刪除？\n" + subject.getName())
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_SUBJECTS).document(subject.obtainDocumentId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            Toast.makeText(context, "科目刪除成功", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(context, "科目刪除失敗", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, mnuUpdateSubject, 0, "編輯");
        menu.add(0, mnuDeleteSubject, 1, "刪除");
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        longClickPosition = acmi.position;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        subject = (Subject) adapter.getItem(longClickPosition);
        switch (item.getItemId()) {
            case mnuUpdateSubject:
                Intent it = new Intent(context, SubjectEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_MODE, MODE_UPDATE);
                bundle.putSerializable(KEY_SUBJECT, subject);
                it.putExtras(bundle);
                startActivity(it);
                break;

            case mnuDeleteSubject:
                deleteSubject();
                break;
        }

        return super.onContextItemSelected(item);
    }

}
