#!/bin/bash

MYSUBJ="/C=CA/ST=Ontario/L=Ottawa/O=Eclipse Foundation/OU=Server/CN=Jetty-CA"
CA_PASS="foobar"

if [ ! -d ca ] ; then
    mkdir ca
fi

echo "## Generate CA Keys"
openssl req -new -newkey rsa:2048 \
    -keyform PEM -keyout ca/ca.key \
    -outform PEM -out ca/ca.csr \
    -passin pass:$CA_PASS -passout pass:$CA_PASS \
    -subj "$MYSUBJ"

echo "## Generate X509 CA Root"
openssl x509 -signkey ca/ca.key \
    -req -days 365 \
    -in ca/ca.csr \
    -out ca/ca-root-x509.crt \
    -passin pass:$CA_PASS \
    -extensions v3_ca

echo "## Print CA Cert"
keytool -printcert -v -file ca/ca-root-x509.crt

echo "## Adding serial"
echo "1234" > ca/serial.txt

echo "## Creating Truststore"
keytool -import -noprompt \
    -alias TestCA \
    -file ca/ca-root-x509.crt \
    -trustcacerts \
    -keystore ca/truststore.p12 \
    -storetype PKCS12 \
    -storepass "${CA_PASS}"


