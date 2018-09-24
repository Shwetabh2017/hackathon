package com.hcl.informix.informixsync;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hcl.informix.informixsync.DB.EmployeeDBHandler;
import com.hcl.informix.informixsync.DB.EmployeeOperations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.hcl.informix.informixsync.constants._ipaddress;
import static com.hcl.informix.informixsync.constants.baseUrl;
import static com.hcl.informix.informixsync.constants.empid;
import static com.hcl.informix.informixsync.constants.mypref;

public class MainActivity extends AppCompatActivity {

    private Button addEmployeeButton;
    private Button editEmployeeButton;
    private Button deleteEmployeeButton;
    private Button viewAllEmployeeButton;
    private Button createCVS;
    private EmployeeOperations employeeOps;
    private static final String EXTRA_EMP_ID = "com.androidtutorialpoint.empId";
    private static final String EXTRA_ADD_UPDATE = "com.androidtutorialpoint.add_update";
    private final int RC_CAMERA_AND_LOCATION = 101;
    public String emp_id;
    EditText portno;
    EditText ipaddress;
    String ipaddressToGo;
    String portNumToGo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        addEmployeeButton = findViewById(R.id.button_add_employee);
        editEmployeeButton = findViewById(R.id.button_edit_employee);
        deleteEmployeeButton = findViewById(R.id.button_delete_employee);
        viewAllEmployeeButton = findViewById(R.id.button_view_employees);
        createCVS = findViewById(R.id.button_create_employees_cvs);
        portno = findViewById(R.id.portno);
        ipaddress = findViewById(R.id.ipaddress);

          getIpAddressFromSharePref();


        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        addEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateData();
                Intent i = new Intent(MainActivity.this, AddUpdateEmployee.class);
                i.putExtra(EXTRA_ADD_UPDATE, "Add");
                startActivity(i);
            }
        });
        editEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateData();
                getEmpIdAndUpdateEmp();
            }
        });
        deleteEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateData();
                getEmpIdAndRemoveEmp();
            }
        });
        viewAllEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ViewAllEmployees.class);
                startActivity(i);
            }
        });
        createCVS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* ExportDatabaseCSVTask task = new ExportDatabaseCSVTask();
                task.execute();*/
                methodRequiresTwoPermission();

            }
        });

    }

    private void getIpAddressFromSharePref() {
        SharedPreferences prefs = getSharedPreferences(mypref, MODE_PRIVATE);
        String ipaddress1 = prefs.getString("ipaddress", "10.115.96.39");//"No name defined" is the default value.
        String portno1 = prefs.getString("port", "27017"); //0 is the default value.
        ipaddress.setText(ipaddress1);
        portno.setText(portno1);
    }

    private void updateData() {
        if (ipaddress.getText().toString().matches("")) {
            ipaddressToGo = "10.115.96.39";
        } else {
            ipaddressToGo = ipaddress.getText().toString().trim();
        }

        if (portno.getText().toString().matches("")) {
            portNumToGo = "27017";

        } else {
            portNumToGo = portno.getText().toString().trim();
        }

        //Saving ipaddress and portno.

        SharedPreferences.Editor editor = getSharedPreferences(mypref, MODE_PRIVATE).edit();
        editor.putString("ipaddress", ipaddressToGo);
        editor.putString("port", portNumToGo);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.employee_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void methodRequiresTwoPermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            new ExportDatabaseCSVTask().execute();
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.camera_and_location_rationale),
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    public void getEmpIdAndUpdateEmp() {

        LayoutInflater li = LayoutInflater.from(this);
        View getEmpIdView = li.inflate(R.layout.dialog_get_emp_id, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set dialog_get_emp_id.xml to alertdialog builder
        alertDialogBuilder.setView(getEmpIdView);

        final EditText userInput = getEmpIdView.findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input and set it to result
                        // edit text
                        Intent i = new Intent(MainActivity.this, AddUpdateEmployee.class);
                        i.putExtra(EXTRA_ADD_UPDATE, "Update");
                        i.putExtra(EXTRA_EMP_ID, Long.parseLong(userInput.getText().toString()));
                        startActivity(i);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.cancel();
                    }
                })

                .create()
                .show();


    }


    public void getEmpIdAndRemoveEmp() {

        LayoutInflater li = LayoutInflater.from(this);
        View getEmpIdView = li.inflate(R.layout.dialog_get_emp_id, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set dialog_get_emp_id.xml to alertdialog builder
        alertDialogBuilder.setView(getEmpIdView);

        final EditText userInput = getEmpIdView.findViewById(R.id.editTextDialogUserInput);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input and set it to result
                        // edit text
                        employeeOps = new EmployeeOperations(MainActivity.this);
                        emp_id = userInput.getText().toString();
                        empid = emp_id;
                        employeeOps.removeEmployee(employeeOps.getEmployee(Long.parseLong(userInput.getText().toString())));
                        Toast t = Toast.makeText(MainActivity.this, "Employee removed successfully!", Toast.LENGTH_SHORT);
                        t.show();
                        deleteFromServer();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.cancel();
                    }
                })

                .create()
                .show();

    }

    private void deleteFromServer() {
        if (!baseUrl.equals(null))
            new SendDataToServerDelete().execute(String.valueOf(""));
    }

    class SendDataToServerDelete extends AsyncTask<String, String, String> {

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
                //   URL url = new URL("http://10.115.96.147:27017/mydb/people");
                //  final String encodedURL = URLEncoder.encode("http://10.115.96.147:27017/mydb/people?query={emp_id:2}", "UTF-8");
                String url1 = baseUrl + "/mydb/people?query=%7Bemp_id:" + emp_id + "%7D";
                URL url = new URL(url1.trim());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                // urlConnection.setRequestProperty("Content-Type", "applicaiton/json; charset=utf-8");
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


    @Override
    protected void onResume() {
        super.onResume();
//        employeeOps.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        employeeOps.close();

    }

    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        EmployeeDBHandler dbhelper;

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            dbhelper = new EmployeeDBHandler(MainActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, "employee.csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor curCSV = dbhelper.raw();
                //csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    String arrStr[] = null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for (int i = 0; i < curCSV.getColumnNames().length; i++) {
                        mySecondStringArray[i] = curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(MainActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
                ShareFile();
            } else {
                Toast.makeText(MainActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
            }

        }

        private void ShareFile() {
            File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");
            String fileName = "myfile.csv";
            File sharingGifFile = new File(exportDir, fileName);
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("application/csv");
            Uri uri = Uri.fromFile(sharingGifFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "Share CSV"));
        }
    }

//    void deleteFirstRow() {
//        CSVReader reader2 = new CSVReader(new FileReader(filelocation));
//        List<String[]> allElements = reader2.readAll();
//        allElements.remove(rowNumber);
//        FileWriter sw = new FileWriter(filelocation);
//        CSVWriter writer = new CSVWriter(sw);
//        writer.writeAll(allElements);
//        writer.close();
//    }
}

