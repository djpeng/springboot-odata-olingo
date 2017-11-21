package com.pengdaijun.odata.olingo.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.core.ODataHandler;
import org.apache.olingo.server.core.debug.ServerCoreDebugger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pengdaijun.odata.olingo.util.ODataHttp;

//@RestController
//@RequestMapping("/odata")
public class PDJODataController {

	@Autowired
	CsdlAbstractEdmProvider abstractEdmProvider;

	@Autowired
	EntityCollectionProcessor entityCollectionProcessor;

	@RequestMapping("/demo")
	public ResponseEntity<String> demo(HttpServletRequest httpRequest) {
		try {
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(abstractEdmProvider, new ArrayList<EdmxReference>());
			ODataHandler handler = new ODataHandler(odata, edm, new ServerCoreDebugger(odata));
			handler.register(entityCollectionProcessor);

			ODataResponse response = handler.process(new ODataHttp().createODataRequest(httpRequest, 0));
			String responseStr = StreamUtils.copyToString(response.getContent(), Charset.defaultCharset());
			MultiValueMap<String, String> headers = new HttpHeaders();
			for (String key : response.getAllHeaders().keySet()) {
				headers.add(key, response.getHeader(key));
			}
			return new ResponseEntity<String>(responseStr, headers, HttpStatus.valueOf(response.getStatusCode()));
		} catch (ODataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
