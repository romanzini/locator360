CREATE TABLE place_events (
    id              UUID        PRIMARY KEY,
    place_id        UUID        NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    circle_id       UUID        NOT NULL REFERENCES circles(id) ON DELETE CASCADE,
    user_id         UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type      VARCHAR(10) NOT NULL CHECK (event_type IN ('ENTER', 'EXIT')),
    location_id     UUID        REFERENCES locations(id) ON DELETE SET NULL,
    occurred_at     TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_place_events_place_time ON place_events (place_id, occurred_at DESC);
CREATE INDEX idx_place_events_user_time  ON place_events (user_id, occurred_at DESC);
