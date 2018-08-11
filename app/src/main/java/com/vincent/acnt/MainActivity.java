package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.vincent.acnt.adapter.FeatureGridAdapter;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mAuth = FirebaseAuth.getInstance();

        GridView grdFeature = findViewById(R.id.grdFeature);
        grdFeature.setAdapter(new FeatureGridAdapter(context));
        grdFeature.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                executeFeature(position);
            }
        });

        ImageView btnBook = findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, BookListActivity.class));
            }
        });
    }

    private void executeFeature(int position) {
        switch (position) {
            /*
            case 0:
                startActivity(new Intent(context, SubjectActivity.class));
                break;
            case 1:
                startActivity(new Intent(context, JournalActivity.class));
                break;
            case 2:
                startActivity(new Intent(context, LedgerActivity.class));
                break;
            case 3:
                startActivity(new Intent(context, ReportActivity.class));
                break;
                */
            case 5:
                mAuth.signOut();
                startActivity(new Intent(context, LoginActivity.class));
                finish();
                break;
        }
    }
}
