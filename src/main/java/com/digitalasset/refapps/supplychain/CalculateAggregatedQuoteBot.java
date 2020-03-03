/**
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.digitalasset.refapps.supplychain;

import static com.digitalasset.refapps.supplychain.util.TemplateManager.filterTemplates;
import static com.digitalasset.refapps.supplychain.util.TemplateManager.filterTemplatesWith;

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
import da.refapps.supplychain.inventory.InventoryItem;
import da.refapps.supplychain.quote.InventoryQuote;
import da.refapps.supplychain.quote.TransportQuote;
import da.refapps.supplychain.quoterequest.CalculateAggregatedQuoteBotTrigger;
import io.reactivex.Flowable;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class CalculateAggregatedQuoteBot {

  private final Logger logger;
  private final CommandsAndPendingSetBuilder commandBuilder;

  public final TransactionFilter transactionFilter;

  public CalculateAggregatedQuoteBot(
      CommandsAndPendingSetBuilder.Factory commandBuilderFactory, String partyName) {
    String workflowId =
        "WORKFLOW-" + partyName + "-ChooseTransportBot-" + UUID.randomUUID().toString();
    logger = BotLogger.getLogger(CalculateAggregatedQuoteBot.class, workflowId);

    commandBuilder = commandBuilderFactory.create(partyName, workflowId);

    Filter messageFilter =
        new InclusiveFilter(
            Sets.newHashSet(
                CalculateAggregatedQuoteBotTrigger.TEMPLATE_ID,
                TransportQuote.TEMPLATE_ID,
                InventoryQuote.TEMPLATE_ID,
                InventoryItem.TEMPLATE_ID));

    this.transactionFilter = new FiltersByParty(Collections.singletonMap(partyName, messageFilter));
    logger.info("Startup.");
  }

  public Flowable<CommandsAndPendingSet> calculateCommands(
      LedgerViewFlowable.LedgerView<Template> ledgerView) {

    CommandsAndPendingSetBuilder.Builder builder = commandBuilder.newBuilder();

    Map<String, CalculateAggregatedQuoteBotTrigger> transportBotTriggers =
        filterTemplates(
            CalculateAggregatedQuoteBotTrigger.class,
            ledgerView.getContracts(CalculateAggregatedQuoteBotTrigger.TEMPLATE_ID));

    for (Map.Entry<String, CalculateAggregatedQuoteBotTrigger> e :
        transportBotTriggers.entrySet()) {
      String workflowId = e.getValue().workflowId;

      List<TransportQuote.ContractId> quoteCids =
          filterTemplatesWith(
                  TransportQuote.class,
                  ledgerView.getContracts(TransportQuote.TEMPLATE_ID),
                  transpQuote -> transpQuote.workflowId.equals(workflowId))
              .entrySet().stream()
              .map(invItemRes -> new TransportQuote.ContractId(invItemRes.getKey()))
              .collect(Collectors.toList());
      List<Map.Entry<String, InventoryQuote>> inventoryQuotes =
          new ArrayList<>(
              filterTemplatesWith(
                      InventoryQuote.class,
                      ledgerView.getContracts(InventoryQuote.TEMPLATE_ID),
                      invItemRes -> invItemRes.workflowId.equals(workflowId))
                  .entrySet());
      List<InventoryQuote.ContractId> inventoryQuoteCids =
          inventoryQuotes.stream()
              .map(entry -> new InventoryQuote.ContractId(entry.getKey()))
              .collect(Collectors.toList());
      List<InventoryItem.ContractId> inventoryItemCids =
          filterTemplatesWith(
                  InventoryItem.class,
                  ledgerView.getContracts(InventoryItem.TEMPLATE_ID),
                  invItem -> isInventoryItemAmongInvQuotes(inventoryQuotes, invItem))
              .entrySet().stream()
              .map(invItemRes -> new InventoryItem.ContractId(invItemRes.getKey()))
              .collect(Collectors.toList());

      builder.addCommand(
          new CalculateAggregatedQuoteBotTrigger.ContractId(e.getKey())
              .exerciseCalculateAggregatedQuoteBotTrigger_Proceed(
                  quoteCids, inventoryQuoteCids, inventoryItemCids));
    }

    return builder
        .buildFlowable()
        .doOnError(err -> logger.error("There was an error: {}", err.getMessage()));
  }

  private boolean isInventoryItemAmongInvQuotes(
      List<Map.Entry<String, InventoryQuote>> inventoryQuotes, InventoryItem invItem) {
    for (Map.Entry<String, InventoryQuote> reservedInvItem : inventoryQuotes) {
      if (invItem.productName.equals(reservedInvItem.getValue().productName)
          && invItem.warehouse.equals(reservedInvItem.getValue().warehouse)) {
        return true;
      }
    }
    return false;
  }

  public Template getContractInfo(CreatedContract createdContract) {
    //noinspection unchecked
    return TemplateUtils.contractTransformer(
            CalculateAggregatedQuoteBotTrigger.class,
            TransportQuote.class,
            InventoryQuote.class,
            InventoryItem.class)
        .apply(createdContract);
  }
}
