package com.vincent.acnt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vincent.acnt.MyApp;
import com.vincent.acnt.R;
import com.vincent.acnt.entity.User;

import java.util.List;

public class MemberListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<User> members;

    public MemberListAdapter(Context context, List<User> members) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.members = members;
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.lst_member, parent, false);
        }

        ImageView imgProfile = view.findViewById(R.id.imgProfile);
        TextView txtMemberName = view.findViewById(R.id.txtMemberName);
        ImageView imgMgr = view.findViewById(R.id.imgMgr);

        User user = members.get(position);
        txtMemberName.setText(user.getName());

        if (MyApp.browsingBook.isAdminUser(user.getId())) {
            imgMgr.setVisibility(View.VISIBLE);
        } else {
            imgMgr.setVisibility(View.GONE);
        }

        return view;
    }

    public void setMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }

}
