package com.vincent.acnt;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.RegisterProvider;
import com.vincent.acnt.entity.User;

import org.json.JSONObject;

public class LoginActivity extends RegisterHelper {
    private TextInputLayout tilEmail, tilPwd;
    private EditText edtEmail, edtPwd;

    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private final int CODE_GOOGLE_LOGIN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        tilEmail = findViewById(R.id.tilEmail);
        tilPwd = findViewById(R.id.tilPwd);
        edtEmail = findViewById(R.id.edtEmail);
        edtPwd = findViewById(R.id.edtPwd1);
        TextView txtForgetPwd = findViewById(R.id.txtForgetPwd);

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, RegisterActivity.class));
            }
        });

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tilEmail.setError(null);
                tilPwd.setError(null);

                loginWithEmail(edtEmail.getText().toString(), edtPwd.getText().toString());
            }
        });

        prepareFacebookButton();
        prepareGoogleButton();

        txtForgetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareForgetPasswordDialog();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        prepareLogin();
    }

    private void loginWithEmail(String account, final String password) {
        dlgWaiting.show();
        MyApp.mAuth.signInWithEmailAndPassword(account, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            prepareLogin();
                        } else {
                            Toast.makeText(context, "帳號或密碼錯誤，請確認後再嘗試登入", Toast.LENGTH_SHORT).show();
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private void prepareFacebookButton() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        Button btnFB = findViewById(R.id.btnFBLogin);
        btnFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgWaiting.show();
                LoginManager.getInstance().logInWithReadPermissions((Activity) context, null);
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        final AccessToken accessToken = loginResult.getAccessToken();

                        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                loginWithCredential(FacebookAuthProvider.getCredential(accessToken.getToken()),
                                        object.optString("name"), RegisterProvider.FACEBOOK);
                            }
                        });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(context, "取消登入", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void prepareGoogleButton() {
        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgWaiting.show();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, CODE_GOOGLE_LOGIN);
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    private void handleGoogleSignResult(Task<GoogleSignInAccount> task) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            loginWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null),
                    account.getDisplayName(), RegisterProvider.GOOGLE);
        } catch (ApiException e) {
            // Google Sign In failed
            dlgWaiting.dismiss();
            e.printStackTrace();
        }
    }

    private void loginWithCredential(AuthCredential credential, final String name, final RegisterProvider registerProvider) {
        //在Auth中登錄會員
        MyApp.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            currentUser = MyApp.mAuth.getCurrentUser();

                            User user = new User();
                            user.setId(String.valueOf(currentUser.getUid()));
                            user.setName(name);
                            user.setEmail(currentUser.getEmail());
                            user.setRegisterProvider(registerProvider.getProvider());

                            findUserDocument(user,
                                    new TaskListener() {
                                        @Override
                                        public void onFinish(User user) {
                                            prepareLogin();
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void prepareForgetPasswordDialog() {
        View[] layEditText = Utility.getEditTextLayout(context, edtEmail.getText().toString());
        LinearLayout layout = (LinearLayout) layEditText[0];
        final EditText edtWantedEmail = (EditText) layEditText[1];

        Utility.getPlainDialog(context, "忘記密碼", "請填寫您的Email，送出後至信箱收取更改密碼的信件\n（此功能僅限使用Email註冊者使用）")
                .setView(layout)
                .setPositiveButton("送出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = edtWantedEmail.getText().toString();
                        if (Utility.isEmptyString(email)) {
                            Toast.makeText(context, "未填寫Email", Toast.LENGTH_SHORT).show();
                        } else {
                            sendPasswordResetEmail(email);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void sendPasswordResetEmail(String email) {
        MyApp.mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "已送出，請至信箱收取信件", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "請求失敗，請重新嘗試", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_GOOGLE_LOGIN) {
            handleGoogleSignResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }
    }
}
