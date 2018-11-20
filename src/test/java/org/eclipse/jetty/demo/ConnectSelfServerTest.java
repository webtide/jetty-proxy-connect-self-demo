package org.eclipse.jetty.demo;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConnectSelfServerTest
{
    private ConnectSelfServer connectServer;
    private SslContextFactory sslContextFactory;

    @BeforeEach
    public void startServer() throws Exception
    {
        connectServer = new ConnectSelfServer(0,0);
        connectServer.getServer().start();
        sslContextFactory = new SslContextFactory();
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        sslContextFactory.setTrustAll(true);
        sslContextFactory.start();
    }

    @AfterEach
    public void stopServer() throws Exception
    {
        sslContextFactory.stop();
        connectServer.getServer().stop();
    }

    private Socket newSocket() throws IOException
    {
        Socket socket = new Socket("localhost", connectServer.getHttpConnector().getLocalPort());
        socket.setSoTimeout(20000);
        return socket;
    }

    private SSLSocket wrapSocket(Socket socket) throws Exception
    {
        SSLContext sslContext = sslContextFactory.getSslContext();
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket)socketFactory.createSocket(socket, socket.getInetAddress().getHostAddress(), socket.getPort(), true);
        sslSocket.setUseClientMode(true);
        sslSocket.startHandshake();
        return sslSocket;
    }

    @Test
    public void connectWebtideUnsecured() throws IOException
    {
        String hostPort = "webtide.com:80";
        String connectRequest = "" +
                "CONNECT " + hostPort + " HTTP/1.1\r\n" +
                "Host: " + hostPort + "\r\n" +
                "\r\n";

        String httpRequest = "" +
                "GET / HTTP/1.1\r\n" +
                "Host: webtide.com\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        try (Socket socket = newSocket())
        {
            OutputStream output = socket.getOutputStream();

            output.write(connectRequest.getBytes(UTF_8));
            output.flush();

            // Expect 200 OK from the CONNECT request
            HttpTester.Input httpInput = HttpTester.from(socket.getInputStream());
            HttpTester.Response connectResponse = HttpTester.parseResponse(httpInput);
            System.out.println("Connect Response: " +  connectResponse + "\n" + connectResponse.getContent());
            assertEquals(HttpStatus.OK_200, connectResponse.getStatus());

            output.write(httpRequest.getBytes(UTF_8));

            HttpTester.Response httpResponse = HttpTester.parseResponse(httpInput);
            System.out.println("HTTP Response: " +  httpResponse + "\n" + httpResponse.getContent());
            assertEquals(HttpStatus.MOVED_PERMANENTLY_301, httpResponse.getStatus());
            assertEquals("https://webtide.com/", httpResponse.get(HttpHeader.LOCATION));
        }
    }

    @Test
    public void connectWebtideSecured() throws Exception
    {
        String hostPort = "webtide.com:443";
        String connectRequest = "" +
                "CONNECT " + hostPort + " HTTP/1.1\r\n" +
                "Host: " + hostPort + "\r\n" +
                "\r\n";

        String httpRequest = "" +
                "GET / HTTP/1.1\r\n" +
                "Host: webtide.com\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        try (Socket socket = newSocket())
        {
            OutputStream output = socket.getOutputStream();

            output.write(connectRequest.getBytes(UTF_8));
            output.flush();

            // Expect 200 OK from the CONNECT request
            HttpTester.Input httpInput = HttpTester.from(socket.getInputStream());
            HttpTester.Response connectResponse = HttpTester.parseResponse(httpInput);
            System.out.println("Connect Response: " +  connectResponse + "\n" + connectResponse.getContent());
            assertEquals(HttpStatus.OK_200, connectResponse.getStatus());

            // Upgrade the socket to SSL
            try (SSLSocket sslSocket = wrapSocket(socket))
            {
                output = sslSocket.getOutputStream();

                output.write(httpRequest.getBytes(UTF_8));

                HttpTester.Response httpResponse = HttpTester.parseResponse(HttpTester.from(sslSocket.getInputStream()));
                assertEquals(HttpStatus.OK_200, httpResponse.getStatus());
                assertThat(httpResponse.getContent(), containsString("<a href=\"https://webtide.com/\""));
            }
        }
    }

}
