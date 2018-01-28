package com.example.andrushk.apalonnotes;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class Note {
    int id;
    int userID;
    String text;
    Calendar creationDate;
    Calendar editedDate;

    public Note(int userID, String text, Calendar creationDate, Calendar editedDate) {
        this.userID = userID;
        this.text = text;
        this.creationDate = creationDate;
        this.editedDate = editedDate;
    }

    public Note(int id, int userID, String text, Calendar creationDate, Calendar editedDate) {
        this.id = id;
        this.userID = userID;
        this.text = text;
        this.creationDate = creationDate;
        this.editedDate = editedDate;
    }

    public Note(int id, int userID, String text, Calendar creationDate) {
        this.id = id;
        this.userID = userID;
        this.text = text;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreationDateString() {
        String myFormat = "dd-MM-yyyy 'at' HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        return sdf.format(creationDate.getTime());
    }

    public String getEditedDateString(){
        String myFormat = "dd-MM-yyyy 'at' HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        if(editedDate != null) {
            return sdf.format(editedDate.getTime());
        } else {
            return "";
        }
    }

    public Calendar getEditedDate() {
        return editedDate;
    }

    public void setEditedDate(Calendar editedDate) {
        this.editedDate = editedDate;
    }

    public int getUserID() {
        return userID;
    }
}