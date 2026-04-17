# my-blog-back-app

Backend learning project for a blog (Spring Framework 6.1+, Java 21, Maven, WAR packaging, no Spring Boot).

Current implemented endpoint:
- `POST /api/posts/{id}` - get one post by id

## Tech Stack
- Java 21
- Spring Web MVC (non-Boot)
- PostgreSQL (runtime DB)
- H2 (tests)
- Maven
- JUnit 5 + Spring Test + MockMvc
- Packaging: WAR
- Container for local run: Jetty (Maven plugin)

## Architecture (first vertical slice)
- `controller` - REST endpoint and exception mapping
- `service` - business logic and orchestration
- `dao` - data access abstraction (`PostDao`) with PostgreSQL JDBC implementation
- `model` - domain objects
- `dto` - API response objects

## Build / Test / Run

### 0) PostgreSQL preparation (one-time)
Create database:
```sql
CREATE DATABASE blog;
```

PowerShell/psql command:
```powershell
psql -h localhost -p 5432 -U postgres -d postgres -c "CREATE DATABASE blog;"
```

If you see `database "blog" does not exist`, it means this step was skipped.
`schema.sql` creates tables inside an existing DB, but it does not create the DB itself.

Default app DB settings are in `src/main/resources/application.properties`:
- url: `jdbc:postgresql://localhost:5432/blog`
- username: `postgres`
- password: `postgres`
- You can override without editing file:
  - `BLOG_DB_URL`
  - `BLOG_DB_USERNAME`
  - `BLOG_DB_PASSWORD`

PowerShell example:
```powershell
$env:BLOG_DB_URL="jdbc:postgresql://localhost:5432/blog"
$env:BLOG_DB_USERNAME="postgres"
$env:BLOG_DB_PASSWORD="your_real_password"
```

On app startup, SQL scripts are applied automatically:
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/data.sql`

### 1) Run tests
```bash
mvn clean test
```

### 2) Build WAR
```bash
mvn clean package
```

WAR will be created in `target/my-blog-back-app-1.0-SNAPSHOT.war`.

### 3) Run locally (Jetty)
```bash
mvn jetty:run-war
```

App will be available at:
- `http://localhost:8080`

## Endpoint example

Request:
```bash
curl -X POST http://localhost:8080/api/posts/1
```

Response:
```json
{
  "id": 1,
  "title": "Название поста 1",
  "text": "Текст поста в формате Markdown...",
  "tags": ["tag_1", "tag_2"],
  "likesCount": 5,
  "commentsCount": 1
}
```

If post is missing:
- status: `404 Not Found`
- response body:
```json
{
  "message": "Post with id 999 was not found"
}
```

## IntelliJ IDEA setup
1. Open project as Maven project.
2. Set SDK to Java 21 (`File -> Project Structure -> Project SDK`).
3. Reload Maven project (Maven tool window -> Reload).
4. Run from Maven tool window:
   - `Lifecycle -> test`
   - `Lifecycle -> package`
5. For local start:
   - Open `Run -> Edit Configurations...`
   - Click `+` (Add new configuration).
   - Select **Maven**.
   - Name: `Run Blog Backend (Jetty)`.
   - `Working directory`: project root (`.../my-blog-back-app`).
   - `Command line`: `jetty:run-war`
   - (Optional) In `Runner` set `JRE` = Java 21.
   - Click `Apply` and `OK`.
   - Select this run config in top-right and click `Run` (or `Debug`).
   - Wait until logs show Jetty started, then open `http://localhost:8080/api/posts/1`.
