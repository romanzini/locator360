-- V1: Baseline migration — US-001 Cadastro de Conta
-- Tables: users, auth_identities, verification_tokens

-- ─── users ──────────────────────────────────────────────────────────

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) UNIQUE,
    phone_number    VARCHAR(30)  UNIQUE,
    full_name       VARCHAR(255) NOT NULL,
    first_name      VARCHAR(128),
    last_name       VARCHAR(128),
    birth_date      DATE,
    gender          VARCHAR(20),
    profile_photo_url VARCHAR(512),
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'pt-BR',
    timezone        VARCHAR(64)  NOT NULL DEFAULT 'America/Sao_Paulo',
    distance_unit   VARCHAR(10)  NOT NULL DEFAULT 'KM',
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_contact CHECK (email IS NOT NULL OR phone_number IS NOT NULL),
    CONSTRAINT chk_users_distance_unit CHECK (distance_unit IN ('KM', 'MILES')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'PENDING_VERIFICATION'))
);

CREATE INDEX idx_users_email ON users (email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_phone_number ON users (phone_number) WHERE phone_number IS NOT NULL;

-- ─── auth_identities ────────────────────────────────────────────────

CREATE TABLE auth_identities (
    id               UUID PRIMARY KEY,
    user_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider         VARCHAR(20)  NOT NULL,
    provider_user_id VARCHAR(255),
    email            VARCHAR(255),
    phone_number     VARCHAR(30),
    password_hash    VARCHAR(255),
    is_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at    TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_auth_provider CHECK (provider IN ('PASSWORD', 'GOOGLE', 'APPLE', 'FACEBOOK', 'PHONE_SMS'))
);

CREATE INDEX idx_auth_identities_user_id ON auth_identities (user_id);
CREATE UNIQUE INDEX idx_auth_identities_provider_user ON auth_identities (provider, provider_user_id)
    WHERE provider_user_id IS NOT NULL;

-- ─── verification_tokens ────────────────────────────────────────────

CREATE TABLE verification_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(30)  NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_vtoken_type CHECK (type IN ('EMAIL_VERIFICATION', 'PHONE_VERIFICATION', 'PASSWORD_RESET'))
);

CREATE INDEX idx_verification_tokens_token ON verification_tokens (token);
CREATE INDEX idx_verification_tokens_user_type ON verification_tokens (user_id, type);
