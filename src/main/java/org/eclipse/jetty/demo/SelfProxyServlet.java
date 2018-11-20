package org.eclipse.jetty.demo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class SelfProxyServlet extends AsyncProxyServlet
{
    private static final Logger LOG = Log.getLogger(SelfProxyServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Cast to Jetty Base Request object
        Request baseRequest = Request.getBaseRequest(request);

        // So that we can get access to the raw Request URI (complete with query and path parameters)
        LOG.info("Request {} at URI {}", request.getMethod(), baseRequest.getHttpURI().toString());

        super.service(request, response);
    }
}
