# SmartBook — Installation Guide

SmartBook is a self-hosted appointment booking and management system. Install it on your own Windows computer or Linux server using Docker.

## Requirements

- Windows 10/11 or Linux
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) for Windows, or Docker Engine with Docker Compose for Linux
- Internet connection for licence verification and email delivery
- An SMTP email account for appointment confirmations and reminders

## 1. Configure SmartBook

Copy the example configuration file.

**Windows PowerShell:**
```powershell
Copy-Item .env.example .env
```

**Linux / macOS:**
```bash
cp .env.example .env
```

Open `.env` in a text editor and configure these values:

| Variable | Description |
|---|---|
| `SMARTBOOK_DB_PASSWORD` | A strong database password |
| `SMARTBOOK_DB_ROOT_PASSWORD` | A strong MySQL root password |
| `SMARTBOOK_JWT_SECRET` | A random secret of at least 32 characters |
| `SMARTBOOK_MAIL_HOST` | Your SMTP server hostname |
| `SMARTBOOK_MAIL_PORT` | Your SMTP port, usually 587 |
| `SMARTBOOK_MAIL_USERNAME` | Your SMTP account username |
| `SMARTBOOK_MAIL_PASSWORD` | Your SMTP password or app password |
| `SMARTBOOK_MAIL_FROM` | The sender email address |
| `FRONTEND_URL` | Your SmartBook URL, such as `http://localhost:8082` |

**Licence configuration:** The SmartBook provider configures the Gumroad Product ID in the supplied package. You do not need to enter it.

> Never share your `.env` file. It contains private passwords and application secrets.

## 2. Start SmartBook

Open a terminal inside the `customer-package` folder and run:

```bash
docker compose up -d --build
```

The first installation may take several minutes while Docker downloads and builds the required services.

## 3. Verify Installation

Run:

```bash
docker compose ps
```

The MySQL, backend, and frontend services should show as running or healthy.

## 4. Open SmartBook

For a local installation, open:

```text
http://localhost:8082
```

For a server installation, use the domain configured for your installation.

Register your business account and activate SmartBook using the genuine Gumroad licence key provided with your purchase.

## 5. View Logs

To view backend logs:

```bash
docker compose logs -f backend
```

## 6. Stop SmartBook

Run:

```bash
docker compose down
```

This stops the containers while preserving your database volume.

> **Important:** Do not run `docker compose down -v` unless you intend to permanently delete your stored database data.

## 7. Update SmartBook

When a new version is provided, back up your database and configuration first. Then extract the updated package and run:

```bash
docker compose up -d --build
```

## Support

For installation or licence activation assistance, contact your SmartBook software provider.