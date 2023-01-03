# ChatAppBackend

### Description

This is backend for ChatApp.
Using this application people can create their accounts and message with each other in real-time
using WebSocket connection. Application uses AWS S3 to store users profile images. <br /><br />


### Important to run
I'm using PostgreSQL. <br />
To run this application, you have to:
- set profile to "aws", or create your implementation of FileService.
- edit database info in application.yml to match your specification
- set aws.bucket property to your bucket's name <br /><br />

### Other config
- change chat-app.security.jwt.* properties to customize jwt secret and its expiration date
