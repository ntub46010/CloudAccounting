package com.vincent.acnt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.vincent.acnt.adapter.SubjectListAdapter;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.Subject;
import com.vincent.acnt.data.Verifier;

import java.util.ArrayList;

import javax.annotation.Nullable;

import static com.vincent.acnt.MyApp.browsingBook;
import static com.vincent.acnt.data.Utility.binarySearchNumber;
import static com.vincent.acnt.data.Utility.getPlainDialog;
import static com.vincent.acnt.MyApp.KEY_BOOKS;
import static com.vincent.acnt.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.MyApp.PRO_SUBJECT_ID;

public class SubjectActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "會計科目";
    private FirebaseFirestore db;

    private ListView lstSubject;
    private EditText edtId, edtName, edtCredit, edtDebit;
    private ProgressBar prgBar;

    private SubjectListAdapter adapter;
    private final int mnuEditSubject = Menu.FIRST, mnuDelSubject = Menu.FIRST + 1;
    private int longClickPosition;

    private Subject subject;
    private int subjectId, mode;
    private ArrayList<Integer> subjectIds;

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
                mode = 1;
                prepareDialog();
            }
        });

        registerForContextMenu(lstSubject);

        prgBar.setVisibility(View.VISIBLE);
        lstSubject.setVisibility(View.GONE);

        //顯示科目清單
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_SUBJECTS)
                .orderBy(PRO_SUBJECT_ID, Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        ArrayList<Subject> subjects = new ArrayList<>();
                        subjectIds = new ArrayList<>();
                        Subject subject;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            subject = documentSnapshot.toObject(Subject.class);
                            subject.defineDocumentId(documentSnapshot.getId());
                            subjects.add(subject);

                            //取得各科目編號，供新增與編輯科目時確認
                            subjectIds.add(Integer.parseInt(subject.getSubjectId()));
                        }

                        adapter = new SubjectListAdapter(context, subjects);
                        lstSubject.setAdapter(adapter);

                        prgBar.setVisibility(View.GONE);
                        lstSubject.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void prepareDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dlg_add_subject, null);

        Spinner spnType = layout.findViewById(R.id.spnSubjectType);
        edtId = layout.findViewById(R.id.edtSubjectId);
        edtName = layout.findViewById(R.id.edtSubjectName);
        edtCredit = layout.findViewById(R.id.edtCredit);
        edtDebit = layout.findViewById(R.id.edtDebit);
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subjectId = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        AlertDialog.Builder dlalog = getPlainDialog(context, activityTitle, "請輸入科目資料");
        dlalog.setView(layout).setNegativeButton("取消", null);

        if (mode == 1) {
            dlalog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String credit = edtCredit.getText().toString();
                            String debit = edtDebit.getText().toString();
                            addSubject(new Subject(
                                    String.valueOf(subjectId) + edtId.getText().toString(),
                                    edtName.getText().toString(),
                                    credit.equals("") ? 0 : Integer.parseInt(credit),
                                    debit.equals("") ? 0 : Integer.parseInt(debit),
                                    System.currentTimeMillis()
                            ));
                        }
                    })
                    .show();

        }else if (mode == 2) {
            spnType.setSelection(Integer.parseInt(String.valueOf(subject.getSubjectId()).substring(0, 1)) - 1);
            edtId.setText(String.valueOf(subject.getSubjectId().substring(1, 3)));
            edtName.setText(subject.getName());
            if ((subject.getCredit() == 0) != (subject.getDebit() == 0)) {
                if (subject.getDebit() == 0)
                    edtCredit.setText(String.valueOf(subject.getCredit()));
                else
                    edtDebit.setText(String.valueOf(subject.getDebit()));
            }

            dlalog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String credit = edtCredit.getText().toString();
                            String debit = edtDebit.getText().toString();
                            Subject newSubject = new Subject(
                                    String.valueOf(subjectId) + edtId.getText().toString(),
                                    edtName.getText().toString(),
                                    credit.equals("") ? 0 : Integer.parseInt(credit),
                                    debit.equals("") ? 0 : Integer.parseInt(debit),
                                    subject.getStamp()
                            );
                            newSubject.defineDocumentId(subject.obtainDocumentId());
                            editSubject(newSubject);
                        }
                    })
                    .show();
        }
    }

    private void addSubject(Subject subject) {
        if (isNotValid(subject))
            return;

        //指定儲存的位置(集合)，呼叫add加入至資料庫，並定義callback方法
        //藉由物件的get方法，轉化為資料庫可儲存的形式
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_SUBJECTS)
                .add(subject)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful())
                            Toast.makeText(context, "科目新增成功", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, "科目新增失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editSubject(final Subject subject) {
        if (isNotValid(subject))
            return;

        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_SUBJECTS).document(subject.obtainDocumentId())
                .set(subject)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "科目編輯成功", Toast.LENGTH_SHORT).show();
                            updateSubjectInEntry(subject);
                        }else
                            Toast.makeText(context, "科目編輯失敗", Toast.LENGTH_SHORT).show();
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

    private void updateSubjectInEntry(final Subject newSubject) {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_ENTRIES)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        ArrayList<Entry> entries = new ArrayList<>();
                        Entry entry;

                        //取得各個分錄
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            entry = documentSnapshot.toObject(Entry.class);
                            entry.defineDocumentId(documentSnapshot.getId());
                            entries.add(entry);
                        }

                        //更新分錄中的科目名稱
                        for (Entry subEntry : entries) {
                            for (Subject subject : subEntry.getSubjects()) { //逐一檢查分錄科目的ID，是否為正在修改的科目
                                if (subject.getStamp() == newSubject.getStamp()) {

                                    subject.setName(newSubject.getName());
                                    db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_ENTRIES)
                                            .document(subEntry.obtainDocumentId())
                                            .set(subEntry, SetOptions.merge());
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    private boolean isNotValid(Subject subject) {
        StringBuilder errMsg = new StringBuilder(64);
        Verifier v = new Verifier(context);

        errMsg.append(v.chkId(String.valueOf(subject.getSubjectId())));
        //檢查編號重複
        if (mode == 1) {
            if (binarySearchNumber(subjectIds, Integer.parseInt(subject.getSubjectId())) >= 0)
                errMsg.append("科目編號").append(subject.getSubjectId()).append("已被使用\n");
        }

        errMsg.append(v.chkSubjectName(subject.getName()));

        if (!edtCredit.getText().toString().equals("") && !edtDebit.getText().toString().equals(""))
            errMsg.append("需於借貸其中一方輸入金額\n");
        else if (subject.getDebit() == 0)
            errMsg.append(v.chkSubjectAmount(String.valueOf(subject.getCredit())));
        else if (subject.getCredit() == 0)
            errMsg.append(v.chkSubjectAmount(String.valueOf(subject.getDebit())));

        if (errMsg.toString().equals(""))
            return false;
        else {
            getPlainDialog(context, activityTitle, errMsg.toString()).show();
            return true;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, mnuEditSubject, 0, "編輯");
        menu.add(0, mnuDelSubject, 1, "刪除");
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        longClickPosition = acmi.position;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        onOptionsItemSelected(item);

        subject = (Subject) adapter.getItem(longClickPosition);
        switch (item.getItemId()) {
            case mnuEditSubject:
                mode = 2;
                prepareDialog();
                break;

            case mnuDelSubject:
                deleteSubject();
                break;
        }

        return super.onContextItemSelected(item);
    }

}
