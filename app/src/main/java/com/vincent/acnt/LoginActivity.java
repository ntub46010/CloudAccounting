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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText edtEmail, edtPwd;
    private LoginButton facebookLoginButton;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPwd = findViewById(R.id.edtPwd);

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerWithEmail(edtEmail.getText().toString(), edtPwd.getText().toString());
            }
        });

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithEmail(edtEmail.getText().toString(), edtPwd.getText().toString());
            }
        });

        prepareFacebookButton();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkLoginStatus();
    }

    private void registerWithEmail(String account, String password) {
        mAuth.createUserWithEmailAndPassword(account, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "註冊成功", Toast.LENGTH_SHORT).show();
                            checkLoginStatus();
                        }else {
                            Toast.makeText(context, "註冊失敗", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loginWithEmail(String account, String password) {
        mAuth.signInWithEmailAndPassword(account, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "登入成功", Toast.LENGTH_SHORT).show();
                            checkLoginStatus();
                        }else {
                            Toast.makeText(context, "登入失敗", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkLoginStatus() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "未登入", Toast.LENGTH_SHORT).show();
            LoginManager.getInstance().logOut();
            FacebookSdk.sdkInitialize(context);
        }else {
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }
    }

    private void prepareFacebookButton() {
        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton = findViewById(R.id.login_button);
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginWithCredential(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()));
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

    private void loginWithCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            currentUser = mAuth.getCurrentUser();
                            checkLoginStatus();
                        }else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
