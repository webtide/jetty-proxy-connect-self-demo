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
        connectServer.getServer().start();
        connectServer.getServer().join();
    }

    private Server server;
    private ServerConnector httpConnector;
    private ServerConnector sslConnector;

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

        sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        sslConnector.setPort(securePort);
        //sslConnector.addFirstConnectionFactory(new ProxyConnectionFactory());

        server.addConnector(sslConnector);

        ServletContextHandler proxyContext = new ServletContextHandler();
        proxyContext.setContextPath("/");
        proxyContext.addServlet(AsyncProxyServlet.class, "/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(new ConnectSelfHandler("localhost", securePort));
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

    public ServerConnector getSslConnector()
    {
        return sslConnector;
    }

    public SslContextFactory getSslContextFactory() throws IOException
    {
        SslContextFactory ssl = new SslContextFactory();

        Resource keystore = findResource("etc/keystores/proxy.p12");

        ssl.setKeyStoreResource(keystore);
        ssl.setKeyStorePassword("bazbaz");
        ssl.setKeyManagerPassword("bazbaz");

        Resource truststore = findResource("etc/ca/truststore.p12");
        ssl.setTrustStoreResource(truststore);
        ssl.setTrustStorePassword("foobar");
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
