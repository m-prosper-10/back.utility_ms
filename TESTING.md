# Testing Guide

This document defines how to verify that the Utility Billing System works as intended.

It covers:

- environment setup
- startup verification
- role-based authentication
- module-by-module API testing
- negative tests for business rules
- end-to-end workflow testing
- database verification for constraints and triggers

The guide is written against the current implementation in this repository.

## 1. Scope

The goal is to prove the application supports the intended workflow:

Customer -> Meter -> Reading -> Tariff -> Bill -> Approval -> Payment -> Notification

The guide also verifies:

- JWT login and authorization
- role restrictions
- uniqueness and validation rules
- tariff versioning
- flat and tiered billing
- partial and full payments
- PostgreSQL trigger-based notifications
- customer self-service read access

## 2. Prerequisites

Before testing, ensure all of the following are ready:

- Java 21 is installed and active
- PostgreSQL is running
- database `utility_billing` exists
- the SQL scripts below have been applied:
  - `src/main/resources/db/postgresql/01-constraints.sql`
  - `src/main/resources/db/postgresql/02-notification-triggers.sql`
- the app starts successfully with:

```bash
./mvnw spring-boot:run
```

Swagger UI should be available at:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI docs should be available at:

```text
http://localhost:8080/v3/api-docs
```

## 3. Runtime Configuration

Current local defaults come from `src/main/resources/application.properties`.

Important values:

- database URL: `jdbc:postgresql://localhost:5432/utility_billing`
- username: `polo`
- password: `#nelprox92`
- seed admin enabled: `true`
- seed admin email: `admin@utility.rw`
- seed admin password: `password123`

If you override these with environment variables, adjust the tests accordingly.

## 4. Test Tools

Use any of the following:

- Swagger UI
- Postman
- curl
- psql

Recommended approach:

1. Use Swagger or Postman for API calls.
2. Use `psql` for database verification.
3. Save tokens for each role separately.

## 5. Seeded Accounts and Planned Test Accounts

Expected seeded admin:

- email: `admin@utility.rw`
- password: `password123`
- role: `ROLE_ADMIN`

Create these additional test accounts during the flow:

- operator user
- finance user
- customer portal user

## 6. Token Collection

Store tokens after login:

- `ADMIN_TOKEN`
- `OPERATOR_TOKEN`
- `FINANCE_TOKEN`
- `CUSTOMER_TOKEN`

Example login request:

```json
{
  "email": "admin@utility.rw",
  "password": "password123"
}
```

## 7. Global Success Criteria

Every successful API test should confirm:

- HTTP status is `200`
- response JSON has `success: true`
- message is sensible
- returned data matches the request and business rules

Every negative API test should confirm:

- request is rejected
- response JSON has `success: false`
- error message reflects the violated rule

## 8. Startup Verification

Run:

```bash
./mvnw spring-boot:run
```

Pass criteria:

- application starts on port `8080`
- no bean creation failure
- no datasource failure
- no JWT initialization failure
- no schema creation failure

Optional database verification:

```sql
\dt
```

Expected core tables:

- `users`
- `roles`
- `user_roles`
- `customers`
- `meters`
- `meter_readings`
- `tariffs`
- `tariff_tiers`
- `bills`
- `payments`
- `notifications`

## 9. Authentication and Authorization Tests

### 9.1 Admin Login

Action:

- login with `admin@utility.rw / password123`

Pass criteria:

- token is returned
- response roles include `ROLE_ADMIN`

### 9.2 Customer Self-Signup

Precondition:

- create a customer first

Action:

- call `POST /api/auth/signup`

Example:

```json
{
  "fullName": "Portal Customer",
  "email": "portal.customer@example.com",
  "phoneNumber": "0788111222",
  "password": "password123",
  "customerId": 1
}
```

Pass criteria:

- user is created
- assigned role is only `ROLE_CUSTOMER`
- user is linked to the given customer

Negative checks:

- duplicate email is rejected
- inactive customer signup is rejected
- signup for a customer already linked to a user is rejected
- nonexistent `customerId` is rejected

### 9.3 Public Signup Privilege Escalation Prevention

Action:

- attempt to create admin/operator/finance through `/api/auth/signup`

Pass criteria:

- not possible through this endpoint
- resulting user remains `ROLE_CUSTOMER` only

### 9.4 Admin User Creation

Action:

- login as admin
- call `POST /api/users`

Create operator:

```json
{
  "fullName": "System Operator",
  "email": "operator@utility.rw",
  "phoneNumber": "0788000001",
  "password": "password123",
  "roles": ["ROLE_OPERATOR"]
}
```

Create finance:

```json
{
  "fullName": "Finance Officer",
  "email": "finance@utility.rw",
  "phoneNumber": "0788000002",
  "password": "password123",
  "roles": ["ROLE_FINANCE"]
}
```

Pass criteria:

- users are created
- returned roles match request

Negative checks:

- non-admin cannot create users
- duplicate email is rejected
- customer-role user without `customerId` is rejected

### 9.5 Login for All Roles

Login and verify:

- admin user
- operator user
- finance user
- customer user

Pass criteria:

- each login returns a token
- returned role set is correct

### 9.6 Authorization Matrix Checks

Verify at least these cases:

- customer cannot create customer records
- operator cannot create tariffs
- operator cannot approve bills
- finance cannot create users
- admin can create tariffs
- operator can capture readings
- finance can record payments
- customer can view own bills, payments, notifications
- customer cannot view other customers by admin endpoints

## 10. Customer Module Tests

Endpoints:

- `POST /api/customers`
- `GET /api/customers`
- `GET /api/customers/{id}`
- `PUT /api/customers/{id}`
- `PATCH /api/customers/{id}/status`
- `DELETE /api/customers/{id}`

Create customer example:

```json
{
  "fullName": "Mugisha Prosper",
  "nationalId": "1199980012345678",
  "email": "prosper@example.com",
  "phoneNumber": "0788000000",
  "address": "Kigali, Rwanda"
}
```

Positive checks:

- create customer
- list customers
- fetch by id
- update fields
- inactivate customer
- reactivate customer

Negative checks:

- duplicate `nationalId` is rejected
- missing required fields are rejected
- getting unknown customer returns not found

Database check:

```sql
select id, full_name, national_id, status from customers;
```

## 11. Meter Module Tests

Endpoints:

- `POST /api/meters`
- `GET /api/meters`
- `GET /api/meters/{id}`
- `GET /api/customers/{customerId}/meters`
- `PUT /api/meters/{id}`
- `PATCH /api/meters/{id}/status`

Create meter example:

```json
{
  "customerId": 1,
  "meterNumber": "WTR-2026-001",
  "meterType": "WATER",
  "installationDate": "2026-06-01"
}
```

Positive checks:

- create active meter
- get all meters
- get meter by id
- get meters for a customer
- update meter
- inactivate meter

Negative checks:

- duplicate `meterNumber` is rejected
- nonexistent customer is rejected

Database check:

```sql
select id, meter_number, meter_type, status, customer_id from meters;
```

## 12. Reading Module Tests

Endpoints:

- `POST /api/readings`
- `GET /api/readings`
- `GET /api/readings/monthly?month=6&year=2026`
- `GET /api/meters/{meterId}/readings`

Create reading example:

```json
{
  "meterId": 1,
  "previousReading": 120,
  "currentReading": 150,
  "readingDate": "2026-06-05",
  "billingMonth": 6,
  "billingYear": 2026
}
```

Positive checks:

- operator captures reading
- consumption is returned as `30.00`
- monthly query returns the reading
- meter-specific query returns the reading

Negative checks:

- inactive meter cannot receive reading
- `currentReading <= previousReading` is rejected
- second reading for same meter/month/year is rejected
- finance or customer cannot create reading

Database check:

```sql
select meter_id, billing_month, billing_year, previous_reading, current_reading, consumption
from meter_readings;
```

## 13. Tariff Module Tests

Endpoints:

- `POST /api/tariffs`
- `GET /api/tariffs`
- `GET /api/tariffs/{id}`
- `GET /api/tariffs/active?meterType=WATER`
- `PATCH /api/tariffs/{id}/deactivate`

### 13.1 Flat Tariff

Example:

```json
{
  "meterType": "WATER",
  "tariffType": "FLAT",
  "ratePerUnit": 500,
  "fixedCharge": 1000,
  "vatPercentage": 18,
  "penaltyPercentage": 5,
  "effectiveFrom": "2026-07-01"
}
```

Pass criteria:

- tariff is created
- version is `1` for first tariff of that meter type

### 13.2 Tiered Tariff

Example:

```json
{
  "meterType": "ELECTRICITY",
  "tariffType": "TIERED",
  "fixedCharge": 1000,
  "vatPercentage": 18,
  "penaltyPercentage": 5,
  "effectiveFrom": "2026-07-01",
  "tiers": [
    {
      "minUnits": 0,
      "maxUnits": 50,
      "ratePerUnit": 100
    },
    {
      "minUnits": 50,
      "maxUnits": 100,
      "ratePerUnit": 150
    },
    {
      "minUnits": 100,
      "maxUnits": null,
      "ratePerUnit": 200
    }
  ]
}
```

Pass criteria:

- tariff is created
- tiers are saved in order

Negative checks:

- flat tariff with tiers is rejected
- tiered tariff with `ratePerUnit` is rejected
- tiered tariff with discontinuous tiers is rejected
- `effectiveTo < effectiveFrom` is rejected

Versioning checks:

- create a second tariff for the same `meterType`
- verify version increments to `2`

Deactivate checks:

- deactivate an active tariff
- verify status becomes `INACTIVE`

Database checks:

```sql
select id, meter_type, tariff_type, version, status, effective_from, effective_to from tariffs order by id;
```

```sql
select tariff_id, min_units, max_units, rate_per_unit from tariff_tiers order by tariff_id, min_units;
```

## 14. Bill Module Tests

Endpoints:

- `POST /api/bills/generate`
- `GET /api/bills`
- `GET /api/bills/{id}`
- `GET /api/bills/reference/{billReference}`
- `GET /api/customers/{customerId}/bills`
- `GET /api/customer/bills`
- `POST /api/bills/{id}/approve`

Generate bill request:

```json
{
  "readingId": 1,
  "dueDate": "2099-06-30"
}
```

### 14.1 Positive Bill Generation

Pass criteria:

- bill is created from the reading
- `status` is `GENERATED`
- `amountPaid` is `0.00`
- `outstandingBalance == totalAmount`
- customer, meter, and reading references are correct
- generated `billReference` is unique

### 14.2 Flat Tariff Calculation Proof

For:

- consumption `30`
- rate per unit `500`
- fixed charge `1000`
- VAT `18`
- future due date

Expected:

- `amountBeforeTax = 15000.00`
- `taxAmount = 2880.00`
- `penaltyAmount = 0.00`
- `totalAmount = 18880.00`

### 14.3 Tiered Tariff Calculation Proof

For a tiered tariff equivalent to:

- 0 to 10 => 100
- 10 to 30 => 150
- 30+ => 200

For consumption `40`, expected:

- first 10 units => `1000`
- next 20 units => `3000`
- next 10 units => `2000`
- `amountBeforeTax = 6000.00`

If fixed charge is `1000` and VAT is `18`:

- subtotal = `7000.00`
- tax = `1260.00`
- total = `8260.00`

### 14.4 Penalty Proof

Generate another bill using a past due date, for example:

```json
{
  "readingId": 2,
  "dueDate": "2025-01-01"
}
```

Pass criteria:

- `penaltyAmount > 0`
- penalty is based on `penaltyPercentage` and subtotal

### 14.5 Negative Bill Tests

- duplicate bill generation for same reading is rejected
- inactive customer cannot receive a bill
- bill approval works only when current status is `GENERATED`
- operator cannot generate bill
- operator cannot approve bill

### 14.6 Approval Test

Action:

- finance or admin approves generated bill

Pass criteria:

- status becomes `APPROVED`
- `approvedByEmail` is filled
- `approvedAt` is filled

Database check:

```sql
select bill_reference, status, amount_before_tax, tax_amount, penalty_amount, total_amount, amount_paid, outstanding_balance, approved_by
from bills;
```

## 15. Payment Module Tests

Endpoints:

- `POST /api/payments`
- `GET /api/payments`
- `GET /api/bills/{billId}/payments`
- `GET /api/customers/{customerId}/payments`
- `GET /api/customer/payments`

Record payment example:

```json
{
  "billReference": "BILL-2026-06-0001",
  "amountPaid": 5000,
  "paymentMethod": "MOMO",
  "paymentDate": "2026-06-05"
}
```

### 15.1 Partial Payment

Pass criteria:

- payment is recorded
- bill `amountPaid` increases correctly
- `outstandingBalance` decreases correctly
- bill status becomes `PARTIALLY_PAID`

### 15.2 Full Payment

Record the exact remaining balance.

Pass criteria:

- bill status becomes `PAID`
- `outstandingBalance = 0.00`
- `amountPaid = totalAmount`

### 15.3 Negative Payment Tests

- payment on `GENERATED` bill is rejected
- payment on `CANCELLED` bill is rejected
- payment on already `PAID` bill is rejected
- payment amount `<= 0` is rejected
- payment exceeding balance is rejected

### 15.4 Payment History Queries

Verify:

- all payments endpoint
- payments by bill
- payments by customer
- current customer payments

Database check:

```sql
select bill_id, amount_paid, payment_method, payment_date from payments order by id;
```

## 16. Notification Module Tests

Endpoints:

- `GET /api/notifications`
- `GET /api/customers/{customerId}/notifications`
- `GET /api/customer/notifications`
- `PATCH /api/notifications/{id}/mark-sent`

### 16.1 Bill Generation Trigger Proof

After bill creation:

```sql
select customer_id, bill_id, message, status
from notifications
order by id desc;
```

Pass criteria:

- a new notification row exists
- message starts with `Dear <CustomerName>,`
- message includes `MM/YYYY`
- message includes bill total in FRW
- status is `PENDING`

### 16.2 Full Payment Trigger Proof

After full payment:

```sql
select customer_id, bill_id, message, status
from notifications
order by id desc;
```

Pass criteria:

- a second notification row exists
- message states the bill has been fully paid

### 16.3 Notification Access Tests

Verify:

- admin/finance can list all notifications
- admin/finance can list any customer notifications
- customer can access only `/api/customer/notifications`
- customer should not rely on `/api/customers/{customerId}/notifications`

### 16.4 Mark Sent

Action:

- mark a notification as sent

Pass criteria:

- status changes from `PENDING` to `SENT`

## 17. Customer Self-Service Tests

Create a `ROLE_CUSTOMER` user linked to a customer.

Login with that account.

Verify:

- `GET /api/customer/bills` returns only that customer’s bills
- `GET /api/customer/payments` returns only that customer’s payments
- `GET /api/customer/notifications` returns only that customer’s notifications

Negative checks:

- customer cannot create tariffs
- customer cannot create customers
- customer cannot create meters
- customer cannot capture readings
- customer cannot approve bills
- customer cannot record payments

## 18. End-to-End Proof Flow

This is the minimum proof flow that demonstrates the application’s designed workflow.

### Step 1

- login as seeded admin

### Step 2

- create operator user
- create finance user

### Step 3

- login as operator
- create customer

### Step 4

- as admin, create a customer-linked portal user for that customer or let the customer sign up with `customerId`

### Step 5

- as operator, register meter for the customer

### Step 6

- as admin, create tariff

### Step 7

- as operator, capture reading

### Step 8

- as finance or admin, generate bill

Proof:

- bill values are correct
- notification row created by trigger

### Step 9

- as finance or admin, approve bill

### Step 10

- as finance, record partial payment

Proof:

- bill becomes `PARTIALLY_PAID`

### Step 11

- as finance, record final payment

Proof:

- bill becomes `PAID`
- payment notification row created by trigger

### Step 12

- as customer, view own bills
- as customer, view own payments
- as customer, view own notifications

If all steps pass, the main business lifecycle is working.

## 19. Recommended Negative Test Matrix

Run these deliberately:

- duplicate email
- duplicate national ID
- duplicate meter number
- duplicate reading cycle
- flat tariff with tiers
- tiered tariff with invalid ranges
- reading on inactive meter
- bill for inactive customer
- approval of non-generated bill
- payment on generated bill
- payment exceeding balance
- customer calling admin-only endpoints

Every one of these should fail cleanly with a controlled error response.

## 20. Suggested Database Verification Queries

```sql
select * from roles order by id;
```

```sql
select id, email, customer_id, status from users order by id;
```

```sql
select id, full_name, national_id, status from customers order by id;
```

```sql
select id, meter_number, customer_id, meter_type, status from meters order by id;
```

```sql
select id, meter_id, billing_month, billing_year, consumption from meter_readings order by id;
```

```sql
select id, meter_type, tariff_type, version, status from tariffs order by id;
```

```sql
select id, bill_reference, status, total_amount, amount_paid, outstanding_balance from bills order by id;
```

```sql
select id, bill_id, amount_paid, payment_method from payments order by id;
```

```sql
select id, customer_id, bill_id, status, message from notifications order by id;
```

## 21. Final Acceptance Checklist

The application should be considered functionally proven only if all of the following are true:

- app starts cleanly
- seeded admin can log in
- public signup creates only customer users
- admin can create privileged users
- customer creation works
- meter creation works
- reading validation works
- tariff creation and versioning work
- bill generation works
- bill calculations are correct
- bill approval works
- partial payment works
- full payment works
- customer self-service reads work
- trigger-based notifications are created in PostgreSQL
- unauthorized role access is blocked
- core negative tests fail with controlled messages

## 22. Optional Improvement for Formal Submission

For an exam or demo defense, capture evidence for:

- Swagger screenshots
- sample request and response bodies
- database rows before and after key operations
- one flat-tariff bill calculation
- one tiered-tariff bill calculation
- one partial payment transition
- one full payment transition
- trigger-generated notification rows

That evidence will make it easier to prove both the API design and the business logic.
