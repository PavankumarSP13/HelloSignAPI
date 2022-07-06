package com.hellosign.demo;

import com.hellosign.sdk.HelloSignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloSignController {

    private final Logger logger = LoggerFactory.getLogger(HelloSignController.class);

        @Autowired
        HelloSignService helloSignService;

        @PostMapping("sendform/{templateId}")
        public String embeddedSignatureRequest(@PathVariable String templateId) throws HelloSignException {
            return helloSignService.sendForm(templateId);
        }

}
