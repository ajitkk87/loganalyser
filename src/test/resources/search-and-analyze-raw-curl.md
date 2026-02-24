# search-and-analyze-raw curl test

## Test Objective

Validate that `POST /api/logs/search-and-analyze-raw` accepts raw log content with `repoLink` and `logLevel`, clones/uses the repository context (`RosterMate`), and returns AI analysis as a markdown table.

## Curl Command

~~~bash
curl -i -X POST "http://localhost:8080/api/logs/search-and-analyze-raw?query=Find%20critical%20errors&repoLink=https://github.com/ajitkk87/RosterMate&logLevel=ERROR" -H "Content-Type: text/plain" --data "2026-02-24 12:00:00 ERROR com.rostermate.schedule.ShiftService - Failed to allocate shift due to NullPointerException"
~~~

## HTTP URL

http://localhost:8080/api/logs/search-and-analyze-raw?query=Find%20critical%20errors&repoLink=https://github.com/ajitkk87/RosterMate&logLevel=ERROR

## Request

~~~http
POST /api/logs/search-and-analyze-raw?query=Find%20critical%20errors&repoLink=https://github.com/ajitkk87/RosterMate&logLevel=ERROR HTTP/1.1
Host: localhost:8080
Content-Type: text/plain

2026-02-24 12:00:00 ERROR com.rostermate.schedule.ShiftService - Failed to allocate shift due to NullPointerException
~~~

## Response

HTTP/1.1 200

~~~markdown
| Exception                  | Impacted Class                        | Details of Exception                                         | Remediation of Code                                      |
|----------------------------|---------------------------------------|--------------------------------------------------------------|----------------------------------------------------------|
| NullPointerException       | com.rostermate.schedule.ShiftService  | Failed to allocate shift due to NullPointerException         | Add null checks before shift allocation and log root cause|
~~~

---

# search-and-analyze-raw curl test (local repo path)

## Test Objective

Validate that `POST /api/logs/search-and-analyze-raw` works when `repoLink` is a local repository path (non-git URL) and still returns AI analysis in markdown table format.

## Curl Command

~~~bash
curl -i -X POST "http://localhost:8080/api/logs/search-and-analyze-raw?query=Find%20critical%20errors&repoLink=D%3A%5Cideaws%5Cloganalyser%5Cloganalyser&logLevel=ERROR" -H "Content-Type: text/plain" --data "2026-02-24 12:45:00 ERROR com.local.repo.TestService - IndexOutOfBoundsException while processing employee roster"
~~~

## HTTP URL

http://localhost:8080/api/logs/search-and-analyze-raw?query=Find%20critical%20errors&repoLink=D%3A%5Cideaws%5Cloganalyser%5Cloganalyser&logLevel=ERROR

## Request

~~~http
POST /api/logs/search-and-analyze-raw?query=Find%20critical%20errors&repoLink=D%3A%5Cideaws%5Cloganalyser%5Cloganalyser&logLevel=ERROR HTTP/1.1
Host: localhost:8080
Content-Type: text/plain

2026-02-24 12:45:00 ERROR com.local.repo.TestService - IndexOutOfBoundsException while processing employee roster
~~~

## Response

HTTP/1.1 200

~~~markdown
| Exception                  | Impacted Class           | Details of Exception                                      | Remediation of Code                                  |
|----------------------------|--------------------------|-----------------------------------------------------------|------------------------------------------------------|
| IndexOutOfBoundsException  | com.local.repo.TestService | IndexOutOfBoundsException while processing employee roster | Add bounds checking before accessing list elements    |
~~~
