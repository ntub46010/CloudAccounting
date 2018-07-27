package com.vincent.acnt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.Entry;
import com.vincent.acnt.data.EntryElementView;
import com.vincent.acnt.data.MyApp;
import com.vincent.acnt.data.Subject;
import com.vincent.acnt.data.Verifier;

import java.util.ArrayList;
import java.util.Calendar;

import javax.annotation.Nullable;

import static com.vincent.acnt.data.DataHelper.getPlainDialog;
import static com.vincent.acnt.data.MyApp.CODE_CREDIT;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.data.MyApp.PRO_SUBJECT_ID;

public class EntryEditActivity  extends AppCompatActivity {
    protected Context context;
    protected int layout;
    protected String activityTitle;
    protected LayoutInflater inflater;
    protected FirebaseFirestore db;

    protected ImageView btnSubmit;
    protected EditText edtDate, edtMemo, edtPs;
    protected LinearLayout layEntry;
    protected Button btnElementView;

    protected ArrayList<EntryElementView> elementViews;
    protected Entry entry;
    private ArrayList<Long> subjectStamps;
    private ArrayList<String> subjectIds, subjectNames;
    protected ArrayAdapter<String> adpSubjectName;

    protected Calendar now;
    protected Dialog dlgUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);
        db = ((MyApp) getApplication()).getFirestore();
        setResult(0);

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

        btnSubmit = findViewById(R.id.btnSubmit);
        edtDate = findViewById(R.id.edtDate);
        edtMemo = findViewById(R.id.edtMemo);
        edtPs = findViewById(R.id.edtPs);
        layEntry = findViewById(R.id.layEntry);
        btnElementView = findViewById(R.id.btnAddField);

        edtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dlgDate = new DatePickerDialog(
                        context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String sMonth = month < 9 ? "0" + String.valueOf(month + 1) : String.valueOf(month);
                                String sDay = dayOfMonth < 10 ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                                edtDate.setText(String.format("%s/%s/%s", String.valueOf(year), sMonth, sDay));
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dlgDate.setTitle(activityTitle);
                dlgDate.setMessage("請選擇交易發生日期");
                dlgDate.show();
            }
        });

        btnElementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addElementView();
            }
        });

        inflater = LayoutInflater.from(context);
        elementViews = new ArrayList<>();

        now = Calendar.getInstance();

        dlgUpload = new Dialog(context);
        dlgUpload.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlgUpload.setContentView(R.layout.dlg_waiting);
        dlgUpload.setCancelable(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        //取得科目編號、名稱、戳記
        db.collection(KEY_SUBJECTS).orderBy(PRO_SUBJECT_ID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                subjectIds = new ArrayList<>();
                subjectNames = new ArrayList<>();
                subjectStamps = new ArrayList<>();
                Subject subject;

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    subject = documentSnapshot.toObject(Subject.class);
                    subjectIds.add(subject.getSubjectId());
                    subjectNames.add(subject.getName());
                    subjectStamps.add(subject.getStamp());
                }

                //設定自動完成科目名稱
                adpSubjectName = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, subjectNames);
                for (EntryElementView view : elementViews)
                    view.getActSubject().setAdapter(adpSubjectName);
            }
        });
    }

    protected void addElementView() {
        LinearLayout layElement = (LinearLayout) inflater.inflate(R.layout.content_entry_element, null);
        EntryElementView view = new EntryElementView(
                (AutoCompleteTextView) layElement.findViewById(R.id.actSubject),
                (Spinner) layElement.findViewById(R.id.spnDirection),
                (EditText) layElement.findViewById(R.id.edtAmount));

        view.getActSubject().setAdapter(adpSubjectName);

        elementViews.add(view);
        layEntry.addView(layElement);
    }

    protected void prepareEntry() {
        String date = edtDate.getText().toString();
        if (date.equals(""))
            date = "0";
        else
            date = date.replace("/", "");

        entry = new Entry(
                Integer.parseInt(date),
                edtMemo.getText().toString(),
                edtPs.getText().toString()
        );

        Subject subject;
        for (EntryElementView view : elementViews) {
            subject = new Subject();
            subject.setName(view.getSubject());

            //未填寫會計科目的元素將被忽略
            if (subject.getName().equals(""))
                continue;

            //借貸方金額
            if (view.getDirection() == CODE_CREDIT)
                subject.setCredit(view.getAmount());
            else
                subject.setDebit(view.getAmount());

            //設置戳記與編號，當科目名稱更改時，能憑此戳記找到對應科目
            for (int i = 0; i < subjectNames.size(); i++) {
                if (subject.getName().equals(subjectNames.get(i))) {
                    subject.setStamp(subjectStamps.get(i));
                    subject.setSubjectId(subjectIds.get(i));
                }
            }

            entry.addSubject(subject);
        }
    }

    protected boolean isValid(Entry entry) {
        if (entry.getSubjects().size() < 2 || (entry.calTotalCredit() == 0 && entry.calTotalDebit() == 0)) {
            getPlainDialog(context, activityTitle, "會計科目填寫不全").show();
            return false;
        }

        Verifier v = new Verifier(context);
        StringBuffer errMsg = new StringBuffer();
        StringBuffer subjectExist = new StringBuffer();

        if (entry.getDate() == 0)
            errMsg.append("日期未輸入\n");

        errMsg.append(v.chkMemo(entry.getMemo()));
        errMsg.append(v.chkPs(entry.getPs()));

        Subject subject;
        for (int i = 0; i < entry.getSubjects().size(); i++) {
            subject = entry.getSubjects().get(i);

            //檢查科目名稱，若無subjectId代表該科目不存在
            if (subject.getStamp() == 0)
                subjectExist.append(String.valueOf(i + 1)).append("、");
        }

        //調整科目名稱錯誤訊息文字
        if (subjectExist.length() > 0)
            errMsg.append(getString(R.string.chk_subject_name_wrong, subjectExist.substring(0, subjectExist.length() - 1)));

        if (entry.calDifference() != 0)
            errMsg.append("借貸金額不平衡\n");

        if (errMsg.length() == 0)
            return true;
        else {
            getPlainDialog(context, activityTitle, errMsg.toString()).show();
            return false;
        }
    }

    protected void clearContent() {
        elementViews.clear();
        edtDate.setText(null);
        edtMemo.setText(null);
        edtPs.setText(null);
        layEntry.removeAllViews();
    }

}
