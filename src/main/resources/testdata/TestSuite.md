# TestCases

| TestID   | TestName      | Description                                 | Execute   | Priority   | AuthRequired   |
|:---------|:--------------|:--------------------------------------------|:----------|:-----------|:---------------|
| TC001    | Get Posts     | Get post details from JSONPlaceholder       | Y         | High       | N              |
| TC002    | Create User   | Create new user using ReqRes API            | N         | High       | Y              |
| TC003    | Get User      | Get user details from ReqRes                | N         | Medium     | Y              |
| TC004    | Get Country   | Get country details from REST Countries API | N         | Medium     | N              |
| TC005    | Get Weather   | Get weather data for London                 | N         | Medium     | Y              |
| TC006    | Get Repo Info | Get GitHub repository information           | N         | Low        | Y              |

# TestData

| TestID   | BaseURL                              | Endpoint                                                    | Method   | Headers                                | RequestBody                           |   ExpectedStatus | ValidationPath   | ExpectedValue          |
|:---------|:-------------------------------------|:------------------------------------------------------------|:---------|:---------------------------------------|:--------------------------------------|-----------------:|:-----------------|:-----------------------|
| TC001    | https://jsonplaceholder.typicode.com | /posts/1                                                    | GET      | Content-Type: application/json         | nan                                   |              200 | title            | sunt aut facere        |
| TC002    | https://reqres.in                    | /api/users                                                  | POST     | Content-Type: application/json         | {"name": "John Doe","job": "QA Lead"} |              201 | $.name           | John Doe               |
| TC003    | https://reqres.in                    | /api/users/2                                                | GET      | Content-Type: application/json         | nan                                   |              200 | $.data.email     | janet.weaver@reqres.in |
| TC004    | https://restcountries.com            | /v3.1/name/germany                                          | GET      | Content-Type: application/json         | nan                                   |              200 | $.[0].capital[0] | Berlin                 |
| TC005    | https://api.openweathermap.org       | /data/2.5/weather?q=London&appid=6aa6cd8c45d248d374aac371cd | GET      | Content-Type: application/json         | nan                                   |              200 | $.name           | London                 |
| TC006    | https://api.github.com               | /repos/octocat/Hello-World                                  | GET      | Accept: application/vnd.github.v3+json | nan                                   |              200 | $.name           | Hello-World            |

# Keywords

| TestID   | Keyword1   | Keyword2      | Keyword3        |
|:---------|:-----------|:--------------|:----------------|
| TC001    | API_CALL   | VERIFY_STATUS | VERIFY_RESPONSE |
| TC002    | API_CALL   | VERIFY_STATUS | VERIFY_RESPONSE |
| TC003    | API_CALL   | VERIFY_STATUS | VERIFY_RESPONSE |
| TC004    | API_CALL   | VERIFY_STATUS | VERIFY_RESPONSE |
| TC005    | API_CALL   | VERIFY_STATUS | VERIFY_RESPONSE |
| TC006    | API_CALL   | VERIFY_STATUS | VERIFY_RESPONSE |

