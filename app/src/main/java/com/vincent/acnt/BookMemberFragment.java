package com.vincent.acnt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vincent.acnt.adapter.MemberListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.User;

import java.util.List;

public class BookMemberFragment extends Fragment {
    private Context context;

    private List<User> members;
    private MemberListAdapter adapter;
    private int type;

    private Dialog dialog;

    public BookMemberFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_member, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView lstMember = getView().findViewById(R.id.lstMember);
        adapter = new MemberListAdapter(getActivity(), type, members);

        lstMember.setAdapter(adapter);
        lstMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type == Constant.CODE_WAITING) {
                    dialog = prepareAdmissionDialog(position).show();
                } else {
                    dialog = prepareNormalDialog(position).show();
                }
            }
        });
    }

    public void setMembers(List<User> members) {
        if (adapter != null) {
            adapter.setMembers(members);
        }
        this.members = members;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MemberListAdapter getAdapter() {
        return adapter;
    }

    private AlertDialog.Builder prepareAdmissionDialog(final int index) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dlg_member_operation, null);

        ImageView imgProfile = layout.findViewById(R.id.imgProfile);
        TextView txtMemberName = layout.findViewById(R.id.txtMemberName);
        Button btnPositive = layout.findViewById(R.id.btnPositive);
        Button btnNegative = layout.findViewById(R.id.btnNegative);

        txtMemberName.setText(members.get(index).getName());

        btnPositive.setText("接受");
        btnPositive.setTextColor(Color.parseColor("#00AA00"));
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.approveUser(members.get(index));
                dialog.dismiss();
            }
        });

        btnNegative.setText("拒絕");
        btnNegative.setTextColor(Color.parseColor("#AA0000"));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.rejectUser(members.get(index));
                dialog.dismiss();
            }
        });

        return new AlertDialog.Builder(context)
                .setView(layout)
                .setCancelable(true);
    }

    private AlertDialog.Builder prepareNormalDialog(final int index) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dlg_member_operation, null);

        ImageView imgProfile = layout.findViewById(R.id.imgProfile);
        TextView txtMemberName = layout.findViewById(R.id.txtMemberName);
        Button btnPositive = layout.findViewById(R.id.btnPositive);
        Button btnNegative = layout.findViewById(R.id.btnNegative);

        User user = members.get(index);

        txtMemberName.setText(user.getName());

        if (user.getName().equals(MyApp.user.getName()) || !MyApp.browsingBook.isAdmin(MyApp.user.getId())) {
            btnPositive.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);

            return new AlertDialog.Builder(context)
                    .setView(layout)
                    .setCancelable(true);
        }

        if (MyApp.browsingBook.isAdmin(user.getId())) {
            btnPositive.setText("移除管理員");
            btnPositive.setTextColor(Color.parseColor("#00AA00"));
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.degradeUser(members.get(index));
                    dialog.dismiss();
                }
            });
        } else {
            btnPositive.setText("設為管理員");
            btnPositive.setTextColor(Color.parseColor("#00AA00"));
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.upgradeUser(members.get(index));
                    dialog.dismiss();
                }
            });
        }

        btnNegative.setText("移除成員");
        btnNegative.setTextColor(Color.parseColor("#AA0000"));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeUser(members.get(index));
                dialog.dismiss();
            }
        });

        return new AlertDialog.Builder(context)
                .setView(layout)
                .setCancelable(true);
    }


}
