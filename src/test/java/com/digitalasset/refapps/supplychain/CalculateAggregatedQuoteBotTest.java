/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.supplychain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.rxjava.components.LedgerViewFlowable;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import com.digitalasset.refapps.supplychain.util.CommandsAndPendingSetBuilder;
import da.refapps.supplychain.inventory.InventoryItem;
import da.refapps.supplychain.quote.InventoryQuote;
import da.refapps.supplychain.quote.TransportQuote;
import da.refapps.supplychain.quote.TransportQuoteItem;
import da.refapps.supplychain.quoterequest.CalculateAggregatedQuoteBotTrigger;
import da.refapps.supplychain.quoterequest.QuoteRequest;
import da.refapps.supplychain.types.WarehouseProduct;
import da.types.Tuple2;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;
import org.pcollections.HashTreePMap;

public class CalculateAggregatedQuoteBotTest {

  private static final String PARTY = "TESTPARTY";

  final CommandsAndPendingSetBuilder.Factory commandBuilder =
      CommandsAndPendingSetBuilder.factory("appId", Duration.ofSeconds(5));
  private final CalculateAggregatedQuoteBot bot =
      new CalculateAggregatedQuoteBot(commandBuilder, PARTY);

  @Test
  public void calculateCommands() {
    LedgerViewFlowable.LedgerTestView<Template> ledgerView =
        new LedgerViewFlowable.LedgerTestView(
            HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());

    QuoteRequest.ContractId quoteId = new QuoteRequest.ContractId("Q1");
    String wfId = quoteId.toString();
    QuoteRequest.ContractId otherQuoteId = new QuoteRequest.ContractId("Q2");
    String otherWfId = otherQuoteId.toString();

    CalculateAggregatedQuoteBotTrigger trigger =
        new CalculateAggregatedQuoteBotTrigger(
            wfId, "supplier", "buyer", "address", "seller", Collections.emptyList());

    Tuple2<WarehouseProduct, TransportQuoteItem> item = new Tuple2(null, null);

    TransportQuote tq = new TransportQuote(wfId, "transportCompany", "supplier", item);
    TransportQuote tq2 = new TransportQuote(otherWfId, "transportCompany", "supplier", item);
    InventoryQuote invQ =
        new InventoryQuote(wfId, "warehouse", "supplier", "product", 1L, BigDecimal.ONE);
    InventoryItem invItem =
        new InventoryItem("warehouse", "supplier", "product", 1L, BigDecimal.ONE);
    ledgerView =
        ledgerView
            .addActiveContract(CalculateAggregatedQuoteBotTrigger.TEMPLATE_ID, "cid-01", trigger)
            .addActiveContract(TransportQuote.TEMPLATE_ID, "cid-02", tq)
            .addActiveContract(TransportQuote.TEMPLATE_ID, "cid-03", tq2)
            .addActiveContract(InventoryQuote.TEMPLATE_ID, "cid-04", invQ)
            .addActiveContract(InventoryItem.TEMPLATE_ID, "cid-05", invItem);

    CommandsAndPendingSet cmds = bot.calculateCommands(ledgerView).blockingFirst();

    @NonNull List<Command> actualCommands = cmds.getSubmitCommandsRequest().getCommands();
    assertEquals(1, actualCommands.size());
    actualCommands.forEach(
        cmd -> {
          Optional<ExerciseCommand> exerciseCommand = cmd.asExerciseCommand();
          assertTrue(exerciseCommand.isPresent());
          assertEquals(
              "CalculateAggregatedQuoteBotTrigger_Proceed", exerciseCommand.get().getChoice());
        });
  }
}
