/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import { History } from 'history';
import { createToken, dablLoginUrl } from "../config";

type AuthenticatedUser = {
  isAuthenticated : true
  token : string
  party : string
}

type UnAthenticated = {
  isAuthenticated : false
}

type UserState = UnAthenticated | AuthenticatedUser

type LoginSuccess = {
  type : "LOGIN_SUCCESS"
  token : string
  party : string
}

type LoginFailure = {
  type : "LOGIN_FAILURE"
}

type SignoutSuccess = {
  type : "SIGN_OUT_SUCCESS"
}

type LoginAction = LoginSuccess | LoginFailure | SignoutSuccess

const UserStateContext = React.createContext<UserState>({ isAuthenticated: false });
const UserDispatchContext = React.createContext<React.Dispatch<LoginAction>>({} as React.Dispatch<LoginAction>);

function userReducer(state : UserState, action : LoginAction) : UserState {
  switch (action.type) {
    case "LOGIN_SUCCESS":
      return { isAuthenticated: true, token: action.token, party: action.party };
    case "LOGIN_FAILURE":
      return { isAuthenticated: false };
    case "SIGN_OUT_SUCCESS":
      return { isAuthenticated: false };
  }
}

const UserProvider : React.FC = ({ children }) => {
  const party = localStorage.getItem("daml.party")
  const token = localStorage.getItem("daml.token")

  let initState : UserState = (!!party && !!token) ? { isAuthenticated : true, token, party } : { isAuthenticated : false };
  var [state, dispatch] = React.useReducer<React.Reducer<UserState,LoginAction>>(userReducer, initState);

  return (
    <UserStateContext.Provider value={state}>
      <UserDispatchContext.Provider value={dispatch}>
        {children}
      </UserDispatchContext.Provider>
    </UserStateContext.Provider>
  );
}

function useUserState() {
  var context = React.useContext<UserState>(UserStateContext);
  if (context === undefined) {
    throw new Error("useUserState must be used within a UserProvider");
  }
  return context;
}

function useUserDispatch() {
  var context = React.useContext<React.Dispatch<LoginAction>>(UserDispatchContext);
  if (context === undefined) {
    throw new Error("useUserDispatch must be used within a UserProvider");
  }
  return context;
}


// ###########################################################

function loginUser(
    dispatch : React.Dispatch<LoginAction>,
    party : string,
    history : History,
    setIsLoading : React.Dispatch<React.SetStateAction<boolean>>,
    setError : React.Dispatch<React.SetStateAction<boolean>>) {
  setError(false);
  setIsLoading(true);

  if (!!party) {
    const token = createToken(party)
    localStorage.setItem("daml.party", party);
    localStorage.setItem("daml.token", token);

    dispatch({ type: "LOGIN_SUCCESS", token, party });
    setError(false);
    setIsLoading(false);
    history.push("/app");
  } else {
    dispatch({ type: "LOGIN_FAILURE" });
    setError(true);
    setIsLoading(false);
  }
}

const loginDablUser = () => {
  window.location.assign(`https://${dablLoginUrl}`);
}

function signOut(dispatch : React.Dispatch<LoginAction>, history : History) {
  localStorage.removeItem("daml.party");
  localStorage.removeItem("daml.token");

  dispatch({ type: "SIGN_OUT_SUCCESS" });
  history.push("/login");
}

export { UserProvider, useUserState, useUserDispatch, loginUser, loginDablUser, signOut };
