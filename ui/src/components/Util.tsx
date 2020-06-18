/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
export function parseStringList(stringWithCommas: string): string[] {
  return stringWithCommas
        .split(",")
        .map((i: string) => i.trim())
        .filter((i: string) => i !== '')
}

export function addSpacesBetweenWords(s: string): string {
  return s.replace(/([a-z])([A-Z1-9])/g, '$1 $2');
}

function isParty(name: string): boolean {
  return name.includes("::");
}

export function getDisplayName(partyId: string): string {
  return partyId.split("::")[0];
}

export function shorten(text: any): any {
  if (typeof text === "string") {
    if (isParty(text)) {
      return addSpacesBetweenWords(getDisplayName(text));
    }
    if (text.length > 20) {
      return `${text.substr(0,20)}...`;
    }
    return text;
  }
  if (typeof text === "object") {
    text = text.map((t: any) => shorten(t));
    if (Array.isArray(text)) {
      text = text.join(", ");
    }
  }
  return text;
}
