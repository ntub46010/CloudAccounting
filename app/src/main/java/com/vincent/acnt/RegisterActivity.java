package com.vincent.acnt;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Verifier;
import com.vincent.acnt.entity.RegisterProvider;
import com.vincent.acnt.entity.User;

public class RegisterActivity extends RegisterHelper {
    private TextInputLayout tilNickName, tilEmail, tilPwd1, tilPwd2;
    private EditText edtNickName, edtEmail, edtPwd1, edtPwd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        tilNickName = findViewById(R.id.tilNickName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPwd1 = findViewById(R.id.tilPwd1);
        tilPwd2 = findViewById(R.id.tilPwd2);
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd1 = findViewById(R.id.edtPwd1);
        edtPwd2 = findViewById(R.id.edtPwd2);
        edtNickName = findViewById(R.id.edtNickname);

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tilNickName.setError(null);
                tilEmail.setError(null);
                tilPwd1.setError(null);
                tilPwd2.setError(null);

                registerWithEmail(edtNickName.getText().toString(), edtEmail.getText().toString(), edtPwd1.getText().toString(), edtPwd2.getText().toString());
            }
        });

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void registerWithEmail(String nickName, String email, final String pwd1, String pwd2) {
        if (isNotValid(nickName, email, pwd1, pwd2)) {
            return;
        }

        dlgWaiting.show();

        MyApp.mAuth.createUserWithEmailAndPassword(email, pwd1)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = MyApp.mAuth.getCurrentUser();

                            User user = new User();
                            user.setId(currentUser.getUid());
                            user.setName(edtNickName.getText().toString());
                            user.setEmail(currentUser.getEmail());
                            user.setRegisterProvider(RegisterProvider.EMAIL.getProvider());

                            createUserDocument(
                                    user,
                                    new TaskListener() {
                                        @Override
                                        public void onFinish(User user) {
                                            Toast.makeText(context, "註冊成功", Toast.LENGTH_SHORT).show();
                                            prepareLogin();
                                        }
                                    }
                            );
                        } else {
                            Toast.makeText(context, "註冊失敗，請查看提示訊息", Toast.LENGTH_SHORT).show();
                            String errMsg =  task.getException().getMessage();

                            if (errMsg.equals("The email address is already in use by another account.")) {
                                tilEmail.setError("這個Email已經被註冊，請使用另一個");
                            }
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private boolean isNotValid(String nickName, String email, String pwd1, String pwd2) {
        Verifier v = new Verifier(context);
        tilNickName.setError(v.chkNickName(nickName));
        tilEmail.setError(v.chkEmail(email));
        tilPwd1.setError(v.chkPassword(pwd1));
        tilPwd2.setError(v.chkPasswordEqual(pwd1, pwd2));

        return tilNickName.getError() != null || tilEmail.getError() != null || tilPwd1.getError() != null || tilPwd2.getError() != null;
    }

}
