-- US-020: Compartilhar localização com o círculo
-- Tables: locations

CREATE TABLE locations (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    circle_id       UUID         REFERENCES circles(id) ON DELETE SET NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    accuracy_meters DOUBLE PRECISION,
    speed_mps       DOUBLE PRECISION,
    heading_degrees DOUBLE PRECISION,
    altitude_meters DOUBLE PRECISION,
    source          VARCHAR(10)  NOT NULL,
    recorded_at     TIMESTAMPTZ  NOT NULL,
    received_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    is_moving       BOOLEAN      NOT NULL DEFAULT false,
    battery_level   INTEGER,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_locations_source CHECK (source IN ('GPS', 'NETWORK', 'FUSED')),
    CONSTRAINT chk_locations_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_locations_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_locations_battery CHECK (battery_level IS NULL OR battery_level BETWEEN 0 AND 100)
);

CREATE INDEX idx_locations_user_time   ON locations (user_id, recorded_at DESC);
CREATE INDEX idx_locations_circle_time ON locations (circle_id, recorded_at DESC);
