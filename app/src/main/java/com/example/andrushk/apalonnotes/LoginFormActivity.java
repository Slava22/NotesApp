package com.example.andrushk.apalonnotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginFormActivity extends AppCompatActivity implements View.OnClickListener {

    DbHandler db;
    Context mContext;

    EditText inputName;
    EditText inputPassword;
    Button buttonSignIn;
    Button buttonRegister;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        db = new DbHandler(getApplicationContext());
        mContext = this;

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_Pref), Context.MODE_PRIVATE);
        int userId = sharedPref.getInt("user_id", -1);
        String userName = sharedPref.getString("user_name", "");
        if (userId != -1) {
            startReminder(userName);
        }

        inputName = findViewById(R.id.input_name);
        inputPassword = findViewById(R.id.input_password);
        buttonSignIn = findViewById(R.id.button_sign_in);
        buttonRegister = findViewById(R.id.button_register);

        buttonSignIn.setOnClickListener(this);
        buttonRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_sign_in:
                signIn();
                break;
            case R.id.button_register:
                register();
                break;
        }
    }

    public void signIn() {
        String name = String.valueOf(inputName.getText()).trim();
        String password = String.valueOf(inputPassword.getText());
        if (name.equals("")) {
            showAlertDialog(getString(R.string.name_not_entered));
        } else if (password.equals("")) {
            showAlertDialog(getString(R.string.password_not_entered));
        } else {
            String userName = name.substring(0, 1).toUpperCase() + name.substring(1);
            User user = new User(userName, password);
            if (db.checkUser(userName) == -1) {
                showAlertDialog(getString(R.string.a_user_with_this_name_does_not_exists));
            } else if (db.checkUser(user) != -1) {
                startReminder(user.getName());
            } else {
                showAlertDialog(getString(R.string.wrong_password));
            }
        }
    }

    public void register() {
        String name = String.valueOf(inputName.getText()).trim();
        String password = String.valueOf(inputPassword.getText());
        if (name.equals("")) {
            showAlertDialog(getString(R.string.name_not_entered));
        } else if (password.equals("")) {
            showAlertDialog(getString(R.string.password_not_entered));
        } else {
            String userName = name.substring(0, 1).toUpperCase() + name.substring(1);
            User newUser = new User(userName, password);
            if (db.checkUser(userName) == -1) {
                db.addUser(newUser);
                startReminder(newUser.getName());
            } else {
                showAlertDialog(getString(R.string.a_user_with_this_name_already_exists));
            }
        }
    }

    public void showAlertDialog(String text) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(text);
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void startReminder(String name) {
        Intent intent = new Intent(mContext, ReminderActivity.class);
        intent.putExtra("user_name", name);
        intent.putExtra("user_id", db.checkUser(name));
        startActivity(intent);
        finish();
    }
}
