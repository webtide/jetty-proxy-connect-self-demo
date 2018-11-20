package org.eclipse.jetty.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class ConnectSelfServer
{
    public static void main(String[] args) throws Exception
    {
        final int SECURE_PORT = 9443;
        final int PORT = 8080;

        ConnectSelfServer connectServer = new ConnectSelfServer(PORT, SECURE_PORT);
        connectServer.getServer().setDumpAfterStart(true);
        connectServer.getServer().start();
        connectServer.getServer().join();
    }

    private Server server;
    private ServerConnector httpConnector;
    private ServerConnector tlsConnector;

    public ConnectSelfServer(int plainTextPort, int securePort) throws IOException
    {
        server = new Server();

        // HTTP Configuration
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(securePort);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(false);

        httpConnector = new ServerConnector(server,
                new HttpConnectionFactory(httpConfig));
        httpConnector.setPort(plainTextPort);
        httpConnector.setIdleTimeout(30000);
        server.addConnector(httpConnector);

        SslContextFactory sslContextFactory = getSslContextFactory();

        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        tlsConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        tlsConnector.setPort(securePort);
        // tlsConnector.addFirstConnectionFactory(new ProxyConnectionFactory());

        server.addConnector(tlsConnector);

        ServletContextHandler proxyContext = new ServletContextHandler();
        proxyContext.setContextPath("/");
        proxyContext.addServlet(SelfProxyServlet.class, "/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ConnectSelfHandler(this));
        handlers.addHandler(proxyContext);
        handlers.addHandler(new DefaultHandler());

        server.setHandler(handlers);
    }

    public Server getServer()
    {
        return server;
    }

    public ServerConnector getHttpConnector()
    {
        return httpConnector;
    }

    public ServerConnector getTlsConnector()
    {
        return tlsConnector;
    }

    public SslContextFactory getSslContextFactory() throws IOException
    {
        SslContextFactory ssl = new SslContextFactory();

        Resource keystore = findResource("etc/keystores/proxy.p12");

        ssl.setKeyStoreResource(keystore);
        ssl.setKeyStoreType("PKCS12");
        ssl.setKeyStorePassword("bazbaz");
        ssl.setKeyManagerPassword("bazbaz");

        return ssl;
    }

    private Resource findResource(String resourceLocation) throws IOException
    {
        Path path = Paths.get(resourceLocation);
        if (Files.exists(path))
            return new PathResource(path);

        URL url = this.getClass().getClassLoader().getResource(resourceLocation);
        if (url != null)
            return Resource.newResource(url);

        throw new FileNotFoundException("Unable to find Resource: " + resourceLocation);
    }
}
