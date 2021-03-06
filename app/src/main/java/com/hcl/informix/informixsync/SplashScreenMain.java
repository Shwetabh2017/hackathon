package com.hcl.informix.informixsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreenMain extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_main);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

                String dbname = pref.getString("dbname", "");
                Intent i = getIntent();

                if (dbname != null || !dbname.isEmpty()) {
                    i = new Intent(SplashScreenMain.this, MainActivity.class);
                } else {
                    i = new Intent(SplashScreenMain.this, launchScreen.class);
                }


                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}


