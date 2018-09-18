package com.hcl.informix.informixsync;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.hcl.informix.informixsync.DB.EmployeeOperations;
import com.hcl.informix.informixsync.Model.Employee;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddUpdateEmployee extends AppCompatActivity implements DatePickerFragment.DateDialogListener{

    private static final String EXTRA_EMP_ID = "com.androidtutorialpoint.empId";
    private static final String EXTRA_ADD_UPDATE = "com.androidtutorialpoint.add_update";
    private static final String DIALOG_DATE = "DialogDate";
    private ImageView calendarImage;
    private RadioGroup radioGroup;
    private RadioButton maleRadioButton,femaleRadioButton;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText deptEditText;
    private EditText hireDateEditText;
    private Button addUpdateButton;
    private Employee newEmployee;
    private Employee oldEmployee;
    private String mode;
    private long empId;
    private EmployeeOperations employeeOps;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_employee);
        newEmployee = new Employee();
        oldEmployee = new Employee();
        firstNameEditText = (EditText)findViewById(R.id.edit_text_first_name);
        lastNameEditText = (EditText)findViewById(R.id.edit_text_last_name);
        hireDateEditText = (EditText) findViewById(R.id.edit_text_hire_date);
        radioGroup = (RadioGroup) findViewById(R.id.radio_gender);
        maleRadioButton = (RadioButton) findViewById(R.id.radio_male);
        femaleRadioButton = (RadioButton) findViewById(R.id.radio_female);
        calendarImage = (ImageView)findViewById(R.id.image_view_hire_date);
        deptEditText = (EditText)findViewById(R.id.edit_text_dept);
        addUpdateButton = (Button)findViewById(R.id.button_add_update_employee);
        employeeOps = new EmployeeOperations(this);



        mode = getIntent().getStringExtra(EXTRA_ADD_UPDATE);
        if(mode.equals("Update")){

            addUpdateButton.setText("Update Employee");
            empId = getIntent().getLongExtra(EXTRA_EMP_ID,0);

            initializeEmployee(empId);

        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radio_male) {
                    newEmployee.setGender("M");
                    if(mode.equals("Update")){
                        oldEmployee.setGender("M");
                    }
                } else if (checkedId == R.id.radio_female) {
                    newEmployee.setGender("F");
                    if(mode.equals("Update")){
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
               if(mode.equals("Add")) {
                   newEmployee.setFirstname(firstNameEditText.getText().toString());
                   newEmployee.setLastname(lastNameEditText.getText().toString());
                   newEmployee.setHiredate(hireDateEditText.getText().toString());
                   newEmployee.setDept(deptEditText.getText().toString());
                   employeeOps.addEmployee(newEmployee);
                   employeeOps.close();
                   Toast t = Toast.makeText(AddUpdateEmployee.this, "Employee "+ newEmployee.getFirstname() + "has been added successfully !", Toast.LENGTH_SHORT);
                   t.show();
                   Intent i = new Intent(AddUpdateEmployee.this,MainActivity.class);
                   startActivity(i);
               }else {
                   oldEmployee.setFirstname(firstNameEditText.getText().toString());
                   oldEmployee.setLastname(lastNameEditText.getText().toString());
                   oldEmployee.setHiredate(hireDateEditText.getText().toString());
                   oldEmployee.setDept(deptEditText.getText().toString());
                   employeeOps.updateEmployee(oldEmployee);
                   employeeOps.close();
                   Toast t = Toast.makeText(AddUpdateEmployee.this, "Employee "+ oldEmployee.getFirstname() + " has been updated successfully !", Toast.LENGTH_SHORT);
                   t.show();
                   Intent i = new Intent(AddUpdateEmployee.this,MainActivity.class);
                   startActivity(i);

               }


            }
        });


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

}


