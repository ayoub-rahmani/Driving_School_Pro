# 🚗 Auto-École Pro (Driving School Manager)

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

**Complete driving school management system with map integration, student tracking, and payment management**

[Features](#-features) • [Screenshots](#-screenshots) • [Tech Stack](#-tech-stack)

</div>

---

## 📋 About

**Auto-École Pro** is a comprehensive desktop application designed to streamline driving school operations. Built with JavaFX, it offers an intuitive interface for managing students, instructors, vehicles, lessons, payments, and generates detailed reports with integrated map functionality.

## ✨ Features

- 👥 **Student Management** - Complete student profiles with personal info, license types, and registration tracking
- 🗓️ **Session Scheduling** - Plan and manage driving lessons with calendar integration and location selection
- 🗺️ **Map Integration** - Interactive map for selecting meeting points with GPS coordinates
- 💳 **Payment Tracking** - Monitor payments, generate invoices, and track outstanding balances
- 📊 **Dashboard & Analytics** - Real-time statistics and visual reports on instructors, students, and workload
- 🚙 **Vehicle Management** - Track driving school vehicles and assign them to sessions
- 👨‍🏫 **Instructor Management** - Monitor instructor availability and scheduling
- 📄 **PDF Generation** - Export student records, receipts, and reports
- 🔐 **Secure Login** - Authentication system with user credentials and "Remember Me" functionality

## 🎯 Tech Stack

- **Language**: Java
- **UI Framework**: JavaFX with custom styling
- **Database**: MySQL for data persistence
- **Maps**: OpenStreetMap/Leaflet integration
- **PDF Library**: iText/Apache PDFBox for document generation
- **Architecture**: MVC pattern with DAO layer

## 📸 Screenshots

<div align="center">

| Login Screen | Help Center |
|-------------|-------------|
| ![Login](screenshots/login.png) | ![Help](screenshots/help.png) |

| Student Management | Dashboard & Statistics |
|-------------------|----------------------|
| ![Students](screenshots/students.png) | ![Dashboard](screenshots/dashboard.png) |

| Payment Tracking | Session Management with Maps |
|-----------------|----------------------------|
| ![Payments](screenshots/payments.png) | ![Sessions](screenshots/sessions.png) |

</div>

## 🚀 Key Highlights

- **Interactive Maps**: Choose lesson meeting points with real-time map visualization
- **Comprehensive Tracking**: Monitor student progress from registration to license acquisition
- **Financial Management**: Complete payment system with receipt generation and balance tracking
- **Data Visualization**: Charts and graphs for instructor workload and category distribution
- **Multi-Category Support**: Handle different license types (A, B, C, etc.)
- **Export Capabilities**: Generate PDF reports and export data for record-keeping

## 💡 What I Learned

- Building complex desktop applications with JavaFX
- Implementing MVC architecture for maintainable code
- Integrating third-party map services into desktop applications
- Database design for multi-entity management systems
- PDF generation and document handling in Java
- Creating responsive and intuitive user interfaces

## 📝 Installation

```bash
# Clone the repository
git clone https://github.com/ayoub-rahmani/Driving_school_Pro.git

# Navigate to project directory
cd Driving_school_Pro

# Import database
mysql -u root -p < database/autoecole.sql

# Configure database connection in config file
# Update database credentials in src/config/Database.java
```

## 🔧 Configuration

Update the database configuration:
```java
// src/config/Database.java
private static final String URL = "jdbc:mysql://localhost:3306/autoecole";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
```

## 📦 Dependencies

- JavaFX 11+
- MySQL Connector/J
- iText or Apache PDFBox
- Map library (OpenStreetMap)
- Java Mail API (for email recovery)

## 👨‍💻 Developer

**Ayoub Rahmani**

- Portfolio: [ayoub-rahmani.github.io](https://ayoub-rahmani.github.io)
- LinkedIn: [ayoub-rahmani-linkêdin](https://www.linkedin.com/in/ayoub-rahmani-linkêdin)
- Email: ayoub.rahmani.dev@gmail.com

---

<div align="center">

⭐ Star this repository if you find it helpful!

</div>