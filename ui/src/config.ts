///
/// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
/// SPDX-License-Identifier: Apache-2.0
///

import * as jwt from "jsonwebtoken";

export const isLocalDev = process.env.NODE_ENV === 'development';

let host = window.location.host.split('.')

export const ledgerId = "supply-chain";

let apiUrl = host.slice(1)
apiUrl.unshift('api')

const jsonApiUrl = process.env.REACT_APP_JSON_API_URL as string;
const jsonApiWsUrl = process.env.REACT_APP_JSON_API_WS_URL as string;

console.log(`JSON API proxy: ${jsonApiUrl}`);
console.log(`JSON API WS proxy: ${jsonApiWsUrl}`);

export const httpBaseUrl = jsonApiUrl;
    // isLocalDev
    // ? `${window.location.protocol}//${window.location.host}/`
    // : 'https://' + apiUrl.join('.') + (window.location.port ? ':' + window.location.port : '') + '/data/' + ledgerId + '/';

// Unfortunately, the development server of `create-react-app` does not proxy
// websockets properly. Thus, we need to bypass it and talk to the JSON API
// directly in development mode.
export const wsBaseUrl = jsonApiWsUrl;
    // isLocalDev
    // ? 'ws://localhost:7575/'
    // : undefined;

const applicationId = "supply-chain";

export function createToken(party : string): string {
    return jwt.sign({ "https://daml.com/ledger-api": { ledgerId, applicationId, admin: true, actAs: [party], readAs: [party] } }, "secret")
}

let loginUrl = host.slice(1)
loginUrl.unshift('login')

export const dablLoginUrl = loginUrl.join('.') + (window.location.port ? ':' + window.location.port : '') + '/auth/login?ledgerId=' + ledgerId;
