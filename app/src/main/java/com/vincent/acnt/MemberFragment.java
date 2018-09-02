package com.vincent.acnt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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

public class MemberFragment extends Fragment {
    private Context context;

    private ImageView imgProfile;

    private List<User> members;
    private MemberListAdapter adapter;
    private int type;

    private Dialog dialog;

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

        ListView lstMember = getView().findViewById(R.id.lstMember);
        adapter = new MemberListAdapter(getActivity(), type, members);

        lstMember.setAdapter(adapter);
        lstMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type == Constant.CODE_WAITING) {
                    prepareAdmissionDialog(position);
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

    private void prepareAdmissionDialog(final int index) {
        RelativeLayout layAdmission = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.dlg_admission, null);

        imgProfile = layAdmission.findViewById(R.id.imgProfile);
        TextView txtMemberName = layAdmission.findViewById(R.id.txtMemberName);
        Button btnAccept = layAdmission.findViewById(R.id.btnAccept);
        Button btnReject = layAdmission.findViewById(R.id.btnReject);

        txtMemberName.setText(members.get(index).getName());

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.approveUser(members.get(index));
                dialog.dismiss();
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.rejectUser(members.get(index));
                dialog.dismiss();
            }
        });

        dialog = new AlertDialog.Builder(context)
                .setView(layAdmission)
                .setCancelable(true)
                .show();
    }
}
