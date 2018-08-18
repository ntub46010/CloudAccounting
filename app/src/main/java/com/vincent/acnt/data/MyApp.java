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
    public static Book browsingBook;

    public static final String KEY_USERS = "Users";
    public static final String KEY_BOOKS = "Books";
    public static final String PRO_UID = "uid";
    public static final String PRO_BOOKS = "books";
    public static final String KEY_BOOK_NAME = "BookName";
    public static final String KEY_CREATOR = "Creator";
    public static final String PRO_MEMBER_AMOUNT = "memberAmount";
    public static final String PRO_MEMBER_IDS = "memberIds";
    public static final String PRO_ID = "id";
    public static final int CODE_CREDIT = 1;
    public static final int CODE_DEBIT = 2;
    public static final String[] CODE_TYPE = new String[5];
    public static final String PRO_NAME = "name";
    public static final String PRO_SUBJECT_ID = "subjectId";
    public static final String KEY_SUBJECT = "Subject";
    public static final String KEY_SUBJECTS = "Subjects";
    public static final String KEY_ENTRY = "Entry";
    public static final String KEY_ENTRIES = "Entries";
    public static final String PRO_DATE = "date";
    public static final String PRO_MEMO = "memo";
    public static final String PRO_DOCUMENT_ID = "documentId";

    public static final int CODE_QUIT_ACTIVITY = 999;

    @Override
    public void onCreate() {
        super.onCreate();

        res = getResources();
        db = FirebaseFirestore.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
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
