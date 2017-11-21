package com.pengdaijun.odata.olingo.util;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.core.ODataHandlerException;

public class ODataHttp {
	private static String URI = "odata/";

	/**
	 * Creates the o data request.
	 *
	 * @param httpRequest
	 *            the http request
	 * @param split
	 *            the split
	 * @return the o data request
	 * @throws ODataTranslatedException
	 *             the o data translated exception
	 */
	public ODataRequest createODataRequest(final HttpServletRequest httpRequest, final int split)
			throws ODataException {
		try {
			ODataRequest odRequest = new ODataRequest();

			odRequest.setBody(httpRequest.getInputStream());
			extractHeaders(odRequest, httpRequest);
			extractMethod(odRequest, httpRequest);
			extractUri(odRequest, httpRequest, split);

			return odRequest;
		} catch (final IOException e) {
			throw new SerializerException("An I/O exception occurred.", e,
					SerializerException.MessageKeys.IO_EXCEPTION);
		}
	}

	/**
	 * Extract method.
	 *
	 * @param odRequest
	 *            the od request
	 * @param httpRequest
	 *            the http request
	 * @throws ODataTranslatedException
	 *             the o data translated exception
	 */
	public void extractMethod(final ODataRequest odRequest, final HttpServletRequest httpRequest)
			throws ODataException {
		try {
			HttpMethod httpRequestMethod = HttpMethod.valueOf(httpRequest.getMethod());

			if (httpRequestMethod == HttpMethod.POST) {
				String xHttpMethod = httpRequest.getHeader(HttpHeader.X_HTTP_METHOD);
				String xHttpMethodOverride = httpRequest.getHeader(HttpHeader.X_HTTP_METHOD_OVERRIDE);

				if (xHttpMethod == null && xHttpMethodOverride == null) {
					odRequest.setMethod(httpRequestMethod);
				} else if (xHttpMethod == null) {
					odRequest.setMethod(HttpMethod.valueOf(xHttpMethodOverride));
				} else if (xHttpMethodOverride == null) {
					odRequest.setMethod(HttpMethod.valueOf(xHttpMethod));
				} else {
					if (!xHttpMethod.equalsIgnoreCase(xHttpMethodOverride)) {
						throw new ODataHandlerException("Ambiguous X-HTTP-Methods",
								ODataHandlerException.MessageKeys.AMBIGUOUS_XHTTP_METHOD, xHttpMethod,
								xHttpMethodOverride);
					}
					odRequest.setMethod(HttpMethod.valueOf(xHttpMethod));
				}
			} else {
				odRequest.setMethod(httpRequestMethod);
			}
		} catch (IllegalArgumentException e) {
			throw new ODataHandlerException("Invalid HTTP method" + httpRequest.getMethod(),
					ODataHandlerException.MessageKeys.INVALID_HTTP_METHOD, httpRequest.getMethod());
		}
	}

	/**
	 * Extract uri.
	 *
	 * @param odRequest
	 *            the od request
	 * @param httpRequest
	 *            the http request
	 * @param split
	 *            the split
	 */
	public void extractUri(final ODataRequest odRequest, final HttpServletRequest httpRequest, final int split) {
		String rawRequestUri = httpRequest.getRequestURL().toString();

		String rawODataPath;
		if (!"".equals(httpRequest.getServletPath())) {
			int beginIndex;
			beginIndex = rawRequestUri.indexOf(URI);
			beginIndex += URI.length();
			rawODataPath = rawRequestUri.substring(beginIndex);
		} else if (!"".equals(httpRequest.getContextPath())) {
			int beginIndex;
			beginIndex = rawRequestUri.indexOf(httpRequest.getContextPath());
			beginIndex += httpRequest.getContextPath().length();
			rawODataPath = rawRequestUri.substring(beginIndex);
		} else {
			rawODataPath = httpRequest.getRequestURI();
		}

		String rawServiceResolutionUri;
		if (split > 0) {
			rawServiceResolutionUri = rawODataPath;
			for (int i = 0; i < split; i++) {
				int e = rawODataPath.indexOf("/", 1);
				if (-1 == e) {
					rawODataPath = "";
				} else {
					rawODataPath = rawODataPath.substring(e);
				}
			}
			int end = rawServiceResolutionUri.length() - rawODataPath.length();
			rawServiceResolutionUri = rawServiceResolutionUri.substring(0, end);
		} else {
			rawServiceResolutionUri = null;
		}

		String rawBaseUri = rawRequestUri.substring(0, rawRequestUri.length() - rawODataPath.length());

		odRequest.setRawQueryPath(httpRequest.getQueryString());
		odRequest.setRawRequestUri(
				rawRequestUri + (httpRequest.getQueryString() == null ? "" : "?" + httpRequest.getQueryString()));

		odRequest.setRawODataPath(rawODataPath);
		odRequest.setRawBaseUri(rawBaseUri);
		odRequest.setRawServiceResolutionUri(rawServiceResolutionUri);
	}

	/**
	 * Extract headers.
	 *
	 * @param odRequest
	 *            the od request
	 * @param req
	 *            the req
	 */
	public void extractHeaders(final ODataRequest odRequest, final HttpServletRequest req) {
		for (Enumeration<?> headerNames = req.getHeaderNames(); headerNames.hasMoreElements();) {
			String headerName = (String) headerNames.nextElement();
			List<String> headerValues = new ArrayList<String>();
			for (Enumeration<?> headers = req.getHeaders(headerName); headers.hasMoreElements();) {
				String value = (String) headers.nextElement();
				headerValues.add(value);
			}
			odRequest.addHeader(headerName, headerValues);
		}
	}
}
