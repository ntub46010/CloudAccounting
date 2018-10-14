package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.vincent.acnt.accessor.TaskFinishListener;
import com.vincent.acnt.accessor.UserAccessor;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.data.Verifier;
import com.vincent.acnt.entity.RegisterProvider;
import com.vincent.acnt.entity.User;

public class SettingsActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "設定";

    private TextInputLayout tilNickName, tilEmail, tilPwd1, tilPwd2, tilPwd3;
    private EditText edtNickName, edtEmail, edtPwd1, edtPwd2, edtPwd3;

    private Dialog dlgWaiting;

    private UserAccessor accessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;
        accessor = new UserAccessor(MyApp.db.collection(Constant.KEY_USERS));

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

        if (user.getRegisterProvider().equals(RegisterProvider.EMAIL.getProvider())) {
            tilPwd1.setHint("原密碼");
            tilPwd2.setHint("新密碼");
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

                dlgWaiting.show();

                UpdateProfileRequest request = generateUpdateRequest();
                if (MyApp.user.getRegisterProvider().equals(RegisterProvider.EMAIL.getProvider())) {
                    if (Utility.isEmptyString(request.getOldPwd())) {
                        tilPwd1.setError("請輸入原密碼");
                        dlgWaiting.dismiss();
                        return;
                    }

                    processRequest(request);
                } else {
                    updateProfile(request);
                }
            }
        });

        dlgWaiting = Utility.getWaitingDialog(context);
    }

    private UpdateProfileRequest generateUpdateRequest() {
        String nickName = edtNickName.getText().toString();
        String pwd1 = edtPwd1.getText().toString();
        String pwd2 = edtPwd2.getText().toString();
        String pwd3 = edtPwd3.getText().toString();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickName(nickName);
        request.setOldPwd(pwd1);
        request.setNewPwd(pwd2);
        request.setNewPwdConfirm(pwd3);

        return request;
    }

    private void processRequest(final UpdateProfileRequest request) {
        MyApp.mAuth.signInWithEmailAndPassword(MyApp.user.getEmail(), request.getOldPwd())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            tilPwd1.setError("原密碼不正確");
                        }

                        Verifier v = new Verifier(context);
                        String newPwd = request.getNewPwd();
                        String newPwdConfirm = request.getNewPwdConfirm();

                        tilNickName.setError(v.chkNickName(request.getNickName()));
                        if (!Utility.isEmptyString(newPwd) || !Utility.isEmptyString(newPwdConfirm)) {
                            tilPwd2.setError(v.chkPassword(newPwd));
                            tilPwd3.setError(v.chkPasswordEqual(newPwd, newPwdConfirm));
                        }

                        if (isNotValid()) {
                            dlgWaiting.dismiss();
                        } else {
                            updateProfile(request);
                        }
                    }
                });
    }

    private boolean isNotValid() {
        return tilNickName.getError() != null || tilPwd1.getError() != null || tilPwd2.getError() != null || tilPwd3.getError() != null;
    }

    private void updateProfile(final UpdateProfileRequest request) {
        User user = MyApp.user;

        if (!Utility.isEmptyString(request.getNewPwd()) || !Utility.isEmptyString(request.getNewPwdConfirm())) {
            updatePassword(request);
        }

        accessor.patch(user.obtainDocumentId(), Constant.PRO_NAME, request.getNickName(), new TaskFinishListener() {
            @Override
            public void onSuccess() {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(request.getNickName())
                        .build();
                MyApp.mAuth.getCurrentUser().updateProfile(profileUpdates);

                edtPwd1.setText(null);
                edtPwd2.setText(null);
                edtPwd3.setText(null);

                Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show();
                dlgWaiting.dismiss();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "更新失敗", Toast.LENGTH_SHORT).show();
                dlgWaiting.dismiss();
            }
        });
    }

    private void updatePassword(UpdateProfileRequest request) {
        MyApp.mAuth.getCurrentUser()
                .updatePassword(request.getNewPwd())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "密碼更新失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private class UpdateProfileRequest {
        private String nickName, oldPwd, newPwd, newPwdConfirm;

        public UpdateProfileRequest() {

        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getOldPwd() {
            return oldPwd;
        }

        public void setOldPwd(String oldPwd) {
            this.oldPwd = oldPwd;
        }

        public String getNewPwd() {
            return newPwd;
        }

        public void setNewPwd(String newPwd) {
            this.newPwd = newPwd;
        }

        public String getNewPwdConfirm() {
            return newPwdConfirm;
        }

        public void setNewPwdConfirm(String newPwdConfirm) {
            this.newPwdConfirm = newPwdConfirm;
        }
    }
}
