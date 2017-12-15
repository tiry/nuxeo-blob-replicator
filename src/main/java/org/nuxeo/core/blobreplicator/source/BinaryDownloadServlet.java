package org.nuxeo.core.blobreplicator.source;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.blob.BlobInfo;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.runtime.api.Framework;

public class BinaryDownloadServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(BinaryDownloadServlet.class);

	private static final long serialVersionUID = 1L;

	public static final String KEY_PARAM = "key";

	public static final String TOKEN_PARAM = "token";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		log.info("Starting BinaryReplicator Servlet");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String key = req.getParameter(KEY_PARAM);

		String token = req.getParameter(TOKEN_PARAM);

		if (!checkToken(token)) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		if (key == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if ("test".equals(key)) {
			resp.getWriter().write("Hello");
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		BlobManager bm = Framework.getService(BlobManager.class);
		BlobInfo bi = new BlobInfo();
		bi.key = key;
		Blob blob = bm.getBlobProvider("default").readBlob(bi);

		resp.setStatus(HttpServletResponse.SC_OK);
		IOUtils.copy(blob.getStream(), resp.getOutputStream());
		resp.flushBuffer();

	}

	protected boolean checkToken(String token) {
		// XXX check shared secret
		return true;

	}
}
