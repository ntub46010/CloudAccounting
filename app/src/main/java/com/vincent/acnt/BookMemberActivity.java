package com.vincent.acnt;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;
import com.vincent.acnt.accessor.BookAccessor;
import com.vincent.acnt.adapter.MemberPagerAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.List;

public class BookMemberActivity extends AppCompatActivity {
    private Context context;
    private String activityTitle = "成員";

    private ViewPager vpgMember;

    private BookMemberFragment[] memberFragments = new BookMemberFragment[2];
    private MemberPagerAdapter adapter;

    private List<User> legalMembers = new ArrayList<>(), waitingMembers = new ArrayList<>();

    private boolean isFirstIn = true;

    private Dialog dlgWaiting;
    private BookAccessor asrBook;
    private ListenerRegistration regBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_member);
        context = this;
        asrBook = new BookAccessor(MyApp.db.collection(Constant.KEY_BOOKS));

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

        regBook = asrBook.observeBookMembersById(MyApp.browsingBook.obtainDocumentId(), new BookAccessor.RetrieveBookMembersListener() {
            @Override
            public void onRetrieve(List<User> members) {
                legalMembers.clear();
                waitingMembers.clear();

                List<User> approvedMembers = new ArrayList<>(16);
                User user;

                for (int i = 0, len = members.size(); i < len; i++) {
                    user = members.get(i);

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
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "取得使用者資料失敗", Toast.LENGTH_SHORT).show();
                dlgWaiting.dismiss();
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
        }

        dlgWaiting.dismiss();
    }

    @Override
    public void onDestroy() {
        regBook.remove();
        super.onDestroy();
    }
}
