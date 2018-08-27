package com.vincent.acnt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vincent.acnt.R;
import com.vincent.acnt.entity.Book;

public class BookOptionListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private String[] titles;
    private String[] contents;

    public BookOptionListAdapter(Context context, Book book) {
        layoutInflater = LayoutInflater.from(context);

        this.titles = new String[] {
                "帳本名稱",
                "帳本ID",
                "退出帳本",
                "刪除帳本"
        };

        this.contents = new String[] {
                book.getName(),
                book.getId(),
                null,
                null
        };
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.lst_book_option, parent, false);
        }

        TextView txtTitle = view.findViewById(R.id.txtTitle);
        TextView txtContent = view.findViewById(R.id.txtContent);
        txtTitle.setText(titles[position]);

        if (contents[position] == null) {
            txtContent.setVisibility(View.GONE);
        } else {
            txtContent.setVisibility(View.VISIBLE);
            txtContent.setText(contents[position]);
        }

        return view;
    }

    public void setContent(int position, String content) {
        contents[position] = content;
        notifyDataSetChanged();
    }
}
