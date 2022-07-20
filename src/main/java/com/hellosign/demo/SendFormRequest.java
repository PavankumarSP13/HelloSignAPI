package com.hellosign.demo;

public class SendFormRequest {

	String firstName;
	String lastName;
	String clinicalRotation;
	String clinicalSite;
	String templateId;
	String studentEmail;
	String ccEmail;

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

	public String getClinicalRotation() {
		return clinicalRotation;
	}

	public void setClinicalRotation(String clinicalRotation) {
		this.clinicalRotation = clinicalRotation;
	}

	public String getClinicalSite() {
		return clinicalSite;
	}

	public void setClinicalSite(String clinicalSite) {
		this.clinicalSite = clinicalSite;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getStudentEmail() {
		return studentEmail;
	}

	public void setStudentEmail(String studentEmail) {
		this.studentEmail = studentEmail;
	}
	
	public String getCcEmail() {
		return ccEmail;
	}

	public void setCcEmail(String ccEmail) {
		this.ccEmail = ccEmail;
	}
}
