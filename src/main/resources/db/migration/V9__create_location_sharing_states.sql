-- US-020: Compartilhar localização com o círculo
-- Tables: location_sharing_states

CREATE TABLE location_sharing_states (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    circle_id               UUID         NOT NULL REFERENCES circles(id) ON DELETE CASCADE,
    is_sharing_location     BOOLEAN      NOT NULL DEFAULT true,
    is_history_enabled      BOOLEAN      NOT NULL DEFAULT true,
    paused_until            TIMESTAMPTZ,
    last_known_location_id  UUID         REFERENCES locations(id) ON DELETE SET NULL,
    last_updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_location_sharing_user_circle UNIQUE (user_id, circle_id)
);

CREATE INDEX idx_location_sharing_user_circle ON location_sharing_states (user_id, circle_id);
