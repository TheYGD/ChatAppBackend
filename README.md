# ChatAppBackend

### Description

This is backend for ChatApp.
Using this application people can create their accounts and message with each other in real-time
using WebSocket connection. Application uses AWS S3 to store users profile images. 

### Current version on AWS (frontend)
http://jszmidla-chatapp.s3-website.eu-central-1.amazonaws.com/

<br /><br />
### Important to run
I'm using PostgreSQL. <br />
To run this application, you have to:
- edit database info in application.yml to match your specification
- Create your implementation of FileService or set profile to "aws".

### AWS config:
- aws.bucket - your bucket's name 
- aws.accessKey and aws.secretKey
- aws.frontend.origin


### Other config
- change chat-app.security.jwt.* properties to customize jwt secret and its expiration date
