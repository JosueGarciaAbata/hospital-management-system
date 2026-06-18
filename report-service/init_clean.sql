-- =========================
-- SCRIPT DE INICIALIZACIÓN COMPLETA
-- Este script limpia TODA la base de datos y la recrea desde cero
-- =========================

-- Conectar a la base de datos
\c hospital_system;

-- =========================
-- LIMPIAR TODO (con CASCADE para eliminar dependencias)
-- =========================
DROP TABLE IF EXISTS medical_consultations CASCADE;
DROP TABLE IF EXISTS doctors CASCADE;
DROP TABLE IF EXISTS users_roles CASCADE;
DROP TABLE IF EXISTS verification_tokens CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS specialties CASCADE;
DROP TABLE IF EXISTS medical_centers CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Eliminar extensión si existe y recrearla
DROP EXTENSION IF EXISTS pgcrypto CASCADE;
CREATE EXTENSION pgcrypto;

-- =========================
-- CREAR TABLAS DESDE CERO
-- =========================

-- Table: Medical Centers
CREATE TABLE medical_centers (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    address    VARCHAR(200) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    version    BIGINT       NOT NULL DEFAULT 0
);

-- Índices para medical_centers
CREATE UNIQUE INDEX uq_med_centers_name_addr_active
    ON medical_centers (LOWER(name), LOWER(address))
    WHERE deleted = FALSE;

CREATE INDEX idx_med_centers_name
    ON medical_centers (LOWER(name));

CREATE INDEX idx_med_centers_city
    ON medical_centers (LOWER(city));

-- Table: Specialties
CREATE TABLE specialties (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now(),
    deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    version     BIGINT  NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_specialties_name_active
    ON specialties (LOWER(name))
    WHERE deleted = FALSE;

CREATE INDEX idx_specialties_name
    ON specialties (LOWER(name));

-- Table: Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Table: Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    gender VARCHAR(10),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    center_id BIGINT NOT NULL,
    CONSTRAINT fk_user_center FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE
);

-- Table: Users_Roles
CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Table: Verification Tokens
CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    used BOOLEAN NOT NULL,
    expiration TIMESTAMP NOT NULL,
    CONSTRAINT fk_verification_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE
);

-- Table: Doctors
CREATE TABLE doctors (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL UNIQUE,
    specialty_id BIGINT,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    version      BIGINT  NOT NULL DEFAULT 0,

    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_doctor_specialty FOREIGN KEY (specialty_id)
        REFERENCES specialties(id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX uq_doctor_user_active
    ON doctors (user_id) WHERE deleted = FALSE;

CREATE INDEX idx_doctors_specialty
    ON doctors (specialty_id) WHERE deleted = FALSE;

-- Table: Patients
CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10),
    center_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_patient_center FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE
);

-- Table: Medical Consultations
CREATE TABLE medical_consultations (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    center_id BIGINT NOT NULL REFERENCES medical_centers(id) ON DELETE CASCADE,
    "date" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    diagnosis TEXT,
    treatment TEXT,
    notes TEXT,
    status VARCHAR(20),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Índices para medical_consultations
CREATE INDEX idx_mc_patient ON medical_consultations (patient_id);
CREATE INDEX idx_mc_doctor  ON medical_consultations (doctor_id);
CREATE INDEX idx_mc_center  ON medical_consultations (center_id);
CREATE INDEX idx_mc_date    ON medical_consultations ("date");

-- =========================
-- INSERTAR DATOS
-- =========================

-- Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ADMIN'),
(2, 'DOCTOR');

-- Centro médico
INSERT INTO medical_centers (id, name, city, address)
VALUES (53, 'Central Hospital', 'Quito', 'Av. Principal 123');

-- Especialidades
INSERT INTO specialties (id, name, description) VALUES 
(1, 'Cardiología', 'Especialidad en enfermedades del corazón'),
(2, 'Dermatología', 'Especialidad en enfermedades de la piel'),
(3, 'Pediatría', 'Especialidad en salud infantil'),
(4, 'Traumatología', 'Especialidad en lesiones y problemas óseos'),
(5, 'Neurología', 'Especialidad en sistema nervioso'),
(6, 'Oftalmología', 'Especialidad en salud visual'),
(7, 'Ginecología', 'Especialidad en salud femenina'),
(8, 'Psiquiatría', 'Especialidad en salud mental'),
(9, 'Endocrinología', 'Especialidad en sistema endocrino'),
(10, 'Urología', 'Especialidad en sistema urinario');

-- Usuario administrador
INSERT INTO users (id, dni, email, password, gender, first_name, last_name, enabled, center_id)
VALUES (
    1,
    '1500903685',
    'josuegarcab2@hotmail.com',
    crypt('admin123456789', gen_salt('bf')),
    'MALE',
    'System',
    'Admin',
    TRUE,
    53
);

-- Usuarios doctores
INSERT INTO users (id, dni, email, password, gender, first_name, last_name, enabled, center_id) VALUES
    (2, 'doctor001', 'doctor@hotmail.com', crypt('doctor123', gen_salt('bf')), 'MALE', 'Juan', 'Pérez', TRUE, 53),
    (3, 'doctor002', 'martin.gomez@hospital.com', crypt('doctor123', gen_salt('bf')), 'MALE', 'Martín', 'Gómez', TRUE, 53),
    (4, 'doctor003', 'valeria.silva@hospital.com', crypt('doctor123', gen_salt('bf')), 'FEMALE', 'Valeria', 'Silva', TRUE, 53),
    (5, 'doctor004', 'ricardo.fuentes@hospital.com', crypt('doctor123', gen_salt('bf')), 'MALE', 'Ricardo', 'Fuentes', TRUE, 53),
    (6, 'doctor005', 'laura.morales@hospital.com', crypt('doctor123', gen_salt('bf')), 'FEMALE', 'Laura', 'Morales', TRUE, 53),
    (7, 'doctor006', 'javier.ortiz@hospital.com', crypt('doctor123', gen_salt('bf')), 'MALE', 'Javier', 'Ortiz', TRUE, 53),
    (8, 'doctor007', 'carolina.mendez@hospital.com', crypt('doctor123', gen_salt('bf')), 'FEMALE', 'Carolina', 'Méndez', TRUE, 53),
    (9, 'doctor008', 'sergio.ruiz@hospital.com', crypt('doctor123', gen_salt('bf')), 'MALE', 'Sergio', 'Ruiz', TRUE, 53),
    (10, 'doctor009', 'andrea.castro@hospital.com', crypt('doctor123', gen_salt('bf')), 'FEMALE', 'Andrea', 'Castro', TRUE, 53);

-- Relaciones users/roles
INSERT INTO users_roles (user_id, role_id) VALUES 
(1, 1),  -- Admin
(2, 2),  -- Dr. Juan Pérez
(3, 2),  -- Dr. Martín Gómez
(4, 2),  -- Dra. Valeria Silva
(5, 2),  -- Dr. Ricardo Fuentes
(6, 2),  -- Dra. Laura Morales
(7, 2),  -- Dr. Javier Ortiz
(8, 2),  -- Dra. Carolina Méndez
(9, 2),  -- Dr. Sergio Ruiz
(10, 2); -- Dra. Andrea Castro

-- Doctores con especialidades
INSERT INTO doctors (id, user_id, specialty_id) VALUES
(1, 2, 1),  -- Dr. Juan Pérez - Cardiología
(2, 3, 1),  -- Dr. Martín Gómez - Cardiología
(3, 4, 2),  -- Dra. Valeria Silva - Dermatología
(4, 5, 3),  -- Dr. Ricardo Fuentes - Pediatría
(5, 6, 4),  -- Dra. Laura Morales - Traumatología
(6, 7, 5),  -- Dr. Javier Ortiz - Neurología
(7, 8, 6),  -- Dra. Carolina Méndez - Oftalmología
(8, 9, 7),  -- Dr. Sergio Ruiz - Ginecología
(9, 10, 8); -- Dra. Andrea Castro - Psiquiatría

-- Pacientes
INSERT INTO patients (id, dni, first_name, last_name, birth_date, gender, center_id, deleted) VALUES
    (1, 'patient001', 'Alice', 'Johnson', '1990-01-15', 'FEMALE', 53, FALSE),
    (2, 'patient002', 'Bob', 'Smith', '1985-06-20', 'MALE', 53, FALSE),
    (3, 'patient003', 'Carol', 'Davis', '2000-03-10', 'FEMALE', 53, FALSE),
    (4, 'patient004', 'David', 'Martinez', '1995-09-05', 'MALE', 53, FALSE),
    (5, 'patient005', 'Eva', 'Lopez', '1988-12-30', 'FEMALE', 53, FALSE),
    (6, 'patient006', 'Frank', 'Wilson', '1982-04-18', 'MALE', 53, FALSE),
    (7, 'patient007', 'Grace', 'Brown', '1993-11-25', 'FEMALE', 53, FALSE),
    (8, 'patient008', 'Henry', 'Taylor', '1987-08-12', 'MALE', 53, FALSE),
    (9, 'patient009', 'Isabel', 'Anderson', '1998-02-28', 'FEMALE', 53, FALSE),
    (10, 'patient010', 'James', 'Thomas', '1991-07-07', 'MALE', 53, FALSE),
    (11, 'patient011', 'Karen', 'White', '1989-03-14', 'FEMALE', 53, FALSE),
    (12, 'patient012', 'Luis', 'Garcia', '1994-10-31', 'MALE', 53, FALSE),
    (13, 'patient013', 'Maria', 'Rodriguez', '1986-05-22', 'FEMALE', 53, FALSE),
    (14, 'patient014', 'Nathan', 'Clark', '1997-12-09', 'MALE', 53, FALSE),
    (15, 'patient015', 'Olivia', 'Lee', '1992-01-04', 'FEMALE', 53, FALSE);

-- Consultas médicas
INSERT INTO medical_consultations (id, patient_id, doctor_id, center_id, date, diagnosis, treatment, notes, status) VALUES
-- 2023 (Histórico más antiguo)
(1, 1, 1, 53, '2023-01-05 09:00:00', 'Hipertensión arterial', 'Enalapril 10mg', 'Primera consulta', 'COMPLETED'),
(2, 2, 1, 53, '2023-02-15 10:30:00', 'Arritmia cardíaca', 'Amiodarona 200mg', 'Inicio tratamiento', 'COMPLETED'),
(3, 3, 3, 53, '2023-03-10 11:00:00', 'Dermatitis atópica', 'Hidrocortisona tópica', 'Reacción alérgica', 'COMPLETED'),
(4, 4, 4, 53, '2023-04-20 14:00:00', 'Varicela', 'Aciclovir', 'Caso pediátrico leve', 'COMPLETED'),
(5, 5, 5, 53, '2023-05-05 09:30:00', 'Esguince de rodilla', 'Inmovilización', 'Lesión deportiva', 'COMPLETED'),

-- 2024 (Histórico)
(6, 6, 1, 53, '2024-01-05 09:00:00', 'Hipertensión', 'Medicación y control de presión', 'Control rutinario', 'COMPLETED'),
(7, 2, 1, 53, '2024-01-15 10:30:00', 'Arritmia', 'Beta bloqueadores', 'Seguimiento necesario', 'COMPLETED'),
(8, 3, 3, 53, '2024-02-10 11:00:00', 'Dermatitis', 'Crema corticoide', 'Mejora notable', 'COMPLETED'),
(9, 4, 4, 53, '2024-02-20 14:00:00', 'Bronquitis', 'Antibióticos', 'Control en una semana', 'COMPLETED'),
(10, 5, 5, 53, '2024-03-05 09:30:00', 'Fractura de tobillo', 'Yeso y reposo', 'Radiografía de control', 'COMPLETED'),
(11, 6, 6, 53, '2024-03-15 10:00:00', 'Migraña crónica', 'Sumatriptán', 'Seguimiento mensual', 'COMPLETED'),
(12, 7, 7, 53, '2024-04-01 11:30:00', 'Miopía', 'Cambio de graduación', 'Nueva receta', 'COMPLETED'),
(13, 8, 8, 53, '2024-04-10 15:00:00', 'Control prenatal', 'Vitaminas prenatales', 'Embarazo normal', 'COMPLETED'),
(14, 9, 9, 53, '2024-05-05 09:00:00', 'Depresión', 'Terapia y medicación', 'Progreso positivo', 'COMPLETED'),
(15, 10, 8, 53, '2024-05-15 10:30:00', 'Hipotiroidismo', 'Levotiroxina', 'Ajuste de dosis', 'COMPLETED'),

-- 2025 (Actual) - Septiembre
(16, 1, 1, 53, '2025-09-21 10:00:00', 'Control cardíaco', 'Ajuste de medicación', 'Paciente estable', 'COMPLETED'),
(17, 2, 3, 53, '2025-09-21 11:30:00', 'Acné severo', 'Isotretinoína', 'Inicio de tratamiento', 'COMPLETED'),
(18, 3, 4, 53, '2025-09-22 09:00:00', 'Vacunación', 'Vacuna antigripal', 'Rutina anual', 'COMPLETED'),
(19, 4, 5, 53, '2025-09-22 10:30:00', 'Lesión deportiva', 'Fisioterapia', 'Rehabilitación', 'COMPLETED'),
(20, 5, 6, 53, '2025-09-23 14:00:00', 'Cefalea tensional', 'Relajantes musculares', 'Estrés laboral', 'COMPLETED'),

-- Próximas citas
(21, 1, 7, 53, '2025-09-26 09:30:00', NULL, NULL, 'Control rutinario', 'SCHEDULED'),
(22, 2, 8, 53, '2025-09-26 11:00:00', NULL, NULL, 'Revisión postparto', 'SCHEDULED'),
(23, 3, 9, 53, '2025-09-27 10:00:00', NULL, NULL, 'Seguimiento terapia', 'SCHEDULED'),
(24, 4, 8, 53, '2025-09-27 15:30:00', NULL, NULL, 'Control hormonal', 'SCHEDULED'),
(25, 5, 1, 53, '2025-09-30 09:00:00', NULL, NULL, 'Chequeo cardíaco', 'SCHEDULED');

-- =========================
-- ACTUALIZAR SECUENCIAS
-- =========================
SELECT setval('roles_id_seq', 2, true);
SELECT setval('medical_centers_id_seq', 53, true);
SELECT setval('specialties_id_seq', 10, true);
SELECT setval('users_id_seq', 10, true);
SELECT setval('doctors_id_seq', 9, true);
SELECT setval('patients_id_seq', 15, true);
SELECT setval('medical_consultations_id_seq', 25, true);

-- =========================
-- VERIFICACIÓN
-- =========================
\echo '=== VERIFICACIÓN DE DATOS ==='
SELECT 'Roles:' as tabla, count(*) as total FROM roles
UNION ALL
SELECT 'Medical Centers:', count(*) FROM medical_centers
UNION ALL
SELECT 'Especialidades:', count(*) FROM specialties
UNION ALL
SELECT 'Usuarios:', count(*) FROM users
UNION ALL
SELECT 'Doctores:', count(*) FROM doctors
UNION ALL
SELECT 'Pacientes:', count(*) FROM patients
UNION ALL
SELECT 'Consultas:', count(*) FROM medical_consultations;

\echo '=== SCRIPT COMPLETADO EXITOSAMENTE ==='