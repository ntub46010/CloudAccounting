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
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Subject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SubjectActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "會計科目";
    private FirebaseFirestore db;

    private ListView lstSubject;
    private ProgressBar prgBar;

    private SubjectListAdapter adapter;
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
                bundle.putInt(Constant.KEY_MODE, Constant.MODE_CREATE);
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
        db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_SUBJECTS)
                .orderBy(Constant.PRO_SUBJECT_NO, Query.Direction.ASCENDING)
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
        Utility.getPlainDialog(context, activityTitle, "確定要刪除？\n" + subject.getName())
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId()).collection(Constant.KEY_SUBJECTS).document(subject.obtainDocumentId())
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
        menu.add(0, Constant.MODE_UPDATE, 0, "編輯");
        menu.add(0, Constant.MODE_DELETE, 1, "刪除");
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        longClickPosition = acmi.position;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        subject = (Subject) adapter.getItem(longClickPosition);
        switch (item.getItemId()) {
            case Constant.MODE_UPDATE:
                Intent it = new Intent(context, SubjectEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.KEY_MODE, Constant.MODE_UPDATE);
                bundle.putSerializable(Constant.KEY_SUBJECT, subject);
                it.putExtras(bundle);
                startActivity(it);
                break;

            case Constant.MODE_DELETE:
                deleteSubject();
                break;
        }

        return super.onContextItemSelected(item);
    }

}
