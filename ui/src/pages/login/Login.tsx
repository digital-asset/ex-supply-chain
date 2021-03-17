/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React, {useEffect, useState} from "react";
import {
  Button,
  CircularProgress,
  Fade,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Typography
} from "@material-ui/core";
import {RouteComponentProps, withRouter} from "react-router-dom";
import useStyles from "./styles";
import logo from "./logo.svg";
import {loginUser, useUserDispatch} from "../../context/UserContext";
import {createToken, handlePartiesLoad, httpBaseUrl, ledgerId} from "../../config";
import rp from "request-promise";
import {addSpacesBetweenWords} from "../../components/Util";
import { DablPartiesInput } from '@daml/dabl-react'

function addPath(baseUrl: string, path: string): string {
  const pathWithoutLeadingSlash = path.startsWith("/") ? path.slice(1) : path;
  return baseUrl.endsWith('/')
      ? `${baseUrl}${pathWithoutLeadingSlash}`
      : `${baseUrl}/${pathWithoutLeadingSlash}`;
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

function compareByDisplayName(p1: { displayName: string; }, p2: { displayName: string; }): number {
  if ( p1.displayName < p2.displayName ){
    return -1;
  }
  if ( p1.displayName > p2.displayName ){
    return 1;
  }
  return 0;
}

function addSpacesBetweenWordsInDisplayName(p: any) {
  return {...p, displayName: addSpacesBetweenWords(p.displayName)};
}

function sortParties(parties: any[]): any[] {
  return parties
      .sort(compareByDisplayName)
      .map(addSpacesBetweenWordsInDisplayName);
}

export function useSortedPartyNames(): any[] {
  var [parties, setParties] = useState([] as any[]);
  useEffect(() => {
    getParties().then(ps => setParties(sortParties(ps)));
  }, []);
  return parties;
}

function Login(props : RouteComponentProps) {
  var classes = useStyles();

  var userDispatch = useUserDispatch();

  var [isLoading, setIsLoading] = useState(false);
  var [error, setError] = useState(false);
  var [loginValue, setLoginValue] = useState("");

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
                  <MenuItem id="party1" value={"Buyer"}>Buyer</MenuItem>,
                  <MenuItem id="party2" value={"Seller"}>Seller</MenuItem>,
                  <MenuItem id="party3" value={"Warehouse1"}>Warehouse1</MenuItem>,
                  <MenuItem id="party4" value={"Warehouse2"}>Warehouse2</MenuItem>,
                  <MenuItem id="party5" value={"Supplier"}>Supplier</MenuItem>,
                  <MenuItem id="party6" value={"TransportCompany1"}>TransportCompany1</MenuItem>,
                  <MenuItem id="party7" value={"TransportCompany2"}>TransportCompany2</MenuItem>
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
        <div style={{marginTop: "30%"}}>
          <div>
            <label>Upload parties.json (tokens):</label>
          </div>
          <div>
            <DablPartiesInput
              ledgerId={ledgerId}
              onError={error => alert(error)}
              onLoad={handlePartiesLoad}/>
          </div>
        </div>
      </div>
    </Grid>
  );
}

export default withRouter(Login);
