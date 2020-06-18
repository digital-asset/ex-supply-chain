///
/// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
/// SPDX-License-Identifier: Apache-2.0
///

import { makeStyles, createStyles } from "@material-ui/styles";

export default makeStyles((theme : any) => createStyles({
  container: {
    height: "100vh",
    width: "100vw",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
    position: "absolute",
    top: 0,
    left: 0,
  },
  paperRoot: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    paddingTop: theme.spacing(8),
    paddingBottom: theme.spacing(8),
    paddingLeft: theme.spacing(6),
    paddingRight: theme.spacing(6),
    maxWidth: 404,
  },
  errorCode: {
    marginBottom: theme.spacing(5),
    fontSize: 84,
    fontWeight: 600,
  },
  safetyText: {
    marginBottom: theme.spacing(5),
    fontWeight: 300,
    color: theme.palette.text,
    textAlign: "center",
  },
  backButton: {
    textTransform: "none",
    fontSize: 22,
  },
}));
