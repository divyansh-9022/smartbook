# SmartBook

**Your bookings. Your server. Your control.**

SmartBook is a self-hosted appointment booking and management
application designed for independent businesses that want to run their
booking system in their own environment.

## Features

-   Appointment booking and schedule management
-   Customer and service management
-   Business hours and holiday configuration
-   Business dashboard
-   Reminder management
-   Role-based access and secure authentication

> Feature availability may depend on the version included in your
> installation package.

## Technology Stack

-   **Frontend:** React, Vite
-   **Backend:** Java, Spring Boot, Maven
-   **Database:** MySQL 8.4
-   **Security:** Spring Security and JWT
-   **Deployment:** Docker Compose

## Deployment

SmartBook is designed to be self-hosted using Docker Compose.

### Requirements

-   Docker Desktop or Docker Engine with the Docker Compose plugin
-   A computer or server capable of running the application containers
-   Basic familiarity with environment variables and server
    configuration

### Getting started

1.  Obtain the SmartBook customer package.

2.  Extract the package to your computer or server.

3.  Review the included `README` and `.env.example` files.

4.  Create your own `.env` file using `.env.example` as a reference.

5.  Configure the database, application secrets, and any required
    email/SMTP settings.

6.  From the directory containing the Compose file, start the services:

    ``` bash
    docker compose up -d
    ```

7.  Check the service status:

    ``` bash
    docker compose ps
    ```

Use the URLs and ports documented in the customer package. Configure
production networking, HTTPS, backups, and firewall rules before
exposing an installation to the public internet.

**Important:** Never publish your real `.env` file, passwords, JWT
secrets, SMTP credentials, or other private configuration.

## Self-hosting responsibilities

SmartBook is not a centrally hosted service. Each customer is
responsible for their own:

-   Hosting or server
-   Domain name and HTTPS configuration, if required
-   Database and backups
-   SMTP provider and email credentials
-   Server security, updates, and maintenance

Basic Docker and server-configuration knowledge is recommended.

## Licence

The commercial customer licence is **US\$49 one-time** for **one
business and one installation**.

The licence does not grant permission to rebrand, resell, or
redistribute SmartBook. Customers may configure their own installation
for their business, subject to the applicable licence terms.

The public availability of this repository does not grant additional
rights beyond the applicable licence. Review the licence terms before
using, copying, modifying, or distributing the software.

## Customer package

The customer distribution package and its installation documentation are
provided separately. This repository is the project source repository;
use the customer package instructions for a customer installation.

## Contact

For product enquiries and support questions, contact:

**divyansh9022@gmail.com**

------------------------------------------------------------------------

Made by **Divyansh Mishra**.
