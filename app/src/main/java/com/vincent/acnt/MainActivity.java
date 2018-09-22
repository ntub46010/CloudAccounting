package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.firestore.ListenerRegistration;
import com.vincent.acnt.accessor.RetrieveEntityListener;
import com.vincent.acnt.accessor.UserAccessor;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Entity;
import com.vincent.acnt.entity.User;

public class MainActivity extends AppCompatActivity {
    private Context context;

    private UserAccessor accessor;

    private ListenerRegistration regUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        accessor = new UserAccessor(MyApp.db.collection(Constant.KEY_USERS));

        ImageButton btnBook = findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, BookListActivity.class));
            }
        });

        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.mAuth.signOut();
                startActivity(new Intent(context, LoginActivity.class));
                finish();
            }
        });

        loadMyUser();
    }

    private void loadMyUser() {
        regUser = accessor.observeUserById(MyApp.user.obtainDocumentId(), new RetrieveEntityListener() {
            @Override
            public void onRetrieve(Entity entity) {
                MyApp.user = (User) entity;
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    @Override
    public void onDestroy() {
        regUser.remove();
        super.onDestroy();
    }

}
