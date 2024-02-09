# Project 4 Android Example

This is an example Android project to demonstrate how to:

- Change between Android Activities
- Make RESTFul HTTPS requests
- Maintain cookies
- Use ListView with custom row layout
- HTTP is supported by checking /app/src/main/AndroidManifest.xml

## Running this project

This Android app depends on [cs122b-project2-login-cart-example](https://github.com/UCI-Chenli-teaching/cs122b-project2-login-cart-example) as the backend server to work.

Note: You need to make sure your login cart runs at [http://localhost:8080/cs122b_project2_login_cart_example_war](http://localhost:8080/cs122b_project2_login_cart_example_war). 

## About NukeSSLCerts.java

When you access a website through https, the server will send an SSL certificate to the browser for it to verify. We are using self-signed certificates, which will not be trusted by Android applications by default. The application will throw exceptions once it receives such certificate. NukeSSLCerts.java allows us to bypass SSL by accepting all certificates. If you want to learn more about this, see [this blog](https://www.codeproject.com/Articles/826045/Android-security-Implementation-of-Self-signed-SSL)
