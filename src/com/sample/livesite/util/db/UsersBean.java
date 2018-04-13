package com.sample.livesite.util.db;

import java.io.Serializable;
import java.util.Date;

public class UsersBean implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String segment;

    
    public UsersBean() {
    	email = "";
    	password = "";
    	firstName = "";
    	lastName ="";
    	birthDate = null;
    	segment ="";
    }
    
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public Date getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    public String getSegment() {
        return segment;
    }
    public void setSegment(String segment) {
        this.segment = segment;
    }
    
}