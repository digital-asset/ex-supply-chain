#
# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

FROM openjdk:8-jre-alpine

WORKDIR /home/sdk

COPY target/supplychain-docker.jar supplychain-docker.jar
COPY target/lib/* /home/sdk/lib/

ENTRYPOINT java -jar supplychain-docker.jar -s ${SANDBOX_HOST} -p ${SANDBOX_PORT}
