# bipper-server

## Routes

EVERY route is expected to respond with 500 if an internal error happens.

### /login GET 
Login/register and get a token

Receives:
```json
{
	"userID": "int",
	"verification": "int"
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
  "index": "int" -> optional last post read
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
  "index": "int" -> optional last post read
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
- 403: User is original author.
- 404: Post or user does not exist.
- 405: Invalid method type.

