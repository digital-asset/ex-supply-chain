///
/// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
/// SPDX-License-Identifier: Apache-2.0
///

import { makeStyles, createStyles } from "@material-ui/styles";

export default makeStyles((theme : any) => createStyles({
  scrollable: {
    width: '100%',
    overflowX: 'auto',
  },
  tableCell: {
    verticalAlign: "top",
    paddingTop: 6,
    paddingBottom: 6,
    paddingLeft: 8,
    paddingRight: 8,
    fontSize: "0.75rem"
  },
  tableRow: {
    height: "auto"
  },
  textField: {
    fontSize: "0.75rem"
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
  marginB: {
    marginBottom: theme.spacing(2),
  }
}));
