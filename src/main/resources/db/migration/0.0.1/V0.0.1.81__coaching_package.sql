CREATE SEQUENCE coaching_package_id_seq;

CREATE TABLE coaching_package (
    id BIGINT PRIMARY KEY DEFAULT nextval('coaching_package_id_seq'),
    company_id BIGINT NOT NULL REFERENCES company(id),
    pool_type VARCHAR(50) NOT NULL,
    total_units INTEGER NOT NULL,
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    context_ref VARCHAR(4000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT chk_pool_type CHECK (pool_type IN ('CORE', 'MASTER')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_total_units_positive CHECK (total_units > 0)
);

CREATE INDEX idx_coaching_package_company_id ON coaching_package(company_id);
CREATE INDEX idx_coaching_package_status ON coaching_package(status);
CREATE INDEX idx_coaching_package_company_status ON coaching_package(company_id, status);
