# Reference Application: Supply Chain Application

## Overview

The Supply Chain application demonstrates a generic example between a Buyer, Seller, Supplier, two Warehouses, and two independent Transport Companies. The process includes:
* Creating a quote request from Buyer to Seller for a given product
* Supplier collecting quotes for product and transportation from the Warehouses and Transport Companies
* Aggregating delivery details with price, adding margin, and sending a quote to Buyer
* Upon acceptance by Buyer of the quote, order creation and initiation of goods delivery from the Warehouses
* Upon delivery of goods, payment from Buyer to Seller, either in full or partially, and creation of a payment obligation through the supply chain

## Getting Started

### Installing

**Disclaimer:** This reference application is intended to demonstrate the capabilities of the DAML. You are recommended to consider other non-functional aspects, like security, resiliency, recoverability, etc prior to production use.

#### Prerequisites

Be sure you have the following installed.

  - [DAML SDK](https://docs.daml.com/)
  - Java
  - Yarn
  - Node v12
  - Make

### Starting the App

1. Build the App. Type:
    ```shell
    make build
    ```
    **Note:** If you change the DAML models locally, you need to re-run this command before starting the application.
2. Start the DAML Sandbox and Navigator. Type:
    ```shell
    daml start --sandbox-option --address=localhost
    ```
    The navigator will automatically open in new browser tab at http://localhost:7500.
3. Once the sandbox has started, start the automation logic by starting triggers. Type:
    ```shell
    scripts/startTriggers.sh localhost 6865 target/supplychain-triggers.dar
    ```
4. Once the sandbox has started, start the automation logic by starting triggers. Type:
    ```shell
    cd ui
    yarn start
    ```

### Stopping the App

1. Stop the triggers by pressing **Ctrl+C**.
1. Stop the Sandbox and the Navigator by pressing **Ctrl+C** in the DAML assistant.

### Resetting the Prototype

Reset the application by following these steps:
1.  Stop the app by following the steps in [Stopping the App](#stopping-the-app) section.
2.  Start the app by following the steps in [Starting the App](#starting-the-app) section.

## Working with DAML Hub

1. As a first step, build the whole project:
```
make clean build
```

2. Start a new project at DAML Hub. Upload the DARs to DAML Hub (in your new project, Deployments tab / Upload file, two files `target/*.dar`), deploy the model (supplychain.dar, Deploy Instance).

3. Add the parties to the DAML Hub project: Buyer, Seller, Supplier, Warehouse1, Warehouse2, TransportCompany1, TransportCompany2.
    - Download `participants.json` (Ledger settings tab).
    - Download `parties.json` (Users tab).

4. Run the market setup:
```
daml script \
  --participant-config participants.json \
  --json-api \
  --dar target/supplychain.dar \
  --script-name DA.RefApps.SupplyChain.LedgerSetupScript:setupMarketWithDablParties \
  --input-file parties.json
```

5. Run the triggers from the DAML Hub UI:
```
Seller
DA.RefApps.SupplyChain.Triggers.AggregatedQuoteTrigger:trigger
DA.RefApps.SupplyChain.Triggers.DeliveryCompleteTrigger:trigger

Warehouse1
DA.RefApps.SupplyChain.Triggers.InventoryQuoteRequestTrigger:trigger

Warehouse2
DA.RefApps.SupplyChain.Triggers.InventoryQuoteRequestTrigger:trigger

Supplier
DA.RefApps.SupplyChain.Triggers.CalculateAggregatedQuoteTrigger:trigger
```

6. Run `make package LEDGER_ID=<ledgerId>` Upload `target/supplychain.zip` to DAML Hub and deploy the UI. Follow "View site". Upload `parties.json` to the UI using the button on the login screen (Upload parties.json (tokens)). Receiving no error means you have succeeded.

Note: parties.json needs to be re-uploaded to the UI every time the tokens change.

## User Guide
This User Guide will take you step-by-step through the whole supply chain process described in the Overview.

_**Note:** This demo is designed to show successful conclusion of the supply chain workflow without exceptions or error conditions. A full production implementation would include additional features, handle errors and exceptions, and incorporate appropriate security controls._

## Workflow

**Roles and Responsibilities**

<table>
  <tr>
   <td><strong>Role</strong>
   </td>
   <td><strong>Responsibility</strong>
   </td>
  </tr>
  <tr>
   <td>Buyer
   </td>
   <td>Buyer places a request for price and delivery for a quantity of goods with Seller. Once trade details are received from Seller, Buyer can review and accept trade details. After confirmation of trade details, the delivery process can begin. Buyer is responsible for making full or partial payments upon delivery of goods.
   </td>
  </tr>
  <tr>
   <td>Seller
   </td>
   <td>Seller receives an order for goods. Seller is responsible for providing the price and delivery date confirmation to Buyer. Once the order is finalized, Seller is responsible for delivering the goods. Seller is in relationships with  Supplier. Seller does not need to know the full details on inventories or how orders are allocated.
   </td>
  </tr>
  <tr>
   <td>Supplier
   </td>
   <td>In the example, we model Supplier as a logistics provider with established contractual relationship with warehouse owners and transport companies. It is responsible for allocating orders to warehouses and finding transport companies for delivery. Suppliers need to be notified of issues during order fulfillment and transportation. Warehouses are directly owned by Supplier.
   </td>
  </tr>
  <tr>
   <td>Warehouse
   </td>
   <td>Warehouse keeps records of inventory and is responsible for fulfilling orders. In our simplified workflow, once goods are handed off to Transport Companies, its responsibilities are complete.
   </td>
  </tr>
  <tr>
   <td>Transport Company
   </td>
   <td>Transport Company is in possession of goods during transportation. It notifies relevant participants of any changes in the status of the delivery.
   </td>
  </tr>
</table>

**Steps**

The Supply Chain application includes the following steps:

1. **Market Setup:** The application starts with an automated market setup process. Participants and their roles are created, and relationships are set up.

2. **Quote Request:** Buyer requests a trade quote from Seller for the price and delivery date for a quantity of goods. Seller reviews the quote request and starts the pricing and delivery collection process.

    Supplier receives notification of the new trade quote request from Seller.

3. **Inventory Management:** Supplier starts the quote collection process by selecting Warehouses to fulfill the product order request. At the same time, Supplier also sends a quote request for delivery from Transport Company 1 and 2. Transport Companies give a quote for price and estimated time of arrival for the delivery.

4. **Delivery Management:** Supplier uses an automated algorithm optimized for best price to calculate and finalize delivery. The algorithm computes quantities to be delivered from the Warehouses and calculates the amount of goods Transport Company needs to deliver from the Warehouses.

5. **Order Finalization:** Supplier sends an aggregated quote to Seller. Seller can review the quote details (quantity of goods, place of shipment, transport company, price) and can add margin to the price. Seller then sends the quote to Buyer. Buyer can review the quote and approve the order. Once the order is finalized, delivery of goods can start.

6. **Transport:** Transport companies pick up product from Warehouses. Pickup of goods is affirmed by the Warehouse. Transport companies then deliver goods to Buyer.

7. **Delivery and Payment:** Buyer needs to acknowledge the delivery. When a successful delivery is acknowledged by Buyer, an automated payment obligation is created between Buyer and Seller for the amount of goods delivered. If goods are arriving separately, then partial delivery and partial payment occurs. Consecutive payment obligations can also be initiated between Seller, Supplier, Warehouses, and Transport Companies.

## Running the Application

### Choosing and Changing Roles

When you launch the Supply Chain application, you will see the Navigator screen with the option to choose your role.

To log in:

*   On the home screen, select the party from the list.

To switch users:

1. Click the name of the current party at the top of the screen.
2. On the home screen, select a different party.

### Setup

During Navigator startup, the basic buyer-seller relationship and other reference data is set up.

### Requesting a New Quote

#### Entering the New Quote Request

To request a quote:

1. Log in as Buyer.
1. Choose the **Buyer Seller Relationships** tab.
1. Select the relationship contract.
1. Click on the **Send Quote Request** choice.
1. Click **Add**.
1. Fill in the parameters:
    * productName: the name of the ordered product (_must match an existing inventory item_, so use either "Product 1" or "Product 2")
    * quantity: the ordered quantity
    * deliveryFrom: the start of the acceptable delivery period
    * deliveryTo: the end of the acceptable delivery period
1. More products can be added by clicking on **Add**.
1. Choose **Okay**.

#### Accepting the Quote Request

1. Log in as Seller.
1. Choose the **Quote Requests** tab.
1. Select the contract.
1. Fill in the parameter of the choice **Accept**:
    * workflowId: unique identifier of the order workflow
1. Click on **Accept**.

#### Sending the Request to the Supplier

1. Log in as Seller.
1. Choose the **Accepted Quote Request** tab.
1. Select the contract.
1. Fill in the parameter of the choice **Send To Supplier**:
    * supplier: the Supplier party
1. Click on **Send To Supplier**.

#### Accepting the Supply Invitation

1. Log in as Supplier.
1. Choose the **Supply Invitations** tab.
1. Select the contract.
1. Click on **Accept**.

### Preparing the Supply

#### Starting Price Collection

1. Log in as Supplier.
1. Choose the **Supply Request** tab.
1. Select the contract.
1. Click **Start Price Collection**.
1. Fill in the parameters:
    * warehouses: list of Warehouse parties from which to collect the products
    * transportCompanies: list of Transport Companies from whom to request transport quotes
1. Choose **Okay**.

Note that Supplier can see the available quantity of goods in each warehouse, as they are owned by Supplier.

#### Sending Transport Quotes

1. Log in as any Transport Company (TransportCompany1, TransportCompany2).
1. Choose the **Transport Quote Request** tab.
1. Select the contract.
1. Click **Accept**.
1. Fill in the parameters:
    * transportableQuantity: the quantity this transport company is able to deliver from the given location (Warehouse)
    * price: the total price of transportableQuantity
    * pickUpDate: date of pickup at Warehouse
    * deliveryDate: date of delivery to Buyer
1. Choose **Okay**.

#### Choosing the Best Delivery Plan

1. Log in as Supplier.
1. Choose the **Pending Transport Quote Request** tab.
1. Select the contract.
1. Click on **ChooseTransport**.

#### Sending the Aggregated Quote to Seller

1. Log in as Supplier.
1. Choose the **Pending Aggregated Quote** tab.
1. Select the contract.
1. Click **Send To Seller**.

### Preparing and Sending the Quote

#### Adding Margin

1. Log in as Seller.
1. Choose the **Aggregated Quotes** tab.
1. Select the contract.
1. Fill in the parameter of the choice **Add Margin**:
    * margin: a decimal number describing the margin (e.g. `0.1` means 10% margin)
1. Click on **Add Margin**.

_Note:_ You cannot click on the contract if you did not start the triggers. In this case you need to start over, and make sure the triggers run.

### Order and Delivery

#### Accepting the Quote

1. Log in as Buyer.
1. Choose the **Quotes** tab.
1. Select the contract.
1. Click on **Accept**.

#### Accepting the Order

1. Log in as Seller.
1. Choose the **Order** tab.
1. Select the contract.
1. Click on the **StartDelivery** choice.

#### Pick Up Products

1. Log in as a Transport Company (TransportCompany1, TransportCompany2).
1. Choose the **Delivery Instruction** tab.
1. Select the contract.
1. In a real system we could check if it is the pickup date of the delivery instruction, but we don't require this for sake of the demo.
1. Click on **PickUp**.

#### Acknowledge Pickup

1. Log in as a Warehouse (Warehouse1, Warehouse2).
1. Choose the **Pickup Request** tab.
1. Select the contract.
1. Click on **Accept**.

#### Start Delivery

1. Log in as a Transport Company (TransportCompany1, TransportCompany2).
1. Choose the **Pending Transport** tab.
1. Select the contract.
1. Click on **Deliver**.

#### Acknowledge Delivery

1. Log in as Buyer.
1. Choose the **Delivery** tab.
1. Select the contract.
1. In a real system we could check if it is the delivery date, but we don't require this for sake of the demo.
1. Click on **Acknowledge**.

### Payment

#### Supplier Accepts the Payment

1. Log in as Supplier.
1. Choose the **Delivery Payment** tab.
1. Select the contract.
1. Click on **Accept**.

#### Pay the Warehouses and Transport Companies

1. Log in as Supplier.
1. Choose the **Supplier Payment** tab.
1. Select the contract.
1. Click on **Pay**.


CONFIDENTIAL © 2019 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
Any unauthorized use, duplication or distribution is strictly prohibited.
