package de.unirostock.sems.masymos.data;

import org.apache.commons.lang3.StringUtils;


public class PersonWrapper {

	private String firstName;
	private String lastName;
	private String email;
	private String organization;
	
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public PersonWrapper(String firstName, String lastName, String email, String organization){
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.organization = organization;
	}
	
	//call to ensure that all properties are set, avoiding error when storing in neo4j
	public void repairNullStrings(){
		if (StringUtils.isEmpty(firstName)) firstName="";
		if (StringUtils.isEmpty(lastName)) lastName = "";
		if (StringUtils.isEmpty(email)) email="";
		if (StringUtils.isEmpty(organization)) organization="";
		
	}
	public boolean isValid() {
		return (StringUtils.isNotBlank(firstName) ||  StringUtils.isNotBlank(lastName));
	}
	
}

