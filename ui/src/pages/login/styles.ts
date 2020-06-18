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
    justifyContent: "center",
    alignItems: "center",
    position: "absolute",
    top: 0,
    left: 0,
  },
  logotypeContainer: {
    backgroundColor: theme.palette.background.default,
    width: "60%",
    height: "100%",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
  },
  logotypeImage: {
    width: 165,
    marginBottom: theme.spacing(4),
  },
  logotypeText: {
    color: "#3a478f",
    fontWeight: 500,
    fontSize: 64,
  },
  formContainer: {
    width: "40%",
    height: "100%",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
  },
  form: {
    width: 320,
  },
  tab: {
    fontWeight: 400,
    fontSize: 18,
  },
  dablLoginButton: {
    marginBottom: theme.spacing(2)
  },
  errorMessage: {
    textAlign: "center",
  },
  textFieldUnderline: {
    "&:before": {
      borderBottomColor: theme.palette.primary.light,
    },
    "&:after": {
      borderBottomColor: theme.palette.primary.main,
    },
    "&:hover:before": {
      borderBottomColor: `${theme.palette.primary.light} !important`,
    },
  },
  textField: {
    borderBottomColor: theme.palette.background.light,
  },
  formButtons: {
    width: "100%",
    marginTop: theme.spacing(4),
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
  loginLoader: {
    marginLeft: theme.spacing(4),
  },
  formControl: {
    width: "100%",
  },
}));
