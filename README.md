# bipper-server

## Routes

EVERY route is expected to respond with 500 if an internal error happens.

### /login GET 
Receives:
```json
{
	"userID": int,
	"verification": int
}
```
Returns:
```json
{
    "token": string
}
```
Errors:
- 400: Invalid phone format, verification format or request body.
- 401: Invalid verification code.

### /locations GET 
Returns:
```json
{
  "districts": [
    {
      "name": string,
      "counties": [
        {
          "name": string,
          "zones": [
            {
              "name": string,
              "locationID": int
            }
          ]
        }
      ]
    }
  ]
}
 ```

### /posts POST
Receives:
```json
{
  "token": string,
  "text": string,
  "image": string,
  "locationID": int

}
 ```
