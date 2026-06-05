# Application Architecture

This document shows the current implemented architecture of the Utility Billing System.

## High-Level Architecture

```mermaid
flowchart TD
    Client["Client Apps<br/>Swagger UI / Postman / Browser"] --> Security["Spring Security<br/>JWT Filter + Role Checks"]
    Security --> Controllers["REST Controllers"]
    Controllers --> Services["Service Layer<br/>Business Rules + Validation"]
    Services --> Repositories["Spring Data JPA Repositories"]
    Repositories --> DB["PostgreSQL Database"]
    DB --> Triggers["PostgreSQL Triggers"]
    Triggers --> Notifications["Notifications Table"]
```

## Package Architecture

```mermaid
flowchart TB
    App["com.utilitybilling.UtilityBillingApplication"]

    subgraph Auth["auth"]
        AuthController["controller"]
        AuthDto["dto"]
        AuthService["service"]
        Jwt["jwt"]
        Security["security"]
    end

    subgraph User["user"]
        UserController["controller"]
        UserDto["dto"]
        UserEntity["entity"]
        UserRepository["repository"]
        UserService["service"]
    end

    subgraph Customer["customer"]
        CustomerController["controller"]
        CustomerDto["dto"]
        CustomerEntity["entity"]
        CustomerRepository["repository"]
        CustomerService["service"]
    end

    subgraph Meter["meter"]
        MeterController["controller"]
        MeterDto["dto"]
        MeterEntity["entity"]
        MeterRepository["repository"]
        MeterService["service"]
    end

    subgraph Reading["reading"]
        ReadingController["controller"]
        ReadingDto["dto"]
        ReadingEntity["entity"]
        ReadingRepository["repository"]
        ReadingService["service"]
    end

    subgraph Tariff["tariff"]
        TariffController["controller"]
        TariffDto["dto"]
        TariffEntity["entity"]
        TariffRepository["repository"]
        TariffService["service"]
    end

    subgraph Billing["billing"]
        BillController["controller"]
        BillDto["dto"]
        BillEntity["entity"]
        BillRepository["repository"]
        BillService["service"]
    end

    subgraph Payment["payment"]
        PaymentController["controller"]
        PaymentDto["dto"]
        PaymentEntity["entity"]
        PaymentRepository["repository"]
        PaymentService["service"]
    end

    subgraph Notification["notification"]
        NotificationController["controller"]
        NotificationDto["dto"]
        NotificationEntity["entity"]
        NotificationRepository["repository"]
        NotificationService["service"]
    end

    subgraph Common["common"]
        Config["config"]
        CommonEntity["entity"]
        Enums["enums"]
        Exceptions["exception"]
        Response["response"]
    end

    App --> Auth
    App --> User
    App --> Customer
    App --> Meter
    App --> Reading
    App --> Tariff
    App --> Billing
    App --> Payment
    App --> Notification
    App --> Common
```

## Runtime Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant F as JwtAuthenticationFilter
    participant SC as SecurityConfig
    participant CT as Controller
    participant SV as Service
    participant RP as Repository
    participant PG as PostgreSQL

    C->>F: HTTP request with Bearer token
    F->>F: Parse and validate JWT
    F->>SC: Set authentication context
    SC->>CT: Allow request if role matches
    CT->>SV: Call use-case method
    SV->>SV: Validate business rules
    SV->>RP: Load or save entities
    RP->>PG: Execute SQL
    PG-->>RP: Return data
    RP-->>SV: Return entities
    SV-->>CT: Return DTO
    CT-->>C: API response
```

## Domain Workflow Architecture

```mermaid
flowchart LR
    Customer["Customer"] --> Meter["Meter"]
    Meter --> Reading["MeterReading"]
    Reading --> Bill["Bill"]
    Tariff["Tariff / TariffTier"] --> Bill
    Bill --> Approval["Bill Approval"]
    Approval --> Payment["Payment"]
    Bill --> Notification["Notification"]
    Payment --> Notification
```

## Billing and Notification Flow

```mermaid
flowchart TD
    Reading["Approved Meter Reading Input"] --> BillService["BillService.generateBill"]
    BillService --> TariffLookup["TariffRepository.findApplicableTariff"]
    TariffLookup --> Calculation["Flat or Tiered Calculation"]
    Calculation --> BillSave["Save Bill"]
    BillSave --> BillTrigger["DB Trigger: Bill Generated"]
    BillTrigger --> NotificationInsert["Insert Notification"]
    BillSave --> Approval["Approve Bill"]
    Approval --> PaymentRecord["PaymentService.recordPayment"]
    PaymentRecord --> BalanceUpdate["Update Amount Paid + Outstanding Balance"]
    BalanceUpdate --> PaidCheck{"Balance == 0 ?"}
    PaidCheck -- No --> Partial["Status = PARTIALLY_PAID"]
    PaidCheck -- Yes --> Paid["Status = PAID"]
    Paid --> PaidTrigger["DB Trigger: Full Payment"]
    PaidTrigger --> PaymentNotification["Insert Notification"]
```

## Current Role Boundaries

```mermaid
flowchart TB
    Admin["ROLE_ADMIN"] --> AdminOps["Users, Tariffs, Customers, Meters,<br/>Bills, Approval, Notifications"]
    Operator["ROLE_OPERATOR"] --> OperatorOps["Customers, Meters, Readings"]
    Finance["ROLE_FINANCE"] --> FinanceOps["Bills, Approval, Payments, Notifications"]
    CustomerRole["ROLE_CUSTOMER"] --> CustomerOps["Own Bills, Own Payments,<br/>Own Notifications"]
```

## Notes

- This is a monolithic Spring Boot MVC application.
- Controllers expose DTO-based APIs, not entities directly.
- Business rules are enforced in services and reinforced with PostgreSQL constraints.
- Notification creation for billing events is handled by PostgreSQL triggers.
- Customer self-service access is scoped through the authenticated user account.
