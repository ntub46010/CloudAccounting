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
import com.vincent.acnt.data.Verifier;
import com.vincent.acnt.entity.User;

public class RegisterActivity extends RegisterHelper {
    private TextInputLayout tilNickName, tilEmail, tilPwd;
    private EditText edtNickName, edtEmail, edtPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        tilNickName = findViewById(R.id.tilNickName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPwd = findViewById(R.id.tilPwd);
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd = findViewById(R.id.edtPwd);
        edtNickName = findViewById(R.id.edtNickname);

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerWithEmail(edtNickName.getText().toString(), edtEmail.getText().toString(), edtPwd.getText().toString());
            }
        });
    }

    private void registerWithEmail(String nickName, String email, String password) {
        if (isNotValid(nickName, email, password)) {
            return;
        }

        dlgWaiting.show();

        MyApp.mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = MyApp.mAuth.getCurrentUser();

                            User user = new User();
                            user.setUid(currentUser.getUid());
                            user.setName(edtNickName.getText().toString());
                            user.setEmail(currentUser.getEmail());

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
                            Toast.makeText(context, "註冊失敗", Toast.LENGTH_SHORT).show();
                            String errMsg =  task.getException().getMessage();

                            if (errMsg.equals("The email address is already in use by another account.")) {
                                tilEmail.setError("這個Email已經被註冊，請使用另一個");
                            }
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private boolean isNotValid(String nickName, String email, String password) {
        Verifier v = new Verifier(context);
        tilNickName.setError(v.chkNickName(nickName));
        tilEmail.setError(v.chkEmail(email));
        tilPwd.setError(v.chkPassword(password));

        return tilNickName.getError() != null || tilEmail.getError() != null || tilPwd.getError() != null;
    }

}
