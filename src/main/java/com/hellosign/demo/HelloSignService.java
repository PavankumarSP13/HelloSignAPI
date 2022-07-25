package com.hellosign.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hellosign.sdk.resource.SignatureRequest;
import com.hellosign.sdk.resource.support.Signer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.hellosign.sdk.HelloSignClient;
import com.hellosign.sdk.HelloSignException;
import com.hellosign.sdk.resource.Event;
import com.hellosign.sdk.resource.TemplateSignatureRequest;
import com.hellosign.sdk.resource.support.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
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
//			String fname = req.getFirstName();
//			String lname = req.getLastName();
//			String clinicalSite = req.getClinicalSite();
//			String clinicalRotation = req.getClinicalRotation();
//			String subjectLine = lname + ", " + fname;
//			String formName = "STUDENT_CLERKSHIP_EVALUATION_FORM";
//			String emailBlurb = "Please complete this Student Portfolio for: " + subjectLine + "; " + clinicalSite
//					+ " at the end of this rotation";
//			String emailSubject = subjectLine + "; " + clinicalRotation + "; " + formName + "; " + clinicalSite;

			// Custom Fields
//			Map<String, String> customFields = getCustomFields(fname, lname, clinicalRotation, clinicalSite);
//
//			TemplateSignatureRequest request = new TemplateSignatureRequest();
//			request.setTitle(formName);
//			request.setSubject(emailSubject);
//			request.setMessage(emailBlurb);
//			request.setSigner("DME", req.getStudentEmail(), fname);
//			if (req.getCcEmail() != null)
//				request.setCC("DME", req.getCcEmail());
//			request.setClientId(CLIENT_ID);
//			request.setTemplateId(req.getTemplateId());
//			request.setTestMode(true);
//			request.setCustomFields(customFields);
//
//			HelloSignClient client = new HelloSignClient(API_KEY);
//			client.sendTemplateSignatureRequest(request);
			return "Sent";
		} catch (Exception e) {
			e.printStackTrace();
			return "Exception";
		}
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
			String fname = req.getFirstName();
			String lname = req.getLastName();
			String clinicalSite = req.getClinicalSite();
			String clinicalRotation = req.getClinicalRotation();
			String subjectLine = lname + ", " + fname;
			String formName = "STUDENT_CLERKSHIP_EVALUATION_FORM";
			String emailBlurb = "Please complete this Student Portfolio for: " + subjectLine + "; " + clinicalSite
					+ " at the end of this rotation";
			String emailSubject = subjectLine + "; " + clinicalRotation + "; " + formName + "; " + clinicalSite;

			// Custom Fields
//			Map<String, String> customFields = getCustomFields(fname, lname, clinicalRotation, clinicalSite);

			// Using signature Request
			SignatureRequest request = new SignatureRequest();
			List<Signer> signers = new ArrayList<>();
			Signer signer = new Signer(req.getStudentEmail(), "DME");
			signers.add(signer);
			request.setTitle(formName);
			request.setSubject(emailSubject);
			request.setMessage(emailBlurb);
			request.setSigners(signers);
			request.setClientId(CLIENT_ID);
			request.setTestMode(true);
			request.setCustomFields(customFields);
			request.setTestMode(true);

			// Using Template Signature Request
//			TemplateSignatureRequest request = new TemplateSignatureRequest();
//			request.setTitle(formName);
//			request.setSubject(emailSubject);
//			request.setMessage(emailBlurb);
//			request.setSigner("DME", req.getStudentEmail(), fname);
////			if (req.getCcEmail() != null)
////				request.setCC("DME", req.getCcEmail());
//			request.setClientId(CLIENT_ID);
//			request.setTemplateId(req.getTemplateId());
//			request.setTestMode(true);
//			request.setCustomFields(customFields);

			// Image
			byte[] arr = prepareDocument(1,req);
			List<Document> docs = new ArrayList<>();
			Document d = new Document();

//			FileUtils.writeByteArrayToFile(new File("C://Users/pavankumar.sp/Desktop/HOME/StudentPhoto/S.pdf"), arr);
			FileUtils.writeByteArrayToFile(new File("D://Java Projects/HelloSignAPI/src/main/resources/StudentClerkshipEvaluationForm.pdf"), arr);
			d.setFile(new File("D://Java Projects/HelloSignAPI/src/main/resources/StudentClerkshipEvaluationForm.pdf"));
			docs.add(d);
			request.setDocuments(docs);

			HelloSignClient client = new HelloSignClient(API_KEY);
			client.sendSignatureRequest(request);
//			client.sendTemplateSignatureRequest(request);

			return "Sent";
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * This method is used to generate the custom fields to be sent to HS
	 * 
	 * @param fname
	 * @param lname
	 * @param clinicalRotation
	 * @param clinicalSite
	 * @return
	 */
//	private Map<String, String> getCustomFields(String fname, String lname, String clinicalRotation,
//			String clinicalSite) {
		Map<String, String> customFields = new HashMap<>();
//		customFields.put("StudentName", fname + " " + lname);
//		customFields.put("txtStudentID", "13");
//		customFields.put("ClinicalRotation", clinicalRotation);
//		customFields.put("StartDate", "07/01/2022");
//		customFields.put("EndDate", "07/28/2022");
//		customFields.put("ClinicalRotationSite", clinicalSite);
//		if (clinicalRotation.equalsIgnoreCase("Elective"))
//			customFields.put("chkElective", "true");
//		else
//			customFields.put("chkCore", "true");
//		return customFields;
//	}

	private byte[] prepareDocument(int formId,SendFormRequest req) throws IOException, DocumentException {
		String inputForm = "";
		switch (formId) {
		case 1:
			inputForm = HelloSignConstants.STUDENT_CLERKSHIP_EVALUATION_TEMPLATE_PATH;
			break;
		case 2:
			break;
		default:
			break;
		}

		byte[] templateBytes = IOUtils.toByteArray(new ClassPathResource(inputForm).getInputStream());

		PdfReader pdfReader = new PdfReader(templateBytes);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PdfStamper pdfStamper = null;
		try {
			pdfStamper = new PdfStamper(pdfReader, byteArrayOutputStream);
		} catch (DocumentException e) {
			e.printStackTrace();
			throw e;
		}

		PdfContentByte contentByte = pdfStamper.getOverContent(1);
		ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, Phrase.getInstance(req.getFirstName()+ req.getLastName()), 190, 620, 0);
        ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, Phrase.getInstance(req.getClinicalSite()), 190, 530, 0);
		ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, Phrase.getInstance(req.getClinicalRotation()), 190, 585, 0);
		ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, Phrase.getInstance(req.getTxtStudentID()), 450, 620, 0);
		ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, Phrase.getInstance(req.getAscii()), 270, 560, 0);
		ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, Phrase.getInstance(req.getStartDate()), 200, 250, 0);
		ColumnText.showTextAligned(contentByte, Element.ALIGN_LEFT, Phrase.getInstance(req.getEndDate()), 150, 200, 0);

		byte[] imageFile = null;
		File file;
		LOGGER.info("fetching the image and file");
		try {
			String imagePath = "C://Users/rinky.pavagadhi/Desktop/StudentPhotos/hs.jpg";
			file = new File(imagePath + ".jpg");
			imageFile = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		if (imageFile != null) {
			Image image = Image.getInstance(imageFile);
			image.scaleAbsoluteHeight(150);
			image.scaleAbsoluteWidth(150);

			image.setBorder(1);
			image.setBorderWidth(3);
			image.setBorderWidthLeft(3);
			image.setBorderWidthRight(3);
			image.setBorderWidthBottom(3);
			image.setAbsolutePosition(29, 500);
			contentByte.addImage(image);
		}
		pdfStamper.close();
		return byteArrayOutputStream.toByteArray();
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
