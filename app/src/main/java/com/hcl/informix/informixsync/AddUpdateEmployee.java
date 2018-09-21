package com.hcl.informix.informixsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.hcl.informix.informixsync.DB.EmployeeOperations;
import com.hcl.informix.informixsync.Model.Employee;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.hcl.informix.informixsync.constants.baseUrl;
import static com.hcl.informix.informixsync.constants.mypref;

public class AddUpdateEmployee extends AppCompatActivity implements DatePickerFragment.DateDialogListener {

    private static final String EXTRA_EMP_ID = "com.androidtutorialpoint.empId";
    private static final String EXTRA_ADD_UPDATE = "com.androidtutorialpoint.add_update";
    private static final String DIALOG_DATE = "DialogDate";
    private ImageView calendarImage;
    private RadioGroup radioGroup;
    private RadioButton maleRadioButton, femaleRadioButton;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText deptEditText;
    private EditText hireDateEditText;
    private Button addUpdateButton;
    private Button syncButton;
    private Employee newEmployee;
    private Employee oldEmployee;
    private String mode;
    private long empId;
    private EmployeeOperations employeeOps;
    private String empid;
    private String newurl ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_employee);
        newEmployee = new Employee();
        oldEmployee = new Employee();
        firstNameEditText = findViewById(R.id.edit_text_first_name);
        lastNameEditText = findViewById(R.id.edit_text_last_name);
        hireDateEditText = findViewById(R.id.edit_text_hire_date);
        radioGroup = findViewById(R.id.radio_gender);
        maleRadioButton = findViewById(R.id.radio_male);
        femaleRadioButton = findViewById(R.id.radio_female);
        calendarImage = findViewById(R.id.image_view_hire_date);
        deptEditText = findViewById(R.id.edit_text_dept);
        addUpdateButton = findViewById(R.id.button_add_update_employee);
        employeeOps = new EmployeeOperations(this);
        syncButton = findViewById(R.id.button_add_update_sync_employee);
        mode = getIntent().getStringExtra(EXTRA_ADD_UPDATE);

        SharedPreferences prefs = getSharedPreferences(mypref, MODE_PRIVATE);
        String ipaddress = prefs.getString("ipaddress", "http://10.115.96.147:");//"No name defined" is the default value.
        String portno = prefs.getString("port", "27017"); //0 is the default value.
        newurl = baseUrl = ipaddress + portno;
        Log.e( "onCreate: ",baseUrl );

        if (mode.equals("Update")) {
            addUpdateButton.setText("Update Employee");
            empId = getIntent().getLongExtra(EXTRA_EMP_ID, 0);
            empid = String.valueOf(empId);
            initializeEmployee(empId);
        }

/*        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject postData = new JSONObject();
                try {
                    postData.put("firstName", newEmployee.getFirstname());
                    postData.put("lastName", newEmployee.getLastname());
                    postData.put("gender", newEmployee.getGender());
                    postData.put("hireDate", newEmployee.getHiredate());
                    postData.put("department", newEmployee.getDept());
                    new SendDeviceDetails().execute("http://10.115.96.147:27017/mydb/people", postData.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });*/


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radio_male) {
                    newEmployee.setGender("M");
                    if (mode.equals("Update")) {
                        oldEmployee.setGender("M");
                    }
                } else if (checkedId == R.id.radio_female) {
                    newEmployee.setGender("F");
                    if (mode.equals("Update")) {
                        oldEmployee.setGender("F");
                    }

                }
            }

        });

        calendarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager manager = getSupportFragmentManager();
                DatePickerFragment dialog = new DatePickerFragment();
                dialog.show(manager, DIALOG_DATE);
            }
        });


        addUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                employeeOps.open();
                if (mode.equals("Add")) {
                    newEmployee.setFirstname(firstNameEditText.getText().toString());
                    newEmployee.setLastname(lastNameEditText.getText().toString());
                    newEmployee.setHiredate(hireDateEditText.getText().toString());
                    newEmployee.setDept(deptEditText.getText().toString());
                    employeeOps.addEmployee(newEmployee);
                    employeeOps.close();
                    Toast t = Toast.makeText(AddUpdateEmployee.this, "Employee " + newEmployee.getFirstname() + "has been added successfully !", Toast.LENGTH_SHORT);
                    t.show();
                    senddatatoserver();
                    Intent i = new Intent(AddUpdateEmployee.this, MainActivity.class);
                    startActivity(i);
                } else {
                    oldEmployee.setFirstname(firstNameEditText.getText().toString());
                    oldEmployee.setLastname(lastNameEditText.getText().toString());
                    oldEmployee.setHiredate(hireDateEditText.getText().toString());
                    oldEmployee.setDept(deptEditText.getText().toString());
                    employeeOps.updateEmployee(oldEmployee);
                    employeeOps.close();
                    Toast t = Toast.makeText(AddUpdateEmployee.this, "Employee " + oldEmployee.getFirstname() + " has been updated successfully !", Toast.LENGTH_SHORT);
                    t.show();
                    senddatatoserverUpdate();
                    Intent i = new Intent(AddUpdateEmployee.this, MainActivity.class);
                    startActivity(i);


                }


            }
        });

    }

    private void senddatatoserverUpdate() {
        JSONObject post_dict = new JSONObject();
        JSONObject details = new JSONObject();
        String aa = "";
        try {
            post_dict.put("firstName", firstNameEditText.getText().toString());
       /*     post_dict.put("lastName", lastNameEditText.getText().toString());
            post_dict.put("gender", "M".toString());
            post_dict.put("hireDate", hireDateEditText.getText().toString());
            post_dict.put("department", deptEditText.getText().toString());*/

            // details.put("$set", post_dict.toString());

            // details.put("$set", "{\"$set\":\"{\\\"firstName\\\":\\\"nitinnnnnffg\\\"}\"}"); //working

            // details.put("\\\"$set\\\"", "{\\\"firstName\\\":\\\"nitin123\\\"}");
            // details.put("\\\"$set\\\"", "{\\\"firstName\\\":\\\"nitin123\\\"}");

             aa =  "{\"$set\":{\"firstName\":\""+firstNameEditText.getText().toString()+"\",\"lastName\":\""+lastNameEditText.getText().toString()+"\",\"gender\":\"M\",\"hireDate\":\""+hireDateEditText.getText().toString()+"\",\"department\":\""+deptEditText.getText().toString()+"\"}}";

            //  jsonArray.put(post_dict);
            Log.e("o/p", details.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() >= 0) {
            // new SendDataToServerUpdates().execute(String.valueOf(details));
            new SendDataToServerUpdates().execute(String.valueOf(aa));
            // #call to async class
        }
//add background inline class here
    }


    private void initializeEmployee(long empId) {
        oldEmployee = employeeOps.getEmployee(empId);
        firstNameEditText.setText(oldEmployee.getFirstname());
        lastNameEditText.setText(oldEmployee.getLastname());
        hireDateEditText.setText(oldEmployee.getHiredate());
        radioGroup.check(oldEmployee.getGender().equals("M") ? R.id.radio_male : R.id.radio_female);
        deptEditText.setText(oldEmployee.getDept());
    }


    @Override
    public void onFinishDialog(Date date) {
        hireDateEditText.setText(formatDate(date));

    }

    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String hireDate = sdf.format(date);
        return hireDate;
    }


    public void senddatatoserver() {

        JSONObject post_dict = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            post_dict.put("firstName", newEmployee.getFirstname());
            post_dict.put("lastName", newEmployee.getLastname());
            post_dict.put("gender", newEmployee.getGender());
            post_dict.put("hireDate", newEmployee.getHiredate());
            post_dict.put("department", newEmployee.getDept());
            jsonArray.put(post_dict);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonArray.length() >= 0) {
            new SendDataToServer().execute(String.valueOf(jsonArray));
            // #call to async class
        }
//add background inline class here
    }


    class SendDataToServerUpdates extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            //  String dbname = pref.getString("dbname", "");
            //  String tablename = pref.getString("tablename", "");

            try {

                // Uri.Builder builder = new Uri.Builder().appendQueryParameter("emp_id","1");

                //  final String query = builder.build().getEncodedQuery();
                // URL url = new URL(baseUrl + dbname + tablename);

               /* String mQuery = URLEncoder.encode("?query={emp_id:1}", "UTF-8");
                String myurl = "http://10.115.96.147:27017/mydb/people";
                myurl += mQuery;*/

                String url1 = "http://10.115.96.147:27017/mydb/people?query=%7Bemp_id:"+empid+"%7D";
               // Log.e( "doInBackground: ",empid );


                URL url = new URL(url1);

                //    URL url = new URL("http://10.115.96.147:27017/mydb/people");
                //URL url = new URL("http://10.115.96.147:27017/mydb/people");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(60000); //60 secs
                urlConnection.setReadTimeout(60000); //60 secs
                // is output buffer writter
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                //urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.setRequestProperty("Accept", "application/json");
//set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                //writer.write(query);

                writer.write(JsonDATA);
                writer.flush();
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
        }
    }


    private class SendDataToServer extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            //  String dbname = pref.getString("dbname", "");
            //  String tablename = pref.getString("tablename", "");

            try {
                // URL url = new URL(baseUrl + dbname + tablename);
                URL url = new URL("http://10.115.96.147:27017/mydb/people");
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

        }

    }
}


