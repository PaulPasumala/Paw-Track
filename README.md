🐾 PawTrack Management System 2.0

Project Overview

PawTrack Management System is a robust desktop application built with Java Swing that provides a modern, full-featured platform for managing pet adoption, vet appointments, inventory, and user accounts. The system is designed to connect abandoned and surrendered pets with responsible adopters through an intuitive, aesthetic user interface.

This project uses a MySQL database for persistent storage and leverages advanced Java Swing techniques for a high-quality, responsive desktop UI.

🛠️ Technology Stack

Language: Java (JDK 17+)

Frontend: Java Swing with custom rendering, gradients, and animations

Database: MySQL (Remote Aiven Cloud instance specified in DBConnector.java)

Dependencies: Standard Java libraries (A separate MySQL Connector/J JAR is required for database connectivity).

🚀 Getting Started

1. Database Setup (Crucial!)

This application requires a running MySQL instance and relies on the credentials configured in src/DBConnector.java.

Configure DBConnector.java: Update the following fields in src/DBConnector.java to match your local or cloud MySQL database (Aiven is currently configured):

// src/DBConnector.java
private static final String DB_HOST = "your_mysql_host";
private static final String DB_PORT = "your_port";
private static final String DB_NAME = "your_database_name";
private static final String DB_USER = "your_username";
private static final String DB_PASS = "your_password"; // <-- UPDATE THIS
// ...


Create Tables: You must create the following tables in your configured database.

Table Name

Purpose

Key Columns

user_accounts

Stores login credentials and profile info.

username, password, full_name, email

pets_accounts

Stores details for pets available for adoption.

name, status, image (BLOB)

adoption_applications

(Future Use) For detailed adoption form submissions.

N/A (Needs implementation)

Note: The PawManagement panel relies directly on the pets_accounts table to display images and status.

Add MySQL Connector/J: Ensure the MySQL JDBC driver JAR file is added to your project's build path/classpath.

2. Running the Application

Compile: Compile all .java files located in the src folder.

Run: Execute the main method in src/PawTrackLogin.java to start the application.

💡 Key Features and Components

The system is modular, with each feature implemented as a dedicated Java class and integrated into the main Dashboard.java using a CardLayout.

Feature Module

Class Name

Description

Status

User Authentication

PawTrackLogin.java, CreateAccount.java

Handles user login, sign-up, and password recovery (simulated database access).

Functional

Main Dashboard

Dashboard.java

Core navigation, animated sidebar, global header, and the main card switcher.

Functional

Pet Management

PawManagement.java

Displays adoptable pets in modern cards, fetching status and images from the database.

Functional

Pet Adoption Form

PetAdoptionForm.java, AdoptionForm.java

Detailed forms for prospective adopters and registering new pets.

Functional (DB submission logic in AdoptionForm.java)

Vet Appointments

VetAppointment.java

Comprehensive form for booking pet appointments with rotating vet profiles.

Functional

E-Commerce/Shop

PetShop.java

Full-screen marketplace simulation with inventory, cart management, and checkout logic.

Functional

Information

AboutUs.java, TriviaApp.java

Dedicated panels for team information and pet trivia/facts.

Functional

User Profile

UserProfile.java

Integrated module for viewing and editing user information and profile picture.

Functional

Search Functionality

Dashboard.java

Implements a Unified Predictive Search across pet categories and module features.

Functional

📁 File Structure

The project follows a single src directory structure:

PawTrack/
└── src/
    ├── AboutUs.java             # Team Information Panel
    ├── AdoptionForm.java        # Pet Registration Form (DB submission)
    ├── DBConnector.java         # Database Configuration (Requires Update)
    ├── Dashboard.java           # Primary application window and navigation (FIXED)
    ├── PawManagement.java       # Pet Listing Grid (DB Fetching)
    ├── PawTrackLogin.java       # Main Login Screen
    ├── PetAdoptionForm.java     # Detailed Adoption Application (Placeholder logic)
    ├── PetShop.java             # E-commerce Module
    ├── TriviaApp.java           # Trivia/Facts Module
    ├── UserProfile.java         # Profile Management Module
    ├── VetAppointment.java      # Veterinary Appointment Module
    └── ... (other utility classes)
