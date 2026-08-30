# Niramaya — Smart Hospital Platform (Java)

A hospital website with a **Java backend** (no Spring/Maven required — pure JDK)
and an HTML/CSS/JS frontend styled around your logo.

## What's included

```
hospital-java-app/
├── src/main/java/com/hospital/Server.java   # Java backend (HTTP server)
├── public/
│   ├── index.html          # Home page — hero, services, doctors, testimonials
│   ├── login.html          # Role-based login (Patient / Doctor / Admin)
│   ├── dashboard.html      # Dashboard — KPIs, chart, appointments table
│   ├── css/style.css       # Full design system (glassmorphism, blue/teal theme)
│   ├── js/app.js           # Small shared UI helper
│   └── assets/logo.png     # Your uploaded logo
└── README.md
```

## Requirements

You need a **full JDK** (not just a JRE) — `javac` must be available.
Check with:

```bash
javac -version
```

If you only have a JRE, install a JDK (e.g. `sudo apt install openjdk-21-jdk`
on Ubuntu, or download from https://adoptium.net).

## Run it

From the `hospital-java-app` folder:

```bash
# 1. Compile
javac -d out src/main/java/com/hospital/Server.java

# 2. Run (serves the "public" folder, so run this from hospital-java-app/)
java -cp out com.hospital.Server
```

Then open **http://localhost:8080** in your browser.

## Demo logins

| Role    | Email                       | Password  |
|---------|------------------------------|-----------|
| Patient | patient@niramaya.health       | demo1234  |
| Doctor  | doctor@niramaya.health         | demo1234  |
| Admin   | admin@niramaya.health          | demo1234  |

Pick the role tab on the login page, use the matching demo credentials, and
you'll land on the dashboard with that role's name shown in the header.

## How the backend works

`Server.java` uses only the JDK's built-in `com.sun.net.httpserver.HttpServer`
— no Maven, no Spring Boot, no internet connection needed to build it:

- **Static file serving** — everything in `public/` is served as-is (with
  path-traversal protection).
- **`POST /api/login`** — checks email/password against an in-memory user
  map and returns `{ success, role, name }` as JSON.
- **`GET /api/dashboard-stats`** — returns a small JSON series that powers
  the revenue/appointments chart on the dashboard.

If the Java server isn't running, `login.html` still works stand-alone: it
falls back to checking the demo credentials directly in the browser, so you
can preview the UI by just opening the HTML files too.

## Growing this into the full system

The current build is a **working core slice** — home page, role-based login,
and one dashboard — matching what's realistic to hand you as a working,
verified project in one pass. The uploaded spec describes a much larger
enterprise system (7 roles, MySQL, Razorpay, WebRTC, pharmacy/lab/billing
modules, etc.). To grow toward that:

1. **Swap in MySQL**: replace the in-memory `USERS` map in `Server.java`
   with JDBC calls (`mysql-connector-j`). Suggested core tables:
   `users, patients, doctors, appointments, medical_records, prescriptions,
   lab_tests, medicines, bills, payments, departments, rooms, admissions`.
2. **Add real password hashing**: use `jBCrypt` or Spring Security's
   `BCryptPasswordEncoder` instead of plaintext demo passwords.
3. **Move to Spring Boot** once you need routing/DI/JPA at scale — the
   current plain-JDK server is intentionally dependency-free so it runs
   anywhere immediately, but a bigger system benefits from Spring Boot +
   Spring Data JPA + Spring Security.
4. **Add the other role dashboards** (Doctor, Receptionist, Lab, Pharmacy,
   Billing) as additional HTML pages reusing `style.css`'s `.dash-shell`
   layout — the sidebar/topbar/KPI components are already built to be reused.
5. **Payments**: integrate Razorpay's Java SDK from a new
   `/api/payments/*` set of endpoints.
6. **Video consultation**: embed the Jitsi Meet iframe API on a new
   `consultation.html` page.

Ask and I can build out any one of these next — happy to keep going module
by module (e.g. "add the Doctor dashboard next" or "wire this up to MySQL").
