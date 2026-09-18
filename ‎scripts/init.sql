-- Drop the table if it already exists
DROP TABLE IF EXISTS tb_order;

CREATE TABLE IF NOT EXISTS tb_order (
                                        id VARCHAR(50) PRIMARY KEY,
    order_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    reconciliation_status VARCHAR(30),
    reconciled_at TIMESTAMP
    );

CREATE INDEX idx_tb_order_status ON tb_order(status);
CREATE INDEX idx_tb_order_created_at ON tb_order(created_at);
CREATE INDEX idx_tb_order_reconciliation_status ON tb_order(reconciliation_status);

-- Section 1: 40,000 standard orders (should reconcile)
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((1000 + (gs * 733) % 199000) / 100.0, 2),
    'PENDING',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(1, 40000) AS gs;

-- Section 2: 3,000 orders for amount-discrepancy testing
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((1000 + (gs * 733) % 199000) / 100.0, 2),
    'PENDING',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(40001, 43000) AS gs;

-- Section 3: 2,000 orders for status-discrepancy testing
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((1000 + (gs * 733) % 199000) / 100.0, 2),
    'PENDING',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(43001, 45000) AS gs;

-- Section 4: 1,000 orders already cancelled (should reconcile)
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((1000 + (gs * 733) % 199000) / 100.0, 2),
    'CANCELLED',
    CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '30 days')
FROM generate_series(45001, 46000) AS gs;

-- Section 5: 1,000 old orders, intentionally absent from the gateway file (NOT_FOUND scenario)
INSERT INTO tb_order (id, order_amount, status, created_at)
SELECT
    'TRX-' || LPAD(gs::TEXT, 6, '0'),
    ROUND((1000 + (gs * 733) % 199000) / 100.0, 2),
    'PENDING',
    CURRENT_TIMESTAMP - INTERVAL '180 days' - (RANDOM() * INTERVAL '180 days')
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