-- Drop the table if it already exists
DROP TABLE IF EXISTS tb_order;

CREATE TABLE IF NOT EXISTS tb_order (
    id VARCHAR(50) PRIMARY KEY,
    order_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );

CREATE INDEX idx_tb_order_status ON tb_order(status);
CREATE INDEX idx_tb_order_created_at ON tb_order(created_at);

-- Generate 40,000 standard orders with random amounts and dates
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((10 + RANDOM() * 1990)::NUMERIC, 2),
    'PENDING',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(1, 40000) AS gs;

-- Generate 3,000 orders for testing amount discrepancies
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((50 + RANDOM() * 1500)::NUMERIC, 2),
    'PENDING',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(40001, 43000) AS gs;

-- Generate 2,000 orders for testing status discrepancies
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((50 + RANDOM() * 1500)::NUMERIC, 2),
    'PENDING',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(43001, 45000) AS gs;

-- Generate 1,000 orders that are already cancelled
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((20 + RANDOM() * 1000)::NUMERIC, 2),
    'CANCELLED',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(45001, 46000) AS gs;

-- Generate 1,000 old orders to test date range filtering
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((10 + RANDOM() * 2000)::NUMERIC, 2),
    'PENDING',
    CURRENT_TIMESTAMP
        - INTERVAL '180 days'
    - (RANDOM() * INTERVAL '180 days')
FROM generate_series(46001, 47000) AS gs;

-- Fixed records for specific reconciliation test cases
INSERT INTO tb_order (id, order_amount, status, created_at)
VALUES
    ('TRX-TEST-001', 150.00, 'PENDING', CURRENT_TIMESTAMP),
    ('TRX-TEST-002', 299.90, 'PENDING', CURRENT_TIMESTAMP),
    ('TRX-TEST-003', 100.00, 'PENDING', CURRENT_TIMESTAMP),
    ('TRX-TEST-004', 50.00, 'CANCELLED', CURRENT_TIMESTAMP),
    ('TRX-TEST-005', 500.00, 'PENDING', CURRENT_TIMESTAMP),
    ('TRX-TEST-006', 999.99, 'PENDING', CURRENT_TIMESTAMP),
    ('TRX-TEST-007', 10.00, 'PENDING', CURRENT_TIMESTAMP),
    ('TRX-TEST-008', 1250.50, 'PENDING', CURRENT_TIMESTAMP);