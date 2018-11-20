package org.eclipse.jetty.demo;

import java.nio.channels.SocketChannel;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ConnectSelfHandler extends ConnectHandler
{
    private static final Logger LOG = Log.getLogger(ConnectSelfHandler.class);
    private final String selfHost;
    private final int selfPort;

    public ConnectSelfHandler(String host, int port)
    {
        this.selfHost = host;
        this.selfPort = port;
    }

    @Override
    protected void connectToServer(HttpServletRequest request, String host, int port, Promise<SocketChannel> promise)
    {
        LOG.info("CONNECT: {}:{}", host, port);
        // super.connectToServer(request, this.selfHost, this.selfPort, promise);
        super.connectToServer(request, host, port, promise);
    }
}
