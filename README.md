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

Be sure you have the following installed:
- [DAML SDK](https://docs.daml.com/)
- Docker
- Java
- Maven

#### Build with Maven

Type:
```shell
mvn clean package
```

**Note:** If you change the DAML models locally, you need to re-run this command before starting the application.

### Starting the App

**Note:** Make sure you have built the application with Maven (see: [Build with Maven](#build-with-maven)).

There are two options:

#### Option 1: Start App with Docker

1. Type:
    ```shell
    docker-compose up --build
    ```
2. Open UI with a browser at http://localhost:7500.


#### Option 2: Start App in Standalone

1. Start the DAML Sandbox and Navigator. Type:
    ```shell
    daml start --sandbox-option --address=localhost
    ```
    The navigator will automatically open in new browser tab at http://localhost:7500.
2. Once the sandbox has started, start the automation logic by starting triggers. Type:
    ```shell
    scripts/startTriggers.sh localhost 6865 .daml/dist/*.dar
    ```
3. Start the automation logic by starting bots. Type:
    ```shell
    java -jar target/supplychain-0.0.1-SNAPSHOT.jar
    ```

### Stopping the App

#### Stopping Dockerized Run
1. Stop the Docker containers or bots by pressing **Ctrl+C**. (Alternatively, you can also stop it by typing `docker-compose down`.)

#### Stopping Standalone Run
1. Stop the bots by pressing **Ctrl+C**.
1. Stop the Sandbox and the Navigator by pressing **Ctrl+C** in the DAML assistant.

### Resetting the Prototype

Reset the application by following these steps:
1.  Stop the app by following the steps in [Stopping the App](#stopping-the-app) section.
2.  Start the app in [Docker](#using-docker) or [Standalone](#standalone-mode) by following the steps in the relevant section.

## User Guide
This User Guide will take you step-by-step through the whole supply chain process described in the Overview.

_**Note:** This demo is designed to show successful conclusion of the supply chain workflow without exceptions or error conditions. A full production implementation would include additional features, handle errors and exceptions, and incorporate appropriate security controls._

## Workflow

**Roles and Responsibilities**

<table>
  <tr>
   <td><strong>Role</strong>
   </td>
   <td><strong>Responsibilities</strong>
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

### Setting the System Date

The system date is visible in the top-right corner after you log in. The application starts with the example system date of June 3, 2019.

To change the system date:

1. Log in as any role.
2. Click on current date.
3. Select the new date in the calendar view.
4. Choose the **Set** button.

### Setup

During Navigator startup, the basic buyer-seller relationship and other reference data is set up.

### Requesting a New Quote

#### Entering the New Quote Request

To request a quote:

1. Log in as Buyer.
2. Choose the **Seller Relationships** tab.
3. Click on the relationship contract.
4. Select the **BuyerSellerRelationship_SendQuoteRequest** choice.
5. Click on **Add new element**
6. Fill in the parameters:
    * productName: the name of the ordered product (_must match an existing inventory item_, see them on the Inventory page logged in as Supplier)
    * quantity: the ordered quantity
    * deliveryFrom: the start of the acceptable delivery period
    * deliveryTo: the end of the acceptable delivery period
7. More products can be added by clicking on **Add new element**
8. Choose **Submit**.

#### Accepting the Quote Request

1. Log in as Seller.
2. Choose the **Quote Requests** tab.
3. Click on the contract.
4. Select the **QuoteRequest_Accept** choice.
5. Fill in the parameter:
    * workflowId: unique identifier of the order workflow
6. Choose **Submit**.

#### Sending the Request to the Supplier

1. Log in as Seller.
2. Choose the **Accepted Quote Request** tab.
3. Click on the contract.
4. Select the **QuoteRequestAccepted_SendToSupplier** choice.
5. Fill in the parameter:
    * supplier: the Supplier party
6. Choose **Submit**.

#### Accepting the Supply Invitation

1. Log in as Supplier.
2. Choose the **Supply Invitation** tab.
3. Click on the contract.
4. Select the **QuoteRequestSupplyInvitation_Accept** choice.
5. Choose **Submit**.

### Preparing the Supply

#### Starting Price Collection

1. Log in as Supplier.
2. Choose the **Supply Request** tab.
3. Click on the contract.
4. Select the **SupplyRequest_StartPriceCollection** choice.
5. Fill in the parameters:
    * warehouses: list of Warehouse parties from which to collect the products
    * transportCompanies: list of Transport Companies from whom to request transport quotes
6. Choose **Submit**.

Note that Supplier can see the available quantity of goods in each warehouse, as they are owned by Supplier.

#### Sending Transport Quotes

1. Log in as any Transport Company (TransportCompany1, TransportCompany2).
2. Choose the **Transport Quote Request** tab.
3. Click on the contract.
4. Select the **TransportQuoteRequest_Accept** choice.
5. Fill in the parameters:
    * transportableQuantity: the quantity this transport company is able to deliver from the given location (Warehouse)
    * price: the total price of transportableQuantity
    * pickUpDate: date of pickup at Warehouse
    * deliveryDate: date of delivery to Buyer
6. Choose **Submit**.

#### Choosing the Best Delivery Plan

1. Log in as Supplier.
2. Choose the **Pending Quote Request** tab.
3. Click on the contract.
4. Select the **TransportQuoteRequestPending_ChooseTransport** choice.
5. Choose **Submit**.

#### Sending the Aggregated Quote to Seller

1. Log in as Supplier.
2. Choose the **Aggregated Pending Quote** tab.
3. Click on the contract.
4. Select the **AggregatedQuotePending_SendQuoteToSeller** choice.
5. Choose **Submit**.

### Preparing and Sending the Quote

#### Adding Margin

1. Log in as Seller.
2. Choose the **Aggregated Quote** tab.
3. Click on the contract.
4. Select the **AggregatedQuote_AddMargin** choice.
5. Fill in the parameter:
    * margin: a decimal number describing the margin (e.g. `0.1` means 10% margin)
6. Choose **Submit**.

_Note:_ You cannot click on the contract if you did not start the bots. In this case you need to start over, and make sure the bots run.

### Order and Delivery

#### Accepting the Quote

1. Log in as Buyer.
2. Choose the **Received Quote** tab.
3. Click on the contract.
4. Select the **QuoteForBuyer_Accept** choice.
5. Choose **Submit**.

#### Accepting the Order

1. Log in as Seller.
2. Choose the **Confirmed Order** tab.
3. Click on the contract.
4. Select the **ConfirmedOrder_StartDelivery** choice.
5. Choose **Submit**.

#### Pick Up Products

1. Log in as a Transport Company (TransportCompany1, TransportCompany2).
2. Choose the **Delivery Instruction** tab.
3. Click on the contract.
4. Set the ledger date to the pickup date of the delivery instruction.
5. Select the **DeliveryInstruction_PickUp** choice.
6. Choose **Submit**.

#### Acknowledge Pickup

1. Log in as a Warehouse (Warehouse1, Warehouse2).
2. Choose the **Pickup Request** tab.
3. Click on the contract.
4. Select the **PickUpRequest_Accept** choice.
5. Choose **Submit**.

#### Start Delivery

1. Log in as a Transport Company (TransportCompany1, TransportCompany2).
2. Choose the **Pending Transport** tab.
3. Click on the contract.
4. Select the **TransportPending_Deliver** choice.
5. Choose **Submit**.

#### Acknowledge Delivery

1. Log in as Buyer.
2. Choose the **Delivery** tab.
3. Click on the contract.
4. Set the ledger date to the delivery date.
5. Select the **Delivery_Acknowledge** choice.
6. Choose **Submit**.

### Payment

#### Supplier Accepts the Payment

1. Log in as Supplier.
2. Choose the **Delivery Payment** tab.
3. Click on the contract.
4. Select the **DeliveryPayment_Accept** choice.
5. Choose **Submit**.

#### Pay the Warehouses and Transport Companies

1. Log in as Supplier.
2. Choose the **Supplier Payment** tab.
3. Click on the contract.
4. Select the **DeliverySupplierPayment_Pay** choice.
5. Choose **Submit**.


CONFIDENTIAL © 2019 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
Any unauthorized use, duplication or distribution is strictly prohibited.
