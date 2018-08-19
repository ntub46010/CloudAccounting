package com.vincent.acnt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vincent.acnt.data.EntryElementView;
import com.vincent.acnt.entity.Subject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.vincent.acnt.MyApp.KEY_BOOKS;
import static com.vincent.acnt.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.MyApp.PRO_DATE;
import static com.vincent.acnt.MyApp.PRO_DOCUMENT_ID;
import static com.vincent.acnt.MyApp.PRO_MEMO;
import static com.vincent.acnt.MyApp.browsingBook;

public class EntryUpdateActivity extends EntryEditActivity {
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        layout = R.layout.activity_entry_edit;
        activityTitle = "修改會計分錄";
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        edtDate.setText(bundle.getString(PRO_DATE));
        edtMemo.setText(bundle.getString(PRO_MEMO));

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareEntry();
                updateEntry();
            }
        });

        ArrayList<Subject> subjects = (ArrayList<Subject>) getIntent().getSerializableExtra(KEY_SUBJECTS);
        for (Subject subject :  subjects)
            addElementView(subject);

        try {
            now.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(bundle.getString(PRO_DATE)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        documentId = bundle.getString(PRO_DOCUMENT_ID);
    }

    private void addElementView(Subject subject) {
        LinearLayout layElement = (LinearLayout) inflater.inflate(R.layout.content_entry_element, null);
        EntryElementView view = new EntryElementView(
                (AutoCompleteTextView) layElement.findViewById(R.id.actSubject),
                (Spinner) layElement.findViewById(R.id.spnDirection),
                (EditText) layElement.findViewById(R.id.edtAmount));

        view.getActSubject().setAdapter(adpSubjectName);
        view.getActSubject().setText(subject.getName());
        view.getSpnDirection().setSelection(subject.getDebit() == 0 ? 0 : 1);
        view.getEdtAmount().setText(String.valueOf(subject.getCredit() == 0 ? subject.getDebit() : subject.getCredit()));

        elementViews.add(view);
        layEntry.addView(layElement);
    }

    private void updateEntry() {
        if (isNotValid(entry))
            return;

        dlgWaiting.show();
        db.collection(KEY_BOOKS).document(browsingBook.gainDocumentId()).collection(KEY_ENTRIES).document(documentId)
                .set(entry)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "修改分錄成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }else
                            Toast.makeText(context, "修改分錄失敗", Toast.LENGTH_SHORT).show();

                        dlgWaiting.dismiss();
                    }
                });
    }
}
