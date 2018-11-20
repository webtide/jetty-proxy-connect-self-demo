
## Abusing ConnectHandler to proxy all requests through Jetty

When using the `ConnectHandler` in the normal way, a Jetty server can
be setup to proxy requests arriving from User-Agents that issue
a `CONNECT` HTTP Method to establish a raw Socket connection to
a destination server.

This means for User-Agents that connect to the Jetty Proxy server
and wish to communicate with a destination server that is encrypted
with TLS the Jetty Proxy server will not be able to see the contents
of the HTTP Request (as the TLS encryption is established once the
`CONNECT` header is satisfied and the connection is upgraded)

This example project abuses the `ConnectHandler` to force
communications via the `CONNECT` method to always use the Jetty Proxy
server as its destination, which then relies on using `AsyncProxyServlet`
to perform the proxying behaviors to the destination server.

This does not allow the User-Agent to use the destination server
certificates, as the Jetty Proxy server is now the destination server
(in the eyes of the User-Agent).

This is behavior is intentionally non-standard and you will hit
all sorts of protections present on the open internet for preventing
this kind of man-in-the-middle style of behavior.

This project is a demonstration of this behavior for those people and
organizations that are troubleshooting their User-Agent behaviors
and want to ensure that the User-Agents behave according to the
security policies that they have established.



