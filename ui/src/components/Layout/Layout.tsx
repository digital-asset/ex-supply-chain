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

const Layout = () => {
  const classes = useStyles();
  const user = useUserState();

  if(!user.isAuthenticated){
    return null;
  } else {
    return (
      <DamlLedger party={user.party} token={user.token} httpBaseUrl={httpBaseUrl} wsBaseUrl={wsBaseUrl}>
        <div className={classes.root}>
            <>
              <Header />
              <Sidebar />
              <div className={classes.content}>
                <div className={classes.fakeToolbar} />
                <Switch>
                  <Route path="/app/buyerSellerRelationships" component={BuyerSellerRelationships} />
                </Switch>
              </div>
            </>
        </div>
      </DamlLedger>
    );
  }
}

export default withRouter(Layout);
