#!/bin/bash

MYDNAME="CN=Jetty, OU=RT, O=Eclipse Foundation, L=McAllen, ST=Texas, C=US"
MYSUBJ="/C=US/ST=Texas/L=McAllenO=Eclipse Foundation/OU=RT/CN=Jetty"
CA_PASS="foobar"
KEY_PASS="bazbaz"

if [ ! -d keystores ] ; then
    mkdir keystores
fi

echo "## Generating Proxy Server Key"
openssl genrsa \
    -aes128 \
    -out keystores/proxy.key \
    -passout pass:${KEY_PASS}

echo "## Generating Proxy Server CSR"
openssl req \
    -new \
    -newkey rsa:2048 \
    -sha256 \
    -key keystores/proxy.key \
    -out keystores/proxy.csr \
    -passin pass:${KEY_PASS} \
    -subj "${MYSUBJ}"

echo "## Generating X509 Proxy Server CSR"
openssl x509 \
    -req \
    -in keystores/proxy.csr \
    -CA ca/ca-root-x509.crt \
    -CAkey ca/ca.key \
    -CAcreateserial \
    -CAserial ca/serial.txt \
    -out keystores/proxy.crt \
    -days 1825 \
    -sha256 \
    -passin pass:$CA_PASS

echo "## Converting certificate and key to PKCS 12"
openssl pkcs12 \
    -export \
    -in keystores/proxy.crt \
    -inkey keystores/proxy.key \
    -name "Proxy" \
    -out keystores/proxy.p12 \
    -passin pass:${KEY_PASS} \
    -passout pass:${KEY_PASS}

# echo "## Importing certificates into PKCS 12 keystore"
# keytool -importkeystore \
#     -deststorepass "${KEY_PASS}" \
#     -destkeystore keystores/proxy.p12 \
#     -srckeystore keystores/proxy-openssl.p12 \
#     -srcstoretype PKCS12

echo "## Importing CA into Proxy Keystore"
keytool -import -noprompt \
    -alias TestCA \
    -file ca/ca-root-x509.crt \
    -trustcacerts \
    -keystore keystores/proxy.p12 \
    -storetype PKCS12 \
    -storepass "${KEY_PASS}"
# 
# echo "## Importing X509 Signed Proxy Certificate into Keystore"
# keytool -import \
#     -alias Proxy \
#     -file keystores/proxy.crt \
#     -keystore keystores/proxy.p12 \
#     -storetype PKCS12 \
#     -storepass "${KEY_PASS}"


