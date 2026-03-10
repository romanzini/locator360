-- V4: US-010 Criar Círculo
-- Tables: circle_settings

CREATE TABLE circle_settings (
    id                  UUID PRIMARY KEY,
    circle_id           UUID        NOT NULL REFERENCES circles(id) ON DELETE CASCADE,
    driving_alert_level VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    allow_member_chat   BOOLEAN     NOT NULL DEFAULT TRUE,
    allow_member_sos    BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_circle_settings_circle_id UNIQUE (circle_id),
    CONSTRAINT chk_circle_settings_alert_level CHECK (driving_alert_level IN ('LOW', 'MEDIUM', 'HIGH'))
);
