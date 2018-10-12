package com.vincent.acnt;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Verifier;
import com.vincent.acnt.entity.RegisterProvider;
import com.vincent.acnt.entity.User;

public class SettingsActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "設定";

    private TextInputLayout tilNickName, tilEmail, tilPwd1, tilPwd2, tilPwd3;
    private EditText edtNickName, edtEmail, edtPwd1, edtPwd2, edtPwd3;

    private String loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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

        tilNickName = findViewById(R.id.tilNickName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPwd1 = findViewById(R.id.tilPwd1);
        tilPwd2 = findViewById(R.id.tilPwd2);
        tilPwd3 = findViewById(R.id.tilPwd3);
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd1 = findViewById(R.id.edtPwd1);
        edtPwd2 = findViewById(R.id.edtPwd2);
        edtPwd3 = findViewById(R.id.edtPwd3);
        edtNickName = findViewById(R.id.edtNickname);

        User user = MyApp.user;
        edtNickName.setText(user.getName());
        edtEmail.setText(user.getEmail());
        edtEmail.setEnabled(false);
        //edtPwd2.setText(null);
        //edtPwd3.setText(null);

        loginPassword = getSharedPreferences(getApplication().getPackageName(), MODE_PRIVATE)
                .getString(Constant.KEY_PASSWORD, null);

        if (user.getRegisterProvider().equals(RegisterProvider.EMAIL.getProvider())) {
            tilPwd1.setHint("原密碼");
            tilPwd2.setHint("新密碼");
            edtPwd1.setText(loginPassword);
        } else {
            tilPwd1.setVisibility(View.GONE);
            tilPwd2.setVisibility(View.GONE);
            tilPwd3.setVisibility(View.GONE);
        }


        Button btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tilNickName.setError(null);
                tilPwd1.setError(null);
                tilPwd2.setError(null);
                tilPwd3.setError(null);

                if (isNotValid()) {
                    Toast.makeText(context, "X", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "O", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private boolean isNotValid() {
        String nickName = edtNickName.getText().toString();
        String pwd1 = edtPwd1.getText().toString();
        String pwd2 = edtPwd2.getText().toString();
        String pwd3 = edtPwd3.getText().toString();

        Verifier v = new Verifier(context);
        tilNickName.setError(v.chkNickName(nickName));

        if (!pwd1.equals(loginPassword)) {
            tilPwd1.setError("原密碼不正確");
        }

        if (!pwd2.equals("") || !pwd3.equals("")) {
            tilPwd2.setError(v.chkPassword(pwd2));
            tilPwd3.setError(v.chkPasswordEqual(pwd2, pwd3));
        }

        return tilNickName.getError() != null || tilPwd1.getError() != null || tilPwd2.getError() != null || tilPwd3.getError() != null;
    }
}
