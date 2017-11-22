package com.pengdaijun.odata.olingo.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pengdaijun.odata.olingo.data.Storage;
import com.pengdaijun.odata.olingo.service.PDJEdmProvider;
import com.pengdaijun.odata.olingo.service.PDJEntityCollectionProcessor;
import com.pengdaijun.odata.olingo.service.PDJEntityProcessor;
import com.pengdaijun.odata.olingo.service.PDJPrimitiveProcessor;
import com.pengdaijun.odata.olingo.util.PDJODataHttpHandler;

@WebServlet(urlPatterns = { "/odata/*" }, loadOnStartup = 1)
public class PDJWebServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(PDJWebServlet.class);

	protected void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			HttpSession session = req.getSession(true);
			Storage storage = (Storage) session.getAttribute(Storage.class.getName());
			if (storage == null) {
				storage = new Storage();
				session.setAttribute(Storage.class.getName(), storage);
			}
			// create odata handler and configure it with CsdlEdmProvider and
			// Processor
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(new PDJEdmProvider(), new ArrayList<EdmxReference>());
			// ODataHttpHandler handler = odata.createHandler(edm);
			ODataHttpHandler handler = new PDJODataHttpHandler(odata, edm);
			// 读取数据集合集
			// handler.register(new PDJEntityCollectionProcessor());
			// 独立数据类
			handler.register(new PDJEntityCollectionProcessor(storage));
			// 读取单条数据
			handler.register(new PDJEntityProcessor(storage));
			// 读取对象属性
			handler.register(new PDJPrimitiveProcessor(storage));

			// let the handler do the work
			handler.process(req, resp);
		} catch (RuntimeException e) {
			LOG.error("Server Error occurred in ExampleServlet", e);
			throw new ServletException(e);
		}
	}
}
