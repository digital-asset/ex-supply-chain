/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.supplychain.util;

import java.util.function.Supplier;

/** Error handling utilities, throw error on illegal state / argument. */
public class ErrorUtils {
  public static Supplier<IllegalStateException> illegalState(String msg) {
    return () -> new IllegalStateException(msg);
  }

  public static Supplier<IllegalArgumentException> illegalArgument(String msg) {
    return () -> new IllegalArgumentException(msg);
  }
}
