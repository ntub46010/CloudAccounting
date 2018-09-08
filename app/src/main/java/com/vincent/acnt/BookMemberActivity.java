package com.vincent.acnt;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.vincent.acnt.adapter.MemberPagerAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.User;

import java.util.List;

import javax.annotation.Nullable;

public class BookMemberActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private String activityTitle = "成員";

    private ViewPager vpgMember;

    private BookMemberFragment[] memberFragments;
    private MemberPagerAdapter adapter;

    private boolean isFirstIn = true;
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

        loadMembers();
    }

    private void loadMembers() {
        adapter = new MemberPagerAdapter(getSupportFragmentManager());
        memberFragments = new BookMemberFragment[2];
        memberFragments[0] = new BookMemberFragment();
        memberFragments[1] = new BookMemberFragment();
        memberFragments[0].setType(Constant.CODE_APPROVED);
        memberFragments[1].setType(Constant.CODE_WAITING);

        lsrBook= MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        Book book = documentSnapshot.toObject(Book.class);

                        List<User> legalMembers = book.getAdminMembers();
                        legalMembers.addAll(book.getApprovedMembers());

                        List<User> waitingMembers = book.getWaitingMembers();

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
                    }
                });
    }

    @Override
    public void onDestroy() {
        lsrBook.remove();
        super.onDestroy();
    }
}
