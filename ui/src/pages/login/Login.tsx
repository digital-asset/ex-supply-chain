/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React, { useState, useMemo } from "react";
import { Grid, CircularProgress, Typography, Button, Fade, Select, MenuItem, FormControl, InputLabel } from "@material-ui/core";
import { withRouter, RouteComponentProps } from "react-router-dom";
import useStyles from "./styles";
import logo from "./logo.svg";
import { useUserDispatch, loginUser } from "../../context/UserContext";
import { createToken, httpBaseUrl } from "../../config";
import rp from "request-promise";
import { addSpacesBetweenWords } from "../../components/Util";

function addPath(baseUrl: string, path: string): string {
  return baseUrl.endsWith('/')
      ? `${baseUrl}${path}`
      : `${baseUrl}/${path}`;
}

async function getParties(): Promise<any[]> {
  const defaultUser = "Buyer";
  const token = createToken(defaultUser);
  const options = {
      url: addPath(httpBaseUrl, '/v1/parties'),
      headers: {
          Authorization: `Bearer ${token}`
      }
  };
  const response = await rp(options).catch(e => console.error(e));
  const { result } = JSON.parse(response);

  return result;
}

export class SortedPartyNames {
  private parties: any[];

  private compareByDisplayName(p1 : any, p2 : any) {
    if ( p1.displayName < p2.displayName ){
      return -1;
    }
    if ( p1.displayName > p2.displayName ){
      return 1;
    }
    return 0;
  }

  private addSpacesBetweenWordsInDisplayName(p: any) {
    return {...p, displayName: addSpacesBetweenWords(p.displayName)};
  }

  constructor() {
    var [parties, setParties] = useState([] as any[]);
    const partiesPromiseMemo = useMemo(() => getParties(), []);
    partiesPromiseMemo.then(ps => setParties(ps));
    this.parties = parties;
  }

  getParties() {
    return this.parties
      .sort(this.compareByDisplayName)
      .map(this.addSpacesBetweenWordsInDisplayName, this);
  }
}

function Login(props : RouteComponentProps) {
  var classes = useStyles();

  var userDispatch = useUserDispatch();

  var [isLoading, setIsLoading] = useState(false);
  var [error, setError] = useState(false);
  var [loginValue, setLoginValue] = useState("");
  const parties = new SortedPartyNames().getParties();

  return (
    <Grid container className={classes.container}>
      <div className={classes.logotypeContainer}>
        <Typography className={classes.logotypeText}>Supply Chain</Typography>
        <img src={logo} alt="logo" className={classes.logotypeImage} />
      </div>
      <div className={classes.formContainer}>
        <div className={classes.form}>
            <React.Fragment>
              <Fade in={error}>
                <Typography color="secondary" className={classes.errorMessage}>
                  Something is wrong with your login or password :(
                </Typography>
              </Fade>
              <FormControl className={classes.formControl}>
                <InputLabel>Username</InputLabel>
                <Select
                  id="email"
                  value={loginValue}
                  defaultValue={loginValue}
                  onChange={e => setLoginValue(e.target.value as string)}
                  onKeyDown={e => {
                    if (e.key === "Enter") {
                      loginUser(
                        userDispatch,
                        loginValue,
                        props.history,
                        setIsLoading,
                        setError,
                      )
                    }
                  }}
                  fullWidth
                >
                  {
                    parties
                      .map(p => <MenuItem key={p.identifier} value={p.identifier}>{p.displayName}</MenuItem>)
                  }
                </Select>
              </FormControl>
              <div className={classes.formButtons}>
                {isLoading ?
                  <CircularProgress size={26} className={classes.loginLoader} />
                : <Button
                    disabled={loginValue.length === 0}
                    onClick={() =>
                      loginUser(
                        userDispatch,
                        loginValue,
                        props.history,
                        setIsLoading,
                        setError,
                      )
                    }
                    variant="contained"
                    color="primary"
                    size="large"
                  >
                    Login
                  </Button>}
              </div>
            </React.Fragment>
        </div>
      </div>
    </Grid>
  );
}

export default withRouter(Login);
