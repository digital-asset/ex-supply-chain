/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import { Stream } from '@daml/ledger';
import { ThemeProvider } from "@material-ui/styles";
import { CssBaseline } from "@material-ui/core";
import Themes from "../themes";
import { UserProvider } from "../context/UserContext";


const mockLedgerFunction = jest.fn();

// See: https://discuss.daml.com/t/how-to-mock-usestreamqueries-in-jest-test/2095/2
jest.mock('@daml/ledger', () => class {
  streamQueries(...args: unknown[]): Stream<object, string, string, string[]>{
    return {...mockLedgerFunction(...args), on: jest.fn(), close: jest.fn()};
  }
});

it('renders without crashing', () => {
  const div = document.createElement('div');
  document.body.appendChild(div);
  ReactDOM.render(
    <UserProvider>
      <ThemeProvider theme={Themes.default}>
        <CssBaseline />
        <App />
      </ThemeProvider>
    </UserProvider>,
    div);
  ReactDOM.unmountComponentAtNode(div);
});
