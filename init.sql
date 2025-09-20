-- ================================
-- Database: Hospital Management System
-- ================================

-- Table: Medical Centers
CREATE TABLE medical_centers (
                                 id BIGSERIAL PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL,
                                 city VARCHAR(100) NOT NULL,
                                 address VARCHAR(200) NOT NULL
);

-- Table: Specialties
CREATE TABLE specialties (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,
                             description TEXT
);

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
                          center_id BIGINT NOT NULL,
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
