🐾 PawTrack Management System 2.0

<p align="center">
A modern, feature-rich desktop application built entirely with Java Swing for managing pet adoption, veterinary appointments, and internal administration.
</p>

✨ Features and Highlights

PawTrack stands out with its modern, custom-drawn Java Swing UI, featuring gradients, dynamic shadows, and animations to deliver a professional desktop experience.

Feature Area

Key Modules

Highlight

Unified Dashboard

Dashboard.java

Single-window application with an animated sidebar and header, acting as the control center for all features.

Predictive Search

Dashboard.java

Intelligent search implemented across the header for quick lookup of pets, categories, or functional keywords (e.g., searching "vet" navigates to the Appointment scheduler).

Pet Management

PawManagement.java

Displays adoptable animals in modern, responsive cards, with data fetched directly from the MySQL database.

E-Commerce/Shop

PetShop.java

A fully contained virtual marketplace simulator with inventory, filter management, and a robust cart system.

Core Functions

AdoptionForm.java, VetAppointment.java

Dedicated, detailed modules for submitting new pet profiles for adoption and scheduling vet visits with professional vet profiles.

Security & Accounts

PawTrackLogin.java, CreateAccount.java

Secure login and account creation with database integration and smooth UI/UX.

🚀 Getting Started

To run this application, you need Java installed and a configured MySQL database.

1. Prerequisites

Java Development Kit (JDK 17 or higher)

MySQL Server (Local or Cloud instance)

MySQL Connector/J (JDBC Driver JAR file). You must download this file and add it to your project's classpath/build path.

2. Database Configuration

The application uses the centralized DBConnector.java file to establish a database connection.

Update Credentials: Edit src/DBConnector.java with your database connection information:

// src/DBConnector.java

private static final String DB_HOST = "your_mysql_host";
private static final String DB_PORT = "your_port"; 
private static final String DB_NAME = "your_database_name"; 
private static final String DB_USER = "your_username"; 
private static final String DB_PASS = "YOUR_SECURE_PASSWORD"; // ⚠️ MUST BE UPDATED

// ...


Create Necessary Tables: The following tables are minimally required for core functionality to work (especially PawManagement):

-- Table for User Accounts
CREATE TABLE user_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20),
    email VARCHAR(100) UNIQUE
);

-- Table for Pet Listings/Management
CREATE TABLE pets_accounts (
    pet_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    age VARCHAR(20),
    breed VARCHAR(100),
    health_status VARCHAR(100),
    contact_number VARCHAR(20),
    personal_traits TEXT,
    reason_for_adoption TEXT,
    image LONGBLOB, -- Used to store pet images
    status VARCHAR(50) DEFAULT 'available' 
);
