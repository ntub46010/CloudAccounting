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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.BookGridAdapter;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.List;

import static com.vincent.acnt.MyApp.CODE_QUIT_ACTIVITY;
import static com.vincent.acnt.MyApp.PRO_MEMBER_IDS;
import static com.vincent.acnt.MyApp.browsingBook;
import static com.vincent.acnt.data.Utility.getPlainDialog;
import static com.vincent.acnt.MyApp.KEY_BOOKS;
import static com.vincent.acnt.MyApp.KEY_BOOK_NAME;
import static com.vincent.acnt.MyApp.KEY_CREATOR;
import static com.vincent.acnt.MyApp.KEY_USERS;
import static com.vincent.acnt.MyApp.PRO_BOOKS;
import static com.vincent.acnt.MyApp.PRO_ID;
import static com.vincent.acnt.data.Utility.convertTo62Notation;

public class BookListActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "我的帳本";
    private FirebaseFirestore db;
    private User user;

    private GridView grdBook;
    private ProgressBar prgBar;

    private Dialog dialog;

    private ArrayList<Book> books;
    private int cnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        context = this;
        db = MyApp.getInstance().getFirestore();
        user = MyApp.getInstance().getUser();

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

        FloatingActionButton fabAddBook = findViewById(R.id.fabAddBook);
        grdBook = findViewById(R.id.grdBook);
        prgBar = findViewById(R.id.prgBar);

        fabAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        grdBook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(context, BookHomeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_BOOK_NAME, books.get(position).getName());
                bundle.putString(KEY_CREATOR, books.get(position).getCreator());
                it.putExtras(bundle);
                browsingBook = books.get(position);
                startActivityForResult(it, 0);
            }
        });

        prepareDialog();

        loadBooksData(true);
    }

    private void prepareDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layAddBook = (LinearLayout) inflater.inflate(R.layout.dlg_add_book, null);
        final EditText edtBookIdentity = layAddBook.findViewById(R.id.edtBookIdentity);
        final RadioGroup rgpAddMode = layAddBook.findViewById(R.id.rgpAddMode);

        rgpAddMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoCreateBook)
                    edtBookIdentity.setHint("輸入帳本名稱");
                else
                    edtBookIdentity.setHint("輸入帳本ID");
            }
        });

        dialog = getPlainDialog(context, activityTitle, "請選擇帳本加入方式")
                .setView(layAddBook)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String bookIdentity = edtBookIdentity.getText().toString();

                        if (rgpAddMode.getCheckedRadioButtonId() == R.id.rdoCreateBook) {
                            if (bookIdentity.equals(""))
                                getPlainDialog(context, activityTitle, "帳本名稱未輸入").show();
                            else
                                createBook(bookIdentity);
                        }else {
                            if (bookIdentity.equals(""))
                                getPlainDialog(context, activityTitle, "帳本ID未輸入").show();
                            else
                                importBook(edtBookIdentity.getText().toString());
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

    private void loadBooksData(boolean showPrgBar) {
        if (showPrgBar) {
            prgBar.setVisibility(View.VISIBLE);
            grdBook.setVisibility(View.INVISIBLE);
        }

        if (user.getBooks().isEmpty()) {
            prgBar.setVisibility(View.GONE);
            grdBook.setVisibility(View.VISIBLE);
            Toast.makeText(context, "您目前沒有帳本", Toast.LENGTH_SHORT).show();
            return;
        }

        cnt = user.getBooks().size();
        books = new ArrayList<>();
        downloadBook();
    }

    private void downloadBook() {
        if (cnt > 0) {
            final String bookId = user.getBooks().get(user.getBooks().size() - cnt);
            db.collection(KEY_BOOKS)
                    .whereEqualTo(PRO_ID, bookId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                                if (documentSnapshots.isEmpty()) {
                                    //若發現帳本已不存在，則把帳本ID由使用者資料中移除
                                    User user = MyApp.getInstance().getUser();
                                    user.getBooks().remove(bookId);
                                    db.collection(KEY_USERS).document(user.gainDocumentId()).update(PRO_BOOKS, user.getBooks());
                                }else {
                                    DocumentSnapshot documentSnapshot = documentSnapshots.get(0);
                                    Book book = documentSnapshot.toObject(Book.class);
                                    book.giveDocumentId(documentSnapshot.getId());
                                    books.add(book);
                                }
                            }

                            cnt--;
                            downloadBook();
                        }
                    });
        }else {
            grdBook.setAdapter(new BookGridAdapter(context, books));
            prgBar.setVisibility(View.GONE);
            grdBook.setVisibility(View.VISIBLE);
        }
    }

    private void createBook(String bookName) {
        prgBar.setVisibility(View.VISIBLE);

        final Book book = new Book(convertTo62Notation(String.valueOf(System.currentTimeMillis())), bookName, user.getName());
        db.collection(KEY_BOOKS)
                .add(book)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Book fakeBook = new Book();
                            fakeBook.giveDocumentId(task.getResult().getId());
                            addToBookList(book.getId(), fakeBook);
                        }else {
                            Toast.makeText(context, "新增帳本失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void importBook(final String bookId) {
        prgBar.setVisibility(View.VISIBLE);

        for (String id : user.getBooks()) {
            if (id.equals(bookId)) {
                Toast.makeText(context, "您先前已經匯入該帳本", Toast.LENGTH_SHORT).show();
                prgBar.setVisibility(View.GONE);
                return;
            }
        }

        db.collection(KEY_BOOKS)
                .whereEqualTo(PRO_ID, bookId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshot = task.getResult().getDocuments();
                            if (documentSnapshot.isEmpty()) {
                                Toast.makeText(context, "該帳本不存在，請確認帳本ID", Toast.LENGTH_SHORT).show();
                                prgBar.setVisibility(View.GONE);
                            }else {
                                Book fakeBook = new Book();
                                fakeBook.giveDocumentId(task.getResult().getDocuments().get(0).getId());
                                addToBookList(bookId, fakeBook);
                            }
                        }else {
                            Toast.makeText(context, "帳本確認失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void addToBookList(final String bookId, final Book book) {
        user.addBooks(bookId);

        db.collection(KEY_USERS).document(user.gainDocumentId())
                .update(PRO_BOOKS, user.getBooks())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "加入帳本成功", Toast.LENGTH_SHORT).show();
                            loadBooksData(true);
                            addMember(book);
                        }else {
                            Toast.makeText(context, "加入帳本失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void addMember(Book book) {
        book.addMember(user.getUid());

        db.collection(KEY_BOOKS)
                .document(book.gainDocumentId())
                .update(PRO_MEMBER_IDS, book.getMemberIds())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadBooksData(resultCode == CODE_QUIT_ACTIVITY);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
