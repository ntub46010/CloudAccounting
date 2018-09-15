package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.BookGridAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Book;

import java.util.ArrayList;
import java.util.List;

public class BookListActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "我的帳本";

    private GridView grdBook;
    private FloatingActionButton fabAddBook;
    private ProgressBar prgBar;
    private RelativeLayout layHint;

    private Dialog dialog;

    private List<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        context = this;

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
                requestToUseBook(books.get(position));
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

        books = new ArrayList<>(16);

        CollectionReference ref = MyApp.db.collection(Constant.KEY_BOOKS);

        for (int i = 0, len = MyApp.user.getBooks().size(); i < len; i++) {
            ref.whereEqualTo(Constant.PRO_ID, MyApp.user.getBooks().get(i));
        }

        ref.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            books.clear();
                            Book book;

                            for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                                book = documentSnapshots.get(i).toObject(Book.class);
                                book.defineDocumentId(documentSnapshots.get(i).getId());
                                books.add(book);
                            }

                            if (books.isEmpty()) {
                                TextView txtHint = findViewById(R.id.txtHint);
                                txtHint.setText("您現在沒有帳本，點擊右下方按鈕加入帳本");
                                layHint.setVisibility(View.VISIBLE);
                            } else {
                                layHint.setVisibility(View.GONE);
                            }

                            grdBook.setAdapter(new BookGridAdapter(context, books));
                            prgBar.setVisibility(View.GONE);
                            grdBook.setVisibility(View.VISIBLE);
                            fabAddBook.setVisibility(View.VISIBLE);

                            if (books.size() != MyApp.user.getBooks().size()) {
                                removeMissingBook();
                            }
                        }
                    }
                });
    }

    private void removeMissingBook() {
        List<String> expectBookIds = MyApp.user.getBooks();
        List<String> actualBookIds = new ArrayList<>(16);

        for (int i = 0, len = books.size(); i < len; i++) {
            actualBookIds.add(books.get(i).getId());
        }

        for (int i = 0, len = expectBookIds.size(); i < len; i++) {
            if (!actualBookIds.contains(expectBookIds.get(i))) {
                expectBookIds.remove(i);
            }
        }

        MyApp.db.collection(Constant.KEY_USERS).document(MyApp.user.obtainDocumentId())
                .update(Constant.PRO_BOOKS, expectBookIds);
    }

    private void createBook(String bookName) {
        prgBar.setVisibility(View.VISIBLE);
        fabAddBook.setVisibility(View.INVISIBLE);

        final Book book = new Book();
        book.setId(Utility.convertTo62Notation(String.valueOf(System.currentTimeMillis())));
        book.setName(bookName);
        book.setCreator(MyApp.user.getId());
        book.addAdminMember(MyApp.user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS)
                .add(book)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Book fakeBook = new Book();
                            fakeBook.defineDocumentId(task.getResult().getId());
                            addToMyBookList(book.getId(), fakeBook, Constant.MODE_CREATE);
                        } else {
                            Toast.makeText(context, "新增帳本失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
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

        MyApp.db.collection(Constant.KEY_BOOKS)
                .whereEqualTo(Constant.PRO_ID, bookId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshot = task.getResult().getDocuments();
                            if (documentSnapshot.isEmpty()) {
                                Toast.makeText(context, "該帳本不存在，請確認帳本ID", Toast.LENGTH_SHORT).show();
                                prgBar.setVisibility(View.GONE);
                            } else {
                                DocumentSnapshot bookDocument = task.getResult().getDocuments().get(0);
                                Book book = bookDocument.toObject(Book.class);
                                book.defineDocumentId(bookDocument.getId());
                                addToMyBookList(bookId, book, Constant.MODE_IMPORT);
                            }
                        } else {
                            Toast.makeText(context, "帳本確認失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void addToMyBookList(final String bookId, final Book book, final int mode) {
        MyApp.user.addBooks(bookId);

        MyApp.db.collection(Constant.KEY_USERS).document(MyApp.user.obtainDocumentId())
                .update(Constant.PRO_BOOKS, MyApp.user.getBooks())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "加入帳本成功", Toast.LENGTH_SHORT).show();
                            loadBooks(true);

                            if (mode == Constant.MODE_IMPORT) {
                                addMemberToBook(book);
                            }
                        } else {
                            Toast.makeText(context, "加入帳本失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void addMemberToBook(Book book) {
        book.addWaitingMember(MyApp.user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS)
                .document(book.obtainDocumentId())
                .set(book);

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
        MyApp.db.collection(Constant.KEY_USERS).document(MyApp.user.obtainDocumentId())
                .update(Constant.PRO_BOOKS, MyApp.user.getBooks())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadBooks(true);
                        } else {
                            Toast.makeText(context, "移除帳本失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadBooks(resultCode == Constant.MODE_QUIT);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
