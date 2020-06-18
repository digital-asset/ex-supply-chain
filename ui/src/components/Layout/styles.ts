///
/// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
/// SPDX-License-Identifier: Apache-2.0
///

import { makeStyles, createStyles } from "@material-ui/styles";

export default makeStyles((theme : any) => createStyles({
  root: {
    display: "flex",
    maxWidth: "100vw",
    overflowX: "hidden",
  },
  content: {
    flexGrow: 1,
    padding: theme.spacing(3),
    width: `calc(100vw - ${240 + theme.spacing(6)}px)`,
    minHeight: "100vh",
  },
  fakeToolbar: {
    ...theme.mixins.toolbar,
  },
}));
