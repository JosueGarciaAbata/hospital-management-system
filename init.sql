-- ================================
-- Database: Hospital Management System
-- ================================

-- Table: Medical Centers
CREATE TABLE medical_centers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL
);

-- Table: Specialties
CREATE TABLE specialties (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Table: Roles
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Table: Users
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- encrypted password
    gender VARCHAR(10),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    center_id INT NOT NULL,
    role_id INT NOT NULL,
    CONSTRAINT fk_center_user FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_user FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- Table: Doctors
CREATE TABLE doctors (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    specialty_id INT NOT NULL,
    CONSTRAINT fk_user_doctor FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_specialty_doctor FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT
);

-- Table: Patients
CREATE TABLE patients (
    id SERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    center_id INT NOT NULL,
    CONSTRAINT fk_center_patient FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE
);

-- Table: Medical Consultations
CREATE TABLE medical_consultations (
    id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    center_id INT NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    diagnosis TEXT,
    treatment TEXT,
    notes TEXT,
    CONSTRAINT fk_patient_consult FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_doctor_consult FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    CONSTRAINT fk_center_consult FOREIGN KEY (center_id) REFERENCES medical_centers(id) ON DELETE CASCADE
);
