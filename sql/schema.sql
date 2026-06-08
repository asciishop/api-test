-- ============================================================
-- Imports API – Database Schema
-- PostgreSQL 15+
-- ============================================================

-- Create database (run as superuser outside of this script)
-- CREATE DATABASE imports_db;
-- \c imports_db

-- ── Suppliers ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS suppliers (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    tax_id        VARCHAR(100) NOT NULL UNIQUE,
    country       VARCHAR(100),
    address       VARCHAR(500),
    contact_email VARCHAR(255)
);

-- ── Booking Requests ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS booking_requests (
    id                  BIGSERIAL     PRIMARY KEY,
    booking_code        VARCHAR(100)  NOT NULL UNIQUE,
    issue_date          DATE          NOT NULL,
    expiration_date     DATE          NOT NULL,
    currency            VARCHAR(10)   NOT NULL,
    incoterm_code       VARCHAR(10)   NOT NULL,
    freight_mode        VARCHAR(10)   NOT NULL,
    origin_country      VARCHAR(100),
    destination_country VARCHAR(100),
    fob_value           NUMERIC(19,2),
    status              VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    active              BOOLEAN       NOT NULL DEFAULT TRUE,
    supplier_id         BIGINT        NOT NULL,
    CONSTRAINT fk_booking_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- ── Booking Items ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS booking_items (
    id                 BIGSERIAL     PRIMARY KEY,
    sku                VARCHAR(100)  NOT NULL,
    description        VARCHAR(500),
    quantity           INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price         NUMERIC(19,2) NOT NULL CHECK (unit_price > 0),
    total_amount       NUMERIC(19,2) NOT NULL,
    booking_request_id BIGINT        NOT NULL,
    CONSTRAINT fk_item_booking FOREIGN KEY (booking_request_id) REFERENCES booking_requests(id)
);

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    tax_id   VARCHAR(100) NOT NULL REFERENCES suppliers(tax_id)
);

-- ── Seed Data ─────────────────────────────────────────────────
INSERT INTO suppliers (name, tax_id, country, address, contact_email) VALUES
    ('Acme Corp',          '12-3456789-0', 'USA',     '123 Main St, New York, NY',    'contact@acme.com'),
    ('Global Imports Ltd', '98-7654321-0', 'China',   '456 Trade Blvd, Shanghai',     'info@globalimports.cn'),
    ('EuroSupply GmbH',    '76-5432198-0', 'Germany', '789 Export Str, Hamburg',      'supply@eurosupply.de');

INSERT INTO booking_requests
    (booking_code, issue_date, expiration_date, currency, incoterm_code, freight_mode,
     origin_country, destination_country, fob_value, status, supplier_id)
VALUES
    ('BK-2024-001', '2024-01-15', '2024-06-30', 'USD', 'FOB', 'SEA', 'China',   'USA',     50000.00, 'DRAFT',     1),
    ('BK-2024-002', '2024-02-01', '2024-08-31', 'EUR', 'CIF', 'AIR', 'Germany', 'Colombia', 12000.00, 'CONFIRMED', 3);

INSERT INTO booking_items (sku, description, quantity, unit_price, total_amount, booking_request_id) VALUES
    ('WIDGET-001', 'Blue Widget',      100, 100.00, 10000.00, 1),
    ('WIDGET-002', 'Red Widget',       200,  75.00, 15000.00, 1),
    ('PART-A100',  'Mechanical Part',   50, 240.00, 12000.00, 2);
