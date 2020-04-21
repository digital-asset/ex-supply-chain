/**
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
import da.refapps.supplychain.aggregate.AggregatedQuoteTrigger;
import da.refapps.supplychain.delivery.DeliveryPlan;
import da.refapps.supplychain.quoterequest.QuoteRequest;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;
import org.pcollections.HashTreePMap;

public class AggregatedQuoteBotTest {

  private static final String PARTY = "TESTPARTY";

  CommandsAndPendingSetBuilder.Factory commandBuilder =
      CommandsAndPendingSetBuilder.factory("appId", Duration.ofSeconds(5));
  private LedgerViewFlowable.LedgerTestView<Template> ledgerView;
  private AggregatedQuoteBot bot = new AggregatedQuoteBot(commandBuilder, PARTY);

  @Test
  public void calculateCommands() throws InvocationTargetException, IllegalAccessException {
    ledgerView =
        new LedgerViewFlowable.LedgerTestView(
            HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());

    DeliveryPlan.ContractId dCoid1 = new DeliveryPlan.ContractId("cid-01");
    DeliveryPlan.ContractId dCoid2 = new DeliveryPlan.ContractId("cid-02");

    QuoteRequest.ContractId quoteId = new QuoteRequest.ContractId("Q1");
    String wfId = quoteId.toString();

    AggregatedQuoteTrigger aQt1 =
        new AggregatedQuoteTrigger(
            wfId, "supplier", "buyer", "address", "seller", Collections.emptyList(), dCoid1);
    AggregatedQuoteTrigger aQt2 =
        new AggregatedQuoteTrigger(
            wfId, "supplier", "buyer", "address", "seller", Collections.emptyList(), dCoid2);

    ledgerView =
        ledgerView
            .addActiveContract(AggregatedQuoteTrigger.TEMPLATE_ID, "cid-03", aQt1)
            .addActiveContract(AggregatedQuoteTrigger.TEMPLATE_ID, "cid-04", aQt2);

    CommandsAndPendingSet cmds = bot.calculateCommands(ledgerView).blockingFirst();

    @NonNull List<Command> actualCommands = cmds.getSubmitCommandsRequest().getCommands();
    assertEquals(2, actualCommands.size());
    actualCommands.forEach(
        cmd -> {
          Optional<ExerciseCommand> exerciseCommand = cmd.asExerciseCommand();
          assertTrue(exerciseCommand.isPresent());
          assertEquals("AggregatedQuoteTrigger_Execute", exerciseCommand.get().getChoice());
        });
  }
}
