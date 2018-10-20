package com.vincent.acnt;

import android.app.Application;
import android.content.res.Resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.data.ObjectMapTable;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.Subject;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.List;

public class MyApp extends Application {
    public static Resources res;
    public static FirebaseFirestore db;
    public static FirebaseAuth mAuth;

    public static User user;
    public static Book browsingBook;

    public static ObjectMapTable<Long, Subject> subjectTable = new ObjectMapTable<>(Subject.class, "id");
    public static List<Entry> thisMonthEntries = new ArrayList<>(64);

    @Override
    public void onCreate() {
        super.onCreate();

        res = getResources();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
    }
}
