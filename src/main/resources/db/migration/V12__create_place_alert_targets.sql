CREATE TABLE place_alert_targets (
    id          UUID PRIMARY KEY,
    policy_id   UUID NOT NULL REFERENCES place_alert_policies(id),
    user_id     UUID NOT NULL REFERENCES users(id),
    CONSTRAINT uq_place_alert_targets_policy_user UNIQUE (policy_id, user_id)
);
