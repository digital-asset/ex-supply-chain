/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import { Link, withRouter, RouteComponentProps } from "react-router-dom";
import { Location } from "history";
import Drawer from "@material-ui/core/Drawer";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import ListItemText from "@material-ui/core/ListItemText";
import ListIcon from "@material-ui/icons/List";
import useStyles from "./styles";
import { useUserState } from "../../context/UserContext";

type SidebarLinkProps = {
  path : string
  icon : JSX.Element
  label : string
  location : Location<unknown>
}

const Sidebar = ({ location } : RouteComponentProps) => {
  const classes = useStyles();
  const userState = useUserState();
  const party = userState.isAuthenticated ? userState.party.displayName : "unauthenticated";

  const whatIsVisibleByWhom = new Map([
    ['quoteRequests', ["Buyer", "Seller"]],
    ['buyerSellerRelationships', ["Buyer"]],
    ['quoteRequestsAccepted', ["Buyer", "Seller"]],
    ['supplyInvitations', ["Seller", "Supplier"]],
    ['supplyRequests', ["Supplier"]],
    ['transportQuoteRequests', ["TransportCompany1", "TransportCompany2"]],
    ['transportQuoteRequestPendings', ["Supplier"]],
    ['aggregatedQuotePendings', ["Supplier"]],
    ['aggregatedQuotes', ["Seller"]],
    ['quoteForBuyer', ["Buyer", "Seller"]],
    ['orders', ["Buyer", "Seller"]],
    ['deliveryInstructions', ["TransportCompany1", "TransportCompany2"]],
    ['pickupRequests', ["Warehouse1", "Warehouse2"]],
    ['transportPendings', ["TransportCompany1", "TransportCompany2"]],
    ['deliveries', ["Buyer", "Warehouse1", "Warehouse2", "TransportCompany1", "TransportCompany2"]],
    ['deliveryPayments', ["Seller", "Supplier"]],
    ['supplierPayments', ["Supplier"]],
    ['paymentObligations', ["Seller", "Supplier", "Buyer", "Warehouse1", "Warehouse2", "TransportCompany1", "TransportCompany2"]],
  ]);
  const panelNames = new Map([
    ['buyerSellerRelationships', "Buyer Seller Relationships"],
    ['quoteRequests', "Quote Requests"],
    ['quoteRequestsAccepted', "Accepted Quote Requests"],
    ['supplyInvitations', "Supply Invitations"],
    ['supplyRequests', "Supply Requests"],
    ['transportQuoteRequests', "Transport Quote Requests"],
    ['transportQuoteRequestPendings', "Pending Transport Quote Requests"],
    ['aggregatedQuotePendings', "Pending Aggregated Quotes"],
    ['aggregatedQuotes', "Aggregated Quotes"],
    ['quoteForBuyer', "Quotes"],
    ['orders', "Orders"],
    ['deliveryInstructions', "Delivery Instructions"],
    ['pickupRequests', "Pickup Requests"],
    ['transportPendings', "Pending Transport"],
    ['deliveries', "Deliveries"],
    ['deliveryPayments', "Delivery Payments"],
    ['supplierPayments', "Supplier Payments"],
    ['paymentObligations', "Payment Obligations"],
  ]);

  function SidebarItem(props: { identifier: string }) {
    const partiesWhoCanSee = whatIsVisibleByWhom.get(props.identifier);
    if (partiesWhoCanSee && partiesWhoCanSee.includes(party)) {
      return (
        <SidebarLink
          key={0}
          label={panelNames.get(props.identifier) || "unassigned"}
          path={"/app/" + props.identifier}
          icon={(<ListIcon />)}
          location={location}
        />
        );
    }
    return null;
  }

  return (
    <Drawer open variant="permanent" className={classes.drawer} classes={{ paper: classes.drawer }}>
      <div className={classes.toolbar} />
      <List style={{ width: "100%" }}>
        <SidebarItem identifier="buyerSellerRelationships" />
        <SidebarItem identifier="quoteRequests" />
        <SidebarItem identifier="quoteRequestsAccepted" />
        <SidebarItem identifier="supplyInvitations" />
        <SidebarItem identifier="supplyRequests" />
        <SidebarItem identifier="transportQuoteRequests" />
        <SidebarItem identifier="transportQuoteRequestPendings" />
        <SidebarItem identifier="aggregatedQuotePendings" />
        <SidebarItem identifier="aggregatedQuotes" />
        <SidebarItem identifier="quoteForBuyer" />
        <SidebarItem identifier="orders" />
        <SidebarItem identifier="deliveryInstructions" />
        <SidebarItem identifier="pickupRequests" />
        <SidebarItem identifier="transportPendings" />
        <SidebarItem identifier="deliveries" />
        <SidebarItem identifier="deliveryPayments" />
        <SidebarItem identifier="supplierPayments" />
        <SidebarItem identifier="paymentObligations" />
      </List>
    </Drawer>
  );
};

const SidebarLink = ({ path, icon, label, location } : SidebarLinkProps) => {
  const classes = useStyles();
  const active = path && (location.pathname === path || location.pathname.indexOf(path) !== -1);

  return (
    <ListItem button component={Link} to={path} className={classes.link} classes={{ root: active ? classes.linkActive : classes.linkRoot }} disableRipple>
      <ListItemIcon className={active ? classes.linkIconActive : classes.linkIcon}>{icon}</ListItemIcon>
      <ListItemText classes={{ primary: active ? classes.linkTextActive : classes.linkText }} primary={label} />
    </ListItem>
  );
}

export default withRouter(Sidebar);
