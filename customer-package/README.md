# SmartBook — Installation Guide

SmartBook is a self-hosted appointment booking system. Follow the steps below to get it running on your machine or server.

## Requirements

- Windows 10/11 or Linux
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows) or Docker Engine with Docker Compose (Linux)
- Internet connection (for licence verification and email)
- A Gmail account or any SMTP email provider

---

## 1. Configure SmartBook

Copy the example configuration file:

**Windows PowerShell:**
```
Copy-Item .env.example .env
```

**Linux / macOS:**
```
cp .env.example .env
```

Open `.env` in a text editor and fill in your own values:

| Variable | Description |
|---|---|
| `SMARTBOOK_DB_PASSWORD` | A strong password for the database |
| `SMARTBOOK_DB_ROOT_PASSWORD` | A strong root password for MySQL |
| `SMARTBOOK_JWT_SECRET` | A long random secret (minimum 32 characters) |
| `SMARTBOOK_MAIL_USERNAME` | Your Gmail address |
| `SMARTBOOK_MAIL_PASSWORD` | Your [Gmail App Password](https://support.google.com/accounts/answer/185833) |
| `GUMROAD_PRODUCT_ID` | Your SmartBook Gumroad product ID (provided in your purchase email) |
| `FRONTEND_URL` | The URL where SmartBook will be accessed (e.g. `http://localhost:8082`) |

> **Never share your `.env` file.** It contains sensitive credentials.

---

## 2. Start SmartBook

Open a terminal inside the `customer-package` folder and run:

```
docker compose up -d --build
```

The first start may take a few minutes while Docker downloads images and builds the application.

---

## 3. Verify Installation

Check that all services are running:

```
docker compose ps
```

All three services — `mysql`, `backend`, and `frontend` — should show as **running** or **healthy**.

---

## 4. Open SmartBook

Open your browser and go to:

```
http://localhost:8082
```

Register your business account, then activate your Gumroad licence key when prompted.

---

## 5. View Logs

To watch live backend logs:

```
docker compose logs -f backend
```

---

## 6. Stop SmartBook

```
docker compose down
```

This stops the containers but **preserves your database**. Your data is safe.

> ⚠️ **Warning:** Do NOT run `docker compose down -v` unless you want to permanently delete all data.

---

## 7. Update SmartBook

When a new version is available, download the updated package and run:

```
docker compose up -d --build
```

---

## Support

For installation help, contact your SmartBook software provider.
