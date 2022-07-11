package com.hellosign.demo;

import com.hellosign.sdk.HelloSignClient;
import com.hellosign.sdk.HelloSignException;
import com.hellosign.sdk.resource.SignatureRequest;
import com.hellosign.sdk.resource.TemplateSignatureRequest;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class HelloSignService {

    private String imagePath = "C://Users/pavankumar.sp/Desktop/HOME/StudentPhotos";

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(HelloSignService.class);

    @Autowired
    private Environment env;

    public String sendForm(String templateId) throws HelloSignException {
    try {

        SignatureRequest request = new SignatureRequest();
        request.setTitle("Demo Title");
        request.setSubject("Demo Subject");
        request.setMessage("Please sign this form");
        request.addSigner("pavankumar.sp@Triconinfotech.com", "Pavan");
        request.setClientId("f8ff965827dfae4528fc388077a8bd93");
//           request.setTemplateId("2ee2c69f376952a1f18c3909dbc8875f523d4b2d");
//            request.setCustomFieldValue("StudentName","Pavan");
//            request.setCustomFieldValue("txtStudentID","13");
//            request.setCustomFieldValue("ClinicalRotation","Core");
//            request.setCustomFieldValue("StartDate","07/07/2022");
//            request.setCustomFieldValue("EndDate","07/28/2022");
//            request.setCustomFieldValue("ClinicalRotationSite","Apollo");

//            StudentName,txtStudentID,ClinicalRotation,StartDate,EndDate,ClinicalRotationSite
        request.setTestMode(true);

        HelloSignClient client = new HelloSignClient(
                "ac2685093dd7cc99066561d0446e49ffd7c9d5179cec3797b3a65ba86a53e934");
        SignatureRequest newRequest = client.sendSignatureRequest(request);

        return "Sent";
    } catch (Exception e) {
        e.printStackTrace();
        return "Exception";
    }
    }

    private byte[] prepareDocument(int formId) throws IOException, DocumentException {
        String inputForm = "";
        switch (formId) {
            case 1:
                inputForm = HelloSignConstants.STUDENT_CLERKSHIP_EVALUATION_TEMPLATE_PATH;
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
        byte[] imageFile = null;
        File file;
        LOGGER.info("fetching the image and file");
        long startTime = System.currentTimeMillis();
        try {
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
            image.setAbsolutePosition(29, 470);
            contentByte.addImage(image);
        }
        pdfStamper.close();
        return byteArrayOutputStream.toByteArray();
    }


}



