-- 03_distribute_by_center.sql
-- Objetivo: asegurar que medical_consultations quede como REFERENCE
-- (sin cambiar el esquema de columnas/constraints)

DO $$
    BEGIN
        -- Si la tabla está distribuida (hash), convertirla a reference si la UDF existe;
        -- si no existe, intentar undistribute y luego crear como reference.
        IF EXISTS (
            SELECT 1
            FROM pg_dist_partition
            WHERE logicalrelid = 'medical_consultations'::regclass
              AND partmethod = 'h'
        ) THEN
            BEGIN
                PERFORM alter_distributed_table('medical_consultations', 'reference');
            EXCEPTION WHEN undefined_function THEN
                BEGIN
                    PERFORM undistribute_table('medical_consultations');
                EXCEPTION WHEN undefined_function OR others THEN
                    -- Si no existen estas UDF (versiones antiguas) o falla, seguimos y probamos create_reference_table abajo
                    NULL;
                END;
            END;
        END IF;

        -- Si aún no tiene entrada en pg_dist_partition (ni distribuida ni reference), crearla como reference
        IF NOT EXISTS (
            SELECT 1
            FROM pg_dist_partition
            WHERE logicalrelid = 'medical_consultations'::regclass
        ) THEN
            PERFORM create_reference_table('medical_consultations');
        END IF;

    EXCEPTION WHEN others THEN
        -- Si ya es reference o la UDF devuelve error benigno, ignorar para mantener idempotencia
        NULL;
    END$$;
