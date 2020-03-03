/**
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
import da.refapps.supplychain.inventory.InventoryItem;
import da.refapps.supplychain.quoterequest.InventoryQuoteRequestBotTrigger;
import io.reactivex.Flowable;
import java.util.*;
import org.slf4j.Logger;

public class InventoryQuoteRequestBot {

  private final Logger logger;
  private final CommandsAndPendingSetBuilder commandBuilder;

  public final TransactionFilter transactionFilter;

  public InventoryQuoteRequestBot(
      CommandsAndPendingSetBuilder.Factory commandBuilderFactory, String partyName) {
    String workflowId =
        "WORKFLOW-" + partyName + "-InventoryQuoteRequestBot-" + UUID.randomUUID().toString();
    logger = BotLogger.getLogger(InventoryQuoteRequestBot.class, workflowId);

    commandBuilder = commandBuilderFactory.create(partyName, workflowId);

    Filter messageFilter =
        new InclusiveFilter(
            Sets.newHashSet(
                InventoryQuoteRequestBotTrigger.TEMPLATE_ID, InventoryItem.TEMPLATE_ID));

    this.transactionFilter = new FiltersByParty(Collections.singletonMap(partyName, messageFilter));

    logger.info("Startup.");
  }

  public Flowable<CommandsAndPendingSet> calculateCommands(
      LedgerViewFlowable.LedgerView<Template> ledgerView) {

    CommandsAndPendingSetBuilder.Builder builder = commandBuilder.newBuilder();

    Map<String, InventoryQuoteRequestBotTrigger> transportQuoteRequestBotTriggers =
        filterTemplates(
            InventoryQuoteRequestBotTrigger.class,
            ledgerView.getContracts(InventoryQuoteRequestBotTrigger.TEMPLATE_ID));

    Map<String, InventoryItem> inventoryItems =
        filterTemplates(InventoryItem.class, ledgerView.getContracts(InventoryItem.TEMPLATE_ID));

    for (Map.Entry<String, InventoryQuoteRequestBotTrigger> invQuoteReqTrigger :
        transportQuoteRequestBotTriggers.entrySet()) {
      String product = invQuoteReqTrigger.getValue().product.productName;
      String warehouse = invQuoteReqTrigger.getValue().warehouse;

      Optional<String> invItem =
          inventoryItems.entrySet().stream()
              .filter(
                  cidWItem ->
                      cidWItem.getValue().productName.equals(product)
                          && cidWItem.getValue().warehouse.equals(warehouse))
              .findFirst()
              .map(Map.Entry::getKey);
      if (invItem.isPresent()) {
        InventoryItem.ContractId inventoryItemCid = new InventoryItem.ContractId(invItem.get());
        builder.addCommand(
            new InventoryQuoteRequestBotTrigger.ContractId(invQuoteReqTrigger.getKey())
                .exerciseInventoryQuoteRequestBotTrigger_Accept(inventoryItemCid));
      } else {
        logger.error("No inventory item found for: {} in {}", product, warehouse);
      }
    }

    return builder
        .buildFlowable()
        .doOnError(err -> logger.error("There was an error: {}", err.getMessage()));
  }

  public Template getContractInfo(CreatedContract createdContract) {
    //noinspection unchecked
    return TemplateUtils.contractTransformer(
            InventoryQuoteRequestBotTrigger.class, InventoryItem.class)
        .apply(createdContract);
  }
}
