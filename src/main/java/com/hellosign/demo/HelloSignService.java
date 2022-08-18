package com.hellosign.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.hellosign.openapi.ApiClient;
import com.hellosign.openapi.ApiException;
import com.hellosign.openapi.Configuration;
import com.hellosign.openapi.api.SignatureRequestApi;
import com.hellosign.openapi.api.TemplateApi;
import com.hellosign.openapi.auth.HttpBasicAuth;
import com.hellosign.openapi.model.SignatureRequestGetResponse;
import com.hellosign.openapi.model.SignatureRequestSendWithTemplateRequest;
import com.hellosign.openapi.model.SubCustomField;
import com.hellosign.openapi.model.SubSignatureRequestTemplateSigner;
import com.hellosign.openapi.model.SubSigningOptions;
import com.hellosign.openapi.model.TemplateUpdateFilesRequest;
import com.hellosign.openapi.model.TemplateUpdateFilesResponse;
import com.hellosign.sdk.HelloSignClient;
import com.hellosign.sdk.HelloSignException;
import com.hellosign.sdk.resource.Event;
import com.hellosign.sdk.resource.TemplateSignatureRequest;
import com.hellosign.sdk.resource.support.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

@Service
public class HelloSignService {

	private static final Logger LOGGER = LoggerFactory.getLogger(HelloSignService.class);
	private static final String API_KEY = "ac2685093dd7cc99066561d0446e49ffd7c9d5179cec3797b3a65ba86a53e934";
	private static final String CLIENT_ID = "f8ff965827dfae4528fc388077a8bd93";

	/**
	 * This method sends the details using the TemplateID to HS
	 *
	 * @param templateId
	 * @param fname
	 * @param lname
	 * @param clinicalRotation
	 * @param clinicalSite
	 * @param email
	 * @return
	 * @throws HelloSignException
	 */
	public String sendFormUsingTemplateId(SendFormRequest req) throws HelloSignException {
		try {
			String fname = req.getFirstName();
			String lname = req.getLastName();
			String clinicalSite = req.getClinicalSite();
			String clinicalRotation = req.getClinicalRotation();
			String subjectLine = lname + ", " + fname;
			String formName = req.getFormName();
			String Id = req.getTxtStudentID();
			String startDate = req.getStartDate();
			String endDate = req.getEndDate();

			String emailBlurb = "Please complete this" + formName + " for: " + subjectLine + "; " + clinicalSite
					+ " at the end of this rotation";

			// Custom Fields
			Map<String, String> customFields = getCustomFields(fname, lname, clinicalRotation, clinicalSite, Id,
					startDate, endDate);

			TemplateSignatureRequest request = new TemplateSignatureRequest();
			request.setTitle(formName);

			request.setMessage(emailBlurb);
			request.setSigner("Student", req.getStudentEmail(), fname);
			request.setClientId(CLIENT_ID);
			request.setTemplateId(req.getTemplateId());
			request.setTestMode(true);
			request.setCustomFields(customFields);

			// Image
			byte[] arr = prepareDocument(req.getFormId());
			List<Document> docs = new ArrayList<>();
			Document d = new Document();
			File outFile = null;
			if (req.getFormId() == 1) {
				outFile = new File("./out/StudentClerkshipEvaluationForm.pdf");
			}
			FileUtils.writeByteArrayToFile(outFile, arr);
			d.setFile(outFile);
			docs.add(d);
			request.setDocuments(docs);

			HelloSignClient client = new HelloSignClient(API_KEY);
			client.sendTemplateSignatureRequest(request);
			return "Sent";
		} catch (Exception e) {
			e.printStackTrace();
			return "Exception";
		}
	}

	private Map<String, String> getCustomFields(String fname, String lname, String clinicalRotation,
			String clinicalSite, String txtStudentID, String startDate, String endDate) {
		Map<String, String> customFields = new HashMap<>();
		customFields.put("StudentName", fname + " " + lname);
		customFields.put("txtStudentID", txtStudentID);
		customFields.put("ClinicalRotation", clinicalRotation);
		customFields.put("StartDate", startDate);
		customFields.put("EndDate", endDate);
		customFields.put("ClinicalRotationSite", clinicalSite);
		if (clinicalRotation.equalsIgnoreCase("Elective"))
			customFields.put("chkElective", "true");
		else
			customFields.put("chkCore", "true");
		return customFields;
	}

	/**
	 * This method sends the details with image to HS
	 *
	 * @param fname
	 * @param lname
	 * @param clinicalRotation
	 * @param clinicalSite
	 * @param email
	 * @return
	 * @throws Exception
	 */
	public String sendFormWithImage(SendFormRequest req) throws Exception {
		try {
			// Update Template Request
			String updatedTemplateID = updateTemplateRequest(req);

			if (null == updatedTemplateID) {
				return "Unable to Send";
			}

			Thread.sleep(5000);

			String fname = req.getFirstName();
			String lname = req.getLastName();
			String clinicalSite = req.getClinicalSite();
			String clinicalRotation = req.getClinicalRotation();
			String subjectLine = lname + ", " + fname;
			String formName = req.getFormName();
			String id = req.getTxtStudentID();
			String startDate = req.getStartDate();
			String endDate = req.getEndDate();

			String emailBlurb = "Please complete this" + formName + " for: " + subjectLine + "; " + clinicalSite
					+ " at the end of this rotation";
			String emailSubject = subjectLine + "; " + clinicalRotation + "; " + formName + "; " + clinicalSite;

			// Custom Fields
//			Map<String, String> customFields = getCustomFields(fname, lname, clinicalRotation, clinicalSite, id,
//					startDate, endDate);
			
			ApiClient defaultClient = Configuration.getDefaultApiClient();
			SignatureRequestApi api = new SignatureRequestApi(defaultClient);

			SubSignatureRequestTemplateSigner signer1 = new SubSignatureRequestTemplateSigner().role("Student")
					.emailAddress(req.getStudentEmail()).name("George");
			ArrayList<SubCustomField> customFields = new ArrayList<>();

			SubCustomField customField1 = new SubCustomField().name("StudentName").value(fname + " " + lname);
			SubCustomField customField2 = new SubCustomField().name("txtStudentID").value(id);
			SubCustomField customField3 = new SubCustomField().name("ClinicalRotation").value(clinicalRotation);
			SubCustomField customField4 = new SubCustomField().name("StartDate").value(startDate);
			SubCustomField customField5 = new SubCustomField().name("EndDate").value(endDate);
			SubCustomField customField7 = new SubCustomField().name("ClinicalRotationSite").value(clinicalSite);
			SubCustomField customField6 = new SubCustomField();
			if (clinicalRotation.equalsIgnoreCase("Elective")) {
				customField6.name("chkElective").value("true");
			} else {
				customField6.name("chkCore").value("true");
			}
			customFields.add(customField1);
			customFields.add(customField2);
			customFields.add(customField3);
			customFields.add(customField4);
			customFields.add(customField5);
			customFields.add(customField6);
			customFields.add(customField7);

			SubSigningOptions signingOptions = new SubSigningOptions().draw(true).type(true).upload(true).phone(false)
					.defaultType(SubSigningOptions.DefaultTypeEnum.DRAW);

			SignatureRequestSendWithTemplateRequest data = new SignatureRequestSendWithTemplateRequest()
					.templateIds(List.of(updatedTemplateID)).subject(emailSubject).title(formName)
					.message(emailBlurb).signers(List.of(signer1))
					.customFields(customFields).signingOptions(signingOptions).testMode(true);

			try {
				SignatureRequestGetResponse result = api.signatureRequestSendWithTemplate(data);
				System.out.println(result);
			} catch (ApiException e) {
				System.err.println("Exception when calling AccountApi#accountCreate");
				System.err.println("Status code: " + e.getCode());
				System.err.println("Reason: " + e.getResponseBody());
				System.err.println("Response headers: " + e.getResponseHeaders());
				e.printStackTrace();
			}
			
			
			
	//		TemplateSignatureRequest request = new TemplateSignatureRequest();
//			request.setTitle(formName);
//			request.setSubject(emailSubject);
//			request.setMessage(emailBlurb);
//			request.setSigner("Student", req.getStudentEmail(), fname);
//			request.setClientId(CLIENT_ID);
//			request.setTemplateId(updatedTemplateID);
//			request.setTestMode(true);
//			request.setCustomFields(customFields);

//			HelloSignClient client = new HelloSignClient(API_KEY);
//			client.sendTemplateSignatureRequest(data);

			// Delete the template
			deleteTemplate(updatedTemplateID);

			return "Sent";

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}



	private void init() {

		ApiClient defaultClient = Configuration.getDefaultApiClient();
		// Configure HTTP basic authorization: api_key
		HttpBasicAuth api_key = (HttpBasicAuth) defaultClient.getAuthentication("api_key");
		api_key.setUsername(API_KEY);
	}

	private String updateTemplateRequest(SendFormRequest req) throws Exception {

		ApiClient defaultClient = Configuration.getDefaultApiClient();
		init();
		// Stamp image and data
		byte[] arr = prepareDocument(req.getFormId());
		File outFile = null;
		outFile = new File("./out/StudentClerkshipEvaluationForm.pdf");

		FileUtils.writeByteArrayToFile(outFile, arr);

		TemplateUpdateFilesRequest data = new TemplateUpdateFilesRequest().file(List.of(outFile));

		try {
			TemplateApi api = new TemplateApi(defaultClient);
			TemplateUpdateFilesResponse result = api.templateUpdateFiles(req.templateId, data);
			System.out.println(result);
			return result.getTemplate().getTemplateId();
		} catch (ApiException e) {
			System.err.println("Exception when calling AccountApi#accountCreate");
			System.err.println("Status code: " + e.getCode());
			System.err.println("Reason: " + e.getResponseBody());
			System.err.println("Response headers: " + e.getResponseHeaders());
			e.printStackTrace();
		}
		return null;
	}

	private void deleteTemplate(String templateId) throws Exception {

		ApiClient defaultClient = Configuration.getDefaultApiClient();

		// Configure HTTP basic authorization: api_key
		init();

		TemplateApi api = new TemplateApi(defaultClient);

		try {
			api.templateDelete(templateId);
		} catch (ApiException e) {
			System.err.println("Exception when calling AccountApi#accountCreate");
			System.err.println("Status code: " + e.getCode());
			System.err.println("Reason: " + e.getResponseBody());
			System.err.println("Response headers: " + e.getResponseHeaders());
			e.printStackTrace();
		}

	}

	private byte[] prepareDocument(int formId) throws Exception {
		String inputForm = "";
		switch (formId) {
		case 1:
			inputForm = HelloSignConstants.STUDENT_CLERKSHIP_EVALUATION_TEMPLATE_PATH;
			break;

		default:
			break;
		}

		try {
			byte[] templateBytes = IOUtils.toByteArray(new ClassPathResource(inputForm).getInputStream());
			PdfReader pdfReader = new PdfReader(templateBytes);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			PdfStamper pdfStamper = null;
			pdfStamper = new PdfStamper(pdfReader, byteArrayOutputStream);

			PdfContentByte cb = pdfStamper.getOverContent(1);
			byte[] imageFile = null;
			File file;
			LOGGER.info("fetching the image and file");
			String imagePath = "C://Users/faizanahmed.khan/Desktop/StudentPhotos/faiz";
			file = new File(imagePath + ".jpg");
			imageFile = FileUtils.readFileToByteArray(file);

			if (imageFile != null) {
				if (formId != 6) {
					Image image = Image.getInstance(imageFile);
					image.scaleAbsoluteHeight(150);
					image.scaleAbsoluteWidth(150);

					image.setBorder(1);
					image.setBorderWidth(3);
					image.setBorderWidthLeft(3);
					image.setBorderWidthRight(3);
					image.setBorderWidthBottom(3);
					image.setAbsolutePosition(29, 500);
					cb.addImage(image);

				} else {
					Image image = Image.getInstance(imageFile);
					image.scaleAbsoluteHeight(150);
					image.scaleAbsoluteWidth(150);

					image.setBorder(1);
					image.setBorderWidth(3);
					image.setBorderWidthLeft(3);
					image.setBorderWidthRight(3);
					image.setBorderWidthBottom(3);
					image.setAbsolutePosition(34, 470);
					cb.addImage(image);
				}
			}
			pdfStamper.close();
			return byteArrayOutputStream.toByteArray();
		} catch (DocumentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public String handleRequestsFromHelloSign(String requestJson) throws HelloSignException {
		JSONObject jsonObject = new JSONObject(requestJson);
		try {
			Event event = new Event(jsonObject);
			boolean validRequest = event.isValid(API_KEY);
			if (validRequest) {
				switch (event.getTypeString()) {
				case "callback_test":
					LOGGER.info("Callback Test call, eventPayload: {}", event.getTypeString());
					break;
				case "signature_request_sent":
					LOGGER.info("Signature Request Sent, eventPayload: {}", event.getTypeString());
					break;
				case "signature_request_all_signed":
					LOGGER.info("Signature Request Signed, eventPayload: {}", event.getTypeString());
					break;
				default:
					LOGGER.info("HS event occured: {}", event.getTypeString());
					break;
				}
			}
		} catch (HelloSignException e) {
			e.printStackTrace();
			throw e;
		}

		return "Hello API Event Received";
	}

}
