package com.vincent.acnt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.vincent.acnt.entity.User;

import javax.annotation.Nullable;

import static com.vincent.acnt.MyApp.KEY_USERS;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        db = MyApp.getInstance().getFirestore();
        mAuth = FirebaseAuth.getInstance();

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
                mAuth.signOut();
                startActivity(new Intent(context, LoginActivity.class));
                finish();
            }
        });

        db.collection(KEY_USERS).document(MyApp.getInstance().getUser().obtainDocumentId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        User user = documentSnapshot.toObject(User.class);
                        user.defineDocumentId(documentSnapshot.getId());
                        MyApp.getInstance().setUser(user);
                    }
                });
    }

}
