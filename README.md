# bipper-server

This is a small server application made for a hackaton. It managed the backend of an application which allowed users to report problems in public infrastructure and anonymously vote on problems they deem the most important. The application is dockerized.

## Routes

EVERY route is expected to respond with 500 if an internal error happens.

### /login GET 
Login/register and get a token

Can send a request first without locationID, ot check if user exists.

Receives:
```json
{
	"userID": "int",
	"verification": "int",
	"locationID": "int" -> only needed the first time. 
}
```
Returns:
```json
{
    "token": "string"
}
```
Errors:
- 400: Invalid phone format, verification format or request body.
- 401: Invalid verification code.
- 404: User not registered.

### /locations GET 
Get list of available loacations

Returns:
```json
{
  "districts": [
    {
      "name": "string",
      "counties": [
        {
          "name": "string",
          "zones": [
            {
              "name": "string",
              "locationID": "int"
            }
          ]
        }
      ]
    }
  ]
}
 ```

### /posts POST
Create a post

Receives:
```json
{
  "token": "string",
  "text": "string",
  "image": "string",
  "imageType": "string", -> "gif", "png" ou "jpg"
  "locationID": "int"

}
 ```
 Errors:
 - 400: Invalid request body.
 - 403: Posting too fast.

### /posts/new GET
Get new, unread posts

Receives:
```json
{
  "token": "string",
  "locationID": "int" -> posts location
}
```
Returns:
```json
[
  {
    "postID": "int",
    "score": "int",
    "date": "jun 21, 2021",
    "text": "string",
    "image": "string"
  }
]
```

Errors:
- 400: Invalid request body.

### /posts/top/(all-time | year | month | week) GET
Get top posts
Optional:
```json
{
  "locationID": "int" -> posts location
}
```
Returns:
```json
[
  {
    "postID": "int",
    "score": "int",
    "date": "jun 21, 2021",
    "text": "string",
    "image": "string" 
    "imageType": "string" 
  }
]
```
Errors:
- 400: Invalid time period
- 405: Invalid method type.
 
### /vote/(up | down) POST
Vote for a post

Receives:
```json
{
  "token": "string",
  "postID": "int"
}
```

Errors: 
- 400: Invalid request body or vote type.
- 401: User is original author.
- 404: Post does not exist.
- 405: Invalid method type.

Notes: 
- duplicate votes are ignored

### /score POST
Get a users score

Receives:
```json
{
  "token": "string",
}
```

Responds:
```json
{
  "userScore": "int",
}
```

Errors: 
- 400: Invalid request body or token.
- 401: User token not authorized or expired.
- 404: user does not exist.
- 405: Invalid method type.

### /user_location GET
Get current users location

Receives:
```json
{
  "token": "string",
}
```

Responds:
```json
{
  "locationID": "int",
}
```

Errors: 
- 400: Invalid request body.
- 404: Invalid location id.

### /user_location POST
Set users location

Receives:
```json
{
  "token": "string",
  "locationID": "int"
}
```

Errors: 
- 400: Invalid request body or token.
- 401: User token not authorized or expired.
- 404: Invalid location id.

