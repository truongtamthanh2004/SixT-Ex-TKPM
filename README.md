# Student Management System

A Java Spring Boot application for managing student records with Redis caching and distributed locking.  

## Technologies
- Java
- Spring Boot
- Spring Data JPA/Hibernate
- Redis (for caching and distributed locks)
- Maven
- Docker

## Features
- **Student CRUD Operations**: Create, read, update, and delete student records
- **Distributed Locking**: Prevents race conditions using Redisson's read-write locks
- **Caching**: Improves performance by caching student data in Redis
- **Search Functionality**: Search students by ID or full name
- **Data Validation**: Prevents duplicate student IDs and emails

## Architecture

### Models
#### `StudentEntity`
Represents a student record with attributes like:
- **Student ID**
- **Full name**
- **Email**
- **Birthday**
- **Gender**
- **Department**
- **Course/Program details**
- **Contact information**
- **Status**

### Services
#### `StudentServiceImpl`
Implements CRUD operations with:
- Redis caching
- Distributed locking
- Transaction management
- Error handling

## Key Implementation Details
- **Data Integrity**: Uses Redis distributed locks to prevent concurrent modifications
- **Performance Optimization**: Implements caching with Spring's `@CacheEvict`
- **Database Indexing**: Optimizes queries with indexes on `studentId` and `email`
- **Serialization**: Entities implement `Serializable` for Redis storage
- **Enum Types**: Uses enum types for `Gender`, `Department`, and `StudentStatus`

## Database Design
Student records are stored in a `students` table with:
- **Auto-incremented primary key**
- **Unique constraints on `studentId` and `email`**
- **Indexed fields for faster lookups**
- **Timestamps for creation and updates**

## Source Code Structure
```
sixt/
│── src/
│   ├── main/
│   │   ├── java/com/example/sixt/
│   │   │   ├── commons/ 
│   │   │   ├── configs/
│   │   │   ├── controllers/
│   │   │   ├── exceptions/ 
│   │   │   ├── models/
│   │   │   ├── repository/
│   │   │   ├── services/
│   │   │   │   ├── impl/
│   │   │   ├── SixtApplication.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   ├── test/
│── Dockerfile
│── docker-compose.yml
│── pom.xml
│── README.md
```

## How to Run the Project

### Prerequisites
- Git
- Docker and Docker Compose
- JDK 17+
- Maven (optional, if not using Docker)

### Setup and Run
#### Clone the repository:
```sh
git clone https://github.com/truongtamthanh2004/SixT-Ex-TKPM.git
```
#### Navigate to the project directory:
```sh
cd SixT-Ex-TKPM
```
#### Build and run with Docker Compose:
```sh
docker-compose up -d --build
```
#### Access the application:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## API Endpoints
| Method | Endpoint | Description |
|--------|---------|-------------|
| **POST** | `/api/v1/students/add` | Create a new student |
| **PUT** | `/api/v1/students/update/{studentId}` | Update a student |
| **DELETE** | `/api/v1/students/delete/{studentId}` | Delete a student |
| **GET** | `/api/v1/students/search/{keyword}` | Search students by Student ID or Name |

# Version 2.0

## Overview
This project provides APIs for importing and exporting data in CSV and JSON formats, along with various functionalities for managing department data.

## API Endpoints

### Import CSV
```http
POST /import/csv
```
**Description:** Imports department data from a CSV file.

**Request:**
- `file` (MultipartFile) - The CSV file to be uploaded.

**Response:**
```json
{
  "status": "200",
  "message": "Import CSV successfully!",
  "data": 1
}
```

### Import JSON
```http
POST /import/json
```
**Description:** Imports department data from a JSON file.

**Request:**
- `file` (MultipartFile) - The JSON file to be uploaded.

**Response:**
```json
{
  "status": "200",
  "message": "Import JSON successfully!",
  "data": <number_of_records>
}
```

### Export JSON
```http
GET /export/json
```
**Description:** Exports all department data into a JSON file.

**Response:**
- Returns a downloadable JSON file containing department data.

### Export CSV
```http
GET /export/csv
```
**Description:** Exports all department data into a CSV file.

**Response:**
- Returns a downloadable CSV file containing department data.

### Add, modify name of departure, program, student status
```http
POST /
PUT /{id}
```
**Description:** add, modify name of departure, program and student status
---

## Feature Demonstration

Below are screenshots showcasing the implemented features:

![Import CSV Feature](images/import_csv.png)
![Import JSON Feature](images/import_json.png)
![Export JSON Feature](images/export_json.png)
![Export CSV Feature](images/export_csv.png)

For detailed usage and setup, refer to the full documentation.
