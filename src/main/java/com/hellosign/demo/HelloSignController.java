package com.hellosign.demo;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hellosign.sdk.HelloSignException;

@RestController
public class HelloSignController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HelloSignController.class);

	@Autowired
	HelloSignService helloSignService;

//	@PostMapping("sendformUsingTemplateId")
//	public String sendFormUsingTemplateId(@RequestBody SendFormRequest req) throws HelloSignException {
//		LOGGER.info("Invoked sendFormUsingTemplateId");
//		return helloSignService.sendFormUsingTemplateId(req);
//	}

	@PostMapping("sendformWithImage")
	public String sendFormWithImage(@RequestBody SendFormRequest req) throws Exception {
		LOGGER.info("Invoked sendFormWithImage");
		return helloSignService.sendFormWithImage(req);
	}

	@PostMapping(value = "/hellosign/webhook")
	public String handleRequestsFromHelloSign(@RequestParam String json)
			throws HelloSignException, JSONException {
		LOGGER.info("Invoked handleRequestsFromHelloSign");
		return helloSignService.handleRequestsFromHelloSign(json);

	}
}
