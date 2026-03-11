-- V6: US-011 Convidar pessoas para o círculo
-- Tables: circle_invites

CREATE TABLE circle_invites (
    id                   UUID PRIMARY KEY,
    circle_id            UUID         NOT NULL REFERENCES circles(id) ON DELETE CASCADE,
    invited_by_user_id   UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_email         VARCHAR(255),
    target_phone         VARCHAR(20),
    invite_code          VARCHAR(20)  NOT NULL,
    status               VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    accepted_by_user_id  UUID         REFERENCES users(id) ON DELETE SET NULL,
    expires_at           TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_circle_invites_invite_code UNIQUE (invite_code),
    CONSTRAINT chk_circle_invites_status CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_circle_invites_circle_id   ON circle_invites (circle_id);
CREATE INDEX idx_circle_invites_invite_code ON circle_invites (invite_code);
