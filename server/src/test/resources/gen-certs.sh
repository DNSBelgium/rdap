#!/bin/bash

###
# #%L
# Server
# %%
# Copyright (C) 2013 DNS Belgium vzw
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###
 
# default password for keys
PASSWORD=changeit
 
OUT_DIR=certs
 
# Subject items
C="BE"
ST="Vlaams-Brabant"
L="Heverlee"
O="DNS Belgium"
 
CN_CA="CA Root"
CN_SERVER="rdap.dns.be"
CN_CLIENT="pieterv@cert.be"
 
###############################
 
# Create output directory
mkdir -p ${OUT_DIR}
 
###############################
 
# create CA key
openssl genrsa -des3 -out ${OUT_DIR}/ca.key -passout pass:$PASSWORD 4096
 
# create CA cert
openssl req -new -x509 -days 365 -key ${OUT_DIR}/ca.key -out ${OUT_DIR}/ca.crt \
 -passin pass:$PASSWORD -subj "/C=${C}/ST=${ST}/L=${L}/O=${O}/CN=${CN_CA}/"
 
# create truststore
keytool -import -trustcacerts -alias caroot -file ${OUT_DIR}/ca.crt \
 -keystore ${OUT_DIR}/truststore.jks -storepass ${PASSWORD} -noprompt
 
###############################
 
# create server key
openssl genrsa -des3 -out ${OUT_DIR}/server.key -passout pass:$PASSWORD 4096
 
# create server cert request
openssl req -new -key ${OUT_DIR}/server.key -out ${OUT_DIR}/server.csr \
 -passin pass:$PASSWORD -subj "/C=${C}/ST=${ST}/L=${L}/O=${O}/CN=${CN_SERVER}/"
 
# create server cert
openssl x509 -req -days 365 -in ${OUT_DIR}/server.csr -CA ${OUT_DIR}/ca.crt \
 -CAkey ${OUT_DIR}/ca.key -set_serial 01 -out ${OUT_DIR}/server.crt \
 -passin pass:${PASSWORD}
 
# convert server cert to PKCS12 format, including key
openssl pkcs12 -export -out ${OUT_DIR}/server.p12 -inkey ${OUT_DIR}/server.key \
 -in ${OUT_DIR}/server.crt -passin pass:${PASSWORD} -passout pass:${PASSWORD}
 
###############################
 
# create client key
openssl genrsa -des3 -out ${OUT_DIR}/client.key -passout pass:${PASSWORD} 4096
 
# create client cert request
openssl req -new -key ${OUT_DIR}/client.key -out ${OUT_DIR}/client.csr \
 -passin pass:$PASSWORD -subj "/C=${C}/ST=${ST}/L=${L}/O=${O}/CN=${CN_CLIENT}/"
 
 
# create client cert
openssl x509 -req -days 365 -in ${OUT_DIR}/client.csr -CA ${OUT_DIR}/ca.crt \
 -CAkey ${OUT_DIR}/ca.key -set_serial 02 -out ${OUT_DIR}/client.crt \
 -passin pass:${PASSWORD}
 
# convert client cert to PKCS12, including key
openssl pkcs12 -export -out ${OUT_DIR}/client.p12 -inkey ${OUT_DIR}/client.key \
 -in ${OUT_DIR}/client.crt -passin pass:${PASSWORD} -passout pass:${PASSWORD}

# create client PEM (for curl)
cat ${OUT_DIR}/client.key ${OUT_DIR}/client.crt > ${OUT_DIR}/client.pem
