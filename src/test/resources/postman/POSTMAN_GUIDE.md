# Log Analyzer REST API - Postman Collection Guide

## Base URL
```
http://localhost:8080
```

---

## Endpoint 1: Search and Analyze from Environment

### Request Details
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/logs/search-and-analyze-env`
- **Content-Type:** `application/x-www-form-urlencoded` OR Query Parameters

### Option A: Using Query Parameters (Recommended)

```
POST http://localhost:8080/api/logs/search-and-analyze-env?env=TST&query=Find+critical+errors&logLevel=ERROR&days=7&applicationName=user-service&repoLink=https://github.com/example/myapp
```

### Option B: Form Data in Postman

| Key | Value | Required |
|-----|-------|----------|
| env | TST | Yes (default) |
| query | Find critical errors | Yes (default) |
| logLevel | ERROR | No |
| days | 7 | No |
| applicationName | user-service | No |
| repoLink | https://github.com/example/myapp | No |

### Sample cURL Request
```bash
curl -X POST "http://localhost:8080/api/logs/search-and-analyze-env?env=TST&query=Find+critical+errors&logLevel=ERROR&days=7&applicationName=user-service&repoLink=https://github.com/example/myapp"
```

### Postman Steps:
1. Set Method: **POST**
2. Set URL: `http://localhost:8080/api/logs/search-and-analyze-env`
3. Go to **Params** tab and add:
   - env: `TST`
   - query: `Find critical errors`
   - logLevel: `ERROR`
   - days: `7`
   - applicationName: `user-service`
   - repoLink: `https://github.com/example/myapp`
4. Click **Send**

---

## Endpoint 2: Search and Analyze Raw Logs

### Request Details
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/logs/search-and-analyze-raw`
- **Content-Type:** `text/plain` (raw log data in body)
- **Query Parameters:** Same as Endpoint 1

### Sample JSON Request Body (Raw Text)

```
2024-01-01 10:00:00 INFO: Application startup successful.
2024-01-01 10:05:00 ERROR: NullPointerException at com.example.UserService.getUser(UserService.java:101)
java.lang.NullPointerException
    at com.example.UserService.getUser(UserService.java:101)
    at com.example.UserController.getUserDetails(UserController.java:45)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
2024-01-01 10:06:00 ERROR: SQLException: Connection timeout
java.sql.SQLException: Connection timeout to database
    at com.example.database.ConnectionPool.getConnection(ConnectionPool.java:234)
2024-01-01 10:10:00 WARN: Deprecated API usage detected in LoginService
2024-01-01 10:15:00 ERROR: OutOfMemoryError: Java heap space
java.lang.OutOfMemoryError: Java heap space
    at java.util.HashMap.resize(HashMap.java:707)
    at com.example.cache.CacheManager.put(CacheManager.java:156)
2024-01-01 10:20:00 INFO: Application gracefully shutdown.
```

### Sample cURL Request
```bash
curl -X POST "http://localhost:8080/api/logs/search-and-analyze-raw?query=Find+critical+errors&logLevel=ERROR&days=1&applicationName=user-service&repoLink=https://github.com/example/myapp" \
  -H "Content-Type: text/plain" \
  -d '2024-01-01 10:00:00 INFO: Application startup successful.
2024-01-01 10:05:00 ERROR: NullPointerException at com.example.UserService.getUser(UserService.java:101)
java.lang.NullPointerException
    at com.example.UserService.getUser(UserService.java:101)
    at com.example.UserController.getUserDetails(UserController.java:45)'
```

### Postman Steps:
1. Set Method: **POST**
2. Set URL: `http://localhost:8080/api/logs/search-and-analyze-raw`
3. Go to **Params** tab and add:
   - query: `Find critical errors`
   - logLevel: `ERROR`
   - days: `1`
   - applicationName: `user-service`
   - repoLink: `https://github.com/example/myapp`
4. Go to **Body** tab:
   - Select **raw**
   - Change dropdown from "JSON" to **Text**
   - Paste the log data above
5. Click **Send**

---

## Expected Response

Both endpoints return analysis results as plain text. Example response:

```
Analysis Results:
Exception,Impacted Class,Details of Exception,Remediation of Code
NullPointerException,com.example.UserService,Null reference at line 101 in getUser() method,Add null check before accessing user object
SQLException,com.example.database.ConnectionPool,Connection timeout to database server,Increase connection pool size or add retry logic with exponential backoff
OutOfMemoryError,com.example.cache.CacheManager,Java heap space exhausted in HashMap resize,Implement cache eviction policy or increase heap size
```

---

## Summary

| Endpoint | Method | URL | Body Type |
|----------|--------|-----|-----------|
| Env Analysis | POST | `/api/logs/search-and-analyze-env` | Query Parameters |
| Raw Log Analysis | POST | `/api/logs/search-and-analyze-raw` | Raw Text |

**Common Query Parameters:**
- `env` - Environment name (TST, UAT, PROD, etc.) - Default: TST
- `query` - Analysis query/instruction - Default: "Find critical errors"
- `logLevel` - Filter by log level (ERROR, WARN, INFO, etc.) - Optional
- `days` - Number of days to analyze - Optional
- `applicationName` - Filter by application name - Optional
- `repoLink` - Repository URL for context - Optional

---

## Testing with Postman Collections

You can import this as a Postman collection or manually create the requests above.
