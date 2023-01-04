package com.hellosign.demo;

import lombok.Data;

@Data
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
	String formName;
	int formId;

}