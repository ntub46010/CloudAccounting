package com.vincent.acnt.data;

import android.app.Application;
import android.content.res.Resources;

import com.google.firebase.firestore.FirebaseFirestore;

public class MyApp extends Application {
    private Resources res;
    private FirebaseFirestore db;

    public static int CODE_CREDIT = 1;
    public static int CODE_DEBIT = 2;
    public static final String CODE_ASSET = "1";
    public static final String CODE_LIABILITY = "2";
    public static final String CODE_CAPITAL = "3";
    public static final String CODE_REVENUE = "4";
    public static final String CODE_EXPANSE = "5";
    public static String PRO_STAMP = "stamp";
    public static String PRO_NAME = "name";
    public static String PRO_SUBJECT_ID = "subjectId";
    public static String KEY_SUBJECTS = "Subjects";
    public static String PRO_SUBJECTS = "subjects";
    public static String KEY_ENTRY = "Entry";
    public static String KEY_ENTRIES = "Entries";
    public static String PRO_DATE = "date";
    public static String PRO_MEMO = "memo";
    public static String PRO_DOCUMENT_ID = "documentId";
    public static String KEY_REPORT_ITEMS = "ReportItems";

    @Override
    public void onCreate() {
        super.onCreate();

        res = getResources();
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getFirestore() {
        return db;
    }

    public Resources getResource() {
        return res;
    }
}
