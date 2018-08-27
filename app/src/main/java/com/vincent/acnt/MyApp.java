package com.vincent.acnt;

import android.app.Application;
import android.content.res.Resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.Subject;
import com.vincent.acnt.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MyApp extends Application {
    private static MyApp myApp;
    private Resources res;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private User user;
    public static Book browsingBook;
    public static Map<Long, Subject> mapSubjectById = new TreeMap<>();
    public static Map<String, Subject> mapSubjectByName = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        res = getResources();
        db = FirebaseFirestore.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
        mAuth = FirebaseAuth.getInstance();

        for (int i = 0; i < 5; i++) {
            Constant.CODE_TYPE[i] = String.valueOf(i + 1);
        }

        myApp = this;
    }

    public static MyApp getInstance() {
        return myApp;
    }

    public FirebaseFirestore getFirestore() {
        return db;
    }

    public Resources getResource() {
        return res;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
