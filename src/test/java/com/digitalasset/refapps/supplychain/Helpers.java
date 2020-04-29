/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.supplychain;

import com.daml.ledger.rxjava.components.LedgerViewFlowable.LedgerTestView;
import org.pcollections.HashTreePMap;

class Helpers {
  static <T> LedgerTestView<T> emptyLedgerTestView() {
    return new LedgerTestView<>(
        HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());
  }
}
