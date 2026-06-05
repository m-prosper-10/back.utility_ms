DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_users_email'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT uq_users_email UNIQUE (email);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_customers_national_id'
    ) THEN
        ALTER TABLE customers
            ADD CONSTRAINT uq_customers_national_id UNIQUE (national_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_meters_meter_number'
    ) THEN
        ALTER TABLE meters
            ADD CONSTRAINT uq_meters_meter_number UNIQUE (meter_number);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_meter_readings_cycle'
    ) THEN
        ALTER TABLE meter_readings
            ADD CONSTRAINT uq_meter_readings_cycle UNIQUE (meter_id, billing_month, billing_year);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_bills_bill_reference'
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT uq_bills_bill_reference UNIQUE (bill_reference);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_current_gt_previous'
    ) THEN
        ALTER TABLE meter_readings
            ADD CONSTRAINT chk_current_gt_previous
            CHECK (current_reading > previous_reading);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_payment_amount_non_negative'
    ) THEN
        ALTER TABLE payments
            ADD CONSTRAINT chk_payment_amount_non_negative
            CHECK (amount_paid >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_bill_outstanding_balance_non_negative'
    ) THEN
        ALTER TABLE bills
            ADD CONSTRAINT chk_bill_outstanding_balance_non_negative
            CHECK (outstanding_balance >= 0);
    END IF;
END $$;
