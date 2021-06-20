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
    "token": "fFADgtdfhsdhdfhShsadhsHSDhsdhsagdasDgGGdSGShHSDHSHASDH=="
}
```
Errors:
- 400: Invalid phone format, verification format or request body.
- 401: Invalid verification code.

### /locations GET 
Returns:
```json
[
  {
    "locationID": int,
    "district": string,
    "county": string,
    "zone": string
  }
 ]
 ```
