package com.vincent.acnt;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.vincent.acnt.adapter.MemberListAdapter;
import com.vincent.acnt.entity.User;

import java.util.List;

public class MemberFragment extends Fragment {
    protected Context context;
    ListView lstMember;
    private List<User> members;
    private MemberListAdapter adapter;
    private int type;

    public MemberFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_member, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lstMember = getView().findViewById(R.id.lstMember);
        adapter = new MemberListAdapter(getActivity(), type, members);
    }

    @Override
    public void onResume() {
        super.onResume();
        lstMember.setAdapter(adapter);
        lstMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, ((User) adapter.getItem(position)).getEmail(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setMembers(List<User> members) {
        if (adapter == null) {
            this.members = members;
        } else {
            adapter.setMembers(members);
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public MemberListAdapter getAdapter() {
        return adapter;
    }
}
