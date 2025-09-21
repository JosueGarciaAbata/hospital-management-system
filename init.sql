-- ================================
-- Database: Hospital Management System
-- ================================

-- Table: Medical Centers
CREATE TABLE medical_centers (
                                 id         BIGSERIAL PRIMARY KEY,
                                 name       VARCHAR(100) NOT NULL,
                                 city       VARCHAR(100) NOT NULL,
                                 address    VARCHAR(200) NOT NULL,
                                 created_at TIMESTAMP    NOT NULL DEFAULT now(),
                                 updated_at TIMESTAMP    NOT NULL DEFAULT now(),
                                 deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
                                 version    BIGINT       NOT NULL DEFAULT 0 -- Optimistic Locking
);

-- Unicidad (name + address) solo para registros activos (case-insensitive)
CREATE UNIQUE INDEX uq_med_centers_name_addr_active
    ON medical_centers (LOWER(name), LOWER(address))
    WHERE deleted = FALSE;

-- Índices para búsquedas frecuentes
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
                             version     BIGINT  NOT NULL DEFAULT 0 -- Optimistic Locking
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_specialties_name_active
    ON specialties (LOWER(name))
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_specialties_name
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
                       password VARCHAR(255) NOT NULL, -- encrypted password
                       gender VARCHAR(10),
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       center_id BIGINT NOT NULL,
                       CONSTRAINT fk_user_center FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE
);

-- Table: Users_Roles (join table for many-to-many)
CREATE TABLE users_roles (
                             user_id BIGINT NOT NULL,
                             role_id BIGINT NOT NULL,
                             PRIMARY KEY (user_id, role_id),
                             CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Table: Doctors
CREATE TABLE doctors (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL UNIQUE,
                         specialty_id BIGINT NOT NULL,
                         CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_doctor_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE SET NULL
);

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
                          CONSTRAINT fk_patient_center FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE
);

-- Table: Medical Consultations
CREATE TABLE medical_consultations (
                                       id BIGSERIAL PRIMARY KEY,
                                       patient_id BIGINT NOT NULL,
                                       doctor_id BIGINT NOT NULL,
                                       center_id BIGINT NOT NULL,
                                       date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       diagnosis TEXT,
                                       treatment TEXT,
                                       notes TEXT,
                                       deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                       CONSTRAINT fk_consult_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_consult_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
                                       CONSTRAINT fk_consult_center FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE
);

-- Habilitar extensión para hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Insert default role ADMIN
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('DOCTOR');

-- Insert default medical center (needed for FK)
INSERT INTO medical_centers (name, city, address)
VALUES ('Central Hospital', 'Quito', 'Av. Principal 123');

-- Insert default admin user (password: admin123)
INSERT INTO users (dni, password, gender, first_name, last_name, enabled, center_id)
VALUES (
           'admin001',
           crypt('admin123', gen_salt('bf')),
           'MALE',
           'System',
           'Admin',
           TRUE,
           1
       );

-- Link user to ADMIN role
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);


-- Insert new doctor user
INSERT INTO users (dni, password, gender, first_name, last_name, enabled, center_id)
VALUES (
           'doctor001',
           crypt('doctor123', gen_salt('bf')),
           'MALE',
           'Juan',
           'Perez',
           TRUE,
           1
       );

-- Link user to DOCTOR ROL
INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);

-- Insert default specialty
INSERT INTO specialties (name, description)
VALUES ('Medicina General', 'Especialidad general para atención primaria');

-- Insert doctor profile linking user to specialty (suponiendo specialty_id = 1)
INSERT INTO doctors (user_id, specialty_id)
VALUES (2, 1);

-- =========================
-- Insert 5 patients
-- =========================
INSERT INTO patients (dni, first_name, last_name, birth_date, gender, center_id, deleted)
VALUES
    ('patient001', 'Alice', 'Johnson', '1990-01-15', 'FEMALE', 1, FALSE),
    ('patient002', 'Bob', 'Smith', '1985-06-20', 'MALE', 1, FALSE),
    ('patient003', 'Carol', 'Davis', '2000-03-10', 'FEMALE', 1, FALSE),
    ('patient004', 'David', 'Martinez', '1995-09-05', 'MALE', 1, FALSE),
    ('patient005', 'Eva', 'Lopez', '1988-12-30', 'FEMALE', 1, FALSE);



-- =========================
-- Insert 5 medical consultation
-- =========================
INSERT INTO medical_consultations (patient_id, doctor_id, center_id, date, diagnosis, treatment, notes)
VALUES
-- Consultas paciente 1 (Alice Johnson)
(1, 2, 1, '2025-09-21 10:00:00', 'Gripe común', 'Reposo y líquidos', 'Paciente con fiebre y tos'),
(1, 2, 1, '2025-09-22 11:30:00', 'Dolor de cabeza', 'Analgésicos', 'Dolor leve, seguimiento recomendado'),

-- Consultas paciente 2 (Bob Smith)
(2, 2, 1, '2025-09-23 09:00:00', 'Chequeo rutinario', 'Ninguno', 'Todo dentro de parámetros normales'),
(2, 2, 1, '2025-09-24 14:15:00', 'Infección de garganta', 'Antibióticos', 'Revisar respuesta en 5 días'),

-- Consultas paciente 3 (Carol Davis)
(3, 2, 1, '2025-09-25 08:45:00', 'Control postoperatorio', 'Curaciones y reposo', 'Paciente estable, cicatrización correcta'),

-- Consultas paciente 4 (David Martinez)
(4, 2, 1, '2025-09-25 09:30:00', 'Dolor de espalda', 'Fisioterapia', 'Seguir tratamiento por 2 semanas'),

-- Consultas paciente 5 (Eva Lopez)
(5, 2, 1, '2025-09-26 10:00:00', 'Alergia estacional', 'Antihistamínicos', 'Revisar síntomas en 1 semana');

