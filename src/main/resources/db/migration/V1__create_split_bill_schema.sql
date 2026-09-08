CREATE TABLE bill_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE participants (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES bill_groups(id),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uk_participants_group_lower_name
    ON participants (group_id, LOWER(name));
CREATE INDEX idx_participants_group_id ON participants (group_id);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES bill_groups(id),
    payer_id UUID NOT NULL REFERENCES participants(id),
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 0) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_expenses_group_id ON expenses (group_id);
CREATE INDEX idx_expenses_payer_id ON expenses (payer_id);

CREATE TABLE expense_shares (
    expense_id UUID NOT NULL REFERENCES expenses(id),
    participant_id UUID NOT NULL REFERENCES participants(id),
    share_amount NUMERIC(19, 0) NOT NULL CHECK (share_amount > 0),
    allocation_order INTEGER NOT NULL CHECK (allocation_order >= 0),
    PRIMARY KEY (expense_id, participant_id),
    UNIQUE (expense_id, allocation_order)
);

CREATE INDEX idx_expense_shares_participant_id ON expense_shares (participant_id);
