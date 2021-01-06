///
/// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
/// SPDX-License-Identifier: Apache-2.0
///

import * as jwt from "jsonwebtoken";

export const isLocalDev = process.env.NODE_ENV === 'development';

// let host = window.location.host.split('.')

export const ledgerId = "supply-chain";

// let apiUrl = host.slice(1)
// apiUrl.unshift('api')

export const httpBaseUrl = process.env.REACT_APP_HTTP_BASE_URL as string
    // isLocalDev
    // ? `${window.location.protocol}//${window.location.host}/`
    // : 'https://' + apiUrl.join('.') + (window.location.port ? ':' + window.location.port : '') + '/data/' + ledgerId + '/';

// Unfortunately, the development server of `create-react-app` does not proxy
// websockets properly. Thus, we need to bypass it and talk to the JSON API
// directly in development mode.
export const wsBaseUrl = process.env.REACT_APP_WS_BASE_URL as string
    // isLocalDev
    // ? 'ws://localhost:7575/'
    // : undefined;

const applicationId = "supply-chain";

export function createToken(party : string): string {
    return jwt.sign({ "https://daml.com/ledger-api": { ledgerId, applicationId, admin: true, actAs: [party], readAs: [party] } }, "secret")
}

// let loginUrl = host.slice(1)
// loginUrl.unshift('login')

// loginUrl.join('.') + (window.location.port ? ':' + window.location.port : '')

console.log("Bing Bunny" + httpBaseUrl)
export const dablLoginUrl = process.env.REACT_APP_LOGIN_URL as string + '/auth/login?ledgerId=' + ledgerId;
