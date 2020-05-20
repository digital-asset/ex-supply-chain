/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.supplychain;

import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.components.Bot;
import com.digitalasset.refapps.supplychain.util.CliOptions;
import com.digitalasset.refapps.supplychain.util.CommandsAndPendingSetBuilder;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupplyChain {
  public static final String APPLICATION_ID = "direct-asset-control";
  private static final Logger logger = LoggerFactory.getLogger(SupplyChain.class);

  private static final String SELLER_PARTY = "Seller";
  private static final String SUPPLIER_PARTY = "Supplier";
  //  private static final String TRANSPORT_PARTY1 = "TransportCompany1";
  //  private static final String TRANSPORT_PARTY2 = "TransportCompany2";
  private static final String WAREHOUSE1 = "Warehouse1";
  private static final String WAREHOUSE2 = "Warehouse2";

  public static void main(String[] args) throws InterruptedException {
    CliOptions options = CliOptions.parseArgs(args);

    DamlLedgerClient client =
        DamlLedgerClient.newBuilder(options.getSandboxHost(), options.getSandboxPort()).build();
    logger.info(
        "Waiting for DAML Sandbox on {}:{}", options.getSandboxHost(), options.getSandboxPort());
    runBots(client);

    logger.info("Welcome to Direct Asset Control Demo Application!");
    logger.info("Press Ctrl+C (for Mac and Linux, Ctrl+Z on Windows) to shut down the program.");
    Thread.currentThread().join();
  }

  public static void runBots(DamlLedgerClient client) {
    waitForSandbox(client);
    logger.info("Connected to DAML Sandbox.");

    Duration mrt = Duration.ofSeconds(10);
    CommandsAndPendingSetBuilder.Factory commandBuilderFactory =
        CommandsAndPendingSetBuilder.factory(APPLICATION_ID, mrt);

    CalculateAggregatedQuoteBot calculateAggregatedQuoteBot =
        new CalculateAggregatedQuoteBot(commandBuilderFactory, SUPPLIER_PARTY);
    Bot.wire(
        APPLICATION_ID,
        client,
        calculateAggregatedQuoteBot.transactionFilter,
        calculateAggregatedQuoteBot::calculateCommands,
        calculateAggregatedQuoteBot::getContractInfo);

    DeliveryCompleteBot transportCapacityReleaseBot =
        new DeliveryCompleteBot(commandBuilderFactory, SELLER_PARTY);
    Bot.wire(
        APPLICATION_ID,
        client,
        transportCapacityReleaseBot.transactionFilter,
        transportCapacityReleaseBot::calculateCommands,
        transportCapacityReleaseBot::getContractInfo);

    InventoryQuoteRequestBot inventoryQuoteRequestBot1 =
        new InventoryQuoteRequestBot(commandBuilderFactory, WAREHOUSE1);
    Bot.wire(
        APPLICATION_ID,
        client,
        inventoryQuoteRequestBot1.transactionFilter,
        inventoryQuoteRequestBot1::calculateCommands,
        inventoryQuoteRequestBot1::getContractInfo);

    InventoryQuoteRequestBot inventoryQuoteRequestBot2 =
        new InventoryQuoteRequestBot(commandBuilderFactory, WAREHOUSE2);
    Bot.wire(
        APPLICATION_ID,
        client,
        inventoryQuoteRequestBot2.transactionFilter,
        inventoryQuoteRequestBot2::calculateCommands,
        inventoryQuoteRequestBot2::getContractInfo);
  }

  public static void waitForSandbox(DamlLedgerClient client) {
    boolean connected = false;
    while (!connected) {
      try {
        client.connect();
        connected = true;
      } catch (Exception _ignored) {
        logger.info("Connecting to sandbox...");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }
}
