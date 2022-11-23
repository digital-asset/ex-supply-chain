///
/// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
/// SPDX-License-Identifier: Apache-2.0
///

import * as jwt from "jsonwebtoken";
import { addTrailingSlashIfNeeded } from "./components/Util";
import { convertPartiesJson, PartyDetails } from '@daml/dabl-react';
import { PartyIdWithName } from "./context/UserContext";

export const httpBaseUrl = addTrailingSlashIfNeeded(process.env.REACT_APP_HTTP_BASE_URL as string);
export const wsBaseUrl = addTrailingSlashIfNeeded(process.env.REACT_APP_WS_BASE_URL as string);

console.log(`JSON API: ${httpBaseUrl}`);
console.log(`JSON API WS: ${wsBaseUrl}`);

const applicationId = "supply-chain";
let host = window.location.host.split('.')
let loginUrl = host.slice(1)
loginUrl.unshift('login')
export const isDevMode = process.env.NODE_ENV === 'development'
export const ledgerId = isDevMode ? "supply-chain" : host[0];
export const dablLoginUrl = loginUrl.join('.') + (window.location.port ? ':' + window.location.port : '') + '/auth/login?ledgerId=' + ledgerId;

export function createToken(party: PartyIdWithName): string {
    if (isDevMode) {
        console.log("Using generated token.");
        return jwt.sign({ "https://daml.com/ledger-api": { ledgerId, applicationId, admin: true, actAs: [party.identifier], readAs: [party.identifier] } }, "secret");
    } else {
        console.log("Using token from parties.json file.");
        const parties = retrieveParties();
        const partyInfo = parties?.find(o => o.partyName === party.displayName);
        if (partyInfo && partyInfo.token) {
            return partyInfo.token;
        }
        alert(`Warning: no credentials available for ${party.displayName} (${party.identifier}).`);
        return "undefined";
    }
}

export const handlePartiesLoad = async (parties: PartyDetails[]) => {
    try {
        storeParties(parties);
    } catch (e) {
        alert(`Error while trying to store parties: ${e}`);
    }
}

const PARTIES_STORAGE_KEY = 'imported_parties';

function storeParties(partiesJson: PartyDetails[]) {
    localStorage.setItem(PARTIES_STORAGE_KEY, JSON.stringify(partiesJson));
}

export function retrieveParties() {
    const partiesJson = localStorage.getItem(PARTIES_STORAGE_KEY);

    if (!partiesJson) {
        return undefined;
    }

    const [ parties, error ] = convertPartiesJson(partiesJson, ledgerId, true);

    if (error) {
        console.warn("Tried to load an invalid parties file from cache.", error);

        localStorage.removeItem(PARTIES_STORAGE_KEY);
        return undefined;
    }

    return parties;
}
