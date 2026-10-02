# Employee Service

This is a small REST API for creating, viewing and searching employee records. It is written using Spring Boot 4 and Java 21. It doesnt need a databse, all employees records are stored in a JSON file in [`data/employees.json`](data/employees.json).

## Minimum Requirements

- Java 21
- Maven 3.9

## Run

```bash
mvn spring-boot:run
```

This runs the application abd the API will be accessible at `http://localhost:8080/employees`. You can stop it with "ctrl + c"

## Test

```bash
mvn test
```

This runs the tests. There is a test file written using Junit. It runs the app on a random port and tests the endpoints without altering the JSOn file storing the employee records.

## API endpoints

| Method | Path              | Result                                                                                                    |
| ------ | ----------------- | --------------------------------------------------------------------------------------------------------- |
| `POST` | `/employees`      | This will create an employee and return their ID.                                                         |
| `GET`  | `/employees/{id}` | Returns the employee having the ID that is passed. If there is no employee having that ID it reurns `404` |
| `GET`  | `/employees`      | Returns all employees if no filters are passed. See the Filters section below.                            |

### Creating an employee

Pass a JSON body in this format to create an employee:

```json
{
  "firstName": "Ali",
  "lastName": "Hassan",
  "dateOfBirth": "1995-06-15",
  "salary": "1500",
  "joinDate": "2024-01-10",
  "department": "IT"
}
```

- Every field is required.
- `dateOfBirth` must be in the past.
- `salary` must be 0 or more. Otherwise the API returns `400`. It is passed as a String.
- Dates use the `YYYY-MM-DD` format.
- The server will auto increment the `id`, any `id` passed is just ignored.

### Searching

Filters are optional

| Parameter    | Matches                                                    |
| ------------ | ---------------------------------------------------------- |
| `name`       | First or last name containing this text (case-insensitive) |
| `fromSalary` | Salary greater than or equal to this value                 |
| `toSalary`   | Salary less than or equal to this value                    |

Example: `GET /employees?name=moh&fromSalary=500&toSalary=1500`

## Configuration

| Property         | Default               | Purpose                                    |
| ---------------- | --------------------- | ------------------------------------------ |
| `employees.file` | `data/employees.json` | Where employees are read from and saved to |

Override it at startup:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--employees.file=/path/to/employees.json
```

The records in this file persist even after a restart of the application.

## Project layout

```
pom.xml
data/employees.json                                   sample data
src/main/java/com/exam/employee/
  EmployeeServiceApplication.java                     starts the app
  EmployeeController.java                             HTTP endpoints, request validation
  EmployeeService.java                                search filtering
  EmployeeRepository.java                             reads and writes the JSON file, auto increments IDs
  Employee.java                                       the employee record and its validation rules
src/test/java/com/exam/employee/
  EmployeeServiceApplicationTests.java                end-to-end test of all endpoints
```

The requests flow from controller → service → repository.

## Improvements

This is the minimum reuirements that was asked for, it is not production ready. I thought to map out any improvements I can think of in here rather than building all of it.

### Security

1. **Authentication and Authorization.**

Right now none exist, anyone can access this API and access all employee records, or add fake employees.

- We can use Spring security and OAuth2 for example to put this service behind single sign on with MFA.
- We can check roles for each endpoint using `@PreAuthorize`. For example: allow HR admins to add employees, HR staff can read everything, and every employee can access their record only.
- Deny all by default, and explicitly list whats allowed. Whitelisting. So if a new endpoint is added later everyone has no access to it by default.

2. **Hide sensitive fields by role like `salary` and `dateOFBirth`.**
3. **Only roles that can see salaries should be able to use the salary filters. Otherwise multiple calls can help someone pinpoint the salary of someone by decreasing the range.**
4. **Serve HTTPS only.**
5. **Keep an audit log that cant be edited. Without mentioning any sensitive info in them like salaries**
6. **Limit input.**
   - Names: maximum length (`@Size`) and letters only (`@Pattern` with `\p{L}` so Arabic names still pass).
   - Names can only contain letters and restrict length too
   - Department should be an enum so nothing is passed at random
   - use `@Digits` and `@DecimalMax` to restrict what can be sent for salaries
   - A maximum size for the request body
7. **Rate limit requests**
8. **Use random UUIDs instead of sequential IDs.**

### Data and storage

- **Replace the JSON file with a database. It should have encryption and backups and uses database transactions.**

### API design

- **Add update and delete endpoints** (`PUT` / `PATCH` / `DELETE /employees/{id}`).
- **Add pagination**
- **Better error messages to esplain what is wrong**
- **Support full-name search. Passing this filter `?name=mohamed ahmed` currently returns no one, even if an employee exists with that first and last name.**
- **Version the API with a v1 prefix for example**
- **Add a currency to salaries**

### Validation

- Employees must be at least 18.
- Reject unknown JSON fields. Currently extra fields are silently ignored.
