# User Administration Application

## Features

- Create new user accounts with validation
- Retrieve all users with pagination
- Deactivate users (soft delete)
- Permanently delete users
- Get users created in the last 24 hours
- Password validation with custom rules
- Input validation
- Error handling and logging

## Database

The application uses an H2 database that stores data in a file within the project directory (`./data/userAdministrationDB`). This file-based storage ensures that data persists between application restarts. The database is configured to automatically update the schema and includes a web-based console for administration.

### Database Console
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:file:./data/userAdministrationDB
- **Username**: sa
- **Password**: password

### Initial Data
For testing purposes, the database is pre-populated with test users:
- Admin User: admin@example.com / Admin123456!
- Test User: test@gmail.com / Test123!
- Example User: user@gmail.com / User123!

## API Endpoints

### Testing with cURL

Here are some example cURL commands to test the API:

1. **Create a new user**
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","password":"SecurePass123!"}'
```

2. **Get all users**
```bash
curl "http://localhost:8080/users?page=0&limit=10"
```

3. **Get users created in the last 24 hours**
```bash
curl "http://localhost:8080/users/createdLastDay?page=0&limit=10"
```

4. **Deactivate a user**
```bash
curl -X PUT http://localhost:8080/users/deactivate/1
```

5. **Delete a user**
```bash
curl -X DELETE http://localhost:8080/users/1
```

### 1. Create a New User

```http
POST /users
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "active": true,
  "createdAt": "2025-11-12T16:30:00Z"
}
```

### 2. Get All Users

```http
GET /users?page=0&limit=10
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number (0-indexed)
- `limit` (optional, default: 10) - Number of users per page

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "active": true,
      "createdAt": "2025-11-12T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 3. Deactivate a User (Soft Delete)

```http
PUT /users/deactivate/{id}
```

**Path Parameters:**
- `id` - ID of the user to deactivate

**Response:**
- `204 No Content` - User deactivated successfully
- `500 Internal Server Error` - Database error

### 4. Delete a User (Permanent)

```http
DELETE /users/{id}
```

**Path Parameters:**
- `id` - ID of the user to delete

**Response:**
- `204 No Content` - User deleted successfully
- `500 Internal Server Error` - Database error

### 5. Get Users Created in the Last 24 Hours

```http
GET /users/createdLastDay?page=0&limit=10
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number (0-indexed)
- `limit` (optional, default: 10) - Number of users per page

**Response (200 OK):**
Same format as Get All Users, but only includes users created within the last 24 hours.

## Validation Rules

- **First Name:** 
  - Required
  - Cannot be blank

- **Last Name:** 
  - Required
  - Cannot be blank

- **Email:** 
  - Required
  - Must be a valid email format
  - Must be unique

- **Password:** 
  - Required
  - Must be at least 4 characters long
  - Must contain at least one uppercase letter
  - Must contain at least one lowercase letter
  - Must contain at least one digit
  - Must contain at least one special character from: @#$%^&+=
  - For users with email ending in "@example.com": must be at least 12 characters long

## Error Responses

### 400 Bad Request

When request validation fails:
```json
{
  "errors": {
    "email": "must be a well-formed email address",
    "password": "Password must be at least 4 characters long and contain..."
  }
}
```

### 500 Internal Server Error

When an unexpected error occurs:
```json
{
  "message": "Failed to delete user with id 2 from data base"
}
```

## Running the Application

1. Ensure you have Java 21 and Maven installed
2. Clone the repository
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
4. The API will be available at `http://localhost:8080`

## Testing

Run the test suite with:
```bash
./mvnw test
```

## Dependencies

- Spring Boot 3.5.7
- Spring Web
- Spring Security
- Spring Validation
- Spring Data JPA
- Spring Retry
- Spring Aspects
- Lombok
- H2 Database (for development)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.