<h1 align="center">Event Management App</h1>

<div align="center">
  <img width="800" alt="Event Management App Dashboard" src="https://github.com/user-attachments/assets/56f136c8-5782-4bec-a813-ba7f849887d0" />
</div>

<p align="center">
  <strong>A comprehensive Spring Boot web application for managing events, registrations, and notifications.</strong><br>
  Built as part of the <em>Coderslab Java Course</em>.
</p>

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue?style=for-the-badge&logo=mysql)
![Status](https://img.shields.io/badge/Status-Completed-success?style=for-the-badge)

</div>

---

## 📋 Table of Contents

- [✨ Features](#-features)
- [🛠 Tech Stack](#-tech-stack)
- [🚀 Getting Started](#-getting-started)
- [🔗 API Endpoints](#-api-endpoints)
- [🗄 Database Schema](#-database-schema)
- [🎨 Screenshots](#-screenshots)
- [🔐 Security Features](#-security-features)
- [🤝 Contributing](#-contributing)
- [📊 Project Status](#-project-status)

---

# ✨ Features 

### 🎯 Core Functionality
- **Event Management**: Create, view, edit, and delete events
- **User Registration**: Secure user authentication and role-based access
- **Event Registration**: Users can register for events with capacity limits
- **Real-time Notifications**: Automatic notifications for registrations and cancellations
- **Comment System**: Users can comment on events 
- **Role-based Access Control**: Admin, Organizer and Participant roles (in Frontend part)

# 🔧 Technical Features
- **RESTful API**: Full REST API for frontend integration
- **MVC Architecture**: Traditional web interface with Thymeleaf
- **Transaction Management**: Consistent data handling with Spring transactions
- **Security**: Spring Security with form-based authentication
- **Data Validation**: Input validation and error handling
- **Responsive Design**: Mobile-friendly interface

---

# 🛠 Tech Stack

<table>
<tr>
<td>

**Backend**
- Java 17+
- Spring Boot 3.x
- Spring MVC - Web framework
- Spring Data JPA - Data persistence
- Spring Security - Authentication & authorization
- Hibernate - ORM framework

</td>
<td>

**Frontend**
- Thymeleaf - Server-side templating
- HTML5/CSS3
- Bootstrap - Responsive design
- JavaScript - Interactive features

</td>
</tr>
<tr>
<td>

**Database**
- MySQL - Production database

</td>
<td>

**Tools**
- Maven - Dependency management
- Git - Version control
- IntelliJ IDEA - IDE

</td>
</tr>
</table>

---

# 🚀 Getting Started

### Prerequisites
- ☑️ **Java 17** or higher
- ☑️ **Maven 3.6+**
- ☑️ **MySQL 8.0+**
- ☑️ **Git**


### Installation
1. Clone the repository**
   ```bash
   git clone https://github.com/yourusername/event-management-app.git
   cd event-management-app
2. Configure Database
   #application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/EventApp?useSSL=false
   spring.datasource.username=root
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
4. Build and Run
   mvn clean install
   mvn spring-boot:run
5. Access the Application
🌐 Web Interface: http://localhost:8080
6. In SecutityConfig there are comments how to use Http endpoints without login

# 🔗 API Endpoints
## Events
Get event by ID
GET http://localhost:8080/events/20

Get events by location
GET http://localhost:8080/events/locations/6

Delete event
DELETE localhost:8080/events/delete/15

## Registrations
Get user registration by ID
GET localhost:8080/registrations/user/14

Register for an event
POST localhost:8080/registrations/register?userId=4&eventId=6

## Comments
Get comment by ID
GET http://localhost:8080/comments/3

Search comments by keyword
GET http://localhost:8080/comments/event/2/search?keyword=awesome


# 🗄 Database Schema

### 📊 Core Entities

- 👤 **Users** - User accounts with roles
- 🎪 **Events** - Event information and details  
- 📝 **Registrations** - User event registrations
- 🔔 **Notifications** - System notifications
- 💬 **Comments** - Event comments
- 🛡️ **Roles** - User role definitions

### Key Relationships
- 👤 **User → Events** (One-to-Many) - *User can create multiple events*
- 👤 **User → Registrations** (One-to-Many) - *User can register for multiple events*
- 🎪 **Event → Registrations** (One-to-Many) - *Event can have multiple registrations*
- 👤 **User → Notifications** (One-to-Many) - *User can receive multiple notifications*

---

# 🎨 Screenshots

### 🏠 Dashboard
<div align="center">
  <img width="700" alt="Dashboard" src="https://github.com/user-attachments/assets/cd986222-91d0-4001-b0b2-e893b5bedb9b" />
</div>

### 📋 Event Details
<div align="center">
  <img width="700" alt="Event Details" src="https://github.com/user-attachments/assets/e9989f92-6cc4-45ec-8d2b-9003e6aab837" />
</div>

### ⚙️ Admin Location Dashboard
<div align="center">
  <img width="700" alt="Admin Dashboard" src="https://github.com/user-attachments/assets/70907201-7654-423b-b1cd-7e16844136aa" />
</div>

---

# 🔐 Security Features

- 🔒 **Password Encryption**: BCrypt hashing
- 🛡️ **CSRF Protection**: Cross-site request forgery prevention
- 👥 **Role-based Access**: Different permissions for Admin/Organizer/Participant
- 🔑 **Session Management**: Secure session handling

---

# 🤝 Contributing

1. **Fork** the repository
2. Create a **feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. Open a **Pull Request**

---

# 📊 Project Status

- 🚀 **Status**: Completed
- 📅 **Date**: September 2025  
- 🎯 **Course**: Coderslab Java Developer Course

---

# 🔄 Future Enhancements

- [ ] 📧 Email notifications
- [ ] 📨 Invitations
- [ ] 💳 Payment integration
- [ ] 📱 Mobile app (React Native)
- [ ] 📊 Advanced reporting dashboard
- [ ] 🌐 Social media integration

---

<div align="center">

 ☕ 

</div>




