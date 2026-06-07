# Application Workflows

This document explains the main workflows of the Utility Billing System as currently implemented.

It is meant to answer three questions clearly:

- what each workflow does
- who performs it
- how the workflows connect end to end

## 1. Core Business Flow

The application is built around one main business lifecycle:

```text
Authentication
    ->
User and Role Access
    ->
Customer Registration
    ->
Meter Registration
    ->
Meter Reading Capture
    ->
Tariff Configuration
    ->
Bill Generation
    ->
Bill Approval
    ->
Payment Recording
    ->
Email and Notification Delivery
    ->
Customer Self-Service
```

The domain relationship underneath that flow is:

```text
Customer owns meters.
Meters produce readings.
Readings generate bills.
Bills receive payments.
Important events create email and in-system notifications.
```

## 2. Authentication Workflow

Purpose:
Allow users to access protected parts of the system securely.

Actor:
- admin
- operator
- finance
- customer

Flow:
1. User submits email and password to `POST /api/auth/login`.
2. The system validates the credentials.
3. If valid, the system returns a JWT token.
4. The token is used in the `Authorization: Bearer <token>` header for protected endpoints.

Important rules:
- login is stateless through JWT
- roles determine access after authentication
- Swagger UI can also use the same JWT through the `Authorize` button

## 3. User and Role Management Workflow

Purpose:
Control who can use the system and what they are allowed to do.

Main roles:
- `ROLE_ADMIN`
- `ROLE_OPERATOR`
- `ROLE_FINANCE`
- `ROLE_CUSTOMER`

Flow:
1. Seeded admin logs in.
2. Admin creates internal system users through `POST /api/users`.
3. Roles are assigned based on responsibilities.

Role meaning:
- admin manages users and tariffs, and can also participate in billing approval
- operator manages customers, meters, and readings
- finance manages bills, approvals, payments, and notifications
- customer only views their own data

Important rules:
- internal privileged roles are not created through public signup
- duplicate email addresses are rejected

## 4. Customer Registration Workflow

Purpose:
Create the business identity for a utility customer.

Actor:
- admin
- operator

Flow:
1. Staff sends customer details to `POST /api/customers`.
2. The system validates the request.
3. A customer record is created with business profile data.

Stored as customer data:
- full name
- national ID
- email
- phone number
- address
- status

Important rules:
- `nationalId` must be unique
- inactive customers should not receive bills

This is the first business workflow. Billing cannot start without a customer.

## 5. Customer User Account Workflow

Purpose:
Allow an existing business customer to gain portal access.

This is the workflow that often feels redundant, so it is important to explain it clearly.

### 5.1 Why `Customer` and `User` Both Exist

They are not the same concept.

- `Customer` is the business record used in billing
- `User` is the login account used in authentication

Meaning:
- a customer can exist without having a login account
- internal users like admin, operator, and finance are users but are not customers
- a customer portal user is a login account linked to an existing customer

### 5.2 Actual Workflow

Flow:
1. Staff creates the `Customer` first.
2. If that customer needs portal access, a linked `User` account is created.
3. That happens either:
   - through `POST /api/auth/signup`
   - or through admin `POST /api/users` with `ROLE_CUSTOMER`
4. The customer-linked user can then log in and access self-service endpoints.

Important rule:
- customer signup does not create the business customer record
- customer signup creates a login account for an existing customer record

### 5.3 Where the Redundancy Comes From

The redundancy is not that both entities exist.

The redundancy is that some profile fields appear in both models, such as:
- full name
- email
- phone number

So the current workflow is conceptually valid, but it duplicates some data entry when creating a customer portal account.

## 6. Meter Registration Workflow

Purpose:
Link a service meter to a customer.

Actor:
- admin
- operator

Flow:
1. Staff creates a meter through `POST /api/meters`.
2. The meter is linked to a customer.
3. The meter becomes available for reading capture.

Stored as meter data:
- customer
- meter number
- meter type
- installation date
- status

Important rules:
- `meterNumber` must be unique
- one customer may own multiple meters
- only active meters can receive readings

## 7. Meter Reading Workflow

Purpose:
Capture usage data for a billing cycle.

Actor:
- operator

Flow:
1. Operator submits a reading to `POST /api/readings`.
2. The system validates the request.
3. Consumption is calculated automatically.
4. The reading is stored for later billing.

Important rules:
- meter must be active
- current reading must be greater than previous reading
- only one reading per meter per month/year
- reading date must match the billing month/year

Calculation:

```text
consumption = currentReading - previousReading
```

This is where raw utility usage becomes billable data.

## 8. Tariff Configuration Workflow

Purpose:
Define how utility usage is priced.

Actor:
- admin

Flow:
1. Admin creates a tariff through `POST /api/tariffs`.
2. The tariff is saved with pricing and effective-date information.
3. Billing later selects the applicable tariff for the reading cycle.

Tariff types:
- `FLAT`
- `TIERED`

Flat tariff example:

```text
amountBeforeTax = consumption * ratePerUnit
```

Tiered tariff example:

```text
0 - 10 units   -> first rate
11 - 30 units  -> second rate
31+ units      -> third rate
```

Important rules:
- tariffs are versioned
- tariffs are selected by meter type and effective date
- tier ranges must be continuous
- invalid VAT, penalty, and amount values are rejected

## 9. Bill Generation Workflow

Purpose:
Turn a meter reading into a bill.

Actor:
- finance
- admin

Flow:
1. Finance or admin calls `POST /api/bills/generate`.
2. The system loads the reading.
3. The system checks whether a bill already exists for that reading.
4. The system confirms the customer is active.
5. The system finds the applicable tariff for the billing cycle.
6. The system calculates all billing amounts.
7. The bill is saved with status `GENERATED`.

Calculated fields:
- consumption
- amount before tax
- fixed charge
- penalty amount
- tax amount
- total amount
- outstanding balance

High-level formula:

```text
subtotal = amountBeforeTax + fixedCharge
taxAmount = subtotal * VAT / 100
totalAmount = subtotal + taxAmount + penaltyAmount
outstandingBalance = totalAmount
```

Important rules:
- one bill per reading
- bill reference must be unique
- due date cannot be before the reading date

## 10. Bill Approval Workflow

Purpose:
Apply financial control before payment is accepted.

Actor:
- finance
- admin

Flow:
1. User reviews a generated bill.
2. User calls `POST /api/bills/{id}/approve`.
3. Bill moves from `GENERATED` to `APPROVED`.
4. Approver and approval time are stored.

Important rule:
- only generated bills can be approved

This is the checkpoint between calculation and payment.

## 11. Payment Workflow

Purpose:
Record customer payments and update the financial state of the bill.

Actor:
- finance

Flow:
1. Finance submits a payment through `POST /api/payments`.
2. The system finds the bill by bill reference.
3. The payment is validated.
4. The system updates the bill.
5. The bill status changes depending on the remaining balance.

Possible results:
- partial payment -> `PARTIALLY_PAID`
- full payment -> `PAID`

Important rules:
- payment amount must be greater than zero
- payment must not exceed outstanding balance
- bill must already be approved or partially paid
- payment date must be valid

This workflow proves the application supports real balance tracking, not just payment storage.

## 12. Email and Notification Workflow

Purpose:
Communicate important billing events to the customer.

Current implementation uses two channels:
- email
- in-system notification records

### 12.1 Bill Generation Communication

Flow:
1. Bill service prepares the bill.
2. Email is sent to the customer first.
3. Bill is saved.
4. PostgreSQL trigger inserts a notification record after bill creation.

### 12.2 Full Payment Communication

Flow:
1. Payment service detects that balance becomes zero.
2. Email is sent to the customer first.
3. Bill status changes to `PAID`.
4. PostgreSQL trigger inserts a full-payment notification record.

### 12.3 Message Consistency

The email message and notification message are intentionally aligned.

That means the customer receives the same communication content in:
- the email inbox
- the notifications table and notification endpoints

### 12.4 Due-Date Reminder Workflow

Flow:
1. A scheduled job scans approved and partially paid bills with outstanding balances.
2. By default, the job runs every day at `16:25`.
3. If a bill is within the configured reminder window before the due date, the system sends an email reminder.
4. The bill is marked with `reminderSentAt` so the reminder is not repeatedly sent.
5. If email delivery fails, the reminder remains unsent and can be retried later.

Important rules:
- reminder delivery is scheduled, not manual
- reminders only apply to bills that still have an outstanding balance
- reminders are sent before or near the due date according to configuration
- the default reminder time is `16:25`

This is important for consistency during presentation and during actual usage.

## 13. Customer Self-Service Workflow

Purpose:
Allow the customer to view their own information securely.

Actor:
- customer

Flow:
1. Customer logs in with a linked customer user account.
2. Customer accesses self-service endpoints:
   - own bills
   - own payments
   - own notifications
3. The system resolves the currently authenticated user.
4. The system resolves the linked customer record.
5. Only that customer’s records are returned.

This is not id-based open access. It is ownership-based access through the authenticated user.

## 14. Validation Workflow

Purpose:
Protect data quality before invalid information enters billing operations.

Validation exists at two levels:
- DTO and controller validation
- service and business-rule validation

Examples of protected fields:
- money amounts
- VAT and penalty percentages
- reading values
- billing dates
- meter installation dates
- payment dates

Examples of enforced rules:
- no negative money values
- no invalid billing months
- no future reading dates
- no due date before reading date
- no future installation dates

## 15. Database Integrity Workflow

Purpose:
Enforce rules even below the application layer.

The database reinforces important invariants such as:
- unique user email
- unique customer national ID
- unique meter number
- unique reading per meter per billing cycle
- unique bill reference
- non-negative balances
- current reading greater than previous reading

This is important because Java validation is not the only line of defense.

## 16. End-to-End Recommended Demonstration Workflow

Use this order when presenting the application:

1. Login as admin
2. Create operator user
3. Create finance user
4. Login as operator
5. Register customer
6. Register meter for that customer
7. Login as admin
8. Configure tariff
9. Login as operator
10. Capture reading
11. Login as finance
12. Generate bill
13. Approve bill
14. Record partial payment
15. Record final payment
16. Show notifications
17. Explain matching email delivery
18. Login as customer
19. Show own bills
20. Show own payments
21. Show own notifications

This is the clearest single path through the application because it demonstrates:
- security
- business validation
- billing logic
- payment lifecycle
- email delivery
- database-triggered notifications
- customer self-service

## 17. Workflow Summary

The application is not just a set of independent endpoints.

It models one continuous business process:

```text
Users authenticate.
Staff create customers.
Customers receive meters.
Operators capture readings.
Admins configure tariffs.
Finance generates and approves bills.
Payments reduce balances.
Key events send email and create notifications.
Customers view their own records securely.
```

That is the full workflow of the Utility Billing System as implemented in this repository.
