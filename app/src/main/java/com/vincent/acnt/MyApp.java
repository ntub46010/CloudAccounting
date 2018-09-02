package com.vincent.acnt;

import android.app.Application;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.util.LongSparseArray;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.Subject;
import com.vincent.acnt.entity.User;

import java.util.Map;
import java.util.TreeMap;

public class MyApp extends Application {
    public static Resources res;
    public static FirebaseFirestore db;
    public static FirebaseAuth mAuth;

    public static User user;
    public static Book browsingBook;

    public static LongSparseArray<Subject> mapSubjectById = new LongSparseArray<>();
    public static Map<String, Subject> mapSubjectByNo = new TreeMap<>();
    public static Map<String, Subject> mapSubjectByName = new ArrayMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        res = getResources();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        for (int i = 0; i < 5; i++) {
            Constant.CODE_TYPE[i] = String.valueOf(i + 1);
        }

    }
}
