package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.acnt.accessor.RetrieveEntityListener;
import com.vincent.acnt.accessor.SubjectAccessor;
import com.vincent.acnt.accessor.TaskFinishListener;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.data.Verifier;
import com.vincent.acnt.entity.Entity;
import com.vincent.acnt.entity.Subject;

public class SubjectEditActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "會計科目";
    private Bundle bundle;

    private Spinner spnType;
    private EditText edtNo, edtName, edtCredit, edtDebit;

    private Dialog dlgWaiting;

    private long id;
    private String documentId;
    private SubjectAccessor accessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_edit);
        context = this;
        bundle = getIntent().getExtras();
        accessor = new SubjectAccessor(MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .collection(Constant.KEY_SUBJECTS));

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView txtBarTitle = toolbar.findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText(activityTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView btnSubmit = findViewById(R.id.btnSubmit);
        spnType = findViewById(R.id.spnSubjectType);
        edtNo = findViewById(R.id.edtSubjectNo);
        edtName = findViewById(R.id.edtSubjectName);
        edtCredit = findViewById(R.id.edtCredit);
        edtDebit = findViewById(R.id.edtDebit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String credit = edtCredit.getText().toString();
                String debit = edtDebit.getText().toString();

                Subject subject = new Subject();
                subject.setNo(String.valueOf(spnType.getSelectedItemPosition()) + edtNo.getText().toString());
                subject.setName(edtName.getText().toString());
                subject.setCredit(credit.equals("") ? 0 : Integer.parseInt(credit));
                subject.setDebit(debit.equals("") ? 0 : Integer.parseInt(debit));

                if (isNotValid(subject)) {
                    return;
                }

                dlgWaiting.show();

                if (bundle.getInt(Constant.KEY_MODE) == Constant.MODE_CREATE) {
                    subject.setId(System.currentTimeMillis());
                    createSubject(subject);
                } else {
                    subject.setId(id);
                    subject.defineDocumentId(documentId);
                    updateSubject(subject);
                }
            }
        });

        if (bundle.getInt(Constant.KEY_MODE) == Constant.MODE_UPDATE) {
            Subject subject = (Subject) bundle.getSerializable(Constant.KEY_SUBJECT);
            id = subject.getId();
            documentId = subject.obtainDocumentId();
            spnType.setSelection(Integer.parseInt(subject.getNo().substring(0, 1)));
            edtNo.setText(subject.getNo().substring(1, 3));
            edtName.setText(subject.getName());
            edtCredit.setText(String.valueOf(subject.getCredit()));
            edtDebit.setText(String.valueOf(subject.getDebit()));
        }

        dlgWaiting = Utility.getWaitingDialog(context);
    }

    private void createSubject(Subject subject) {
        accessor.create(subject, new RetrieveEntityListener() {
            @Override
            public void onRetrieve(Entity entity) {
                dlgWaiting.dismiss();
                Toast.makeText(context, "科目新增成功", Toast.LENGTH_SHORT).show();

                spnType.setSelection(0);
                edtNo.setText(null);
                edtName.setText(null);
                edtCredit.setText(null);
                edtDebit.setText(null);
            }

            @Override
            public void onFailure(Exception e) {
                dlgWaiting.dismiss();
                Toast.makeText(context, "科目新增失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSubject(Subject subject) {
        accessor.update(subject, new TaskFinishListener() {
            @Override
            public void onSuccess() {
                dlgWaiting.dismiss();
                Toast.makeText(context, "科目更新成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                dlgWaiting.dismiss();
                Toast.makeText(context, "科目更新失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNotValid(Subject subject) {
        StringBuilder errMsg = new StringBuilder(64);
        Verifier v = new Verifier(context);

        if (subject.getNo().substring(0, 1).equals("0")) {
            errMsg.append("科目類別未選擇\n");
        }

        if (bundle.getInt(Constant.KEY_MODE) == Constant.MODE_CREATE) {

            if (MyApp.subjectTable.existByProperty(Constant.PRO_SUBJECT_NO, subject.getNo())) {
                errMsg.append("科目編號").append(subject.getNo()).append("已被使用\n");

            } else if (MyApp.subjectTable.existByProperty(Constant.PRO_SUBJECT_NO, subject.getName())) {
                errMsg.append("科目名稱").append(subject.getNo()).append("已被使用\n");
            }
        }

        errMsg.append(v.chkSubjectNo(String.valueOf(subject.getNo())));
        errMsg.append(v.chkSubjectName(subject.getName()));

        if (subject.getCredit() != 0 && subject.getDebit() != 0) {
            errMsg.append("只能在借貸其中一方輸入金額\n");
        }

        if (errMsg.length() != 0) {
            Utility.getPlainDialog(context, activityTitle, errMsg.toString()).show();
            return true;
        }

        return false;
    }
}
