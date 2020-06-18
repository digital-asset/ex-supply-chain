/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import Typography from "@material-ui/core/Typography";
import ExitToApp from "@material-ui/icons/ExitToApp";
import { useUserDispatch, signOut, useUserState } from "../../context/UserContext";
import useStyles from "./styles";

const Header = ({ history } : RouteComponentProps) => {
  const classes = useStyles();

  const userState = useUserState();
  const userDispatch = useUserDispatch();

  return (
    <AppBar position="fixed" className={classes.appBar}>
      <Toolbar className={classes.toolbar}>
        <Typography variant="h6" className={classes.logotype}>
          DAML App Template
        </Typography>
        <div className={classes.grow} />
        { userState.isAuthenticated && <Typography variant="h6">User: {userState.party}</Typography> }
        <IconButton
          aria-haspopup="true"
          color="inherit"
          className={classes.headerMenuButton}
          aria-controls="profile-menu"
          onClick={(event) => signOut(userDispatch, history)}
        >
          <ExitToApp className={classes.headerIcon} />
        </IconButton>
      </Toolbar>
    </AppBar>
  );
}

export default withRouter(Header);