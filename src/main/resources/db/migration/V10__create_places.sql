CREATE TABLE places (
    id              UUID PRIMARY KEY,
    circle_id       UUID NOT NULL REFERENCES circles(id),
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(50) NOT NULL,
    address_text    VARCHAR(500),
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    radius_meters   DOUBLE PRECISION NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_user_id UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_places_circle_id ON places(circle_id);
