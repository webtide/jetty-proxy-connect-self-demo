package org.eclipse.jetty.demo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.AsyncMiddleManServlet;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class SelfProxyServlet extends AsyncMiddleManServlet
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

    @Override
    protected ContentTransformer newClientRequestContentTransformer(HttpServletRequest clientRequest, org.eclipse.jetty.client.api.Request proxyRequest)
    {
        // This is to allow you to see and/or modify the Client Request Body Content
        return ContentTransformer.IDENTITY;
    }

    @Override
    protected ContentTransformer newServerResponseContentTransformer(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse)
    {
        // This is to allow you to see and/or modify the Server Response Body Content
        LOG.info("newServerResponseContentTransformer({}, {}, {})", clientRequest, proxyResponse, serverResponse);
        return new ShaSumContentTransformer("Server Response " + Request.getBaseRequest(clientRequest).getHttpURI().toString());
    }
}
