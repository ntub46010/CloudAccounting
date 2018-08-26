package com.vincent.acnt;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.data.EntryElementView;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.data.Verifier;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.vincent.acnt.MyApp.MODE_CREATE;
import static com.vincent.acnt.MyApp.CODE_CREDIT;
import static com.vincent.acnt.MyApp.KEY_BOOKS;
import static com.vincent.acnt.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.MyApp.KEY_ENTRY;
import static com.vincent.acnt.MyApp.KEY_MODE;
import static com.vincent.acnt.MyApp.PRO_DATE;
import static com.vincent.acnt.MyApp.browsingBook;

public class EntryEditActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle;
    private Bundle bundle;
    private FirebaseFirestore db;

    private EditText edtDate, edtMemo, edtPs;
    private LinearLayout layEntryContainer;

    private ArrayAdapter<String> adpSubjectName;
    private List<EntryElementView> elementViews;
    private LayoutInflater inflater;
    private Calendar now;
    private String documentId;

    private Dialog dlgWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_edit);
        context = this;
        db = MyApp.getInstance().getFirestore();
        bundle = getIntent().getExtras();

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView txtBarTitle = toolbar.findViewById(R.id.txtToolbarTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView btnSubmit = findViewById(R.id.btnSubmit);
        edtDate = findViewById(R.id.edtDate);
        edtMemo = findViewById(R.id.edtMemo);
        edtPs = findViewById(R.id.edtPs);
        Button btnAddField = findViewById(R.id.btnAddField);
        layEntryContainer = findViewById(R.id.layEntryContainer);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitEntry();
            }
        });

        edtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });

        btnAddField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addElementView(null);
            }
        });

        adpSubjectName = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        inflater = LayoutInflater.from(context);
        elementViews = new ArrayList<>();
        now = Calendar.getInstance();

        if (bundle.getInt(KEY_MODE) == MODE_CREATE) {
            edtDate.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
            addElementView(null);
            addElementView(null);

            activityTitle = "新增分錄";
            txtBarTitle.setText(activityTitle);
        } else {
            Entry entry = (Entry) bundle.getSerializable(KEY_ENTRY);

            edtDate.setText(bundle.getString(PRO_DATE));
            edtMemo.setText(entry.getMemo());
            edtPs.setText(entry.getPs());

            for (Subject subject : entry.getSubjects()) {
                addElementView(subject);
            }

            documentId = entry.obtainDocumentId();
            activityTitle = "編輯分錄";
            txtBarTitle.setText(activityTitle);
        }

        dlgWaiting = Utility.getWaitingDialog(context);

        loadSubjects();
    }

    private void loadSubjects() {
        ArrayAdapter<String> adpSubjectName = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());

        for (long subjectId : MyApp.mapSubjectById.keySet()) {
            adpSubjectName.add(MyApp.mapSubjectById.get(subjectId).getName());
        }

        for (EntryElementView view : elementViews) {
            view.getActSubjectName().setAdapter(adpSubjectName);
        }
    }

    private void addElementView(Subject subject) {
        LinearLayout layElement = (LinearLayout) inflater.inflate(R.layout.content_entry_element, null);
        EntryElementView view = new EntryElementView(
                (AutoCompleteTextView) layElement.findViewById(R.id.actSubjectName),
                (Spinner) layElement.findViewById(R.id.spnDirection),
                (EditText) layElement.findViewById(R.id.edtAmount)
        );

        view.getActSubjectName().setAdapter(adpSubjectName);

        if (subject != null) {
            view.getActSubjectName().setText(subject.getName());
            view.getSpnDirection().setSelection(subject.getDebit() == 0 ? 0 : 1);
            view.getEdtAmount().setText(String.valueOf(Math.abs(subject.getCredit() - subject.getDebit())));
        }

        elementViews.add(view);
        layEntryContainer.addView(layElement);
    }

    private void showDateDialog() {
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

    private void submitEntry() {
        Entry entry = generateEntry();

        if (isNotValid(entry)) {
            return;
        }

        dlgWaiting.show();

        if (bundle.getInt(KEY_MODE) == MODE_CREATE) {
            createEntry(entry);
        } else {
            entry.defineDocumentId(documentId);
            updateEntry(entry);
        }
    }

    private Entry generateEntry() {
        Entry entry = new Entry();

        String date = edtDate.getText().toString();
        if (date.equals("")) {
            entry.setDate(Integer.parseInt("0"));
        } else {
            entry.setDate(Integer.parseInt(date.replace("/", "")));
        }

        entry.setMemo(edtMemo.getText().toString());
        entry.setPs(edtPs.getText().toString());

        Subject subject;
        for (EntryElementView view : elementViews) {
            subject = new Subject();
            subject.setName(view.getSubjectName());

            //未填寫會計科目的元素將被忽略
            if (subject.getName().equals("")) {
                continue;
            }

            //借貸方金額
            if (view.getDirection() == CODE_CREDIT) {
                subject.setCredit(view.getAmount());
            } else {
                subject.setDebit(view.getAmount());
            }

            entry.addSubject(subject);
        }

        return entry;
    }

    private boolean isNotValid(Entry entry) {
        if (entry.getSubjects().size() < 2 || (entry.calTotalCredit() == 0 && entry.calTotalDebit() == 0)) {
            Utility.getPlainDialog(context, activityTitle, "會計科目填寫不全").show();
            return true;
        }

        Verifier v = new Verifier(context);
        StringBuilder errMsg = new StringBuilder(256);
        StringBuilder unknownSubject = new StringBuilder(256);

        if (entry.getDate() == 0) {
            errMsg.append("日期未輸入\n");
        }

        errMsg.append(v.chkMemo(entry.getMemo()));
        errMsg.append(v.chkPs(entry.getPs()));

        if (entry.calDifference() != 0) {
            errMsg.append("借貸金額不平衡\n");
        }

        //檢查科目是否存在
        Subject subject;
        for (int i = 0, len = entry.getSubjects().size(); i < len; i++) {
            subject = entry.getSubjects().get(i);

            //分錄中的科目只儲存ID，不儲存名稱
            if (MyApp.mapSubjectByName.containsKey(subject.getName())) {
                subject.setId(MyApp.mapSubjectByName.get(subject.getName()).getId());
                subject.setName(null);
            } else {
                unknownSubject.append(subject.getName()).append("、");
            }
        }

        if (unknownSubject.length() > 0) {
            errMsg.append("\n以下科目不存在：").append(unknownSubject.toString().substring(0, unknownSubject.length() - 1));
        }

        if (errMsg.length() != 0) {
            Utility.getPlainDialog(context, activityTitle, errMsg.toString()).show();
            return true;
        }

        return false;
    }

    private void createEntry(Entry entry) {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_ENTRIES)
                .add(entry)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        dlgWaiting.dismiss();

                        if (task.isSuccessful()) {
                            Toast.makeText(context, "新增分錄成功", Toast.LENGTH_SHORT).show();
                            clearContent();
                            addElementView(null);
                            addElementView(null);
                            return;
                        }

                        Toast.makeText(context, "新增分錄失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEntry(Entry entry) {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId()).collection(KEY_ENTRIES).document(documentId)
                .set(entry)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "修改分錄成功", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        Toast.makeText(context, "修改分錄失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearContent() {
        elementViews.clear();
        edtMemo.setText(null);
        edtPs.setText(null);
        layEntryContainer.removeAllViews();
    }

}
