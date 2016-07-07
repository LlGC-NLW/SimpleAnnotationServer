package uk.org.llgc.annotation.store;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;

import java.net.URL;

import java.util.Map;
import java.util.List;

import com.github.jsonldjava.utils.JsonUtils;

import com.hp.hpl.jena.rdf.model.Model;

import uk.org.llgc.annotation.store.adapters.StoreAdapter;
import uk.org.llgc.annotation.store.exceptions.IDConflictException;

public class Populate extends HttpServlet {
	protected AnnotationUtils _annotationUtils = null;
	protected StoreAdapter _store = null;

	public void init(final ServletConfig pConfig) throws ServletException {
		super.init(pConfig);
		_annotationUtils = new AnnotationUtils(new File(super.getServletContext().getRealPath("/contexts")));
		_store = StoreConfig.getConfig().getStore();
	}

	public void doPost(final HttpServletRequest pReq, final HttpServletResponse pRes) throws IOException {
		InputStream tAnnotationList = null;
		if (pReq.getParameter("uri") != null) {
			System.out.println("Reading from " + pReq.getParameter("uri"));
			tAnnotationList = new URL(pReq.getParameter("uri")).openStream();
		} else {
			tAnnotationList = pReq.getInputStream();
		}
		List<Map<String, Object>> tAnnotationListJSON = _annotationUtils.readAnnotationList(tAnnotationList, StoreConfig.getConfig().getBaseURI(pReq)); //annotaiton list
		/**/System.out.println("JSON in:");
		/**/System.out.println(JsonUtils.toPrettyString(tAnnotationListJSON));

		try {
			_store.addAnnotationList(tAnnotationListJSON);

			pRes.setStatus(HttpServletResponse.SC_CREATED);
			pRes.setContentType("text/plain");
			pRes.getOutputStream().println("SUCCESS");
		} catch (IDConflictException tException) {
			tException.printStackTrace();
			pRes.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			pRes.setContentType("text/plain");
			pRes.getOutputStream().println("Failed to load annotation list as there was a conflict in ids " + tException.toString());
		}
	}
}
