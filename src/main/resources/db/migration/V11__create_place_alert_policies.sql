CREATE TABLE place_alert_policies (
    id              UUID PRIMARY KEY,
    place_id        UUID NOT NULL REFERENCES places(id),
    circle_id       UUID NOT NULL REFERENCES circles(id),
    alert_on_enter  BOOLEAN NOT NULL DEFAULT TRUE,
    alert_on_exit   BOOLEAN NOT NULL DEFAULT TRUE,
    days_of_week    VARCHAR(100),
    start_time      TIME,
    end_time        TIME,
    target_type     VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
