# Loan Service

### Summary

Loan service is a Java17 Spring Boot 3 application serving a REST API with the following operations:

#### 1. Create Loan
- Create a new loan for a given customer, amount, interest rate and number of installments.
#### 2. List Loans
- List loans for a given customer.
#### 3. List Installments
- List installments for a given loan.
#### 4. Pay Loan
- Pay installment for a given loan and amount

All API endpoints require JWT token authorization.

### Prerequisites

Please install a JDK version 17+ distribution into your building environment. Depending on your OS and support choices, here are a few good alternatives:

1. Azul Zulu https://www.azul.com/downloads/?package=jdk#zulu
2. Eclipse Temurin https://adoptium.net/temurin/releases/
3. Oracle HotSpot https://www.oracle.com/java/technologies/downloads/

_Note: This service was only tested with Oracle HotSpot version 17_ 

### Building

Loan service uses maven and its wrapper script. After cloning or unzipping its source directories into your local setup, depending on your OS please follow the following steps within your OS CLI: 

Windows
> mvnw clean install

Linux/MacOS
> chmod +x mvnw<br>
> ./mvnw clean install

### Testing

Maven install task will automatically run all unit tests and if everything goes well, the final output JAR file will have the tested binary code.

### Deployment

In production, once you have the output JAR file in your deployment environment, you can execute the following lines to run the service:

> jwt.secret=<your-base64-encoded-external-jwt-token-server-HMAC-SHA256-secret> java -jar loan-1.0.0.jar

In demonstration mode, you can directly run the app in your building environment using the following maven line:

Windows
> mvnw spring-boot:run

Linux/MacOS
> chmod +x mwnv<br>
> ./mvnw spring-boot:run

_Note: The service will run in demonstration mode by default. If you want to deploy it into production, please set the environment variable `jwt.secret` to a Base64 encoded 32 byte secret. It must contain your jwt token server's HMAC SHA256 key._

### Operations

When run in demonstration mode, the service will generate a random HMAC SHA256 secret and run a test JWT token server endpoint. In demo mode it will automatically create:

1. One Admin User

- Username: admin

2. Two Customer Users

  - Username: first.customer
  - Username: second.customer

Admin user is a supervisor account that can perform operations on behalf of any customer. Customers are Loan service tenants which control their loans in complete isolation. i.e. Loans created by Customer A cannot be seen or modified by Customer B.

#### Generating Tokens
In production, you'll need to set up an external JWT token server. This token server must create tokens with the HMAC SHA256 algorithm. Token components must contain at least the following properties:

- sub: username. e.g. `admin` for the admin user, `first.customer` for the regular user of customer named `First Customer`. i.e. customer username consists of `customer name` `.` `customer surname`.

In demonstration mode, please use the following curl line to create an admin token:

> curl --location 'http://localhost:8080/auth/generateToken' \
--header 'Content-Type: application/json' \
--data '{
"username": "admin",
"password": "secret"
}'<br><br>
> eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczODYyMTEyOSwiZXhwIjoxNzM4NjIyOTI5fQ.57j9IcAvMzuPULI19CDI7IL_HbY6zxXW_1RnSx7SLXw

Using this token, the admin user can access the special `/api/v1/admin/{customerUsername}/loan` API endpoints where a customer username can be specified as a path variable.

_Note: admin user cannot access the regular customer API endpoints located at `/api/v1/loan` because his token will not contain any customer info._

the following curl line will get you a regular user token for a customer account:

> curl --location 'http://localhost:8080/auth/generateToken' \
--header 'Content-Type: application/json' \
--data '{
"username": "first.customer",
"password": "secret"
}'<br><br>
> eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmaXJzdC5jdXN0b21lciIsImlhdCI6MTczODYyMTM2OSwiZXhwIjoxNzM4NjIzMTY5fQ.agK2rZlFffqqSR1VLeKGgTa06MiyfYAYfRIR1jSjeSo

Using this token, the regular customer user can access the regular `/api/v1/loan` endpoints. These endpoints will never determine the customer based on the supplied request content but will parse the `sub` property of the JWT token as the username of the customer user.

#### Creating Loans

Use the following curl line to create a loan:

When using admin user:
> curl --location 'http://localhost:8080/api/v1/admin/<desired-customer-username>/loan' \
--header 'Authorization: Bearer <your-admin-jwt-token>' \
--header 'Content-Type: application/json' \
--data '{
"numberOfInstallments": 6,
"interestRate": "0.2",
"loanAmount": "100"
}'<br><br>
> {
"id": 1,
"installments": [
{
"dueDate": "2025-03-01",
"amount": "₺20,00"
},
{
"dueDate": "2025-04-01",
"amount": "₺20,00"
},
{
"dueDate": "2025-05-01",
"amount": "₺20,00"
},
{
"dueDate": "2025-06-01",
"amount": "₺20,00"
},
{
"dueDate": "2025-07-01",
"amount": "₺20,00"
},
{
"dueDate": "2025-08-01",
"amount": "₺20,00"
}
],
"numberOfInstallments": 6,
"paid": false
}

customer account will be detected via the supplied `<desired-customer-username>` path variable. This endpoint will respond with an HTTP/400 when an unknown `<desired-customer-username>` is supplied:

> curl --location 'http://localhost:8080/api/v1/admin/unknown/loan' \
--header 'Authorization: Bearer <your-admin-jwt-token>' \
--header 'Content-Type: application/json' \
--data '{
"numberOfInstallments": 6,
"interestRate": "0.2",
"loanAmount": "100"
}'<br><br>
> HTTP/400<br>{
"message": "Customer not found using [username: unknown]"
}

When using regular customer user:

> curl --location 'http://localhost:8080/api/v1/loan' \
--header 'Authorization: Bearer <your-regular-customer-user-jwt-token>' \
--header 'Content-Type: application/json' \
--data '{
"numberOfInstallments": 6,
"interestRate": "0.2",
"loanAmount": "100"
}'<br><br>

##### Validations
- Number of installments can only be 6, 9, 12, 24
- Interest rate can be between 0.1 – 0.5
- Customer must have enough limit to get this new loan
##### Facts
- All installments will have same amount. Total amount for loan should be
  amount * (1 + interest rate)
- Due Date of Installments will be the first day of months. The first
  installment’s due date will be the first day of next month.
- On successful creation, customer's used credit limit will be increased by the loan amount.

#### Listing Loans

Use the following curl to list your regular customer account's loans:

> curl --location 'http://localhost:8080/api/v1/loan' \
--header 'Authorization: Bearer <your-regular-customer-user-jwt-token>'<br><br>
> {
"content": [
{
"id": 1,
"installments": [
{
"dueDate": "2025-03-01",
"amount": "20.00",
"paidAmount": "19.48",
"paymentDate": "2025-02-03",
"paid": true
},
{
"dueDate": "2025-04-01",
"amount": "20.00",
"paidAmount": "18.86",
"paymentDate": "2025-02-03",
"paid": true
},
{
"dueDate": "2025-05-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-06-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-07-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-08-01",
"amount": "20.00",
"paid": false
}
],
"numberOfInstallments": 6,
"paid": false
}
],
"page": {
"size": 10,
"number": 0,
"totalElements": 1,
"totalPages": 1
}
}

This API supports pagination query parameters and returns a paginated response. The default page size is 10. When there are multiple pages, you can supply `page=2`, `page=3` as a query parameter to get the other pages. You can also tweak the `pageSize` to get more results in one page. Example:

> curl --location 'http://localhost:8080/api/v1/loan?page=2&pageSize=20' \
--header 'Authorization: Bearer <your-regular-customer-user-jwt-token>'<br><br>
> {
"content": [],
"page": {
"size": 20,
"number": 2,
"totalElements": 0,
"totalPages": 0
}
}

When using an admin JWT token, the required change in the request and its response is similar throughout every loan API endpoint. Please check previous section for details.

#### Listing Installments

Use the following curl to list the installments of one of your regular customer account's loans:

> curl --location 'http://localhost:8080/api/v1/loan/1/installment' \
--header 'Authorization: Bearer <your-regular-customer-user-jwt-token>'<br><br>
> [
{
"dueDate": "2025-03-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-04-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-05-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-06-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-07-01",
"amount": "20.00",
"paid": false
},
{
"dueDate": "2025-08-01",
"amount": "20.00",
"paid": false
}
]

#### Paying Loans

Use the following curl to pay for one of your regular customer account's loans:

> curl --location --request PATCH 'http://localhost:8080/api/v1/loan/1' \
--header 'Authorization: Bearer <your-regular-customer-user-jwt-token>' \
--header 'Content-Type: application/json' \
--data '{
"amount": "60"
}'<br><br>
> {
"numberOfInstallmentsPaid": 2,
"totalAmountSpent": "₺38,34",
"loanPaidCompletely": false
}

Endpoint can pay multiple installments with respect to amount sent with some restriction described below:
- Installments should be paid wholly or not at all. So if installments amount is
  10 and you send 20, 2 installments can be paid. If you send 15, only 1 can be
  paid. If you send 5, no installments can be paid.
- Earliest installment shoul be paid first and if there are more money then you
  should continue to next installment.
- Installments have due date that still more than 3 calendar months cannot be
  paid. So if we were in January, you could pay only for January, February and
  March installments. 