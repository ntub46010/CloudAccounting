package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.acnt.adapter.BookOptionListAdapter;
import com.vincent.acnt.entity.User;

import static com.vincent.acnt.MyApp.CODE_QUIT_ACTIVITY;
import static com.vincent.acnt.MyApp.KEY_BOOKS;
import static com.vincent.acnt.MyApp.KEY_USERS;
import static com.vincent.acnt.MyApp.PRO_BOOKS;
import static com.vincent.acnt.MyApp.PRO_MEMBER_IDS;
import static com.vincent.acnt.MyApp.PRO_NAME;
import static com.vincent.acnt.MyApp.browsingBook;
import static com.vincent.acnt.data.Utility.getPlainDialog;

public class BookOptionActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "帳本選項";
    private FirebaseFirestore db;

    private Dialog dialog;

    private interface TaskListener { void onFinish(User user); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_option);
        context = this;
        db = MyApp.getInstance().getFirestore();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(activityTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        prepareDialog();

        ListView lstBookOption = findViewById(R.id.lstBookOption);

        lstBookOption.setAdapter(new BookOptionListAdapter(context, browsingBook));
        lstBookOption.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                executeFunction(position);
            }
        });
    }

    private void prepareDialog() {
        LinearLayout container = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(40, 0, 40, 0);
        container.setOrientation(LinearLayout.VERTICAL);

        final EditText edtBookName = new EditText(context);
        edtBookName.setLayoutParams(lp);
        edtBookName.setMaxLines(1);
        edtBookName.setSingleLine(true);
        edtBookName.setText(browsingBook.getName());
        container.addView(edtBookName);

        dialog = getPlainDialog(context, activityTitle, "請輸入新帳本名稱")
                .setView(container)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String bookName = edtBookName.getText().toString();
                        if (bookName.equals(""))
                            getPlainDialog(context, activityTitle, "帳本名稱未輸入").show();
                        else
                            updateBookName(bookName);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
    }

    private void executeFunction(int position) {
        switch (position) {
            case 0:
                dialog.show();
                break;
            case 2:
                if (browsingBook.getMemberIds().size() == 1) {
                    getPlainDialog(context, activityTitle, "由於帳本成員只剩下您一個人，因此會同時刪除帳本。\n將無法再參與帳務，並從您的帳本清單中移除。\n確定要退出這個帳本？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    leaveBook(new TaskListener() {
                                        @Override
                                        public void onFinish(User user) {
                                            deleteBook();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();
                }else {
                    getPlainDialog(context, activityTitle, "將無法再參與帳務，並從您的帳本清單中移除。\n確定要退出這個帳本？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    leaveBook(new TaskListener() {
                                        @Override
                                        public void onFinish(User user) {
                                            removeMember(user.getUid());
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();
                }
                break;
            case 3:
                getPlainDialog(context, activityTitle, "將會清除帳本所有資料，並解散成員。\n確定要刪除這個帳本？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leaveBook(new TaskListener() {
                                    @Override
                                    public void onFinish(User user) {
                                        deleteBook();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
                break;
        }
    }

    private void updateBookName(String bookName) {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId())
                .update(PRO_NAME, bookName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            Toast.makeText(context, "修改名稱成功", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, "修改名稱失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void leaveBook(final TaskListener taskListener) {
        final User user = MyApp.getInstance().getUser();
        user.getBooks().remove(browsingBook.getId());

        db.collection(KEY_USERS).document(user.obtainDocumentId())
                .update(PRO_BOOKS, user.getBooks())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        taskListener.onFinish(user);
                    }
                });
    }

    private void removeMember(String userId) {
        browsingBook.getMemberIds().remove(userId);

        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId())
                .update(PRO_MEMBER_IDS, browsingBook.getMemberIds())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "已退出帳本", Toast.LENGTH_SHORT).show();
                            setResult(CODE_QUIT_ACTIVITY);
                            finish();
                        }else
                            Toast.makeText(context, "退出失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteBook() {
        db.collection(KEY_BOOKS).document(browsingBook.obtainDocumentId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "刪除成功", Toast.LENGTH_SHORT).show();
                            setResult(CODE_QUIT_ACTIVITY);
                            finish();
                        }else
                            Toast.makeText(context, "刪除失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
