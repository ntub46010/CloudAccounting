package com.vincent.acnt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vincent.acnt.adapter.MemberListAdapter;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_member, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RelativeLayout layHint = getView().findViewById(R.id.layContentHint);

        if (type == Constant.CODE_WAITING && members.isEmpty()) {
            TextView txtHint = getView().findViewById(R.id.txtHint);
            txtHint.setText("沒有待批准的使用者");
            layHint.setVisibility(View.VISIBLE);
        } else {
            layHint.setVisibility(View.GONE);
        }

        ListView lstMember = getView().findViewById(R.id.lstMember);
        adapter = new MemberListAdapter(getActivity(), members);

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
                approveUser(members.get(index));
                dialog.dismiss();
            }
        });

        btnNegative.setText("拒絕");
        btnNegative.setTextColor(Color.parseColor("#AA0000"));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectUser(members.get(index));
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

        if (user.getName().equals(MyApp.user.getName()) || !MyApp.browsingBook.isAdminUser(MyApp.user.getId())) {
            btnPositive.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);

            return new AlertDialog.Builder(context)
                    .setView(layout)
                    .setCancelable(true);
        }

        if (MyApp.browsingBook.isAdminUser(user.getId())) {
            btnPositive.setText("移除管理員");
            btnPositive.setTextColor(Color.parseColor("#00AA00"));
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.getPlainDialog(context, "成員", "確定要移除" + members.get(index).getName() + "的管理員身份嗎？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    degradeUser(members.get(index));
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();

                    dialog.dismiss();
                }
            });
        } else {
            btnPositive.setText("設為管理員");
            btnPositive.setTextColor(Color.parseColor("#00AA00"));
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.getPlainDialog(context, "成員", "確定要將" + members.get(index).getName() + "升為管理員嗎？")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    upgradeUser(members.get(index));
                                }
                            })
                            .setNegativeButton("否", null)
                            .show();

                    dialog.dismiss();
                }
            });
        }

        btnNegative.setText("移除成員");
        btnNegative.setTextColor(Color.parseColor("#AA0000"));
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.getPlainDialog(context, "成員", "確定要移除" + members.get(index).getName() + "嗎？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeUser(members.get(index));
                        }
                    })
                    .setNegativeButton("否", null)
                    .show();

                dialog.dismiss();
            }
        });

        return new AlertDialog.Builder(context)
                .setView(layout)
                .setCancelable(true);
    }

    public void approveUser(final User user) {
        MyApp.browsingBook.removeWaitingMember(user.getId());
        MyApp.browsingBook.getApprovedMembers().add(user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已加入" + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void rejectUser(final User user) {
        MyApp.browsingBook.removeWaitingMember(user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已拒絕" + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void upgradeUser(final User user) {
        MyApp.browsingBook.removeApprovedMember(user.getId());
        MyApp.browsingBook.addAdminMember(user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已給予" + user.getName() + "管理員身份", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void degradeUser(final User user) {
        MyApp.browsingBook.removeAdminMember(user.getId());
        MyApp.browsingBook.addApprovedMember(user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已移除"  + user.getName() + "的管理員身份", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void removeUser(final User user) {
        MyApp.browsingBook.removeApprovedMember(user.getId());
        MyApp.browsingBook.removeAdminMember(user.getId());

        MyApp.db.collection(Constant.KEY_BOOKS).document(MyApp.browsingBook.obtainDocumentId())
                .set(MyApp.browsingBook)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context, "已移除" + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
