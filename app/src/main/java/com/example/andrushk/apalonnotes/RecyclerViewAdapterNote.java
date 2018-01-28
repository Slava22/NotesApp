package com.example.andrushk.apalonnotes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapterNote extends RecyclerView.Adapter<RecyclerViewAdapterNote.NoteViewHolder> {

    DbHandler db;
    List<Note> notes;
    Context mContext;
    Activity mActivity;
    int userId;

    public RecyclerViewAdapterNote(Activity mActivity, Context mContext, DbHandler db, int userId) {
        this.notes = new ArrayList<>();
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.db = db;
        this.userId = userId;
        try {
            notes = db.getNotes(userId);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void addNote(Note note) {
        notes.add(note);
    }

    public void editNote(Note editedNote, int position) {
        notes.get(position).setText(editedNote.getText());
        notes.get(position).setEditedDate(editedNote.getEditedDate());
        db.editNote(editedNote.getId(), editedNote.getText(), editedNote.getEditedDateString());
        notifyDataSetChanged();
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_note, parent, false);
        NoteViewHolder rvh = new NoteViewHolder(view);
        return rvh;
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.textNote.setText(note.getText());
        holder.dateOfCreation.setText(mContext.getString(R.string.created) + " " + note.getCreationDateString());
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.cv.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 10);
        if (position == notes.size() - 1) {
            layoutParams.setMargins(0, 0, 0, 200);
        }
        if (!note.getEditedDateString().equals("")) {
            holder.dateOfEdited.setText(mContext.getString(R.string.edited) + " " + note.getEditedDateString());
            holder.dateOfEdited.setHeight(holder.dateOfCreation.getHeight());
        } else {
            holder.dateOfEdited.setHeight(0);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView textNote;
        TextView dateOfCreation;
        TextView dateOfEdited;
        ImageButton contextMenu;

        public NoteViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.card_view);
            textNote = itemView.findViewById(R.id.note_text);
            dateOfCreation = itemView.findViewById(R.id.note_date_of_creation);
            dateOfEdited = itemView.findViewById(R.id.note_date_of_edited);
            contextMenu = itemView.findViewById(R.id.context_menu);
            contextMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(view);
                }
            });
        }

        private void showPopupMenu(View v) {
            PopupMenu popupMenu = new PopupMenu(mActivity, v);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.edit:
                            editNoteStartActivity();
                            return true;
                        case R.id.delete:
                            alertDialogDeleteNote();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }

        public void editNoteStartActivity() {
            Intent intent = new Intent(mContext, AddEditNoteActivity.class);
            intent.putExtra("add_or_edit", 2);
            intent.putExtra("note_id", notes.get(getAdapterPosition()).getId());
            intent.putExtra("note_position", getAdapterPosition());
            intent.putExtra("text_note", notes.get(getAdapterPosition()).getText());
            mActivity.startActivityForResult(intent, 1);
        }

        public void alertDialogDeleteNote() {
            final AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);

            alert.setMessage(mContext.getString(R.string.are_you_sure_want_to_delete_this_note));

            alert.setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    db.deleteNote(notes.get(getAdapterPosition()));
                    notes.remove(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });

            alert.setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        }
    }
}
