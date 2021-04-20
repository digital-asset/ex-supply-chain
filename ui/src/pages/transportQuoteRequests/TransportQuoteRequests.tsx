/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React, { useState } from "react";
import Contracts, { number, field, date } from "../../components/Contracts/Contracts";
import { useStreamQuery, useLedger } from "@daml/react";
import { TransportQuoteRequest }
  from "@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/QuoteRequest";
import { CreateEvent } from "@daml/ledger";
import { WarehouseProductWithDates } from "@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/Types";
import { SingleWarehouseProductWithDates } from "./SingleWarehouseProductWithDates";
export default function TransportQuoteRequests() {

  const requests = useStreamQuery(TransportQuoteRequest);

  const ledger = useLedger();

  function accept(
            createEvent : CreateEvent<TransportQuoteRequest>,
            parameters : any) {
    ledger.exercise(
      TransportQuoteRequest.TransportQuoteRequest_Accept,
      createEvent.contractId,
      { quoteItem : parameters,
    });
  };

  const [isDialogOpen, setDialogOpen] = useState(false);
  const [item, setItem] = useState(undefined as WarehouseProductWithDates | undefined);

  function showItem(
            createEvent : CreateEvent<TransportQuoteRequest>,
            _unused : any) {
    setDialogOpen(true);
    setItem(createEvent.payload.item);
  };


  return (
    <>
      <div>
      <SingleWarehouseProductWithDates ledger={ledger} item={item} isDialogOpen={isDialogOpen} setDialogOpen={setDialogOpen} />
      <Contracts
        contracts={requests.contracts}
        columns={[
          { name: "Workflow ID", path: "payload.workflowId" },
          { name: "Buyer", path: "payload.buyer" },
          { name: "Buyer Address", path: "payload.buyerAddress" },
          { name: "Transport Company", path: "payload.transportCompany" },
          { name: "Warehouse", path: "payload.item.warehouseProduct.warehouse" },
          { name: "Supplier", path: "payload.supplier" },
        ]}
        actions={[
          {
            name: "Show item",
            handle: showItem,
            paramName: "",
            condition: (c) => true,
            items: [],
            values: [],
          },
        ]}
        dialogs={[
          {
            name: "Accept",
            dialogFields: [
              field("transportableQuantity", number),
              field("price", number),
              field("pickUpDate", date),
              field("deliveryDate", date),
            ],
            action: accept,
          },
        ]}
      />
      </div>
    </>
  );
}
