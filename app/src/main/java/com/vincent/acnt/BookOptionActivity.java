package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.vincent.acnt.accessor.BookAccessor;
import com.vincent.acnt.accessor.TaskFinishListener;
import com.vincent.acnt.accessor.UserAccessor;
import com.vincent.acnt.adapter.BookOptionListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.User;

import java.util.List;
import java.util.Map;

public class BookOptionActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "帳本選項";

    private Dialog dialog;

    private BookAccessor asrBook;
    private UserAccessor asrUser;

    private interface TaskListener { void onFinish(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_option);
        context = this;
        asrBook = new BookAccessor(MyApp.db.collection(Constant.KEY_BOOKS));
        asrUser = new UserAccessor(MyApp.db.collection(Constant.KEY_USERS));

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

        lstBookOption.setAdapter(new BookOptionListAdapter(context, MyApp.browsingBook));
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
        edtBookName.setText(MyApp.browsingBook.getName());
        container.addView(edtBookName);

        dialog = Utility.getPlainDialog(context, activityTitle, "請輸入新帳本名稱")
                .setView(container)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String bookName = edtBookName.getText().toString();
                        if (bookName.equals("")) {
                            Utility.getPlainDialog(context, activityTitle, "帳本名稱未輸入").show();
                        } else {
                            updateBookName(bookName);
                        }
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
                if (MyApp.browsingBook.getApprovedMembers().size() == 1) {
                    Utility.getPlainDialog(context, activityTitle, "由於帳本成員只剩下您一個人，因此會同時刪除帳本。\n將無法再參與帳務，並從您的帳本清單中移除。\n確定要退出這個帳本？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    leaveBook(new TaskListener() {
                                        @Override
                                        public void onFinish() {
                                            deleteBook();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();
                } else {
                    Utility.getPlainDialog(context, activityTitle, "將無法再參與帳務，並從您的帳本清單中移除。\n確定要退出這個帳本？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    leaveBook(new TaskListener() {
                                        @Override
                                        public void onFinish() {
                                            removeMember(MyApp.user);
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();
                }
                break;
            case 3:
                Utility.getPlainDialog(context, activityTitle, "將會清除帳本所有資料，並解散成員。\n確定要刪除這個帳本？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leaveBook(new TaskListener() {
                                    @Override
                                    public void onFinish() {
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
        asrBook.patch(MyApp.browsingBook.obtainDocumentId(), Constant.PRO_NAME, bookName, new TaskFinishListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "修改名稱成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "修改名稱失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveBook(final TaskListener taskListener) {
        MyApp.user.getBooks().remove(MyApp.browsingBook.getId());

        asrUser.patch(MyApp.user.obtainDocumentId(), Constant.PRO_BOOKS, MyApp.user.getBooks(), new TaskFinishListener() {
            @Override
            public void onSuccess() {
                taskListener.onFinish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "離開帳本失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeMember(User member) {
        List<String> adminMembers = MyApp.browsingBook.getAdminMembers();
        List<String> approvedMembers = MyApp.browsingBook.getApprovedMembers();

        adminMembers.remove(member.getId());
        approvedMembers.remove(member.getId());

        Map<String, Object> properties = new ArrayMap<>();
        properties.put(Constant.PRO_ADMIN_MEMBERS, adminMembers);
        properties.put(Constant.PRO_APPROVED_MEMBERS, approvedMembers);

        asrBook.patch(MyApp.browsingBook.obtainDocumentId(), properties, new TaskFinishListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "已退出帳本", Toast.LENGTH_SHORT).show();
                setResult(Constant.MODE_QUIT);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "退出失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteBook() {
        asrBook.delete(MyApp.browsingBook.obtainDocumentId(), new TaskFinishListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "刪除成功", Toast.LENGTH_SHORT).show();
                setResult(Constant.MODE_QUIT);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "刪除失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
