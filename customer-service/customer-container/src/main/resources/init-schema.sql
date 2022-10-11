DROP SCHEMA IF EXISTS customer CASCADE;

CREATE SCHEMA customer;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS customer.customers CASCADE;

CREATE TABLE customer.customers (
    id uuid NOT NULL,
    username character varying COLLATE pg_catalog."default" NOT NULL,
    first_name character varying COLLATE pg_catalog."default" NOT NULL,
    last_name character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

DROP MATERIALIZED VIEW IF EXISTS customer.order_customer_m_view;

CREATE MATERIALIZED VIEW customer.order_customer_m_view TABLESPACE pg_default AS
SELECT id, username, first_name, last_name FROM customer.customers
WITH DATA;

refresh materialized VIEW customer.order_customer_m_view;

DROP FUNCTION IF EXISTS customer.refresh_order_customer_m_view;

CREATE OR REPLACE FUNCTION customer.refresh_order_customer_m_view()
RETURNS TRIGGER
AS '
BEGIN
    refresh materialized VIEW customer.order_customer_m_view;
    RETURN NULL;
END;
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS refresh_order_customer_m_view ON customer.customers;

CREATE TRIGGER refresh_order_customer_m_view
AFTER INSERT OR DELETE OR UPDATE OR TRUNCATE
ON customer.customers FOR EACH STATEMENT
EXECUTE PROCEDURE customer.refresh_order_customer_m_view();
