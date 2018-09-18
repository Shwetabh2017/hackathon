package com.hcl.informix.informixsync;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.hcl.informix.informixsync.DB.EmployeeDBHandler;
import com.hcl.informix.informixsync.DB.EmployeeOperations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        addEmployeeButton = findViewById(R.id.button_add_employee);
        editEmployeeButton = findViewById(R.id.button_edit_employee);
        deleteEmployeeButton = findViewById(R.id.button_delete_employee);
        viewAllEmployeeButton = findViewById(R.id.button_view_employees);
        createCVS = findViewById(R.id.button_create_employees_cvs);
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }


        addEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddUpdateEmployee.class);
                i.putExtra(EXTRA_ADD_UPDATE, "Add");
                startActivity(i);
            }
        });
        editEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEmpIdAndUpdateEmp();
            }
        });
        deleteEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                }).create()
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
                        employeeOps.removeEmployee(employeeOps.getEmployee(Long.parseLong(userInput.getText().toString())));
                        Toast t = Toast.makeText(MainActivity.this, "Employee removed successfully!", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }).create()
                .show();

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
                csvWrite.writeNext(curCSV.getColumnNames());
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
}

