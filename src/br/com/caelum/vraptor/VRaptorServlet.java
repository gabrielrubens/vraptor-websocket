/**
 * 
 */
package br.com.caelum.vraptor;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.config.BasicConfiguration;
import br.com.caelum.vraptor.core.Execution;
import br.com.caelum.vraptor.core.RequestExecution;
import br.com.caelum.vraptor.core.RequestInfo;
import br.com.caelum.vraptor.core.StaticContentHandler;
import br.com.caelum.vraptor.http.EncodingHandler;
import br.com.caelum.vraptor.http.VRaptorRequest;
import br.com.caelum.vraptor.http.VRaptorResponse;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.ioc.ContainerProvider;

/**
 * @author bglbruno
 *
 */
@WebServlet(urlPatterns="/*",asyncSupported=true)
public class VRaptorServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	private ContainerProvider provider;
	private ServletContext servletContext;

	private StaticContentHandler staticHandler;

	private static final Logger logger = LoggerFactory.getLogger(VRaptorServlet.class);

	public void destroy() {
		provider.stop();
		provider = null;
		servletContext = null;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse)) {
			throw new ServletException(
					"VRaptor must be run inside a Servlet environment. Portlets and others aren't supported.");
		}

		final HttpServletRequest baseRequest = (HttpServletRequest) req;
		final HttpServletResponse baseResponse = (HttpServletResponse) res;

		if (!staticHandler.requestingStaticFile(baseRequest)) {
			logger.debug("VRaptor received a new request");
			logger.trace("Request: {}", req);

			
			VRaptorRequest mutableRequest = new VRaptorRequest(baseRequest);
			VRaptorResponse mutableResponse = new VRaptorResponse(baseResponse);

			final RequestInfo request = new RequestInfo(servletContext, null, mutableRequest, mutableResponse);
			provider.provideForRequest(request, new Execution<Object>() {
				public Object insideRequest(Container container) {
					container.instanceFor(EncodingHandler.class).setEncoding(baseRequest, baseResponse);
					container.instanceFor(RequestExecution.class).execute();
					return null;
				}
			});
			AsyncContext aCtx = req.startAsync(req, res);
			logger.debug("VRaptor ended the request");
		}
		
	}
	
	@Override
	public void init(ServletConfig cfg) throws ServletException {
		servletContext = cfg.getServletContext();
		BasicConfiguration config = new BasicConfiguration(servletContext);
		init(config.getProvider());
		logger.info("VRaptor 3.4.1-SNAPSHOT successfuly initialized");
	}

	void init(ContainerProvider provider) {
		this.provider = provider;
		this.provider.start(servletContext);
		this.staticHandler = provider.getContainer().instanceFor(StaticContentHandler.class);
	}

}