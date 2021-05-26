/*
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.daml.product.supplychain;

import static com.daml.extensions.testing.Dsl.list;
import static com.daml.extensions.testing.Dsl.record;
import static com.daml.extensions.testing.Dsl.text;

import com.daml.extensions.testing.junit4.Sandbox;
import com.daml.extensions.testing.ledger.DefaultLedgerAdapter;
import com.daml.extensions.testing.utils.ContractWithId;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.Text;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import da.refapps.supplychain.aggregate.AggregatedQuote;
import da.refapps.supplychain.aggregate.AggregatedQuotePending;
import da.refapps.supplychain.delivery.Delivery;
import da.refapps.supplychain.delivery.DeliveryInstruction;
import da.refapps.supplychain.delivery.DeliveryPayment;
import da.refapps.supplychain.delivery.DeliverySupplierPayment;
import da.refapps.supplychain.delivery.PickUpRequest;
import da.refapps.supplychain.delivery.TransportPending;
import da.refapps.supplychain.order.ConfirmedOrder;
import da.refapps.supplychain.quote.QuoteForBuyer;
import da.refapps.supplychain.quote.TransportQuoteItem;
import da.refapps.supplychain.quoterequest.CalculateAggregatedQuote;
import da.refapps.supplychain.quoterequest.QuoteRequest;
import da.refapps.supplychain.quoterequest.QuoteRequestAccepted;
import da.refapps.supplychain.quoterequest.QuoteRequestSupplyInvitation;
import da.refapps.supplychain.quoterequest.SupplyRequest;
import da.refapps.supplychain.quoterequest.TransportQuoteRequest;
import da.refapps.supplychain.quoterequest.TransportQuoteRequestPending;
import da.refapps.supplychain.relationship.BuyerSellerRelationship;
import da.refapps.supplychain.types.OrderedProduct;
import da.refapps.supplychain.types.WarehouseProduct;
import da.refapps.supplychain.types.WarehouseProductWithDates;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class SupplychainIT {
  private static final Path RELATIVE_DAR_PATH = Paths.get("./target/supplychain.dar");
  private static final Path RELATIVE_TRIGGER_DAR_PATH =
      Paths.get("./target/supplychain-triggers.dar");
  private static final String TEST_MODULE = "DA.RefApps.SupplyChain.LedgerSetupScript";
  private static final String TEST_SCRIPT = "initialize";

  private static final Party BUYER_PARTY = new Party("Buyer");
  private static final Party SELLER_PARTY = new Party("Seller");
  private static final Party SUPPLIER_PARTY = new Party("Supplier");
  private static final Party WAREHOUSE1_PARTY = new Party("Warehouse1");
  private static final Party WAREHOUSE2_PARTY = new Party("Warehouse2");
  private static final Party TRANSPORT1_PARTY = new Party("TransportCompany1");
  private static final Party TRANSPORT2_PARTY = new Party("TransportCompany2");
  private static final Text BUYER_ADDRESS = new Text("1234, Vice City, Arkham street 13");
  private static final Text WAREHOUSE1_ADDRESS = new Text("1345, Liberty City, Fleet street 1");
  private static final Text WAREHOUSE2_ADDRESS = new Text("2456, San Andreas, Main street 9");

  private static final Sandbox sandbox =
      Sandbox.builder()
          .dar(RELATIVE_DAR_PATH)
          .moduleAndScript(TEST_MODULE, TEST_SCRIPT)
          .parties(BUYER_PARTY.getValue(), SELLER_PARTY.getValue(), SUPPLIER_PARTY.getValue())
          .observationTimeout(Duration.ofSeconds(30))
          .build();

  @ClassRule public static ExternalResource sandboxClassRule = sandbox.getClassRule();
  private Process triggers;

  @Rule public TestRule sandboxRule = RuleChain.outerRule(sandbox.getRule());

  @Before
  public void setUp() throws Throwable {
    // Valid port is assigned only after the sandbox has been started.
    // Therefore trigger has to be configured at the point where this can be guaranteed.
    File log = new File("integration-triggers.log");
    File errLog = new File("integration-triggers.err.log");
    triggers =
        new ProcessBuilder()
            .command("launchers/automation", Integer.toString(sandbox.getSandboxPort()))
            .redirectOutput(ProcessBuilder.Redirect.appendTo(log))
            .redirectError(ProcessBuilder.Redirect.appendTo(errLog))
            .start();
  }

  @After
  public void tearDown() {
    // Use destroy() to allow subprocess cleanup.
    triggers.destroy();
  }

  @Test
  public void testFullWorkflow() throws IOException {
    final DefaultLedgerAdapter ledgerAdapter = sandbox.getLedgerAdapter();

    Text workflowId = text("quoteId");
    LocalDate date1 = LocalDate.of(2019, 6, 6);
    LocalDate date2 = LocalDate.of(2019, 6, 7);

    BuyerSellerRelationship.ContractId buyerSellerRelationshipCid =
        ledgerAdapter.getCreatedContractId(
            BUYER_PARTY,
            BuyerSellerRelationship.TEMPLATE_ID,
            record(BUYER_PARTY, BUYER_ADDRESS, SELLER_PARTY),
            BuyerSellerRelationship.ContractId::new);

    // Send a quote request
    OrderedProduct orderedProduct = new OrderedProduct("Product 1", 100L, date1, date2);
    ledgerAdapter.exerciseChoice(
        BUYER_PARTY,
        buyerSellerRelationshipCid.exerciseBuyerSellerRelationship_SendQuoteRequest(
            Collections.singletonList(orderedProduct)));

    QuoteRequest.ContractId quoteRequestCid =
        ledgerAdapter.getCreatedContractId(
            BUYER_PARTY,
            QuoteRequest.TEMPLATE_ID,
            record(BUYER_PARTY, BUYER_ADDRESS, SELLER_PARTY, list(orderedProduct.toValue())),
            QuoteRequest.ContractId::new);

    // Accept quote request
    ledgerAdapter.exerciseChoice(
        SELLER_PARTY, quoteRequestCid.exerciseQuoteRequest_Accept(workflowId.getValue()));
    QuoteRequestAccepted.ContractId quoteRequestAcceptedCid =
        ledgerAdapter.getCreatedContractId(
            SELLER_PARTY, QuoteRequestAccepted.TEMPLATE_ID, QuoteRequestAccepted.ContractId::new);

    ledgerAdapter.exerciseChoice(
        SELLER_PARTY,
        quoteRequestAcceptedCid.exerciseQuoteRequestAccepted_SendToSupplier(
            SUPPLIER_PARTY.getValue()));
    QuoteRequestSupplyInvitation.ContractId quoteRequestSupplyInvitationCid =
        ledgerAdapter.getCreatedContractId(
            SUPPLIER_PARTY,
            QuoteRequestSupplyInvitation.TEMPLATE_ID,
            QuoteRequestSupplyInvitation.ContractId::new);
    ledgerAdapter.exerciseChoice(
        SUPPLIER_PARTY,
        quoteRequestSupplyInvitationCid.exerciseQuoteRequestSupplyInvitation_Accept());
    SupplyRequest.ContractId supplyRequestCid =
        ledgerAdapter.getCreatedContractId(
            SUPPLIER_PARTY, SupplyRequest.TEMPLATE_ID, SupplyRequest.ContractId::new);

    // supply
    ledgerAdapter.exerciseChoice(
        SUPPLIER_PARTY,
        supplyRequestCid.exerciseSupplyRequest_StartPriceCollection(
            Lists.newArrayList(WAREHOUSE1_PARTY.getValue(), WAREHOUSE2_PARTY.getValue()),
            Lists.newArrayList(TRANSPORT1_PARTY.getValue(), TRANSPORT2_PARTY.getValue())));
    WarehouseProduct warehouseProduct1 =
        new WarehouseProduct(
            "Product 1", WAREHOUSE1_PARTY.getValue(), WAREHOUSE1_ADDRESS.getValue(), 100L);
    WarehouseProduct warehouseProduct2 =
        new WarehouseProduct(
            "Product 1", WAREHOUSE2_PARTY.getValue(), WAREHOUSE2_ADDRESS.getValue(), 100L);
    WarehouseProductWithDates warehouseProductWithDates1 =
        new WarehouseProductWithDates(warehouseProduct1, date1, date2);
    WarehouseProductWithDates warehouseProductWithDates2 =
        new WarehouseProductWithDates(warehouseProduct2, date1, date2);
    TransportQuoteItem transportQuoteItem11 =
        new TransportQuoteItem(25L, new BigDecimal(25), date1, date2);
    TransportQuoteItem transportQuoteItem12 =
        new TransportQuoteItem(50L, new BigDecimal(50), date1, date2);
    TransportQuoteItem transportQuoteItem21 =
        new TransportQuoteItem(50L, new BigDecimal(100), date1, date2);
    TransportQuoteItem transportQuoteItem22 =
        new TransportQuoteItem(100L, new BigDecimal(200), date1, date2);
    TransportQuoteRequest.ContractId transportQuoteReq11 =
        ledgerAdapter.getCreatedContractId(
            TRANSPORT1_PARTY,
            TransportQuoteRequest.TEMPLATE_ID,
            record(
                workflowId,
                TRANSPORT1_PARTY,
                SUPPLIER_PARTY,
                BUYER_PARTY,
                BUYER_ADDRESS,
                warehouseProductWithDates1.toValue()),
            TransportQuoteRequest.ContractId::new);
    ledgerAdapter.exerciseChoice(
        TRANSPORT1_PARTY,
        transportQuoteReq11.exerciseTransportQuoteRequest_Accept(transportQuoteItem11));

    TransportQuoteRequest.ContractId transportQuoteReq12 =
        ledgerAdapter.getCreatedContractId(
            TRANSPORT1_PARTY,
            TransportQuoteRequest.TEMPLATE_ID,
            record(
                workflowId,
                TRANSPORT1_PARTY,
                SUPPLIER_PARTY,
                BUYER_PARTY,
                BUYER_ADDRESS,
                warehouseProductWithDates2.toValue()),
            TransportQuoteRequest.ContractId::new);
    ledgerAdapter.exerciseChoice(
        TRANSPORT1_PARTY,
        transportQuoteReq12.exerciseTransportQuoteRequest_Accept(transportQuoteItem12));

    TransportQuoteRequest.ContractId transportQuoteReq21 =
        ledgerAdapter.getCreatedContractId(
            TRANSPORT2_PARTY,
            TransportQuoteRequest.TEMPLATE_ID,
            record(
                workflowId,
                TRANSPORT2_PARTY,
                SUPPLIER_PARTY,
                BUYER_PARTY,
                BUYER_ADDRESS,
                warehouseProductWithDates1.toValue()),
            TransportQuoteRequest.ContractId::new);
    ledgerAdapter.exerciseChoice(
        TRANSPORT2_PARTY,
        transportQuoteReq21.exerciseTransportQuoteRequest_Accept(transportQuoteItem21));

    TransportQuoteRequest.ContractId transportQuoteReq22 =
        ledgerAdapter.getCreatedContractId(
            TRANSPORT2_PARTY,
            TransportQuoteRequest.TEMPLATE_ID,
            record(
                workflowId,
                TRANSPORT2_PARTY,
                SUPPLIER_PARTY,
                BUYER_PARTY,
                BUYER_ADDRESS,
                warehouseProductWithDates2.toValue()),
            TransportQuoteRequest.ContractId::new);
    ledgerAdapter.exerciseChoice(
        TRANSPORT2_PARTY,
        transportQuoteReq22.exerciseTransportQuoteRequest_Accept(transportQuoteItem22));

    // Choose transport

    TransportQuoteRequestPending.ContractId transportQuoteRequestPendingCid =
        ledgerAdapter.getCreatedContractId(
            SUPPLIER_PARTY,
            TransportQuoteRequestPending.TEMPLATE_ID,
            TransportQuoteRequestPending.ContractId::new);

    ledgerAdapter.exerciseChoice(
        SUPPLIER_PARTY,
        transportQuoteRequestPendingCid.exerciseTransportQuoteRequestPending_ChooseTransport());

    // need to wait here a bit to avoid TimeoutException waiting for AggregatedQuotePending
    ledgerAdapter.getCreatedContractId(
        SUPPLIER_PARTY,
        CalculateAggregatedQuote.TEMPLATE_ID,
        CalculateAggregatedQuote.ContractId::new);

    AggregatedQuotePending.ContractId aggregateQuotePendingCid =
        ledgerAdapter.getCreatedContractId(
            SUPPLIER_PARTY,
            AggregatedQuotePending.TEMPLATE_ID,
            AggregatedQuotePending.ContractId::new);

    ledgerAdapter.exerciseChoice(
        SUPPLIER_PARTY,
        aggregateQuotePendingCid.exerciseAggregatedQuotePending_SendQuoteToSeller());
    AggregatedQuote.ContractId aggregatedQuoteCid =
        ledgerAdapter.getCreatedContractId(
            SELLER_PARTY, AggregatedQuote.TEMPLATE_ID, AggregatedQuote.ContractId::new);

    // order

    ledgerAdapter.exerciseChoice(
        SELLER_PARTY, aggregatedQuoteCid.exerciseAggregatedQuote_AddMargin(new BigDecimal("0.1")));
    QuoteForBuyer.ContractId quoteForBuyerCid =
        ledgerAdapter.getCreatedContractId(
            BUYER_PARTY, QuoteForBuyer.TEMPLATE_ID, QuoteForBuyer.ContractId::new);

    ledgerAdapter.exerciseChoice(BUYER_PARTY, quoteForBuyerCid.exerciseQuoteForBuyer_Accept());
    ConfirmedOrder.ContractId confirmedOrderCid =
        ledgerAdapter.getCreatedContractId(
            SELLER_PARTY, ConfirmedOrder.TEMPLATE_ID, ConfirmedOrder.ContractId::new);

    ledgerAdapter.exerciseChoice(
        SELLER_PARTY, confirmedOrderCid.exerciseConfirmedOrder_StartDelivery());

    // delivery
    ledgerAdapter.setCurrentTime(date1.atStartOfDay(ZoneId.of("UTC")).toInstant());
    nextPickup(ledgerAdapter, TRANSPORT1_PARTY);
    nextPickup(ledgerAdapter, TRANSPORT1_PARTY);
    nextPickup(ledgerAdapter, TRANSPORT2_PARTY);

    ledgerAdapter.setCurrentTime(date2.atStartOfDay(ZoneId.of("UTC")).toInstant());
    nextDelivery(ledgerAdapter);
    nextDelivery(ledgerAdapter);
    nextDelivery(ledgerAdapter);
  }

  private void nextPickup(DefaultLedgerAdapter ledgerAdapter, Party transportCompany)
      throws InvalidProtocolBufferException {
    ContractWithId<DeliveryInstruction.ContractId> deliveryInstructionWithCid =
        ledgerAdapter.getMatchedContract(
            transportCompany, DeliveryInstruction.TEMPLATE_ID, DeliveryInstruction.ContractId::new);
    ledgerAdapter.exerciseChoice(
        transportCompany,
        deliveryInstructionWithCid.contractId.exerciseDeliveryInstruction_PickUp());
    //noinspection OptionalGetWithoutIsPresent
    Party warehouse =
        deliveryInstructionWithCid
            .record
            .asRecord()
            .get()
            .getFieldsMap()
            .get("warehouse")
            .asParty()
            .get();
    PickUpRequest.ContractId pickUpRequestCid =
        ledgerAdapter.getCreatedContractId(
            warehouse, PickUpRequest.TEMPLATE_ID, PickUpRequest.ContractId::new);
    ledgerAdapter.exerciseChoice(warehouse, pickUpRequestCid.exercisePickUpRequest_Accept());
    TransportPending.ContractId transportPendingCid =
        ledgerAdapter.getCreatedContractId(
            transportCompany, TransportPending.TEMPLATE_ID, TransportPending.ContractId::new);
    ledgerAdapter.exerciseChoice(
        transportCompany, transportPendingCid.exerciseTransportPending_Deliver());
  }

  private void nextDelivery(DefaultLedgerAdapter ledgerAdapter)
      throws InvalidProtocolBufferException {
    ContractWithId<Delivery.ContractId> deliveryWithCid =
        ledgerAdapter.getMatchedContract(
            BUYER_PARTY, Delivery.TEMPLATE_ID, Delivery.ContractId::new);
    ledgerAdapter.exerciseChoice(
        BUYER_PARTY, deliveryWithCid.contractId.exerciseDelivery_Acknowledge());
    DeliveryPayment.ContractId deliveryPaymentCid =
        ledgerAdapter.getCreatedContractId(
            BUYER_PARTY, DeliveryPayment.TEMPLATE_ID, DeliveryPayment.ContractId::new);
    ledgerAdapter.exerciseChoice(
        SUPPLIER_PARTY, deliveryPaymentCid.exerciseDeliveryPayment_Accept());
    DeliverySupplierPayment.ContractId supplierPaymentCid =
        ledgerAdapter.getCreatedContractId(
            SUPPLIER_PARTY,
            DeliverySupplierPayment.TEMPLATE_ID,
            DeliverySupplierPayment.ContractId::new);
    ledgerAdapter.exerciseChoice(
        SUPPLIER_PARTY, supplierPaymentCid.exerciseDeliverySupplierPayment_Pay());
  }
}
