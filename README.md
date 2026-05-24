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

### 0. Prasyarat

Pastikan tools berikut sudah terpasang:

| Tool | Kebutuhan |
|------|-----------|
| Node.js + npm | Backend Express.js |
| MariaDB/MySQL Server | Database `siakad_db` |
| MariaDB/MySQL Client | Import file SQL dari terminal |
| JDK 17 | Menjalankan frontend Java Swing |
| Maven | Build dan run frontend |

Untuk Ubuntu/Debian:

```bash
sudo apt update
sudo apt install -y nodejs npm mariadb-server mariadb-client openjdk-17-jdk maven
```
`
Catatan: di mesin ini Node.js dan npm sudah tersedia, tetapi Java, Maven, dan MariaDB/MySQL client belum terdeteksi di terminal.

### 1. Konfigurasi Backend

File environment development sudah disiapkan di:

```bash
backend/.env
```

Default konfigurasi lokal:

```env
PORT=3000
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=
DB_NAME=siakad_db
```

Jika MariaDB/MySQL root kamu memakai password, ubah `DB_PASSWORD` di `backend/.env`.

### 2. Database

Pastikan service database aktif:

```bash
sudo systemctl enable --now mariadb
sudo systemctl status mariadb
```

Import schema dan data awal:

```bash
mysql -u root -p < database/init.sql
```

Jika user root MariaDB tidak memakai password:

```bash
mysql -u root < database/init.sql
```

### 3. Backend

Dependency backend sudah bisa dipasang dengan:

```bash
cd backend
npm install
npm run seed            # Seed data + bcrypt hash
npm run dev             # Start server port 3000
```

Verifikasi backend:

```bash
curl http://localhost:3000/api
```

### 4. Frontend

Pastikan backend sudah berjalan di `http://localhost:3000`, lalu jalankan:

```bash
cd frontend
mvn clean compile exec:java -Dexec.mainClass="com.siakad.Main"
```

Jika memakai NetBeans, buka folder `frontend/` sebagai Maven Project, lalu jalankan class:

```text
com.siakad.Main
```

### 5. Urutan Menjalankan Project

1. Install prasyarat sistem.
2. Aktifkan MariaDB/MySQL.
3. Import `database/init.sql`.
4. Sesuaikan `backend/.env`.
5. Jalankan `npm install` di folder `backend/`.
6. Jalankan `npm run seed`.
7. Jalankan `npm run dev`.
8. Jalankan frontend dari Maven atau NetBeans.

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
