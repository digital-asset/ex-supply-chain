/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import { Route, Switch, withRouter } from "react-router-dom";
import DamlLedger from "@daml/react";
import Header from "../Header/Header";
import Sidebar from "../Sidebar/Sidebar";
import BuyerSellerRelationships from "../../pages/buyerSellerRelationships/BuyerSellerRelationships";
import { useUserState } from "../../context/UserContext";
import { wsBaseUrl, httpBaseUrl } from "../../config";
import useStyles from "./styles";
import QuoteRequests from "../../pages/quoteRequests/QuoteRequests";
import QuoteRequestsAccepted from "../../pages/quoteRequestsAccepted/QuoteRequestsAccepted";
import SupplyInvitations from "../../pages/supplyInvitations/SupplyInvitations";
import SupplyRequests from "../../pages/supplyRequests/SupplyRequests";
import TransportQuoteRequests from "../../pages/transportQuoteRequests/TransportQuoteRequests";
import TransportQuoteRequestPendings from "../../pages/transportQuoteRequestPendings/TransportQuoteRequestPendings";
import AggregatedQuotePending from "../../pages/aggregatedQuotePendings/AggregatedQuotePendings";
import AggregatedQuotes from "../../pages/aggregatedQuotes/AggregatedQuotes";
import QuoteForBuyer from "../../pages/quoteForBuyer/QuoteForBuyer";
import Orders from "../../pages/orders/Orders";
import DeliveryInstructions from "../../pages/deliveryInstructions/DeliveryInstructions";
import PickUpRequests from "../../pages/pickupRequests/PickupRequests";
import TransportPendings from "../../pages/transportPendings/TransportPendings";
import Deliveries from "../../pages/deliveries/Deliveries";
import DeliveryPayments from "../../pages/deliveryPayments/DeliveryPayments";
import SupplierPayments from "../../pages/supplierPayments/SupplierPayments";
import PaymentObligations from "../../pages/paymentObligations/PaymentObligations";

const Layout = () => {
  const classes = useStyles();
  const user = useUserState();

  if(!user.isAuthenticated){
    return null;
  } else {
    return (
      <DamlLedger party={user.party.identifier} token={user.token} httpBaseUrl={httpBaseUrl} wsBaseUrl={wsBaseUrl}>
        <div className={classes.root}>
            <>
              <Header />
              <Sidebar />
              <div className={classes.content}>
                <div className={classes.fakeToolbar} />
                <Switch>
                  <Route path="/app/buyerSellerRelationships" component={BuyerSellerRelationships} />
                  <Route path="/app/quoteRequests" component={QuoteRequests} />
                  <Route path="/app/quoteRequestsAccepted" component={QuoteRequestsAccepted} />
                  <Route path="/app/supplyInvitations" component={SupplyInvitations} />
                  <Route path="/app/supplyRequests" component={SupplyRequests} />
                  <Route path="/app/transportQuoteRequests" component={TransportQuoteRequests} />
                  <Route path="/app/transportQuoteRequestPendings" component={TransportQuoteRequestPendings} />
                  <Route path="/app/aggregatedQuotePendings" component={AggregatedQuotePending} />
                  <Route path="/app/aggregatedQuotes" component={AggregatedQuotes} />
                  <Route path="/app/quoteForBuyer" component={QuoteForBuyer} />
                  <Route path="/app/orders" component={Orders} />
                  <Route path="/app/deliveryInstructions" component={DeliveryInstructions} />
                  <Route path="/app/pickUpRequests" component={PickUpRequests} />
                  <Route path="/app/transportPendings" component={TransportPendings} />
                  <Route path="/app/deliveries" component={Deliveries} />
                  <Route path="/app/deliveryPayments" component={DeliveryPayments} />
                  <Route path="/app/supplierPayments" component={SupplierPayments} />
                  <Route path="/app/paymentObligations" component={PaymentObligations} />
                </Switch>
              </div>
            </>
        </div>
      </DamlLedger>
    );
  }
}

export default withRouter(Layout);
