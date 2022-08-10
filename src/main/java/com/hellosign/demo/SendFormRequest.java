package com.hellosign.demo;

public class SendFormRequest {

	String firstName;
	String lastName;
	String clinicalRotation;
	String clinicalSite;
	String templateId;
	String studentEmail;
	String ccEmail;
	String txtStudentID;
	String startDate;
	String endDate;

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}

	String formName;
	int formId;

	public String getAscii() {
		return ascii;
	}

	public void setAscii(String ascii) {
		this.ascii = ascii;
	}

	String ascii;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTxtStudentID() {
        return txtStudentID;
    }

    public void setTxtStudentID(String txtStudentID) {
        this.txtStudentID = txtStudentID;
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
