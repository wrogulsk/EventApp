Event Management App
<img width="2440" height="1526" alt="image" src="https://github.com/user-attachments/assets/56f136c8-5782-4bec-a813-ba7f849887d0" />


A comprehensive Spring Boot web application for managing events, registrations, and notifications. Built as part of the Coderslab Java Course.

📋 Table of Contents
Features
Tech Stack
Getting Started
API Endpoints
Database Schema
Screenshots
Contributing

✨ Features
🎯 Core Functionality
Event Management: Create, view, edit, and delete events
User Registration: Secure user authentication and role-based access
Event Registration: Users can register for events with capacity limits
Real-time Notifications: Automatic notifications for registrations, cancellations, and comments
Comment System: Users can comment on events
Role-based Access Control: Admin, Organizer, and Participant roles

🔧 Technical Features
RESTful API: Full REST API for frontend integration
MVC Architecture: Traditional web interface with Thymeleaf
Transaction Management: Consistent data handling with Spring transactions
Security: Spring Security with form-based authentication
Data Validation: Input validation and error handling
Responsive Design: Mobile-friendly interface

🛠 Tech Stack
Backend
Java 17+
Spring Boot 3.x
Spring MVC - Web framework
Spring Data JPA - Data persistence
Spring Security - Authentication & authorization
Hibernate - ORM framework
Frontend
Thymeleaf - Server-side templating
HTML5/CSS3
Bootstrap - Responsive design
JavaScript - Interactive features
Database
MySQL - Production database
H2 - Development/testing database
Tools
Maven - Dependency management
Git - Version control
IntelliJ IDEA - IDE

🚀 Getting Started
Prerequisites
Java 17 or higher
Maven 3.6+
MySQL 8.0+ (or use H2 for development)
Git

Installation
Clone the repository
Configure Database
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/eventapp
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

Access the Application
Web Interface: http://localhost:8080

🔗 API Endpoints
Events
Registrations
Notifications
Comments

🗄 Database Schema
Core Entities
Users - User accounts with roles
Events - Event information and details
Registrations - User event registrations
Notifications - System notifications
Comments - Event comments
Roles - User role definitions

Key Relationships
User → Events (One-to-Many) - User can create multiple events
User → Registrations (One-to-Many) - User can register for multiple events
Event → Registrations (One-to-Many) - Event can have multiple registrations
User → Notifications (One-to-Many) - User can receive multiple notifications

🎨 Screenshots
<img width="2380" height="1628" alt="image" src="https://github.com/user-attachments/assets/cd986222-91d0-4001-b0b2-e893b5bedb9b" />


Event Details
<img width="2374" height="1426" alt="image" src="https://github.com/user-attachments/assets/e9989f92-6cc4-45ec-8d2b-9003e6aab837" />


Admin Location Dashboard
<img width="2382" height="1634" alt="image" src="https://github.com/user-attachments/assets/70907201-7654-423b-b1cd-7e16844136aa" />


🔐 Security Features
Password Encryption: BCrypt hashing
CSRF Protection: Cross-site request forgery prevention
Role-based Access: Different permissions for Admin/Organizer/Participant
Session Management: Secure session handling
Input Validation: SQL injection and XSS prevention

🤝 Contributing
Fork the repository
Create a feature branch (git checkout -b feature/amazing-feature)
Commit your changes (git commit -m 'Add amazing feature')
Push to the branch (git push origin feature/amazing-feature)
Open a Pull Request

📊 Project Status
🚀 Status: Completed
📅 Last Updated: December 2024
🎯 Course: Coderslab Java Developer Course

🔄 Future Enhancements
 Email notifications
 Event categories and filtering
 Payment integration
 Mobile app (React Native)
 Advanced reporting dashboard
 Social media integration







