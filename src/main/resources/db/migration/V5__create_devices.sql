-- V5: Create devices table (originally V2, renumbered after circles migrations)

CREATE TABLE devices (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    platform        VARCHAR(20)  NOT NULL,
    device_model    VARCHAR(255),
    os_version      VARCHAR(50),
    app_version     VARCHAR(50),
    push_token      VARCHAR(512),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_seen_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_devices_platform CHECK (platform IN ('ANDROID', 'IOS', 'WEB'))
);

CREATE INDEX idx_devices_user_id ON devices (user_id);
CREATE INDEX idx_devices_push_token ON devices (push_token) WHERE push_token IS NOT NULL;
