/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.supplychain;

import static com.digitalasset.refapps.supplychain.util.TemplateManager.filterTemplates;

import com.daml.ledger.javaapi.data.Filter;
import com.daml.ledger.javaapi.data.FiltersByParty;
import com.daml.ledger.javaapi.data.InclusiveFilter;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.rxjava.components.LedgerViewFlowable;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import com.daml.ledger.rxjava.components.helpers.CreatedContract;
import com.daml.ledger.rxjava.components.helpers.TemplateUtils;
import com.digitalasset.refapps.supplychain.util.BotLogger;
import com.digitalasset.refapps.supplychain.util.CommandsAndPendingSetBuilder;
import com.google.common.collect.Sets;
import da.refapps.supplychain.delivery.DeliveryComplete;
import io.reactivex.Flowable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;

public class DeliveryCompleteBot {

  private final Logger logger;
  private final CommandsAndPendingSetBuilder commandBuilder;

  public final TransactionFilter transactionFilter;

  public DeliveryCompleteBot(
      CommandsAndPendingSetBuilder.Factory commandBuilderFactory, String partyName) {
    String workflowId =
        "WORKFLOW-" + partyName + "-DeliveryCompleteBot-" + UUID.randomUUID().toString();
    logger = BotLogger.getLogger(DeliveryCompleteBot.class, workflowId);

    commandBuilder = commandBuilderFactory.create(partyName, workflowId);

    Filter messageFilter = new InclusiveFilter(Sets.newHashSet(DeliveryComplete.TEMPLATE_ID));

    this.transactionFilter = new FiltersByParty(Collections.singletonMap(partyName, messageFilter));

    logger.info("Startup.");
  }

  public Flowable<CommandsAndPendingSet> calculateCommands(
      LedgerViewFlowable.LedgerView<Template> ledgerView) {

    CommandsAndPendingSetBuilder.Builder builder = commandBuilder.newBuilder();

    Map<String, DeliveryComplete> deliveryCompleteContracts =
        filterTemplates(
            DeliveryComplete.class, ledgerView.getContracts(DeliveryComplete.TEMPLATE_ID));

    for (Map.Entry<String, DeliveryComplete> e : deliveryCompleteContracts.entrySet()) {
      builder.addCommand(
          new DeliveryComplete.ContractId(e.getKey()).exerciseDeliveryComplete_Accept());
    }

    return builder
        .buildFlowable()
        .doOnError(err -> logger.error("There was an error: {}", err.getMessage()));
  }

  public Template getContractInfo(CreatedContract createdContract) {
    //noinspection unchecked
    return TemplateUtils.contractTransformer(DeliveryComplete.class).apply(createdContract);
  }
}
