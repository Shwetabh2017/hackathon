package com.hcl.informix.informixsync.Model;

public class Employee {
	private long empId;
	private String firstname;
	private String lastname;
	private String gender;
	private String hiredate;
	private String dept;

	public Employee(long empId, String firstname, String lastname, String gender, String hiredate, String dept){
		this.empId = empId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.gender = gender;
		this.hiredate = hiredate;
		this.dept = dept;

	}

	public Employee(){

	}

	public long getEmpId() {
		return empId;
	}

	public void setEmpId(long empId) {
		this.empId = empId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getHiredate() {
		return hiredate;
	}

	public void setHiredate(String hiredate) {
		this.hiredate = hiredate;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String toString(){
		return "Emp id: "+getEmpId()+ "\n" +
				"Name: "+getFirstname() + " " + getLastname() + "\n" +
				"Hire Date: "+getHiredate() + "\n" +
				"Department : "+getDept();


	}
}
