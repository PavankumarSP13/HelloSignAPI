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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${HelloSignImagePath}")
    private String imagePath;

    @Value("${HelloSignFontPath}")
    private String fontPath;

    @Value("${HelloSignComprehensiveFilePath}")
    private String comFilePath;

    @Value("${HelloSignClerkshipFilePath}")
    private String clerkFilePath;

    @Value("${HelloSignPortfolioFilePath}")
    private String portFilePath;

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
			String emailBlurb = "Please complete this"+ formName+ " for: " + subjectLine + "; " + clinicalSite
					+ " at the end of this rotation";
			String emailSubject = subjectLine + "; " + clinicalRotation + "; " + formName + "; " + clinicalSite;

//             Custom Fields
			Map<String, String> customFields = getCustomFields(fname, lname, clinicalRotation, clinicalSite);

			TemplateSignatureRequest request = new TemplateSignatureRequest();
			request.setTitle(formName);
			request.setSubject(emailSubject);
			request.setMessage(emailBlurb);
			request.setSigner("DME", req.getStudentEmail(), fname);
//			if (req.getCcEmail() != null)
//				request.setCC("DME", req.getCcEmail());
			request.setClientId(CLIENT_ID);
			request.setTemplateId(req.getTemplateId());
			request.setTestMode(true);
			request.setCustomFields(customFields);

            // Image
            byte[] arr = prepareDocument(req.getFormID(), req);
            List<Document> docs = new ArrayList<>();
            Document d = new Document();
            File outFile = null;
            if (req.getFormID() == 1) {
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

    /**
     * This method is used to generate the custom fields to be sent to HS
     *
     * @param fname
     * @param lname
     * @param clinicalRotation
     * @param clinicalSite
     * @return
    */

	private Map<String, String> getCustomFields(String fname, String lname, String clinicalRotation,
			String clinicalSite) {
    Map<String, String> customFields = new HashMap<>();
		customFields.put("StudentName", fname + " " + lname);
		customFields.put("txtStudentID", "13");
		customFields.put("ClinicalRotation", clinicalRotation);
		customFields.put("StartDate", "07/01/2022");
		customFields.put("EndDate", "07/28/2022");
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
            byte[] arr = prepareDocument(req.getFormID(), req);
            List<Document> docs = new ArrayList<>();
            Document d = new Document();

            if (req.getFormID() == HelloSignConstants.STUDENT_CLERKSHIP_EVALUATION_FORM_ID) {
                FileUtils.writeByteArrayToFile(new File(clerkFilePath), arr);
                d.setFile(new File(clerkFilePath));
                docs.add(d);
                request.setDocuments(docs);
            } else if (req.getFormID() == HelloSignConstants.COMPREHENSIVE_STUDENT_CLERKSHIP_FORM_ID) {
//                FileUtils.writeByteArrayToFile(new File("D:\\HelloSignAPI\\HelloSignAPI\\src\\main\\resources\\ComprehensiveStudentClerkshipAssessmentForm.pdf"), arr);
                FileUtils.writeByteArrayToFile(new File(comFilePath), arr);
                d.setFile(new File(comFilePath));
                docs.add(d);
            } else if (req.getFormID() == HelloSignConstants.STUDENT_PORTFOLIO_FORM_ID) {
                FileUtils.writeByteArrayToFile(new File(portFilePath), arr);
                d.setFile(new File(portFilePath));
                docs.add(d);
            }


            HelloSignClient client = new HelloSignClient(API_KEY);
//			client.sendSignatureRequest(request);

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
            case 4:
                inputForm = HelloSignConstants.STUDENT_PORTFOLIO_TEMPLATE_PATH;
                break;
            case 5:
                inputForm = HelloSignConstants.COMPREHENSIVE_STUDENT_CLERKSHIP_ASSESSMENT_TEMPLATE_PATH;
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
            } else if (formId == 4) {
                cb = getContentByteForStudentPortfolioForm(pdfStamper, req);
            } else if (formId == 5) {
                cb = getContentByteForComprehensiveStudentClerkshipForm(pdfStamper, req);
            } else
                throw new Exception("Invalid FormID");

            byte[] imageFile = null;
            File file;
            LOGGER.info("fetching the image and file");
//			String imagePath = "C://Users/faizanahmed.khan/Desktop/StudentPhotos/faiz";
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
                if (formId == HelloSignConstants.STUDENT_CLERKSHIP_EVALUATION_FORM_ID)
                image.setAbsolutePosition(29, 500);
                else
                image.setAbsolutePosition(34, 503);
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
//			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";

            BaseFont bf;
            bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

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

    private PdfContentByte getContentByteForStudentPortfolioForm(PdfStamper pdfStamper, SendFormRequest req)
            throws DocumentException, IOException {
        try {
            PdfContentByte cb = pdfStamper.getOverContent(1);
//			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";
            BaseFont bf;
            bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            cb.setFontAndSize(bf, 10);
            cb.beginText();
            // StudentName
            cb.showTextAligned(Element.ALIGN_LEFT, req.getFirstName() + " " + req.getLastName(), 192, 630, 0);
            // StudentId
            cb.showTextAligned(Element.ALIGN_LEFT, req.getTxtStudentID(), 400, 630, 0);
            // Clinical Rotation
            cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalRotation(), 192, 595, 0);
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
            cb.showTextAligned(Element.ALIGN_LEFT, req.getClinicalSite(), 192, 540, 0);
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
//			String font = "C:\\Users\\Faizanahmed.khan\\Downloads\\arial-unicode-ms.ttf";

            BaseFont bf;
            bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

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
