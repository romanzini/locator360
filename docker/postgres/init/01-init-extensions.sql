-- ============================================================
-- Family Locator — Inicialização do PostgreSQL
-- ============================================================
-- Este script roda automaticamente na primeira inicialização
-- do container PostgreSQL (via docker-entrypoint-initdb.d).
-- ============================================================

-- Habilitar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Verificar extensões instaladas
DO $$
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Family Locator — Inicialização concluída';
    RAISE NOTICE 'Extensões habilitadas:';
    RAISE NOTICE '  - uuid-ossp (geração de UUIDs)';
    RAISE NOTICE '  - postgis (dados geoespaciais)';
    RAISE NOTICE '  - pg_trgm (busca por similaridade)';
    RAISE NOTICE '==========================================';
END
$$;
