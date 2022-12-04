# RESP to HTTP Spec
## Types

## Commands

### SET
RESP
```redis
SET foo bar
```

HTTP Request
```
POST /set/foo

bar
```

HTTP Response
```json
{
    "command":"SET",
    "success": true,
    "data":"OK"
}
```

### GET
RESP
```redis
GET foo
```

HTTP Request
```
GET /get/foo
```

HTTP Response
```json
{
    "command":"GET",
    "success": true,
    "data":"bar"
}
```



