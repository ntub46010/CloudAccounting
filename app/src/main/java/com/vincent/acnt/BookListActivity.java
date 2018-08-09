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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.List;

import static com.vincent.acnt.data.DataHelper.getPlainDialog;
import static com.vincent.acnt.data.MyApp.KEY_BOOKS;
import static com.vincent.acnt.data.MyApp.KEY_BOOK_NAME;
import static com.vincent.acnt.data.MyApp.KEY_CREATOR;
import static com.vincent.acnt.data.MyApp.KEY_ID;
import static com.vincent.acnt.data.MyApp.KEY_USERS;
import static com.vincent.acnt.data.MyApp.PRO_BOOKS;
import static com.vincent.acnt.data.MyApp.PRO_ID;

public class BookListActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "我的帳本";
    private FirebaseFirestore db;
    private User user;

    private GridView grdBook;
    private ProgressBar prgBar;

    private Dialog dialog;

    private ArrayList<String> bookIds;
    private ArrayList<Book> books;
    private int cnt;

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

        grdBook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(context, BookHomeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_ID, books.get(position).getId());
                bundle.putString(KEY_BOOK_NAME, books.get(position).getName());
                bundle.putString(KEY_CREATOR, books.get(position).getCreator());
                it.putExtras(bundle);
                startActivity(it);
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

        dialog = getPlainDialog(context, activityTitle, "請選擇帳本新增方式")
                .setView(layAddBook)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (rgpAddMode.getCheckedRadioButtonId() == R.id.rdoCreateBook)
                            createBook(edtBookIdentity.getText().toString());
                        else
                            importBook(edtBookIdentity.getText().toString());

                        edtBookIdentity.setText(null);
                        prgBar.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
    }

    private void loadBooksData() {
        prgBar.setVisibility(View.VISIBLE);

        bookIds = user.getBooks();
        if (bookIds == null) {
            prgBar.setVisibility(View.GONE);
            Toast.makeText(context, "您目前沒有帳本", Toast.LENGTH_SHORT).show();
            return;
        }

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
        }else
            grdBook.setAdapter(new BookGridAdapter(context, books));

        prgBar.setVisibility(View.GONE);
    }

    private void createBook(String bookName) {
        final Book book = new Book(String.valueOf(System.currentTimeMillis()), bookName, user.getName());
        db.collection(KEY_BOOKS)
                .add(book)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            addToBookList(book.getId());
                        }else {
                            Toast.makeText(context, "新增帳本失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void importBook(final String bookId) {
        for (String id : user.getBooks()) {
            if (id.equals(bookId)) {
                Toast.makeText(context, "您先前已經匯入該帳本", Toast.LENGTH_SHORT).show();
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
                            if (documentSnapshot.isEmpty())
                                Toast.makeText(context, "該帳本不存在，請確認帳本ID", Toast.LENGTH_SHORT).show();
                            else
                                addToBookList(bookId);
                        }else
                            Toast.makeText(context, "帳本確認失敗", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToBookList(String bookId) {
        user.addBooks(bookId);

        db.collection(KEY_USERS).document(user.gainDocumentId())
                .update(PRO_BOOKS, user.getBooks())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "新增帳本成功", Toast.LENGTH_SHORT).show();
                            loadBooksData();
                        }else {
                            Toast.makeText(context, "新增帳本失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
