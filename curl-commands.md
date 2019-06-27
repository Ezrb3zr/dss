# Testing the API via cURL

### POST
    curl -i -X POST -H "Content-Type: multipart/form-data" -F "file=@C:put/your/file/path/here" http://localhost:8080/storage/documents/

### GET
    curl -i http://localhost:8080/storage/documents/{key}

## PUT
    curl -i -X PUT -H "Content-Type: multipart/form-data" -F "file=@C:put/your/file/path/here" -F key={key} http://localhost:8080/storage/documents/

### DELETE
    curl -X DELETE http://localhost:8080/storage/documents/{key}
    