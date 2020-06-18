/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import React from "react";
import ReactDOM from "react-dom";
import { ThemeProvider } from "@material-ui/styles";
import { CssBaseline } from "@material-ui/core";

import Themes from "./themes";
import App from "./components/App";
import { UserProvider } from "./context/UserContext";

ReactDOM.render(
  <UserProvider>
    <ThemeProvider theme={Themes.default}>
      <CssBaseline />
      <App />
    </ThemeProvider>
  </UserProvider>,
  document.getElementById("root"),
);
