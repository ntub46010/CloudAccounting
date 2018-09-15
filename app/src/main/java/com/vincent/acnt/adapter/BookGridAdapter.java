package com.vincent.acnt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vincent.acnt.MyApp;
import com.vincent.acnt.R;
import com.vincent.acnt.entity.Book;

import java.util.List;

public class BookGridAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<Book> books;

    public BookGridAdapter(Context context, List<Book> books) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.books = books;
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.grd_book, parent, false);
        }

        ImageView imgBook = view.findViewById(R.id.imgBook);
        TextView txtBookName = view.findViewById(R.id.txtBookName);

        Book book = books.get(position);

        txtBookName.setText(book.getName());

        if (book.isLegalUser(MyApp.user.getId())) {
            imgBook.setImageResource(R.drawable.img_book_legal);
            txtBookName.setTextColor(Color.parseColor("#000000"));
        } else if (book.isWaitingUser(MyApp.user.getId())) {
            imgBook.setImageResource(R.drawable.img_book_waiting);
            txtBookName.setTextColor(Color.parseColor("#808080"));
        } else {
            imgBook.setImageResource(R.drawable.img_book_reject);
            txtBookName.setTextColor(Color.parseColor("#808080"));
        }

        return view;
    }
}
