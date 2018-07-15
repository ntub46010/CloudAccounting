package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //宣告一個資料庫物件，對此物件操作等同於對雲端資料庫存取
        //會計科目(Subject)、交易(Entry)等等需與資料庫對映的物件類別，要有get方法，才可儲存物件屬性。要有set方法與空白建構式，才可還原為物件。否則會發生例外
        //且物件類別若為inner class，需宣告為static
        db = FirebaseFirestore.getInstance();

        Button btnSubject = findViewById(R.id.btnSubject);
        btnSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SubjectActivity.class));
            }
        });

        Button btnShowJournal = findViewById(R.id.btnShowJournal);
        btnShowJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, JournalActivity.class));
            }
        });

    }

}
