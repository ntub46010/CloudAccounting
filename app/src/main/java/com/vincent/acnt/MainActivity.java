package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.User;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private Context context;

    private ListenerRegistration lsrUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

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

        lsrUser = MyApp.db.collection(Constant.KEY_USERS).document(MyApp.user.obtainDocumentId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        User user = documentSnapshot.toObject(User.class);
                        user.defineDocumentId(documentSnapshot.getId());
                        MyApp.user = user;
                    }
                });
    }

    @Override
    public void onDestroy() {
        lsrUser.remove();
        super.onDestroy();
    }

}
