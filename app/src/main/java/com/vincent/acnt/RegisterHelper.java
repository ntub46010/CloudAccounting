package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.List;

public class RegisterHelper extends AppCompatActivity {
    protected Context context;
    protected FirebaseFirestore db;
    protected FirebaseAuth mAuth;
    protected FirebaseUser currentUser;

    protected EditText edtEmail, edtPwd;

    protected Dialog dlgWaiting;

    public interface TaskListener { void onFinish(User user); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        db = ((MyApp) getApplication()).getFirestore();
        mAuth = FirebaseAuth.getInstance();

        dlgWaiting = Utility.getWaitingDialog(context);
    }

    protected void prepareLogin() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "未登入", Toast.LENGTH_SHORT).show();

            //Facebook sign out
            LoginManager.getInstance().logOut();
            FacebookSdk.sdkInitialize(context);
        } else {
            if (!dlgWaiting.isShowing()) {
                dlgWaiting.show();
            }

            findUserDocument(new User(currentUser.getUid()),
                    new TaskListener() {
                        @Override
                        public void onFinish(User user) {
                            dlgWaiting.dismiss();

                            MyApp.getInstance().setUser(user);
                            startActivity(new Intent(context, MainActivity.class));
                            finish();
                        }
                    });
        }
    }

    protected void findUserDocument(final User user, final TaskListener taskListener) {
        db.collection(Constant.KEY_USERS)
                .whereEqualTo(Constant.PRO_UID, user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                            if (documentSnapshots.isEmpty()) {
                                if (!dlgWaiting.isShowing()) {
                                    dlgWaiting.show();
                                }

                                createUserDocument(user, taskListener);
                            } else {
                                User userDocument = documentSnapshots.get(0).toObject(User.class);
                                userDocument.defineDocumentId(documentSnapshots.get(0).getId());

                                taskListener.onFinish(userDocument);
                            }
                        } else {
                            Toast.makeText(context, "檢查使用者狀態失敗", Toast.LENGTH_SHORT).show();
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    protected void createUserDocument(final User user, final TaskListener taskListener) {
        user.setBooks(new ArrayList<String>());

        db.collection(Constant.KEY_USERS)
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = task.getResult();
                            user.defineDocumentId(documentReference.getId());

                            taskListener.onFinish(user);
                        }else
                            Toast.makeText(context, "建立使用者失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
