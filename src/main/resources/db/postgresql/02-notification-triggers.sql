DROP TRIGGER IF EXISTS trg_notify_bill_generation ON bills;
DROP FUNCTION IF EXISTS notify_bill_generation();

CREATE OR REPLACE FUNCTION notify_bill_generation()
RETURNS TRIGGER AS $$
DECLARE
    customer_name VARCHAR;
BEGIN
    SELECT full_name INTO customer_name
    FROM customers
    WHERE id = NEW.customer_id;

    INSERT INTO notifications (
        customer_id,
        bill_id,
        message,
        status,
        created_at,
        updated_at
    )
    VALUES (
        NEW.customer_id,
        NEW.id,
        CONCAT(
            'Dear ', customer_name, ',',
            E'\nYour ',
            LPAD(NEW.billing_month::text, 2, '0'), '/', NEW.billing_year,
            ' utility bill of ',
            TRIM(TO_CHAR(NEW.total_amount, 'FM999999999999990D00')),
            ' FRW has been successfully processed.'
        ),
        'PENDING',
        NOW(),
        NOW()
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notify_bill_generation
AFTER INSERT ON bills
FOR EACH ROW
EXECUTE FUNCTION notify_bill_generation();

DROP TRIGGER IF EXISTS trg_notify_full_payment ON bills;
DROP FUNCTION IF EXISTS notify_full_payment();

CREATE OR REPLACE FUNCTION notify_full_payment()
RETURNS TRIGGER AS $$
DECLARE
    customer_name VARCHAR;
BEGIN
    IF NEW.status = 'PAID' AND OLD.status <> 'PAID' THEN
        SELECT full_name INTO customer_name
        FROM customers
        WHERE id = NEW.customer_id;

        INSERT INTO notifications (
            customer_id,
            bill_id,
            message,
            status,
            created_at,
            updated_at
        )
        VALUES (
            NEW.customer_id,
            NEW.id,
            CONCAT(
                'Dear ', customer_name, ',',
                E'\nYour ',
                LPAD(NEW.billing_month::text, 2, '0'), '/', NEW.billing_year,
                ' utility bill of ',
                TRIM(TO_CHAR(NEW.total_amount, 'FM999999999999990D00')),
                ' FRW has been fully paid.'
            ),
            'PENDING',
            NOW(),
            NOW()
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notify_full_payment
AFTER UPDATE ON bills
FOR EACH ROW
EXECUTE FUNCTION notify_full_payment();
