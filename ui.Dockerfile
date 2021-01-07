#
# Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

ARG sdk_vsn=1.8.0

FROM digitalasset/daml-sdk:${sdk_vsn} AS DAML

WORKDIR /home/daml/

COPY --chown=daml daml.yaml .
COPY --chown=daml src src

RUN daml build --output app.dar

RUN daml codegen js -o daml.js app.dar

FROM node:alpine

COPY --from=DAML /home/daml/daml.js target/daml.js

COPY ui/package.json ui/package.json
COPY ui/public ui/public
COPY ui/src ui/src
COPY ui/tsconfig.json ui/tsconfig.json
COPY ui/yarn.lock ui/yarn.lock
COPY ui/.env ui/.env

WORKDIR ui

RUN yarn install --force --frozen-lockfile

EXPOSE 3000

ENTRYPOINT yarn start
