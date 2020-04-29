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
import da.refapps.supplychain.delivery.DeliveryComplete;
import da.refapps.supplychain.lock.TransportCommitment;
import da.refapps.supplychain.payment.PaymentRequest;
import da.refapps.supplychain.quoterequest.QuoteRequest;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

public class DeliveryCompleteBotTest {

  private static final String PARTY = "TESTPARTY";

  final CommandsAndPendingSetBuilder.Factory commandBuilder =
      CommandsAndPendingSetBuilder.factory("appId", Duration.ofSeconds(5));
  private final DeliveryCompleteBot bot = new DeliveryCompleteBot(commandBuilder, PARTY);

  @Test
  public void calculateCommands() {
    LedgerViewFlowable.LedgerTestView<Template> ledgerView = Helpers.emptyLedgerTestView();

    TransportCommitment.ContractId lockedCap1 = new TransportCommitment.ContractId("cid-01");
    TransportCommitment.ContractId lockedCap2 = new TransportCommitment.ContractId("cid-02");
    PaymentRequest.ContractId buyerPayReq1 = new PaymentRequest.ContractId("cid-03");
    PaymentRequest.ContractId buyerPayReq2 = new PaymentRequest.ContractId("cid-04");
    PaymentRequest.ContractId warehousePayReq1 = new PaymentRequest.ContractId("cid-05");
    PaymentRequest.ContractId warehousePayReq2 = new PaymentRequest.ContractId("cid-06");
    PaymentRequest.ContractId supplierPayReq1 = new PaymentRequest.ContractId("cid-07");
    PaymentRequest.ContractId supplierPayReq2 = new PaymentRequest.ContractId("cid-08");

    QuoteRequest.ContractId quoteId = new QuoteRequest.ContractId("Q1");
    String wfId = quoteId.toString();

    DeliveryComplete tCrT1 =
        new DeliveryComplete(
            wfId,
            "buyer",
            "seller",
            "transportCompany",
            lockedCap1,
            buyerPayReq1,
            supplierPayReq1,
            warehousePayReq1);
    DeliveryComplete tCrT2 =
        new DeliveryComplete(
            wfId,
            "buyer",
            "seller",
            "transportCompany",
            lockedCap2,
            buyerPayReq2,
            supplierPayReq2,
            warehousePayReq2);

    ledgerView =
        ledgerView
            .addActiveContract(DeliveryComplete.TEMPLATE_ID, "cid-03", tCrT1)
            .addActiveContract(DeliveryComplete.TEMPLATE_ID, "cid-04", tCrT2);

    CommandsAndPendingSet cmds = bot.calculateCommands(ledgerView).blockingFirst();

    @NonNull List<Command> actualCommands = cmds.getSubmitCommandsRequest().getCommands();
    assertEquals(2, actualCommands.size());
    actualCommands.forEach(
        cmd -> {
          Optional<ExerciseCommand> exerciseCommand = cmd.asExerciseCommand();
          assertTrue(exerciseCommand.isPresent());
          assertEquals("DeliveryComplete_Accept", exerciseCommand.get().getChoice());
        });
  }
}
