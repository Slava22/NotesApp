package com.example.andrushk.apalonnotes;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class DbHandler extends SQLiteOpenHelper {

    private static final int Db_Version = 10;
    private static final String Db_Name = "apalon_notes";
    private static final String Table_Name_User = "user";
    private static final String Table_Name_Note = "note";
    private static final String User_id = "user_id";
    private static final String User_name = "user_name";
    private static final String User_password = "user_password";
    private static final String Note_id = "note_id";
    private static final String Note_text = "note_text";
    private static final String Note_date_of_creation = "note_date_of_creation";
    private static final String Note_date_of_edited = "note_date_of_edited";
    private static SecretKeySpec sks;
    Context mContext;

    public DbHandler(Context context) {
        super(context, Db_Name, null, Db_Version);
        this.mContext = context;
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        String specialKey = sharedPref.getString("special_key", "");
        if(!specialKey.equals("")) {
            byte[] encodedKey = android.util.Base64.decode(specialKey, android.util.Base64.DEFAULT);
            this.sks = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String Create_Table_User = "CREATE TABLE " + Table_Name_User + "(" + User_id
                + " INTEGER PRIMARY KEY," + User_name + " TEXT," + User_password + " BLOB" + ")";
        db.execSQL(Create_Table_User);
        String Create_Table_Notes = "CREATE TABLE " + Table_Name_Note + "(" + Note_id
                + " INTEGER PRIMARY KEY," + Note_text + " TEXT," + Note_date_of_creation + " TEXT," + Note_date_of_edited + " TEXT," + User_id
                + " INTEGER" + ")";
        db.execSQL(Create_Table_Notes);
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        String specialKey = sharedPref.getString("special_key", "");
        if (specialKey.equals("")) {
            try {
                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                sr.setSeed("any data used as random seed".getBytes());
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                kg.init(128, sr);
                sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
                String sksString = android.util.Base64.encodeToString(sks.getEncoded(), android.util.Base64.DEFAULT);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("special_key", sksString);
                editor.commit();
            } catch (Exception e) {
                Log.e("Crypto", "AES secret key spec error");
            }
        } else {
            byte[] encodedKey = android.util.Base64.decode(specialKey, android.util.Base64.DEFAULT);
            sks = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Table_Name_User);
        db.execSQL("DROP TABLE IF EXISTS " + Table_Name_Note);
        onCreate(db);
    }

    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(User_name, user.getName());
        cv.put(User_password, encryptPassword(user.getPassword()));

        db.insert(Table_Name_User, null, cv);
        db.close();
    }

    public void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(Note_text, note.getText());
        cv.put(Note_date_of_creation, note.getCreationDateString());
        cv.put(User_id, note.getUserID());
        cv.put(Note_date_of_edited, "");

        db.insert(Table_Name_Note, null, cv);
        db.close();
    }

    public int checkUser(String name) {
        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM user WHERE user_name = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return id;
    }

    public int checkUser(User user) {
        int id = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user WHERE user_name = ?", new String[]{user.getName()});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (Arrays.equals(cursor.getBlob(cursor.getColumnIndex(User_password)), encryptPassword(user.getPassword()))) {
                id = cursor.getInt(cursor.getColumnIndex(User_id));
            }
            cursor.close();
        }
        db.close();
        return id;
    }

    public void editNote(int noteId, String text, String noteDateOfEdited) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updatedValues = new ContentValues();

        updatedValues.put(Note_text, text);
        updatedValues.put(Note_date_of_edited, noteDateOfEdited);

        db.update(Table_Name_Note, updatedValues, Note_id + " = ?", new String[]{String.valueOf(noteId)});
        db.close();
    }

    public void deleteNote(Note note) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(Table_Name_Note, Note_id + " = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public int getIdLastNote() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Table_Name_Note, new String[]{Note_id}, null, null, null, null, null);
        cursor.moveToLast();
        int noteID = cursor.getInt(cursor.getColumnIndex(Note_id));
        cursor.close();
        db.close();
        return noteID;
    }

    public List<Note> getNotes(int userId) throws ParseException {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String myFormat = "dd-MM-yyyy 'at' HH:mm";
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        Cursor cursor = db.query(Table_Name_Note, new String[]{Note_id, Note_text, Note_date_of_creation, Note_date_of_edited}, User_id + " = ?", new String[]{String.valueOf(userId)}, null, null, null);
        while (cursor.moveToNext()) {
            Calendar dateNoteOfCreation = Calendar.getInstance();
            dateNoteOfCreation.setTime(sdf.parse(cursor.getString(cursor.getColumnIndex(Note_date_of_creation))));
            if(cursor.getString(cursor.getColumnIndex(Note_date_of_edited)) == null){
                notes.add(new Note(cursor.getInt(cursor.getColumnIndex(Note_id)), userId, cursor.getString(cursor.getColumnIndex(Note_text)), dateNoteOfCreation));
            } else {
                Calendar dateNoteOfEdited = Calendar.getInstance();
                dateNoteOfEdited.setTime(sdf.parse(cursor.getString(cursor.getColumnIndex(Note_date_of_edited))));
                notes.add(new Note(cursor.getInt(cursor.getColumnIndex(Note_id)), userId, cursor.getString(cursor.getColumnIndex(Note_text)), dateNoteOfCreation, dateNoteOfEdited));
            }
        }
        cursor.close();
        db.close();
        return notes;
    }

    public byte[] encryptPassword(String userpw) {
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(userpw.getBytes());
        } catch (Exception e) {
            Log.e("Crypto", "AES encryption error");
        }
        return encodedBytes;
    }
}