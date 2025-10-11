-- 02_citus.sql

-- 1) Extensión en el coordinador
CREATE EXTENSION IF NOT EXISTS citus;

-- 2) Registrar workers (usa los hostnames del compose)
--    En 13.x el nombre es citus_add_node (master_add_node sigue funcionando como alias en muchas instalaciones)
SELECT citus_add_node('hosp-citus-worker-1', 5432);
SELECT citus_add_node('hosp-citus-worker-2', 5432);

-- 3) Asegurar extensión en los workers (misma DB) por si no se creó
SELECT run_command_on_workers($cmd$
  CREATE EXTENSION IF NOT EXISTS citus;
$cmd$);

-- 4) Reference tables (replicadas en todos los nodos)
SELECT create_reference_table('medical_centers');
SELECT create_reference_table('specialties');
SELECT create_reference_table('roles');
SELECT create_reference_table('users');
SELECT create_reference_table('users_roles');
SELECT create_reference_table('verification_tokens');
SELECT create_reference_table('doctors');   -- por facilidad/lectura
SELECT create_reference_table('patients');  -- por facilidad/lectura
