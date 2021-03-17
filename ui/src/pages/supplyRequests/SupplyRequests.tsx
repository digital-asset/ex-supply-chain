/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import Contracts, { field, textlist } from "../../components/Contracts/Contracts";
import { useStreamQuery, useLedger } from "@daml/react";
import { SupplyRequest }
  from "@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/QuoteRequest";
import { useState } from "react";
import { CreateEvent } from "@daml/ledger";
import { OrderedProductList } from "../quoteRequests/OrderedProductList";
import { useSortedPartyNames } from "../login/Login";


export default function SupplyRequests() {

  const parties = useSortedPartyNames();

  const ledger = useLedger();
  const roles =
    useStreamQuery(SupplyRequest);

  const warehouses = "Warehouses"
  const transportCompanies = "Transport companies"
  function startPriceCollection(
            createEvent : CreateEvent<SupplyRequest>,
            parameters : any) {
    ledger.exercise(
      SupplyRequest.SupplyRequest_StartPriceCollection,
      createEvent.contractId,
      { warehouses: parameters[warehouses].map((w: any) => w.value),
        transportCompanies: parameters[transportCompanies].map((t: any) => t.value)
      });
  };

  const [isDialogOpen, setDialogOpen] = useState(false);
  const [createEvent, setCreateEvent] = useState(undefined as CreateEvent<SupplyRequest> | undefined);

  function showOrderedProductListDialog(
            createEvent : CreateEvent<SupplyRequest>,
            _unused : any) {
    setDialogOpen(true);
    setCreateEvent(createEvent);
  };

  return (
    <>
      <div>
      <OrderedProductList ledger={ledger} createEvent={createEvent} isDialogOpen={isDialogOpen} setDialogOpen={setDialogOpen} />
      <Contracts
        contracts={roles.contracts}
        columns={[
          { name: "Workflow ID", path: "payload.workflowId" },
          { name: "Buyer", path: "payload.buyer" },
          { name: "Buyer Address", path: "payload.buyerAddress" },
          { name: "Seller", path: "payload.seller" },
          { name: "Supplier", path: "payload.supplier" },
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
        ]}
        dialogs={[
          {
            name: "Start price collection",
            dialogFields: [
              field(warehouses, textlist(parties.map(p => p.displayName), parties.map(p => p.identifier))),
              field(transportCompanies, textlist(parties.map(p => p.displayName), parties.map(p => p.identifier)))
            ],
            action: startPriceCollection,
          },
        ]}
      />
      </div>
    </>
  );
}
