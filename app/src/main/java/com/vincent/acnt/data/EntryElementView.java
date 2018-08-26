package com.vincent.acnt.data;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import static com.vincent.acnt.MyApp.CODE_CREDIT;
import static com.vincent.acnt.MyApp.CODE_DEBIT;

public class EntryElementView {
    private AutoCompleteTextView actSubjectName;
    private Spinner spnDirection;
    private int direction;
    private EditText edtAmount;

    public EntryElementView(AutoCompleteTextView actSubjectName, Spinner spnDirection, EditText edtAmount) {
        this.actSubjectName = actSubjectName;
        this.edtAmount = edtAmount;
        this.spnDirection = spnDirection;

        this.spnDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    direction = CODE_CREDIT;
                else
                    direction = CODE_DEBIT;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public AutoCompleteTextView getActSubjectName() {
        return actSubjectName;
    }

    public Spinner getSpnDirection() {
        return spnDirection;
    }

    public EditText getEdtAmount() {
        return edtAmount;
    }

    public String getSubjectName() {
        return actSubjectName.getText().toString();
    }

    public int getDirection() {
        return direction;
    }

    public int getAmount() {
        String text = edtAmount.getText().toString();
        return text.equals("") ? 0 : Integer.parseInt(text);
    }
}
