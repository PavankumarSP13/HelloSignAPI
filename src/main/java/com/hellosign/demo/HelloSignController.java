package com.hellosign.demo;

import java.io.IOException;

import javax.ws.rs.Produces;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hellosign.openapi.ApiException;
import com.hellosign.openapi.model.FileResponse;
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

	@PostMapping(value = "/hellosign/webhook")
	public String handleRequestsFromHelloSign(@RequestParam String json) throws HelloSignException, JSONException {
		LOGGER.info("Invoked handleRequestsFromHelloSign");
		return helloSignService.handleRequestsFromHelloSign(json);
	}

	@ResponseBody
	@GetMapping("download/{signatureRequestId}")
	@Produces("application/json")
	public FileResponse download(@PathVariable String signatureRequestId) throws ApiException, IOException {
		LOGGER.info("Invoked Download API");
		FileResponse result = helloSignService.download(signatureRequestId);
		return result;
	}

	@PostMapping(value = "/sendformWithImage", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<ResponseMessage> sendFormWithImage(@RequestPart("properties") SendFormRequest req,
			@RequestPart("file") MultipartFile file) {
		String message = "";
		try {
			message = helloSignService.sendFormWithImage(req, file);
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (Exception e) {
			message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}
}
