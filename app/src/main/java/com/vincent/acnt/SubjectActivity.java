package com.vincent.acnt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.acnt.accessor.SubjectAccessor;
import com.vincent.acnt.accessor.TaskFinishListener;
import com.vincent.acnt.adapter.SubjectListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "會計科目";

    private ListView lstSubject;
    private ProgressBar prgBar;
    private RelativeLayout layHint;

    private SubjectListAdapter adapter;
    private int longClickPosition;

    private Subject subject;
    private SubjectAccessor accessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        context = this;
        accessor = new SubjectAccessor(MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .collection(Constant.KEY_SUBJECTS));

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
        layHint = findViewById(R.id.layContentHint);

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

    @Override
    public void onResume() {
        super.onResume();
        loadSubjects();
    }

    private void loadSubjects() {
        //List<Subject> subjects = new ArrayList<>(64);
        List<Subject> subjects = MyApp.subjectTable.findAll();

        /*
        for (String subjectNo : MyApp.mapSubjectByNo.keySet()) {
            subjects.add(MyApp.mapSubjectByNo.get(subjectNo));
        }
        */

        if (subjects.isEmpty()) {
            TextView txtHint = findViewById(R.id.txtHint);
            txtHint.setText("尚未建立會計科目，點擊右下方按鈕進行新增");
            layHint.setVisibility(View.VISIBLE);
        } else {
            layHint.setVisibility(View.GONE);
        }

        adapter = new SubjectListAdapter(context, subjects);
        lstSubject.setAdapter(adapter);

        prgBar.setVisibility(View.GONE);
        lstSubject.setVisibility(View.VISIBLE);
    }

    private void deleteSubject() {
        Utility.getPlainDialog(context, activityTitle, "確定要刪除？\n" + subject.getName())
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        accessor.delete(subject.obtainDocumentId(), new TaskFinishListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(context, "科目刪除成功", Toast.LENGTH_SHORT).show();
                                loadSubjects();
                            }

                            @Override
                            public void onFailure(Exception e) {
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
