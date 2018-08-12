package com.vincent.acnt.data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.EntryUpdateActivity;

import static com.vincent.acnt.data.MyApp.KEY_BOOKS;
import static com.vincent.acnt.data.MyApp.KEY_ENTRIES;
import static com.vincent.acnt.data.MyApp.KEY_SUBJECTS;
import static com.vincent.acnt.data.MyApp.PRO_DATE;
import static com.vincent.acnt.data.MyApp.PRO_DOCUMENT_ID;
import static com.vincent.acnt.data.MyApp.PRO_MEMO;
import static com.vincent.acnt.data.MyApp.browsingBookDocumentId;
import static com.vincent.acnt.data.Utility.getPlainDialog;

public class EntryContextMenuHandler {
    private Context context;
    private Entry entry;
    private FirebaseFirestore db;

    public static final int MENU_UPDATE = Menu.FIRST, MENU_DELETE = Menu.FIRST + 1;


    public EntryContextMenuHandler(Context context, Entry entry, FirebaseFirestore db) {
        this.context = context;
        this.entry = entry;
        this.db = db;
    }

    public void updateEntry() {
        //調整日期格式
        StringBuffer date = new StringBuffer(String.valueOf(entry.getDate()));
        date.insert(4, "/");
        date.insert(7, "/");

        Intent it = new Intent(context, EntryUpdateActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(PRO_DATE, date.toString());
        bundle.putString(PRO_MEMO, entry.getMemo());
        bundle.putString(PRO_DOCUMENT_ID, entry.gainDocumentId());
        it.putExtras(bundle);
        it.putExtra(KEY_SUBJECTS, entry.getSubjects());

        context.startActivity(it);
    }

    public void deleteEntry(String dialogTitle, final ProgressBar prgBar, final RecyclerView recyEntry) {
        getPlainDialog(context, dialogTitle, "確定要刪除分錄「" + entry.getMemo() + "」嗎？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prgBar.setVisibility(View.VISIBLE);
                        recyEntry.setVisibility(View.GONE);

                        db.collection(KEY_BOOKS).document(browsingBookDocumentId).collection(KEY_ENTRIES).document(entry.gainDocumentId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "分錄刪除成功", Toast.LENGTH_SHORT).show();
                                        }else
                                            Toast.makeText(context, "分錄刪除失敗", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }
}
