package com.hellosign.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			return "NOT IMPLEMENTED	";
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
			byte[] arr = prepareDocument(1, req);
			List<Document> docs = new ArrayList<>();
			Document d = new Document();

			FileUtils.writeByteArrayToFile(new File("C://Users/faizanahmed.khan/Desktop/StudentPhotos/d.pdf"), arr);
			d.setFile(new File("C://Users/faizanahmed.khan/Desktop/StudentPhotos/d.pdf"));
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
			} else
				throw new Exception("Invalid FormID");

			byte[] imageFile = null;
			File file;
			LOGGER.info("fetching the image and file");
			String imagePath = "C://Users/faizanahmed.khan/Desktop/StudentPhotos/faiz";
			file = new File(imagePath + ".jpg");
			imageFile = FileUtils.readFileToByteArray(file);

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
				cb.addImage(image);
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
