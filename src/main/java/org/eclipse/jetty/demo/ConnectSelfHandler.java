package org.eclipse.jetty.demo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ConnectSelfHandler extends ConnectHandler
{
    private static final Logger LOG = Log.getLogger(ConnectSelfHandler.class);
    private final ConnectSelfServer server;

    public ConnectSelfHandler(ConnectSelfServer server)
    {
        this.server = server;
    }

    @Override
    protected void connectToServer(HttpServletRequest request, String host, int port, Promise<SocketChannel> promise)
    {
        LOG.info("CONNECT: {}:{}", host, port);

        ServerConnector connector = server.getHttpConnector();

        // Is User-Agent's Proxy CONNECT expecting to upgrade to TLS?
        if (port == 443 || port == 8443)
        {
            connector = server.getTlsConnector();
        }

        SocketChannel channel = null;
        try
        {
            channel = SocketChannel.open();
            channel.socket().setTcpNoDelay(true);
            channel.configureBlocking(false);

            String destHost = connector.getHost();
            if(destHost == null)
                destHost = InetAddress.getLocalHost().getHostAddress();

            InetSocketAddress address = newConnectAddress(destHost, connector.getLocalPort());
            channel.connect(address);
            promise.succeeded(channel);
        }
        catch (Throwable x)
        {
            IO.close(channel);
            promise.failed(x);
        }
    }

    @Override
    protected void onConnectFailure(HttpServletRequest request, HttpServletResponse response, AsyncContext asyncContext, Throwable failure)
    {
        LOG.warn("onConnectFailure(" + request + ", " + response + ", " + asyncContext + ")", failure);
        super.onConnectFailure(request, response, asyncContext, failure);
    }
}
