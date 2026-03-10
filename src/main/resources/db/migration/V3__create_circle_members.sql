-- V3: US-010 Criar Círculo
-- Tables: circle_members

CREATE TABLE circle_members (
    id          UUID PRIMARY KEY,
    circle_id   UUID         NOT NULL REFERENCES circles(id) ON DELETE CASCADE,
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role        VARCHAR(10)  NOT NULL DEFAULT 'MEMBER',
    status      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    joined_at   TIMESTAMPTZ,
    left_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_circle_members_circle_user UNIQUE (circle_id, user_id),
    CONSTRAINT chk_circle_members_role   CHECK (role   IN ('ADMIN', 'MEMBER')),
    CONSTRAINT chk_circle_members_status CHECK (status IN ('ACTIVE', 'PENDING', 'REMOVED'))
);

CREATE INDEX idx_circle_members_circle_id ON circle_members (circle_id);
CREATE INDEX idx_circle_members_user_id   ON circle_members (user_id);
