package com.vincent.acnt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private FirebaseFirestore db;
    private ListView lstTran;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //宣告一個資料庫物件，對此物件操作等同於對雲端資料庫存取
        //會計科目(Subject)、交易(Transaction)等等需與資料庫對映的物件類別，要有get方法，才可儲存物件屬性。要有set方法與空白建構式，才可還原為物件。否則會發生例外
        //且物件類別若為inner class，需宣告為static
        db = FirebaseFirestore.getInstance();

        lstTran = findViewById(R.id.lstTran);

        Button btnAddSubject = findViewById(R.id.btnAddSubject);
        btnAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新增五個會計科目
                addSubject(new Subject(1, "現金", 5000, 0));
                addSubject(new Subject(1, "銀行存款", 8000, 0));
                addSubject(new Subject(1, "保留盈餘", 0, 13000));
                addSubject(new Subject(4, "薪資收入", 0, 0));
                addSubject(new Subject(5, "伙食費", 0, 0));
            }
        });

        Button btnAddTran = findViewById(R.id.btnAddTran);
        btnAddTran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新增兩筆交易

                //第一筆
                Transaction tran = new Transaction("2018/06/30", "領薪水");
                tran.add(new Subject("現金", 10000, 0));
                tran.add(new Subject("銀行存款", 20000, 0));
                tran.add(new Subject("薪資收入", 0, 30000));
                addTransaction(tran);

                //第二筆
                tran = new Transaction("2018/07/01", "午餐、晚餐");
                tran.add(new Subject("伙食費", 200, 0));
                tran.add(new Subject("現金", 0, 200));
                addTransaction(tran);
            }
        });

        Button btnShow = findViewById(R.id.btnShow);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //顯示交易清單
                showTransactions();
            }
        });
    }

    private void addSubject(final Subject subject) {
        //指定儲存的位置(集合)，呼叫add加入至資料庫，並定義callback方法
        //藉由物件的get方法，轉化為資料庫可儲存的形式
        db.collection("Subject")
                .add(subject)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful())
                            Toast.makeText(context, "科目新增成功：" + subject.getName(), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, "科目新增失敗：" + subject.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTransaction(final Transaction tran) {
        db.collection("Transaction")
                .add(tran)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful())
                            Toast.makeText(context, "新增交易成功：" + tran.getPs(), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, "新增交易失敗：" + tran.getPs(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showTransactions() {
        final ArrayList<Transaction> trans = new ArrayList<>();

        //建立查詢物件，以指定的集合為查詢標的
        Query query = db.collection("Transaction");

        //呼叫get進行查詢，並定義callback方法
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //取得查詢結果的快照
                    QuerySnapshot querySnapshot = task.getResult();

                    //將查詢結果內的各個項目(document)，藉著空白建構式與set方法，對映為物件，再放入陣列
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments())
                        trans.add(documentSnapshot.toObject(Transaction.class));

                    //設定adapter給清單，注意不要寫到callback方法外面，否則會沒有資料可顯示
                    lstTran.setAdapter(new TransactionAdapter(context, trans));
                }else
                    Toast.makeText(context, "查詢失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
