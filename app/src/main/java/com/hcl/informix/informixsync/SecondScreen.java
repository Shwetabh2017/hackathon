package com.hcl.informix.informixsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.hcl.informix.informixsync.constants.baseUrl;

public class SecondScreen extends AppCompatActivity {
    private EditText etTablename;
    private Button createTableble;
    private String dbname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);
        etTablename= findViewById(R.id.tablename);
        dbname = getIntent().getStringExtra("dbname");
        createTableble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                senddatatoserver();
            }
        });
    }


    public void senddatatoserver() {

        JSONObject post_dict = new JSONObject();


        try {

            post_dict.put("name",etTablename.getText().toString() );
           // post_dict.put("options");




          //  post_dict.put("locale","en_us.utf8");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() >= 0) {
            new SendDataToServer().execute(String.valueOf(post_dict));
            // #call to async class
        }
//add background inline class here
    }

    class SendDataToServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(baseUrl+dbname);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
//set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
// json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
//input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();
//response data
                //  Log.i(TAG,JsonResponse);
                //send to post execute
                return JsonResponse;


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        //  Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;


        }


        @Override
        protected void onPostExecute(String s) {

            SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

// We need an editor object to make changes
            SharedPreferences.Editor edit = pref.edit();
// Set/Store data
            edit.putString("tablename", etTablename.getText().toString());

// Commit the changes
            edit.apply();
            Intent intent = new Intent( SecondScreen.this, MainActivity.class);
            startActivity(intent);
            finish();

        }

    }
}


