package com.vincent.acnt;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vincent.acnt.data.Entry;
import com.vincent.acnt.data.Subject;

import java.text.NumberFormat;
import java.util.Locale;

import static com.vincent.acnt.data.Utility.getSubjectColor;
import static com.vincent.acnt.data.MyApp.KEY_ENTRY;

public class EntryDetailActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "分錄詳情";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);
        context = this;

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

        Entry entry = (Entry) getIntent().getExtras().getSerializable(KEY_ENTRY);

        TextView txtDate = findViewById(R.id.txtDate);
        TextView txtMemo = findViewById(R.id.txtMemo);
        TextView txtPs = findViewById(R.id.txtPs);
        LinearLayout layContainer = findViewById(R.id.layElement);

        String date = String.valueOf(entry.getDate());
        txtDate.setText(String.format("%s/%s/%s",
                        date.substring(0, 4),
                        date.substring(4, 6),
                        date.substring(6, 8)
        ));
        txtMemo.setText(entry.getMemo());
        txtPs.setText(entry.getPs());

        LayoutInflater inflater = LayoutInflater.from(context);
        TextView txtSubject, txtCredit, txtDebit;
        for (Subject subject : entry.getSubjects()) {
            LinearLayout layElement = (LinearLayout) inflater.inflate(R.layout.lst_entry_element_detail, null);
            txtSubject = layElement.findViewById(R.id.txtSubject);
            txtCredit = layElement.findViewById(R.id.txtCredit);
            txtDebit = layElement.findViewById(R.id.txtDebit);

            txtSubject.setText(subject.getName());
            txtSubject.setTextColor(getSubjectColor(subject));

            if (subject.getDebit() == 0)
                txtCredit.setText(NumberFormat.getNumberInstance(Locale.US).format(subject.getCredit()));
            else
                txtDebit.setText(NumberFormat.getNumberInstance(Locale.US).format(subject.getDebit()));

            layContainer.addView(layElement);
        }
    }
}
