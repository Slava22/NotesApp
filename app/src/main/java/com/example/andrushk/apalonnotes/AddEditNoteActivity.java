package com.example.andrushk.apalonnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class AddEditNoteActivity extends AppCompatActivity implements View.OnClickListener {

    DbHandler db;

    int what;
    int userId;
    int noteId;
    int notePosition;

    EditText textNote;
    Button buttonAddOrEdit;
    Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_change_note);

        db = new DbHandler(this);

        Intent intent = getIntent();
        what = intent.getIntExtra("add_or_edit", 0);
        userId = intent.getIntExtra("user_id", -1);
        noteId = intent.getIntExtra("note_id", -1);
        notePosition = intent.getIntExtra("note_position", -1);

        textNote = findViewById(R.id.text_note);
        buttonAddOrEdit = findViewById(R.id.button_add_or_edit);
        buttonCancel = findViewById(R.id.button_cancel);

        if (what == 1) {
            setTitle(getString(R.string.add_note));
            buttonAddOrEdit.setText(getString(R.string.add));
        } else {
            setTitle(getString(R.string.edit_note));
            buttonAddOrEdit.setText(R.string.edit);
            textNote.setText(intent.getStringExtra("text_note"));
        }

        buttonAddOrEdit.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_add_or_edit:
                if (what == 1) {
                    addNote();
                } else {
                    editNote();
                }
                break;
            case R.id.button_cancel:
                finish();
                break;
        }
    }

    public void addNote() {
        String text = textNote.getText().toString().trim();
        if (!text.equals("")) {
            Intent intent = new Intent();
            intent.putExtra("text_note", text);
            intent.putExtra("add_or_edit", 1);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Note not entered", Toast.LENGTH_LONG).show();
        }
    }

    public void editNote() {
        String text = textNote.getText().toString().trim();
        if (!text.equals("")) {
            Intent intent = new Intent();
            intent.putExtra("text_note", text);
            intent.putExtra("note_position", notePosition);
            intent.putExtra("note_id", noteId);
            intent.putExtra("add_or_edit", 2);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Note not entered", Toast.LENGTH_LONG).show();
        }
    }
}
