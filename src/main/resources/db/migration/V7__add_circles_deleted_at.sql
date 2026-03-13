-- V7: RF-022 Soft-delete de círculos
-- Adds deleted_at column for logical deletion

ALTER TABLE circles ADD COLUMN deleted_at TIMESTAMPTZ;
