package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.BookGridAdapter;
import com.vincent.acnt.data.Book;
import com.vincent.acnt.data.MyApp;
import com.vincent.acnt.data.User;

import java.util.ArrayList;

import static com.vincent.acnt.data.DataHelper.getPlainDialog;
import static com.vincent.acnt.data.MyApp.KEY_BOOKS;
import static com.vincent.acnt.data.MyApp.KEY_USERS;
import static com.vincent.acnt.data.MyApp.PRO_BOOKS;
import static com.vincent.acnt.data.MyApp.PRO_ID;

public class BookListActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "我的帳簿";
    private FirebaseFirestore db;
    private User user;

    private ArrayList<Long> bookIds;
    private ArrayList<Book> books;
    private int cnt;

    private GridView grdBook;
    private ProgressBar prgBar;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
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

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        grdBook = findViewById(R.id.grdBook);
        prgBar = findViewById(R.id.prgBar);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        prepareDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooksData();
    }

    private void prepareDialog() {
        final EditText edtBookName = new EditText(context);
        dialog = getPlainDialog(context, activityTitle, "請輸入新帳簿名稱")
                .setView(edtBookName)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createBook(edtBookName.getText().toString());
                        edtBookName.setText(null);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
    }

    private void loadBooksData() {
        prgBar.setVisibility(View.VISIBLE);

        bookIds = user.getBooks();
        if (bookIds == null)
            bookIds = new ArrayList<>();

        cnt = bookIds.size();
        books = new ArrayList<>();
        downloadBook();
    }

    private void downloadBook() {
        if (cnt > 0) {
            db.collection(KEY_BOOKS)
                    .whereEqualTo(PRO_ID, bookIds.get(bookIds.size() - cnt))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            Book book = documentSnapshot.toObject(Book.class);
                            book.giveDocumentId(documentSnapshot.getId());
                            books.add(book);

                            cnt--;
                            downloadBook();
                        }
                    });
        }else {
            grdBook.setAdapter(new BookGridAdapter(context, books));
            prgBar.setVisibility(View.GONE);
        }
    }

    private void createBook(String bookName) {
        prgBar.setVisibility(View.VISIBLE);

        final Book book = new Book(System.currentTimeMillis(), bookName, user.getName());
        db.collection(KEY_BOOKS)
                .add(book)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            addToBookList(book);
                        }else {
                            Toast.makeText(context, "新增帳簿失敗1", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void addToBookList(Book book) {
        user.addBooks(book.getId());
        db.collection(KEY_USERS).document(user.gainDocumentId())
                .update(PRO_BOOKS, user.getBooks())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "新增帳簿成功", Toast.LENGTH_SHORT).show();
                            loadBooksData();
                        }else {
                            Toast.makeText(context, "新增帳簿失敗2", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
