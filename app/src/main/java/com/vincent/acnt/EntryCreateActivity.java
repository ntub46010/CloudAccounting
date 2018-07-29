package com.vincent.acnt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.vincent.acnt.data.MyApp;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.vincent.acnt.data.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.data.MyApp.KEY_USERS;

public class EntryCreateActivity extends EntryEditActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        layout = R.layout.activity_entry_edit;
        activityTitle = "新建會計分錄";
        super.onCreate(savedInstanceState);
        addElementView();
        addElementView();

        edtDate.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareEntry();
                addEntry();
            }
        });

        now.setTime(new Date());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void addEntry() {
        if (!isValid(entry))
            return;

        dlgWaiting.show();
        db.collection(KEY_USERS).document(MyApp.getInstance().getUser().gainDocumentId()).collection(KEY_ENTRIES)
                .add(entry)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "新增分錄成功", Toast.LENGTH_SHORT).show();
                            clearContent();
                            addElementView();
                            addElementView();
                            setResult(1);
                        }else
                            Toast.makeText(context, "新增分錄失敗", Toast.LENGTH_SHORT).show();

                        dlgWaiting.dismiss();
                    }
                });
    }
}
