package com.example.andrushk.apalonnotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class ReminderActivity extends AppCompatActivity implements View.OnClickListener {

    Context mContext;
    DbHandler db;

    int userId;
    String userName;

    RecyclerViewAdapterNote recyclerViewAdapterNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mContext = this;
        db = new DbHandler(getApplicationContext());

        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);
        userName = intent.getStringExtra("user_name");

        saveState(userId, userName);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        TextView title = findViewById(getResources().getIdentifier("name", "id", getPackageName()));
        title.setText(userName);
        ImageButton buttonLogout = findViewById(getResources().getIdentifier("buton_logout", "id", getPackageName()));
        FloatingActionButton buttonAddRemind = findViewById(R.id.button_add_note);
        buttonAddRemind.setOnClickListener(this);
        buttonLogout.setOnClickListener(this);

        RecyclerView rv = findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        recyclerViewAdapterNote = new RecyclerViewAdapterNote(this, getApplicationContext(), db, userId);
        rv.setLayoutManager(llm);
        rv.setAdapter(recyclerViewAdapterNote);

        LinearLayout layout = findViewById(R.id.layout_transparent_text);
        RVEmptyObserver observer = new RVEmptyObserver(rv, layout);
        recyclerViewAdapterNote.registerAdapterDataObserver(observer);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buton_logout:
                startLogout();
                break;
            case R.id.button_add_note:
                addNoteStartActivity();
                break;
        }
    }

    public void addNoteStartActivity(){
        Intent intent = new Intent(getApplicationContext(), AddEditNoteActivity.class);
        intent.putExtra("add_or_edit", 1);
        intent.putExtra("user_id", userId);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        String text = data.getStringExtra("text_note");
        if (data.getIntExtra("add_or_edit", -1) == 1){
            addNote(text);
        } else {
            Note editedNote = new Note(data.getIntExtra("note_id", -1), userId, text, Calendar.getInstance(), Calendar.getInstance());
            editNote(editedNote, data.getIntExtra("note_position", -1));
        }
    }

    public void addNote(String text) {
        Note newNote = new Note(userId, text, Calendar.getInstance(), null);
        db.addNote(newNote);
        newNote.setId(db.getIdLastNote());
        recyclerViewAdapterNote.addNote(newNote);
        recyclerViewAdapterNote.notifyDataSetChanged();
    }

    public void editNote(Note note, int position){
        recyclerViewAdapterNote.editNote(note, position);
        recyclerViewAdapterNote.notifyDataSetChanged();
    }

    public void startLogout() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(getString(R.string.do_you_really_want_to_log_out));
        alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                saveState(-1, "");
                Intent intent = new Intent(mContext, LoginFormActivity.class);
                startActivity(intent);
                finish();
            }
        });

        alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void saveState(int id, String name) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("user_id", id);
        editor.putString("user_name", name);
        editor.commit();
    }
}
