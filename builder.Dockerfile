#
# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

ARG sdk_vsn=1.8.0

FROM digitalasset/daml-sdk:${sdk_vsn}

USER root
RUN apt-get update || apt-get update
RUN apt-get -y install make
USER daml

WORKDIR /home/daml/

COPY --chown=daml Makefile .
COPY --chown=daml daml.yaml .
COPY --chown=daml src src
COPY --chown=daml scripts scripts
COPY --chown=daml ui/package.json ui/package.json
COPY --chown=daml ui/public ui/public
COPY --chown=daml ui/src ui/src
COPY --chown=daml ui/tsconfig.json ui/tsconfig.json
COPY --chown=daml ui/yarn.lock ui/yarn.lock

USER root
RUN scripts/install-node.sh
RUN scripts/install-yarn.sh
USER daml

RUN make deploy

ENTRYPOINT echo "Build artifacts has been synced to shared volume:" && ls -l deploy/
