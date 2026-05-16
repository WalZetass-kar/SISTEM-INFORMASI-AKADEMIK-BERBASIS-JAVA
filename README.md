# 🎓 Sistem Informasi Akademik Berbasis Java

**Project kolaborasi antar tim** — Software House Style

## Tech Stack

| Layer | Teknologi |
|-------|-----------|
| Frontend/Desktop | Java Swing (NetBeans) |
| Backend API | Express.js (Node.js) |
| Database | MariaDB |
| Auth | JWT + bcrypt |
| Version Control | GitHub |

## Struktur Project

```
├── backend/          # Express.js REST API
│   ├── config/       # Database & JWT config
│   ├── controllers/  # Business logic
│   ├── middleware/    # Auth & validation
│   ├── models/       # Database queries
│   ├── routes/       # API routes
│   ├── seeds/        # Sample data
│   └── server.js     # Entry point
├── frontend/         # Java Swing GUI
│   ├── src/main/java/com/siakad/
│   │   ├── views/    # JFrame & JPanel
│   │   ├── services/ # API communication
│   │   ├── utils/    # Config & helpers
│   │   └── Main.java
│   └── pom.xml       # Maven dependencies
└── database/
    └── init.sql      # Schema & seed data
```

## Setup

### 1. Database
```bash
mysql -u root -p < database/init.sql
```

### 2. Backend
```bash
cd backend
cp .env.example .env    # Edit konfigurasi DB
npm install
npm run seed            # Seed data + bcrypt hash
npm run dev             # Start server port 3000
```

### 3. Frontend
```bash
cd frontend
mvn clean compile exec:java -Dexec.mainClass="com.siakad.Main"
```

## API Endpoints

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| POST | `/api/login` | Login |
| POST | `/api/logout` | Logout |
| GET | `/api/mahasiswa` | List mahasiswa |
| POST | `/api/mahasiswa` | Tambah mahasiswa |
| GET | `/api/pembayaran` | List pembayaran |
| POST | `/api/pembayaran` | Input pembayaran |
| GET | `/api/pembayaran/dashboard/stats` | Dashboard |
| GET | `/api/pembayaran/status/:nim` | Cek status |
| PUT | `/api/pembayaran/:id/status` | Verifikasi |
| GET | `/api/laporan` | List laporan |
| POST | `/api/laporan/generate/*` | Generate laporan |

## Login Credentials (Seed)

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Mahasiswa | 2024001 | mhs123 |

## Modul

- **Login & Data Mahasiswa** — Auth JWT, CRUD Mahasiswa
- **Pembayaran & Laporan** — Input UKT, Status, Dashboard, Cetak Laporan
