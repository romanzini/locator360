-- V2: US-010 Criar Círculo
-- Tables: circles

CREATE TABLE circles (
    id               UUID PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    description      VARCHAR(500),
    photo_url        VARCHAR(512),
    color_hex        VARCHAR(7),
    privacy_level    VARCHAR(20)  NOT NULL DEFAULT 'OPEN_WITH_CODE',
    created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_circles_privacy_level CHECK (privacy_level IN ('OPEN_WITH_CODE', 'INVITE_ONLY'))
);

CREATE INDEX idx_circles_created_by ON circles (created_by_user_id);
