# Kazisafe — User Guide

> Welcome to the official Kazisafe guide. This document explains, step by step, how to use all the features of this commercial and financial management software for SMEs.

---

## 1. About Kazisafe

Kazisafe is an integrated management software that helps you run your business:

- **Products**, **units (measures)** and **stock** management
- **Sales** at the counter with cashier and invoicing
- **Purchases**, **supplier deliveries** and **requisitions**
- **Customers**, **suppliers** and **treasury**
- **Production**, **fixed assets** and **inventory**
- **Financial reports** and stock statements
- Smart assistant **Gratien** that records your documents and answers your questions
- **Offline** operation with automatic synchronization

| Element | Role |
|---|---|
| Left menu | Quick access to all modules |
| Top bar | Search, settings, help |
| Gratien assistant | Integrated smart chat on the right |

---

## 2. Starting and logging in

1. Launch Kazisafe from your desktop or start menu.
2. On first launch, the application automatically creates the working folders and the local database.
3. Enter the **activation token** and the **company ID** provided by your reseller.
4. Choose your **region** (e.g. Goma, Bukavu, Kinshasa...) then the **main currency** (USD or CDF).
5. Click **Sign in**.

> The application connects to your local database even without internet. Data is then periodically synchronized with the cloud.

---

## 3. The dashboard (Home)

The home screen shows an overview of your activity:

- **Turnover** for the day, week and month
- **Recent sales** and top products
- **Stock alerts** (products out of stock or under threshold)
- **Available treasury** per account
- **Supplier debts** and **customer receivables**

To refresh the data, click the **Refresh** button at the top right of each card.

---

## 4. Product management

### 4.1 Creating a product

1. Open the **Products** menu.
2. Click the **New product** (+) button.
3. Fill in:
   - **Product name** and **barcode**
   - **Category**, **brand**, **model**
   - **Size** and **color** (optional)
   - **Image** of the product
4. Click **Save**.

### 4.2 Editing or deleting a product

- Right-click a product in the list to **edit** or **delete** it.
- Deletion is disabled if the product has stock or sales history.

### 4.3 Categories and brands

From the Products module you can manage:

- **Categories** (drinks, food, electronics, ...)
- **Brands** and **models**
- **Product groups**

### 4.4 Units (measures)

Each product has **units** (Piece, Kg, Carton, Litre...).

1. Open a product then the **Units** tab.
2. Click **Add a unit**.
3. Enter the **description** and the **content quantity**:
   - E.g. 1 Carton = 12 Pieces → description "Carton", content quantity = 12
4. Save.

> The base unit (content quantity = 1) is used for reference stock. Other units allow sales by carton, pack, etc.

---

## 5. Suppliers

1. Open the **Suppliers** menu.
2. Click **New supplier**.
3. Fill in: **name**, **address**, **phone**, **ID number**, **RCCM**, **tax number**.
4. Save.

You can then:

- **Edit** the contact details
- **View the history** of deliveries and debts for the supplier
- **Settle debts** directly from the supplier record

---

## 6. Deliveries and purchasing

A **supplier invoice** becomes a supply: reception of goods then stock entry. You can record it in **two ways**:

| Method | When to use it |
|---|---|
| With the **Gratien** assistant | Invoice as photo or PDF, no typing |
| **Manually** | Direct keyboard entry, line by line |

### 6.1 Recording a supplier invoice with the Gratien assistant

1. Open the **Gratien** chat (right panel).
2. Click the **attachment** icon and select the **photo or PDF** of the supplier invoice.
3. Write the instruction, e.g. *"Record this supply"* or *"Put this invoice into stock"*.
4. Gratien reads the invoice and automatically creates:
   - the **products** (it finds existing ones or creates new ones)
   - the **supplier**
   - the **delivery** with the piece number and reference of the invoice
   - the **requisitions** (quantities, purchase costs, lots, expiry dates)
5. Check the proposed information then **confirm** before validation.
6. The stock is added: check the entry in **Purchasing** and the stock statement.

> Gratien derives the purchase costs and selling prices from the invoice amounts. If the invoice currency differs from the main currency, it asks for confirmation before converting.

### 6.2 Recording a supplier invoice manually

1. Open the **Purchasing** menu.
2. Choose the **Purchase** provenance.
3. Click **Add**: the delivery form opens.
4. Select or create the **supplier** from the invoice.
5. Enter the **piece number** (number shown on the invoice) and the **reference**.
6. For each item on the invoice, add: product, **lot**, **quantity**, **unit**, **purchase cost**, **expiry date** and **stock alert**.
7. Click **Save delivery**.
8. Then create the **requisition** to make the goods available for sale (see section 7).

> Purchase cost and selling price can also be filled automatically by Gratien from the invoice (photo or PDF) even if you record the delivery manually.

### 6.3 The lot

Each received good has a **lot number** used to:

- Find the expiry date
- Apply the FIFO / LIFO / FEFO method for stock outflows
- Trace the origin of each item

---

## 7. Requisitions (stock entry)

The **requisition** creates the stock available for sale, by product, lot and unit.

### 7.1 "Purchase" provenance

1. In **Purchasing**, select a **delivery**.
2. Click the **+** icon to add an item.
3. The requisition pre-fills: product, **purchase cost**, quantity, lot.
4. Check the **selling price** tiers and the **selling unit**.
5. Click **Save**: stock is added.

### 7.2 "Warehouse" provenance

To receive goods already dispatched from another warehouse:

1. In **Purchasing**, choose the **Warehouse** provenance.
2. Click **Add**.
3. The **Depot reference** list shows the destocking sent to your region.
4. Select the **reference**: the product, **lot**, **purchase cost** and **quantity** are filled automatically.
5. Adjust if needed then **Save**.

> "Warehouse" entries do not require a supplier piece number: they come from your own warehouses.

---

## 8. Destocking and warehousing

The **Destocking** module moves items out of a warehouse stock to send them to another warehouse or region.

1. Open the **Destocking** menu.
2. Select the **product**: the available lots and the **purchase cost** are shown automatically.
3. Choose the **unit** and the **quantity**.
4. Choose the **destination** (target region) from the list.
5. Enter the **reference** (e.g. DST12345K) and an **observation**.
6. Click **Add to list**, then **Validate** to record the destocking.

> Destocking removes stock from the source warehouse. The destination warehouse can then receive it via a **Warehouse** provenance requisition.

---

## 9. Sales and cashier (POS)

### 9.1 Making a sale

1. Open the **Sale / Cashier** menu.
2. Search for a product by **barcode**, name or brand.
3. Click the product to add it to the cart.
4. Adjust the **quantity** and check the **price** and **selling unit**.
5. Choose the **customer** (or "Walk-in customer").
6. Click **Pay**:
   - Select the **payment method** (Cash, Mobile Money, Card, Credit)
   - Enter the **amount received**
   - The software computes the **change to return**
7. Validate: the ticket is printed (optional) and stock decreases.

### 9.2 Credit sale

- Choose a registered customer then the **Credit** payment method.
- The receivable is added to the customer record and can be **collected** from the Customers or Treasury menu.

### 9.3 Sales history

- The **Sales** tab shows all transactions.
- You can **view the details**, **print** a receipt or **cancel** a sale (with authorization).

---

## 10. Customers

1. Open the **Customers** menu.
2. Click **New customer** and enter **name**, **address**, **phone**.
3. Save.

Each customer record contains:

- The **purchase history**
- The **receivables** (outstanding credit sales)
- The **payments** made

---

## 11. Treasury

### 11.1 Treasury accounts

1. Open the **Treasury** menu.
2. Create your accounts: **Cash**, **Bank**, **Mobile Money**...
3. Enter the **currency** of each account.

### 11.2 Operations

- **Cash in**: funding an account
- **Cash out**: withdrawal or payment
- **Expenses**: record charges with **reason**, **amount** and **date**
- **Transfer**: move funds between accounts

> Expenses can be entered manually or read automatically from a receipt by the Gratien assistant.

### 11.3 Settlements

- **Settle a supplier debt**: select the delivery and enter the amount
- **Collect a customer receivable**: select the credit sale and record the payment

---

## 12. Production

The Production module lets you manufacture finished goods from raw materials.

1. Open the **Production** menu.
2. Create a **bill of materials** (list of raw materials and required quantities).
3. Launch a **manufacturing run**: the software decreases the raw materials and increases the finished product.
4. Track the **production costs** in the statements.

---

## 13. Fixed assets

1. Open the **Fixed assets** menu.
2. Record your assets: **designation**, **acquisition date**, **value**, **depreciation period**.
3. The software computes **depreciation** automatically.

---

## 14. Inventory and counting

1. Open the **Inventory** menu.
2. Create a **counting** for a region or warehouse.
3. Enter the **physical quantity** of each product.
4. Compare with the theoretical stock: the **differences** are displayed.
5. Validate the counting: stock is **adjusted**.

> The adjustment automatically creates the stock entries (in or out) with an inventory reference.

---

## 15. Reports

The **Reports** menu gives access to:

| Report | Content |
|---|---|
| Stock statement | Stock by product, lot, unit and value |
| Sales | Turnover by period, by product, by seller |
| Purchases | Deliveries and purchasing expenses |
| Treasury | Movements and account balances |
| Income statement | Revenue, expenses and result |
| Balance sheet | Assets, liabilities and equity |
| Cash flow | Cash inflows and outflows |

For each report:

1. Choose the **period** (from / to) and the **region**.
2. Click **Generate**.
3. **Export** to Excel, PDF or print the document.

---

## 16. Settings

The **Settings** menu (gear icon) lets you configure:

- **Company**: name, address, phone, ID, logo
- **Main currency** and **USD/CDF exchange rate**
- **Region** and warehouses
- **Interface language** (French, English, Swahili, Lingala, Kinyarwanda, Arabic, Hindi)
- **Theme** light / dark
- **Stock method**: FIFO, LIFO or FEFO
- **Printer** and ticket format
- **Users and permissions**: who can sell, cancel, edit, delete

> Each change is applied immediately. Some preferences require a restart.

---

## 17. The Gratien assistant

**Gratien** is the smart assistant integrated in Kazisafe. It helps you:

### 17.1 Recording a document

Send Gratien a **photo** or **PDF** of a supplier invoice, receipt or ticket:

1. Click the **attachment** icon in the chat.
2. Select your document.
3. Write your instruction, e.g. *"Record this purchase"* or *"Create the expense from this receipt"*.
4. Gratien reads the document, creates the **products**, **supplier**, **delivery** and **requisitions** automatically.
5. It asks for confirmation and details before validating.

> To record a supplier invoice without Gratien, follow the manual procedure in **section 6.2**. For expenses, see **section 11.2**.

### 17.2 Answering your questions

Ask your questions in natural language:

- *"How do I create a product?"*
- *"What is my stock of Coca 1.5L?"*
- *"How much does this customer owe me?"*
- *"Produce last month's balance sheet"*

Gratien answers using the **Kazisafe user guide** and your real-time data.

### 17.3 Special commands

- `/kanuni <instruction>`: saves a personal instruction that Gratien will always follow.
- *"Cancel"*: stops an ongoing operation with confirmation.

---

## 18. Synchronization and multi-station

Kazisafe works on several stations sharing the same data:

- Each station works on its **local database** (offline operation guaranteed).
- Data is **synchronized** with the cloud automatically when a connection is available.
- **Real-time synchronization** uses WebSockets for connected stations.
- You can force synchronization from the **update menu** (sync button).

> Before working, check the synchronization icon at the top: green = up to date, orange = pending.

---

## 19. Backup and maintenance

- The **local database** is created automatically in the Kazisafe working folder.
- Perform **periodic backups** of the `Kazisafe/Media` folder and the database file.
- Always keep a **copy of your activation token** in a safe place.

---

## 20. Troubleshooting

| Problem | Solution |
|---|---|
| Slow application | Check the internet connection and pending synchronization |
| Incorrect stock | Perform an inventory counting and validate the differences |
| Blocked synchronization | Restart the application then force synchronization |
| Forgotten password | Contact your administrator or reseller |
| Printer not responding | Check the USB port and the ticket format in the settings |

---

## 21. Support

For any support:

- Open the **Help?** menu at the bottom of the sidebar: the **PDF guide** of this documentation opens.
- Ask **Gratien** directly in the chat.
- Contact your **reseller** or the Kazisafe team with your license number.

> Thank you for using Kazisafe. This guide is also available as a PDF from the **Help?** menu.
