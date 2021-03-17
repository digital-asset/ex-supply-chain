/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import Contracts from "../../components/Contracts/Contracts";
import { useStreamQuery, useParty, useLedger } from "@daml/react";
import { QuoteRequestAccepted }
  from "@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/QuoteRequest";
import { useState } from "react";
import { CreateEvent } from "@daml/ledger";
import { OrderedProductList } from "../quoteRequests/OrderedProductList";
import { useSortedPartyNames } from "../login/Login";

export default function QuoteRequestsAccepted() {

  const parties = useSortedPartyNames();

  const party = useParty();
  const ledger = useLedger();
  const roles =
    useStreamQuery(QuoteRequestAccepted);

  const [isDialogOpen, setDialogOpen] = useState(false);
  const [createEvent, setCreateEvent] = useState(undefined as CreateEvent<QuoteRequestAccepted> | undefined);

  function showOrderedProductListDialog(
            createEvent : CreateEvent<QuoteRequestAccepted>,
            _unused : any) {
    setDialogOpen(true);
    setCreateEvent(createEvent);
  };

  function sendToSupplier(
            createEvent : CreateEvent<QuoteRequestAccepted>,
            supplier : string) {
    ledger.exercise(
      QuoteRequestAccepted.QuoteRequestAccepted_SendToSupplier,
      createEvent.contractId,
      { supplier: supplier });
  };

  return (
    <>
      <div>
      <OrderedProductList ledger={ledger} createEvent={createEvent} isDialogOpen={isDialogOpen} setDialogOpen={setDialogOpen} />
      <Contracts
        contracts={roles.contracts}
        columns={[
          { name: "Buyer", path: "payload.buyer" },
          { name: "Buyer Address", path: "payload.buyerAddress" },
          { name: "Seller", path: "payload.seller" },
        ]}
        actions={[
          {
            name: "Show order",
            handle: showOrderedProductListDialog,
            paramName: "",
            condition: (c) => true,
            items: [],
            values: [],
          },
          {
            name: "Send to supplier",
            handle: sendToSupplier,
            paramName: "Supplier",
            condition: (c) => c.payload.seller === party,
            items: parties.map(p => p.displayName),
            values: parties.map(p => p.identifier),
          },
        ]}
        dialogs={[]}
      />
      </div>
    </>
  );
}
