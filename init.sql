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
                       email VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL, -- encrypted password
                       gender VARCHAR(10),
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP    NOT NULL DEFAULT now(),
                       updated_at TIMESTAMP    NOT NULL DEFAULT now(),
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
DROP TABLE IF EXISTS doctors CASCADE;

CREATE TABLE doctors (
                         id           BIGSERIAL PRIMARY KEY,
                         user_id      BIGINT NOT NULL UNIQUE,
                         specialty_id BIGINT,
                         created_at   TIMESTAMP NOT NULL DEFAULT now(),
                         updated_at   TIMESTAMP NOT NULL DEFAULT now(),
                         deleted      BOOLEAN NOT NULL DEFAULT FALSE,
                         version      BIGINT  NOT NULL DEFAULT 0, -- Optimistic Locking

                         CONSTRAINT fk_doctor_user FOREIGN KEY (user_id)
                             REFERENCES users(id) ON DELETE CASCADE,

                         CONSTRAINT fk_doctor_specialty FOREIGN KEY (specialty_id)
                             REFERENCES specialties(id) ON DELETE SET NULL
);

-- Un doctor está ligado a un usuario único
CREATE UNIQUE INDEX uq_doctor_user_active
    ON doctors (user_id) WHERE deleted = FALSE;

-- Búsquedas frecuentes por especialidad
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

-- ================================================
-- Tabla de consultas médicas particionada por centro
-- ================================================
CREATE TABLE medical_consultations (
                                       id BIGSERIAL PRIMARY KEY,
                                       patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
                                       doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
                                       center_id BIGINT NOT NULL REFERENCES medical_centers(id) ON DELETE CASCADE,
                                       "date" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       diagnosis TEXT,
                                       treatment TEXT,
                                       notes TEXT,
                                       deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                       created_at TIMESTAMP NOT NULL DEFAULT now(),
                                       updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Indices útiles
CREATE INDEX idx_mc_patient ON medical_consultations (patient_id);
CREATE INDEX idx_mc_doctor  ON medical_consultations (doctor_id);
CREATE INDEX idx_mc_center  ON medical_consultations (center_id);
CREATE INDEX idx_mc_date    ON medical_consultations ("date");

-- Habilitar extensión para hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Insert default role ADMIN
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('DOCTOR');

-- Insert default medical center (needed for FK)
INSERT INTO medical_centers (name, city, address)
VALUES ('Central Hospital', 'Quito', 'Av. Principal 123');

-- Insert default admin user (password: admin123)
INSERT INTO users (dni, email, password, gender, first_name, last_name, enabled, center_id)
VALUES (
           '1500903685',
        'josuegarcab2@hotmail.com',
           crypt('admin123456789', gen_salt('bf')),
           'MALE',
           'System',
           'Admin',
           TRUE,
           1
       );

-- Link user to ADMIN role
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);


-- Insert new doctor user
INSERT INTO users (dni, email, password, gender, first_name, last_name, enabled, center_id)
VALUES (
           'doctor001',
        'doctor@hotmail.com',
           crypt('doctor123', gen_salt('bf')),
           'MALE',
           'Juan',
           'Pérez',
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
-- Nuevo Doctor 2
-- =========================
INSERT INTO users (dni, email, password, gender, first_name, last_name, enabled, center_id)
VALUES (
           'doctor002',
           'doctor2@example.com',
           crypt('doctor234', gen_salt('bf')),
           'MALE',
           'Carlos',
           'García',
           TRUE,
           1
       );

-- Asignar rol DOCTOR al usuario (user_id = 3)
INSERT INTO users_roles (user_id, role_id) VALUES (3, 2);

-- Crear doctor vinculado a la especialidad 1
INSERT INTO doctors (user_id, specialty_id)
VALUES (3, 1);


-- =========================
-- Nuevo Doctor 3
-- =========================
INSERT INTO users (dni, email, password, gender, first_name, last_name, enabled, center_id)
VALUES (
           'doctor003',
           'doctor3@example.com',
           crypt('doctor345', gen_salt('bf')),
           'FEMALE',
           'María',
           'López',
           TRUE,
           1
       );

-- Asignar rol DOCTOR al usuario (user_id = 4)
INSERT INTO users_roles (user_id, role_id) VALUES (4, 2);

-- Crear doctor vinculado a la especialidad 1
INSERT INTO doctors (user_id, specialty_id)
VALUES (4, 1);


-- =========================
-- Nuevo Doctor 4
-- =========================
INSERT INTO users (dni, email, password, gender, first_name, last_name, enabled, center_id)
VALUES (
           'doctor004',
           'doctor4@example.com',
           crypt('doctor456', gen_salt('bf')),
           'MALE',
           'Luis',
           'Martínez',
           TRUE,
           1
       );

-- Asignar rol DOCTOR al usuario (user_id = 5)
INSERT INTO users_roles (user_id, role_id) VALUES (5, 2);

-- Crear doctor vinculado a la especialidad 1
INSERT INTO doctors (user_id, specialty_id)
VALUES (5, 1);


INSERT INTO medical_consultations
(patient_id, doctor_id, center_id, "date", diagnosis, treatment, notes, deleted, created_at, updated_at)
VALUES
-- Doctor 1 (Juan Pérez)
(1, 1, 1, '2025-09-21 10:00:00', 'Flu', 'Rest and hydration', 'Mild fever, cough', FALSE, now(), now()),
(2, 1, 1, '2025-09-22 11:30:00', 'Headache', 'Painkillers', 'Follow up if symptoms persist', FALSE, now(), now()),
(3, 1, 1, '2025-09-23 09:00:00', 'Routine check', 'No treatment required', 'Healthy, no issues', FALSE, now(), now()),

-- Doctor 2 (Carlos García)
(4, 2, 1, '2025-09-24 14:15:00', 'Throat infection', 'Antibiotics', 'Sore throat, prescribed amoxicillin', FALSE, now(), now()),
(5, 2, 1, '2025-09-25 08:45:00', 'Post-surgery control', 'Rest and wound cleaning', 'Stitches healing correctly', FALSE, now(), now()),
(1, 2, 1, '2025-09-25 09:30:00', 'Back pain', 'Physiotherapy', 'Recommended 2 weeks of sessions', FALSE, now(), now()),

-- Doctor 3 (María López)
(2, 3, 1, '2025-09-26 10:00:00', 'Seasonal allergy', 'Antihistamines', 'Watery eyes, sneezing', FALSE, now(), now()),
(3, 3, 1, '2025-09-27 12:00:00', 'Stomach ache', 'Diet changes', 'Recommended hydration and probiotics', FALSE, now(), now()),

-- Doctor 4 (Luis Martínez)
(4, 4, 1, '2025-09-28 15:30:00', 'Hypertension', 'Lifestyle changes + medication', 'Prescribed ACE inhibitors', FALSE, now(), now()),
(5, 4, 1, '2025-09-29 16:00:00', 'Diabetes follow-up', 'Insulin dosage adjustment', 'Reviewed glucose levels', FALSE, now(), now()),
(1, 4, 1, '2025-09-30 09:45:00', 'Bronchitis', 'Cough syrup and antibiotics', 'Persistent cough, lungs checked', FALSE, now(), now());

