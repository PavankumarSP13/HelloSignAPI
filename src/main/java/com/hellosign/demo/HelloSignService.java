package com.hellosign.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hellosign.sdk.resource.TemplateSignatureRequest;
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
import com.hellosign.sdk.resource.SignatureRequest;
import com.hellosign.sdk.resource.support.Document;
import com.hellosign.sdk.resource.support.Signer;
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
			String Id= req.getTxtStudentID();
			String startDate = req.getStartDate();
			String endDate = req.getEndDate();

			String emailBlurb = "Please complete this" + formName + " for: " + subjectLine + "; " + clinicalSite
					+ " at the end of this rotation";
			String emailSubject = subjectLine + "; " + clinicalRotation + "; " + formName + "; " + clinicalSite;

			// Custom Fields
			Map<String, String> customFields = getCustomFields(fname, lname, clinicalRotation, clinicalSite, Id, startDate, endDate);

			TemplateSignatureRequest request = new TemplateSignatureRequest();
			request.setTitle(formName);
			request.setSubject(emailSubject);
			request.setMessage(emailBlurb);
			request.setSigner("DME", req.getStudentEmail(), fname);
//            if (req.getCcEmail() != null)
//                request.setCC("DME", req.getCcEmail());
			request.setClientId(CLIENT_ID);
			request.setTemplateId(req.getTemplateId());
			request.setTestMode(true);
			request.setCustomFields(customFields);

			// Image
			byte[] arr = prepareDocument(req.getFormId(), req);
			List<Document> docs = new ArrayList<>();
			Document d = new Document();
			File outFile = null;
			if (req.getFormId() == 1) {
				outFile = new File("./output/StudentClerkshipEvaluationForm.pdf");
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
			String fname = req.getFirstName();
			String lname = req.getLastName();
			String clinicalSite = req.getClinicalSite();
			String clinicalRotation = req.getClinicalRotation();
			String subjectLine = lname + ", " + fname;
			String formName = req.getFormName();
			String emailBlurb = "Please complete this" + formName + " for: " + subjectLine + "; " + clinicalSite
					+ " at the end of this rotation";
			String emailSubject = subjectLine + "; " + clinicalRotation + "; " + formName + "; " + clinicalSite;

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
			request.setTestMode(true);
			request.addCC(req.getCcEmail());

			// Image
			byte[] arr = prepareDocument(req.getFormId(), req);
			List<Document> docs = new ArrayList<>();
			Document d = new Document();
			File outFile = null;
			if (req.getFormId() == 1) {
				outFile = new File("./output/StudentClerkshipEvaluationForm.pdf");
			} else if (req.getFormId() == 3) {
				outFile = new File("./output/MidClerkshipAssessmentForm.pdf");
			} else if (req.getFormId() == 4) {
				outFile = new File("./output/StudentFacultyEvaluationForm.pdf");
			} else if (req.getFormId() == 6) {
				outFile = new File("./output/AUA_Formative_OSCE.pdf");
			}
			FileUtils.writeByteArrayToFile(outFile, arr);
			d.setFile(outFile);
			docs.add(d);
			request.setDocuments(docs);
			HelloSignClient client = new HelloSignClient(API_KEY);
			client.sendSignatureRequest(request);

			return "Sent";

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private byte[] prepareDocument(int formId, SendFormRequest req) throws Exception {
		String inputForm = "";
		switch (formId) {
			case 1:
				inputForm = HelloSignConstants.STUDENT_CLERKSHIP_EVALUATION_TEMPLATE_PATH;
				break;
			case 2:
				inputForm = HelloSignConstants.COMPREHENSIVE_CLERKSHIP_TEMPLATE_PATH;
				break;
			case 3:
				inputForm = HelloSignConstants.MID_CLERKSHIP_EVALUATION_TEMPLATE_PATH;
				break;
			case 4:
				inputForm = HelloSignConstants.STUDENT_FACULTY_EVALUATION_TEMPLATE_PATH;
				break;
			case 5:
				inputForm = HelloSignConstants.STUDENT_PORTFOLIO_TEMPLATE_PATH;
				break;
			case 6:
				inputForm = HelloSignConstants.AUA_CORE_OSCE_TEMPLATE_PATH;
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

			PdfContentByte cb = null;
			if (formId == 1) {
				cb = getContentByteForStudentClerkshipForm(pdfStamper, req);
			} else if (formId == 3) {
				cb = getContentByteForMidClerkshipForm(pdfStamper, req);
			} else if (formId == 4) {
				cb = getContentByteForStudentFacultyForm(pdfStamper, req);
			} else if (formId == 6) {
				cb = getContentByteForCoreOsceForm(pdfStamper, req);
			} else if (formId == 5) {
				cb = getContentByteForStudentPortfolioForm(pdfStamper, req);
			} else if (formId == 2) {
				cb = getContentByteForComprehensiveStudentClerkshipForm(pdfStamper, req);
			} else {
				throw new Exception("Invalid FormID");
			}

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

	private PdfContentByte getContentByteForStudentClerkshipForm(PdfStamper pdfStamper, SendFormRequest req)
			throws DocumentException, IOException {
		try {
			PdfContentByte cb = pdfStamper.getOverContent(1);
			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";
			BaseFont bf;
			bf = BaseFont.createFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			cb.setFontAndSize(bf, 10);
			cb.beginText();
			// StudentName
			cb.showTextAligned(Element.ALIGN_LEFT, req.getFirstName() + " " + req.getLastName(), 190, 620, 0);
			// StudentId
			cb.showTextAligned(Element.ALIGN_LEFT, req.getTxtStudentID(), 400, 620, 0);
			// Clinical Rotation
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalRotation(), 190, 585, 0);
			// CheckBox
			if (req.getClinicalRotation().equalsIgnoreCase("Core"))
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 563, 0);
			else
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 550, 0);
			// StartDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getStartDate(), 387, 560, 0);
			// EndDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getEndDate(), 480, 560, 0);
			// Clinical Site
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalSite(), 190, 530, 0);
			cb.endText();
			return cb;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private PdfContentByte getContentByteForStudentPortfolioForm(PdfStamper pdfStamper, SendFormRequest req) throws DocumentException, IOException{
		try {
			PdfContentByte cb = pdfStamper.getOverContent(1);
			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";
			BaseFont bf = BaseFont.createFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			cb.setFontAndSize(bf, 10);
			cb.beginText();
			// StudentName
			cb.showTextAligned(Element.ALIGN_LEFT, req.getFirstName() + " " + req.getLastName(), 200, 630, 0);
			// StudentId
			cb.showTextAligned(Element.ALIGN_LEFT, req.getTxtStudentID(), 400, 630, 0);
			// Clinical Rotation
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalRotation(), 200, 595, 0);
			// CheckBox
			if (req.getClinicalRotation().equalsIgnoreCase("Core"))
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 573, 0);
			else
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 560, 0);
			// StartDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getStartDate(), 387, 570, 0);
			// EndDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getEndDate(), 480, 570, 0);
			// Clinical Site
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalSite(), 200, 540, 0);
			cb.endText();
			return cb;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private PdfContentByte getContentByteForMidClerkshipForm(PdfStamper pdfStamper, SendFormRequest req)
			throws DocumentException, IOException {
		try {
			PdfContentByte cb = pdfStamper.getOverContent(1);
			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";
			BaseFont bf;
			bf = BaseFont.createFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			cb.setFontAndSize(bf, 10);
			cb.beginText();
			// StudentName
			cb.showTextAligned(Element.ALIGN_LEFT, req.getFirstName() + " " + req.getLastName(), 190, 610, 0);
			// StudentId
			cb.showTextAligned(Element.ALIGN_LEFT, req.getTxtStudentID(), 400, 610, 0);
			// Clinical Rotation
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalRotation(), 190, 577, 0);
			// CheckBox
			if (req.getClinicalRotation().equalsIgnoreCase("Core"))
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 555, 0);
			else
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 542, 0);
			// StartDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getStartDate(), 387, 553, 0);
			// EndDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getEndDate(), 480, 553, 0);
			// Clinical Site
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalSite(), 190, 522, 0);
			cb.endText();
			return cb;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private PdfContentByte getContentByteForComprehensiveStudentClerkshipForm(PdfStamper pdfStamper, SendFormRequest req)
			throws DocumentException, IOException {
		try {
			PdfContentByte cb = pdfStamper.getOverContent(1);
			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";

			BaseFont bf;
			bf = BaseFont.createFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			cb.setFontAndSize(bf, 10);
			cb.beginText();
			// StudentName
			cb.showTextAligned(Element.ALIGN_LEFT, req.getFirstName() + " " + req.getLastName(), 198, 630, 0);
			// StudentId
			cb.showTextAligned(Element.ALIGN_LEFT, req.getTxtStudentID(), 405, 630, 0);
			// Clinical Rotation
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalRotation(), 198, 596, 0);
			// CheckBox
			if (req.getClinicalRotation().equalsIgnoreCase("Core"))
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 291, 574, 0);
			else
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 291, 561, 0);
			// StartDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getStartDate(), 391, 572, 0);
			// EndDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getEndDate(), 484, 572, 0);
			// Clinical Site
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalSite(), 198, 540, 0);
			cb.endText();
			return cb;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private PdfContentByte getContentByteForStudentFacultyForm(PdfStamper pdfStamper, SendFormRequest req)
			throws DocumentException, IOException {
		try {
			PdfContentByte cb = pdfStamper.getOverContent(1);
			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";
			BaseFont bf;
			bf = BaseFont.createFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			cb.setFontAndSize(bf, 10);
			cb.beginText();
			// StudentName
			cb.showTextAligned(Element.ALIGN_LEFT, req.getFirstName() + " " + req.getLastName(), 190, 620, 0);
			// StudentId
			cb.showTextAligned(Element.ALIGN_LEFT, req.getTxtStudentID(), 400, 620, 0);
			// Clinical Rotation
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalRotation(), 190, 585, 0);
			// CheckBox
			if (req.getClinicalRotation().equalsIgnoreCase("Core"))
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 563, 0);
			else
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 286, 550, 0);
			// StartDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getStartDate(), 387, 560, 0);
			// EndDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getEndDate(), 480, 560, 0);
			// Clinical Site
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalSite(), 190, 530, 0);
			cb.endText();
			return cb;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private PdfContentByte getContentByteForCoreOsceForm(PdfStamper pdfStamper, SendFormRequest req)
			throws DocumentException, IOException {
		try {
			PdfContentByte cb = pdfStamper.getOverContent(1);
			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";
			BaseFont bf;
			bf = BaseFont.createFont(font, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

			cb.setFontAndSize(bf, 10);
			cb.beginText();
			// StudentName
			cb.showTextAligned(Element.ALIGN_LEFT, req.getFirstName() + " " + req.getLastName(), 190, 610, 0);
			// StudentId
			cb.showTextAligned(Element.ALIGN_LEFT, req.getTxtStudentID(), 400, 610, 0);
			// Clinical Rotation
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalRotation(), 190, 575, 0);
			// CheckBox
			if (req.getClinicalRotation().equalsIgnoreCase("Core"))
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 333, 585, 0);
			else
				cb.showTextAligned(Element.ALIGN_LEFT, req.getAscii(), 333, 572, 0);
			// StartDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getStartDate(), 427, 573, 0);
			// EndDate
			cb.showTextAligned(Element.ALIGN_LEFT, req.getEndDate(), 523, 573, 0);
			// Clinical Site
			cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalSite(), 190, 528, 0);
			cb.endText();
			return cb;
		} catch (DocumentException | IOException e) {
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