package com.vincent.acnt.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vincent.acnt.EntryDetailActivity;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.R;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.Subject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EntryCardAdapter extends RecyclerView.Adapter<EntryCardAdapter.DataViewHolder> {
    private Context context;
    private List<Entry> entries;

    private LayoutInflater inflater;

    public int longClickPosition;

    public EntryCardAdapter(Context context, List<Entry> entries) {
        this.context = context;
        this.entries = entries;

        inflater = LayoutInflater.from(context);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private int position;
        private CardView cardEntry;
        private TextView txtDate, txtMemo;
        private ImageView imgCreator;
        private RelativeLayout layEntry;
        private LinearLayout layContainer;

        DataViewHolder(View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);

            cardEntry = itemView.findViewById(R.id.cardEntry);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtMemo = itemView.findViewById(R.id.txtMemo);
            imgCreator = itemView.findViewById(R.id.imgCreator);
            layEntry = itemView.findViewById(R.id.layEntry);
            layContainer = itemView.findViewById(R.id.layContainer);

            cardEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(context, EntryDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.KEY_ENTRY, getItem(position));
                    it.putExtras(bundle);
                    context.startActivity(it);
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (getItem(longClickPosition).getCreator().equals(MyApp.user.getId()) || MyApp.browsingBook.isAdminUser(MyApp.user.getId())) {
                menu.add(0, Constant.MODE_UPDATE, 0, "編輯");
                menu.add(0, Constant.MODE_DELETE, 1, "刪除");
            }
        }
    }

    @NonNull
    @Override
    public EntryCardAdapter.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_entry, parent, false);
        return new EntryCardAdapter.DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryCardAdapter.DataViewHolder holder, final int position) {
        holder.position = position;
        Entry entry = entries.get(position);
        String date = String.valueOf(entry.getDate());

        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("yyyyMMdd").parse(date));
            holder.layEntry.setBackgroundColor(Utility.getWeekColor(c));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.txtDate.setText(String.format("%s. %s",
                        Utility.getEngMonth(date.substring(4, 6)),
                        date.substring(6, 8)
        ));
        holder.txtMemo.setText(entry.getMemo());

        if (entry.getCreator() != null && entry.getCreator().equals(MyApp.user.getId())) {
            holder.imgCreator.setVisibility(View.VISIBLE);
        } else {
            holder.imgCreator.setVisibility(View.GONE);
        }

        holder.layContainer.removeAllViews();
        TextView txtSubject, txtCredit, txtDebit;
        for (Subject subject : entry.getSubjects()) {
            LinearLayout layElement = (LinearLayout) inflater.inflate(R.layout.lst_entry_element, null);
            txtSubject = layElement.findViewById(R.id.txtSubject);
            txtCredit = layElement.findViewById(R.id.txtCredit);
            txtDebit = layElement.findViewById(R.id.txtDebit);

            txtSubject.setText(subject.getName());

            if (subject.getDebit() == 0) {
                txtCredit.setText(NumberFormat.getNumberInstance(Locale.US).format(subject.getCredit()));
            } else {
                txtDebit.setText(NumberFormat.getNumberInstance(Locale.US).format(subject.getDebit()));
            }

            holder.layContainer.addView(layElement);
        }

        holder.cardEntry.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickPosition = position;
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public Entry getItem(int position) {
        return entries.get(position);
    }

    public void setEntries(List<Entry> entries) {
        this.entries.clear();
        this.entries = entries;
        notifyDataSetChanged();
    }
}
