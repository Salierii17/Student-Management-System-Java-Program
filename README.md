# Student Information Management System

A system to handle student information using CRUD operations (Create, Read, Update, Delete), developed as the final project for the Object-Oriented Visual Programming course.

## Description

This project is designed to manage student details, including personal information and enrollment status, faculty information, and course data. It includes user authentication, allowing secure access to the system. Users can add or delete faculty members and maintain course-related information such as titles and durations.

## Getting Started

### Dependencies

- Java Development Kit (JDK)
- MySQL
- Libraries:
    - Swing
    - jdatepicker
    - MySQL Connector

### Installing

1. **Clone the repository:**
    
    ```
    git clone <https://github.com/yourusername/student-info-management.git>
    cd student-info-management
    
    ```
    
2. **Set up the database:**
    - Install MySQL and create a new database.
    - Import sms.sql file on the root of the folder
    - Update the database configuration in the application with your MySQL database name, username, and password.

### Executing program

1. **Compile the Java files:**
    - Open a terminal or command prompt.
    - Navigate to the directory containing the Java files.
    - Compile the files using:
        
        ```
        javac *.java
        
        ```
        
2. **Run the application:**
    - Execute the main class:
        
        ```
        java MainClass
        
        ```
        
3. **Login:**
    - Enter the database name, username, and password to access the system.
4. **Manage Students:**
    - Add, view, update, or delete student information.
5. **Manage Faculty:**
    - Add or remove faculty members.
6. **Manage Courses:**
    - Maintain course details including titles and durations.
