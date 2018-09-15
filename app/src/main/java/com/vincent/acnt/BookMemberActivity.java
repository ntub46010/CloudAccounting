package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.adapter.MemberPagerAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class BookMemberActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "成員";

    private ViewPager vpgMember;

    private BookMemberFragment[] memberFragments = new BookMemberFragment[2];
    private MemberPagerAdapter adapter;

    private Set<String> userIds = new HashSet<>();
    private List<User> legalMembers = new ArrayList<>(), waitingMembers = new ArrayList<>();

    private boolean isFirstIn = true;

    private Dialog dlgWaiting;

    private CollectionReference ref;
    private ListenerRegistration lsrBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_member);
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

        vpgMember = findViewById(R.id.vpgReport);
        TabLayout tabHome = findViewById(R.id.tabs);

        vpgMember.setOffscreenPageLimit(15);
        vpgMember.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabHome.setupWithViewPager(vpgMember);

        adapter = new MemberPagerAdapter(getSupportFragmentManager());
        memberFragments[0] = new BookMemberFragment();
        memberFragments[1] = new BookMemberFragment();
        memberFragments[0].setType(Constant.CODE_APPROVED);
        memberFragments[1].setType(Constant.CODE_WAITING);

        dlgWaiting = Utility.getWaitingDialog(context);

        try {
            loadMembers();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            dlgWaiting.dismiss();
        }
    }

    private void loadMembers() {
        dlgWaiting.show();

        lsrBook = MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        Book book = documentSnapshot.toObject(Book.class);

                        legalMembers.clear();
                        waitingMembers.clear();
                        userIds.clear();

                        userIds.addAll(book.getAdminMembers());
                        userIds.addAll(book.getApprovedMembers());
                        userIds.addAll(book.getWaitingMembers());

                        ref = MyApp.db.collection(Constant.KEY_USERS);

                        for (String userId : userIds) {
                            ref.whereEqualTo(Constant.PRO_ID, userId);
                        }

                        sortMembers();
                    }
                });
    }

    private void sortMembers() {
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            List<User> approvedMembers = new ArrayList<>();
                            User user;

                            for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                                user = documentSnapshots.get(i).toObject(User.class);
                                user.defineDocumentId(documentSnapshots.get(i).getId());

                                if (MyApp.browsingBook.isAdminUser(user.getId())) {
                                    legalMembers.add(user);
                                }

                                if (MyApp.browsingBook.isApprovedUser(user.getId())) {
                                    approvedMembers.add(user);
                                }

                                if (MyApp.browsingBook.isWaitingUser(user.getId())) {
                                    waitingMembers.add(user);
                                }
                            }

                            legalMembers.addAll(approvedMembers);

                            showMembers();
                        } else {
                            Toast.makeText(context, "取得使用者資料失敗", Toast.LENGTH_SHORT).show();
                            dlgWaiting.dismiss();
                        }
                    }
                });
    }

    private void showMembers() {
        memberFragments[0].setMembers(legalMembers);
        memberFragments[1].setMembers(waitingMembers);

        if (isFirstIn) {
            adapter.addFragment(memberFragments[0], String.format("全部（ %d ）", legalMembers.size()));
            adapter.addFragment(memberFragments[1], String.format("待批准（ %d ）", waitingMembers.size()));
            vpgMember.setAdapter(adapter);

            isFirstIn = false;
        } else {
            adapter.setTitle(0, String.format("全部（ %d ）", legalMembers.size()));
            adapter.setTitle(1, String.format("待批准（ %d ）", waitingMembers.size()));
            adapter.notifyDataSetChanged();
            memberFragments[0].getAdapter().notifyDataSetChanged();
            memberFragments[1].getAdapter().notifyDataSetChanged();
        }

        dlgWaiting.dismiss();
    }

    @Override
    public void onDestroy() {
        lsrBook.remove();
        super.onDestroy();
    }
}
