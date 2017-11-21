package com.pengdaijun.odata.olingo.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pengdaijun.odata.olingo.service.PDJEdmProvider;
import com.pengdaijun.odata.olingo.service.PDJEntityCollectionProcessor;
import com.pengdaijun.odata.olingo.util.PDJODataHttpHandler;

@WebServlet(urlPatterns = { "/odata/*" }, loadOnStartup = 1)
public class PDJWebServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(PDJWebServlet.class);

	protected void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			// create odata handler and configure it with CsdlEdmProvider and
			// Processor
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(new PDJEdmProvider(), new ArrayList<EdmxReference>());
//			ODataHttpHandler handler = odata.createHandler(edm);
			ODataHttpHandler handler = new PDJODataHttpHandler(odata, edm);
			handler.register(new PDJEntityCollectionProcessor());

			// let the handler do the work
			handler.process(req, resp);
		} catch (RuntimeException e) {
			LOG.error("Server Error occurred in ExampleServlet", e);
			throw new ServletException(e);
		}
	}
}
