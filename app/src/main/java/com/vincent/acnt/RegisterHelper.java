package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseUser;
import com.vincent.acnt.accessor.RetrieveEntityListener;
import com.vincent.acnt.accessor.UserAccessor;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Entity;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;

public class RegisterHelper extends AppCompatActivity {
    protected Context context;
    protected FirebaseUser currentUser;

    protected EditText edtEmail, edtPwd;

    protected Dialog dlgWaiting;

    protected UserAccessor accessor;

    public interface TaskListener { void onFinish(User user); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        accessor = new UserAccessor(MyApp.db.collection(Constant.KEY_USERS));
        
        dlgWaiting = Utility.getWaitingDialog(context);
    }

    protected void prepareLogin() {
        currentUser = MyApp.mAuth.getCurrentUser();
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

                            MyApp.user = user;
                            startActivity(new Intent(context, MainActivity.class));
                            finish();
                        }
                    });
        }
    }

    protected void findUserDocument(final User user, final TaskListener taskListener) {
        accessor.loadUserById(user.getId(), new RetrieveEntityListener() {
            @Override
            public void onRetrieve(Entity entity) {
                if (entity == null) {
                    if (!dlgWaiting.isShowing()) {
                        dlgWaiting.show();
                    }

                    //若無該使用者則建立一個
                    createUserDocument(user, taskListener);
                } else {
                    taskListener.onFinish((User) entity);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "檢查使用者狀態失敗", Toast.LENGTH_SHORT).show();
                dlgWaiting.dismiss();
            }
        });
    }

    protected void createUserDocument(final User user, final TaskListener taskListener) {
        user.setBooks(new ArrayList<String>());

        accessor.create(user, new RetrieveEntityListener() {
            @Override
            public void onRetrieve(Entity entity) {
                taskListener.onFinish((User) entity);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "建立使用者失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
