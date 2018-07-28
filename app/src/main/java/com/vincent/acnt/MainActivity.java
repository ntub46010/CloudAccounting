package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //宣告一個資料庫物件，對此物件操作等同於對雲端資料庫存取
        //會計科目(Subject)、交易(Entry)等等需與資料庫對映的物件類別，要有get方法，才可儲存物件屬性。要有set方法與空白建構式，才可還原為物件。否則會發生例外
        //且物件類別若為inner class，需宣告為static
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Button btnSubject = findViewById(R.id.btnSubject);
        btnSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SubjectActivity.class));
            }
        });

        Button btnJournal = findViewById(R.id.btnJournal);
        btnJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, JournalActivity.class));
            }
        });

        Button btnLedger = findViewById(R.id.btnLedger);
        btnLedger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, LedgerActivity.class));
            }
        });

        Button btnReport = findViewById(R.id.btnReport);
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ReportActivity.class));
            }
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(context, LoginActivity.class));
                finish();
            }
        });

    }
}
