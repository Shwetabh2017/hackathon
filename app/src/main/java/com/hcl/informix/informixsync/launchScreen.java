package com.hcl.informix.informixsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class launchScreen extends AppCompatActivity {
    private EditText urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);
        urls = findViewById(R.id.urls);

        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

// We need an editor object to make changes
        SharedPreferences.Editor edit = pref.edit();
// Set/Store data
        edit.putString("urls", urls.getText().toString());

// Commit the changes
        edit.apply();


        Intent intent = new Intent( launchScreen.this,FirstScreen.class);
        startActivity(intent);
        finish();

    }
}
