package com.vincent.acnt.data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vincent.acnt.EntryEditActivity;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.accessor.EntryAccessor;
import com.vincent.acnt.accessor.TaskFinishListener;
import com.vincent.acnt.entity.Entry;

public class EntryContextMenuHandler {
    private Context context;
    private Entry entry;
    private EntryAccessor accessor;

    public EntryContextMenuHandler(Context context, Entry entry) {
        this.context = context;
        this.entry = entry;
        this.accessor = new EntryAccessor(MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .collection(Constant.KEY_ENTRIES));
    }

    public void updateEntry() {
        //調整日期格式
        StringBuilder date = new StringBuilder(String.valueOf(entry.getDate()));
        date.insert(4, "/");
        date.insert(7, "/");

        Intent it = new Intent(context, EntryEditActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt(Constant.KEY_MODE, Constant.MODE_UPDATE);
        bundle.putString(Constant.PRO_DATE, date.toString());
        bundle.putSerializable(Constant.KEY_ENTRY, entry);
        it.putExtras(bundle);

        context.startActivity(it);
    }

    public void deleteEntry(String dialogTitle, final ProgressBar prgBar, final RecyclerView recyEntry) {
        Utility.getPlainDialog(context, dialogTitle, "確定要刪除分錄「" + entry.getMemo() + "」嗎？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prgBar.setVisibility(View.VISIBLE);
                        recyEntry.setVisibility(View.INVISIBLE);

                        accessor.delete(entry.obtainDocumentId(), new TaskFinishListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(context, "分錄刪除成功", Toast.LENGTH_SHORT).show();
                                prgBar.setVisibility(View.GONE);
                                recyEntry.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(context, "分錄刪除失敗", Toast.LENGTH_SHORT).show();
                                prgBar.setVisibility(View.GONE);
                                recyEntry.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                })
                .setNegativeButton("否", null)
                .show();
    }
}
