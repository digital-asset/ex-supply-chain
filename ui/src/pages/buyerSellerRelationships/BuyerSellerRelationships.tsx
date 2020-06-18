/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import Contracts from "../../components/Contracts/Contracts";
import { useStreamQuery } from "@daml/react";
import { BuyerSellerRelationship } from "@daml.js/supplychain-1.0.0/lib/DA/RefApps/SupplyChain/Relationship";

export default function ValidEmission() {

  const roles =
    useStreamQuery(BuyerSellerRelationship);

  return (
    <>
      <Contracts
        contracts={roles.contracts}
        columns={[
          { name: "Buyer", path: "payload.buyer" },
          { name: "Buyer Address", path: "payload.buyerAddress" },
          { name: "Seller", path: "payload.seller" },
        ]}
        actions={[]}
        dialogs={[]}
      />
    </>
  );
}
