-- User allocation table
CREATE SEQUENCE user_allocation_id_seq;

CREATE TABLE user_allocation (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_allocation_id_seq'),
    company_id BIGINT NOT NULL REFERENCES company(id),
    package_id BIGINT NOT NULL REFERENCES coaching_package(id),
    user_id VARCHAR(255) NOT NULL,
    allocated_units INTEGER NOT NULL DEFAULT 0,
    consumed_units INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    context_ref VARCHAR(4000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_user_allocation_package_user UNIQUE (package_id, user_id),
    CONSTRAINT chk_allocation_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_allocated_units_non_negative CHECK (allocated_units >= 0),
    CONSTRAINT chk_consumed_units_non_negative CHECK (consumed_units >= 0)
);

CREATE INDEX idx_user_allocation_package_id ON user_allocation(package_id);
CREATE INDEX idx_user_allocation_user_id ON user_allocation(user_id);
CREATE INDEX idx_user_allocation_company_id ON user_allocation(company_id);
CREATE INDEX idx_user_allocation_status ON user_allocation(status);

-- Add indexes on scheduled_session for session queries
CREATE INDEX idx_scheduled_session_username ON scheduled_session(username);
CREATE INDEX idx_scheduled_session_status ON scheduled_session(status);
CREATE INDEX idx_scheduled_session_username_status ON scheduled_session(username, status);
