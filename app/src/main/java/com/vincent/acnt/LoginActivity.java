package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.MyApp;
import com.vincent.acnt.data.User;

import org.json.JSONObject;

import java.util.Random;

import static com.vincent.acnt.data.DataHelper.getWaitingDialog;
import static com.vincent.acnt.data.MyApp.KEY_USERS;
import static com.vincent.acnt.data.MyApp.PRO_UID;

public class LoginActivity extends AppCompatActivity {
    private Context context;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private EditText edtEmail, edtPwd;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private final int CODE_GOOGLE_LOGIN = 2;

    private Dialog dlgWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        db = ((MyApp) getApplication()).getFirestore();
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
        prepareGoogleButton();

        dlgWaiting = getWaitingDialog(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        prepareLogin(null);
    }

    private void prepareLogin(User user) {
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "未登入", Toast.LENGTH_SHORT).show();

            //Facebook sign out
            LoginManager.getInstance().logOut();
            FacebookSdk.sdkInitialize(context);
        }else {
            //沒登出或剛註冊
            if (!dlgWaiting.isShowing())
                dlgWaiting.show();

            if (user == null) //onStart、email login、email register
                user = new User(currentUser.getUid(), null, currentUser.getEmail());
            else //fbLogin、googleLogin
                user = new User(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());

            checkIsUserExist(user);
        }
    }

    private void checkIsUserExist(final User user) {
        //查詢該會員是否存在，若不存在則新增一個
        db.collection(KEY_USERS)
                .whereEqualTo(PRO_UID, user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot.isEmpty()) //剛註冊，會員不存在於資料庫
                                createUser(user);
                            else { //會員存在，抓取資料庫的資料
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                User user = documentSnapshot.toObject(User.class);
                                user.giveDocumentId(documentSnapshot.getId());
                                MyApp.getInstance().setUser(user);

                                dlgWaiting.dismiss();
                                startActivity(new Intent(context, MainActivity.class));
                                finish();
                            }
                        }else {
                            Toast.makeText(context, "檢查使用者狀態失敗", Toast.LENGTH_SHORT).show();
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private void createUser(final User user) {
        //email註冊隨機產生使用者名字
        if (user.getName() == null)
            user.setName("user" + String.valueOf(new Random().nextInt(8999) + 1000));

        db.collection(KEY_USERS)
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = task.getResult();
                            user.giveDocumentId(documentReference.getId());
                            MyApp.getInstance().setUser(user);

                            dlgWaiting.dismiss();
                            startActivity(new Intent(context, MainActivity.class));
                            finish();
                        }else {
                            Toast.makeText(context, "建立使用者失敗", Toast.LENGTH_SHORT).show();
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private void registerWithEmail(String account, String password) {
        dlgWaiting.show();
        mAuth.createUserWithEmailAndPassword(account, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "註冊成功", Toast.LENGTH_SHORT).show();
                            prepareLogin(null);
                        }else {
                            Toast.makeText(context, "註冊失敗", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private void loginWithEmail(String account, String password) {
        dlgWaiting.show();
        mAuth.signInWithEmailAndPassword(account, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "登入成功", Toast.LENGTH_SHORT).show();
                            prepareLogin(null);
                        }else {
                            Toast.makeText(context, "登入失敗", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private void prepareFacebookButton() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton facebookLoginButton = findViewById(R.id.login_button_facebook);
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlgWaiting.show();
            }
        });
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();

                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        loginWithCredential(FacebookAuthProvider.getCredential(accessToken.getToken()), object.optString("name"));
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
        SignInButton googleLoginButton = findViewById(R.id.login_button_google);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
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
            loginWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null), account.getDisplayName());
        }catch (ApiException e) {
            // Google Sign In failed
            dlgWaiting.dismiss();
            e.printStackTrace();
        }
    }

    private void loginWithCredential(AuthCredential credential, final String name) {
        //在Auth中登錄會員
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            currentUser = mAuth.getCurrentUser();
                            prepareLogin(new User(currentUser.getUid(), name, currentUser.getEmail()));
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

        if (requestCode == CODE_GOOGLE_LOGIN)
            handleGoogleSignResult(GoogleSignIn.getSignedInAccountFromIntent(data));
    }
}
