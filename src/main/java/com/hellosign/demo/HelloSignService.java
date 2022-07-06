package com.hellosign.demo;

import com.hellosign.sdk.HelloSignClient;
import com.hellosign.sdk.HelloSignException;
import com.hellosign.sdk.resource.SignatureRequest;
import com.hellosign.sdk.resource.TemplateSignatureRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class HelloSignService {
    @Autowired
    private Environment env;

    public String sendForm(String templateId) throws HelloSignException {
    try {

        TemplateSignatureRequest request = new TemplateSignatureRequest();
        request.setTitle("Demo Title");
        request.setSubject("Demo Subject");
        request.setMessage("Please sign this form");
        request.setSigner("DME", "pavankumar.sp@Triconinfotech.com", "Pavan");
        request.setClientId("f8ff965827dfae4528fc388077a8bd93");
        request.setTemplateId("2ee2c69f376952a1f18c3909dbc8875f523d4b2d");
        request.setTestMode(true);

        HelloSignClient client = new HelloSignClient(
                "ac2685093dd7cc99066561d0446e49ffd7c9d5179cec3797b3a65ba86a53e934");
        SignatureRequest newRequest = client.sendTemplateSignatureRequest(request);
        return "Sent";
    } catch (Exception e) {
        e.printStackTrace();
        return "Exception";
    }
}
}
