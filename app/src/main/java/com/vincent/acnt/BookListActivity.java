package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.acnt.accessor.BookAccessor;
import com.vincent.acnt.accessor.RetrieveEntitiesListener;
import com.vincent.acnt.accessor.RetrieveEntityListener;
import com.vincent.acnt.accessor.TaskFinishListener;
import com.vincent.acnt.accessor.UserAccessor;
import com.vincent.acnt.adapter.BookGridAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "我的帳本";

    private GridView grdBook;
    private FloatingActionButton fabAddBook;
    private ProgressBar prgBar;
    private RelativeLayout layHint;

    private List<Book> mBooks;
    private BookGridAdapter adapter;

    private Dialog dialog;
    private BookAccessor asrBook;
    private UserAccessor asrUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
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

        fabAddBook = findViewById(R.id.fabAddBook);
        grdBook = findViewById(R.id.grdBook);
        prgBar = findViewById(R.id.prgBar);
        layHint = findViewById(R.id.layContentHint);

        fabAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        grdBook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                requestToUseBook(mBooks.get(position));
            }
        });

        prepareDialog();

        loadBooks(true);
    }

    private void prepareDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layAddBook = (LinearLayout) inflater.inflate(R.layout.dlg_add_book, null);
        final EditText edtBookIdentity = layAddBook.findViewById(R.id.edtBookIdentity);
        final RadioGroup rgpAddMode = layAddBook.findViewById(R.id.rgpAddMode);

        rgpAddMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoCreateBook) {
                    edtBookIdentity.setHint("輸入帳本名稱");
                } else {
                    edtBookIdentity.setHint("輸入帳本ID");
                }
            }
        });

        dialog = Utility.getPlainDialog(context, activityTitle, "請選擇帳本加入方式")
                .setView(layAddBook)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String bookIdentity = edtBookIdentity.getText().toString();

                        if (rgpAddMode.getCheckedRadioButtonId() == R.id.rdoCreateBook) {
                            if (bookIdentity.equals("")) {
                                Utility.getPlainDialog(context, activityTitle, "帳本名稱未輸入").show();
                            } else {
                                createBook(bookIdentity);
                            }
                        } else {
                            if (bookIdentity.equals("")) {
                                Utility.getPlainDialog(context, activityTitle, "帳本ID未輸入").show();
                            } else {
                                importBook(edtBookIdentity.getText().toString());
                            }
                        }

                        rgpAddMode.check(R.id.rdoCreateBook);
                        edtBookIdentity.setText(null);
                        prgBar.setVisibility(View.VISIBLE);
                        grdBook.setVisibility(View.INVISIBLE);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
    }

    private void loadBooks(boolean showPrgBar) {
        if (MyApp.user.getBooks().isEmpty()) {
            prgBar.setVisibility(View.GONE);
            grdBook.setVisibility(View.INVISIBLE);

            Toast.makeText(context, "您目前沒有帳本", Toast.LENGTH_SHORT).show();
            return;
        }

        prgBar.setVisibility(showPrgBar ? View.VISIBLE : View.GONE);
        grdBook.setVisibility(showPrgBar ? View.INVISIBLE : View.VISIBLE);
        fabAddBook.setVisibility(showPrgBar ? View.INVISIBLE : View.VISIBLE);

        mBooks = new ArrayList<>(16);

        asrBook.loadBooksByList(MyApp.user.getBooks(), new RetrieveEntitiesListener() {
            @Override
            public void onRetrieve(List<? extends Entity> entities) {
                List<Book> books = (List<Book>) entities;
                mBooks.clear();
                mBooks.addAll(books);

                if (mBooks.isEmpty()) {
                    TextView txtHint = findViewById(R.id.txtHint);
                    txtHint.setText("您現在沒有帳本，點擊右下方按鈕加入帳本");
                    layHint.setVisibility(View.VISIBLE);
                } else {
                    layHint.setVisibility(View.GONE);
                }

                if (adapter == null) {
                    adapter = new BookGridAdapter(context, mBooks);
                    grdBook.setAdapter(adapter);
                } else {
                    adapter.setBooks(mBooks);
                }

                prgBar.setVisibility(View.GONE);
                grdBook.setVisibility(View.VISIBLE);
                fabAddBook.setVisibility(View.VISIBLE);

                if (mBooks.size() != MyApp.user.getBooks().size()) {
                    removeMissingBook();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context,"取得帳本失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeMissingBook() {
        List<String> actualBookIds = new ArrayList<>(16);

        for (int i = 0, len = mBooks.size(); i < len; i++) {
            actualBookIds.add(mBooks.get(i).getId());
        }

        asrUser.patch(MyApp.user.obtainDocumentId(), Constant.PRO_BOOKS, actualBookIds, null);
    }

    private void createBook(String bookName) {
        prgBar.setVisibility(View.VISIBLE);
        fabAddBook.setVisibility(View.INVISIBLE);

        final Book book = new Book();
        book.setId(Utility.convertTo62Notation(String.valueOf(System.currentTimeMillis())));
        book.setName(bookName);
        book.setCreator(MyApp.user.getId());
        book.addAdminMember(MyApp.user.getId());

        asrBook.create(book, new RetrieveEntityListener() {
            @Override
            public void onRetrieve(Entity entity) {
                addToMyBookList(book.getId(), (Book) entity, Constant.MODE_CREATE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "新增帳本失敗", Toast.LENGTH_SHORT).show();
                prgBar.setVisibility(View.GONE);
            }
        });
    }

    private void importBook(final String bookId) {
        if (MyApp.user.getBooks().contains(bookId)) {
            Toast.makeText(context, "您已經擁有該帳本", Toast.LENGTH_SHORT).show();
            return;
        }

        prgBar.setVisibility(View.VISIBLE);
        fabAddBook.setVisibility(View.INVISIBLE);

        asrBook.loadBookById(bookId, new RetrieveEntityListener() {
            @Override
            public void onRetrieve(Entity entity) {
                if (entity == null) {
                    Toast.makeText(context, "該帳本不存在，請確認帳本ID", Toast.LENGTH_SHORT).show();
                    prgBar.setVisibility(View.GONE);
                    return;
                }

                addToMyBookList(bookId, (Book) entity, Constant.MODE_IMPORT);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "帳本確認失敗", Toast.LENGTH_SHORT).show();
                prgBar.setVisibility(View.GONE);
            }
        });
    }

    private void addToMyBookList(final String bookId, final Book book, final int mode) {
        MyApp.user.addBooks(bookId);

        asrUser.patch(MyApp.user.obtainDocumentId(), Constant.PRO_BOOKS, MyApp.user.getBooks(), new TaskFinishListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "加入帳本成功", Toast.LENGTH_SHORT).show();
                loadBooks(true);

                if (mode == Constant.MODE_IMPORT) {
                    addMemberToBook(book);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "加入帳本失敗", Toast.LENGTH_SHORT).show();
                prgBar.setVisibility(View.GONE);
            }
        });
    }

    private void addMemberToBook(Book book) {
        book.addWaitingMember(MyApp.user.getId());
        asrBook.patch(book.obtainDocumentId(), Constant.PRO_WAITING_MEMBERS, book.getWaitingMembers(), null);

        fabAddBook.setVisibility(View.VISIBLE);
    }

    private void requestToUseBook(Book book) {
        // 自己在合法成員名單才可打開帳本
        if (book.isLegalUser(MyApp.user.getId())) {
            Intent it = new Intent(context, BookHomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constant.KEY_BOOK_NAME, book.getName());
            bundle.putString(Constant.KEY_CREATOR, book.getCreator());
            it.putExtras(bundle);
            startActivityForResult(it, 0);

            MyApp.browsingBook = book;
            return;
        }

        if (book.isWaitingUser(MyApp.user.getId())) {
            Toast.makeText(context, "尚未被帳本成員批准加入", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "您被拒絕使用該帳本，將從清單中移除", Toast.LENGTH_SHORT).show();

        MyApp.user.getBooks().remove(book.getId());
        asrUser.patch(MyApp.user.obtainDocumentId(), Constant.PRO_BOOKS, MyApp.user.getBooks(), new TaskFinishListener() {
            @Override
            public void onSuccess() {
                loadBooks(true);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "移除帳本失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadBooks(resultCode == Constant.MODE_QUIT);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
