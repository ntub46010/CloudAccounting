package com.vincent.acnt.data;

import android.app.Application;
import android.content.res.Resources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApp extends Application {
    private static MyApp myApp;
    private Resources res;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private User user;

    public static String KEY_USERS = "Users";
    public static String PRO_UID = "uid";
    public static int CODE_CREDIT = 1;
    public static int CODE_DEBIT = 2;
    public static String[] CODE_TYPE = new String[5];
    public static String PRO_NAME = "name";
    public static String PRO_SUBJECT_ID = "subjectId";
    public static String KEY_SUBJECT = "Subject";
    public static String KEY_SUBJECTS = "Subjects";
    public static String KEY_ENTRY = "Entry";
    public static String KEY_ENTRIES = "Entries";
    public static String PRO_DATE = "date";
    public static String PRO_MEMO = "memo";
    public static String PRO_DOCUMENT_ID = "documentId";

    @Override
    public void onCreate() {
        super.onCreate();

        res = getResources();
        db = FirebaseFirestore.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance();

        for (int i = 0; i < 5; i++)
            CODE_TYPE[i] = String.valueOf(i + 1);

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
