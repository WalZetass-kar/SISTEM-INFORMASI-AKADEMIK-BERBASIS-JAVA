-- ============================================================
-- SISTEM INFORMASI AKADEMIK - DATABASE INITIALIZATION
-- Database: MariaDB
-- Shared database untuk semua kelompok
-- ============================================================

CREATE DATABASE IF NOT EXISTS siakad_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE siakad_db;

-- ============================================================
-- TABEL: users (Authentication & Authorization)
-- Digunakan oleh: SEMUA KELOMPOK
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,  -- bcrypt hash
  role ENUM('admin', 'mahasiswa') NOT NULL DEFAULT 'mahasiswa',
  nim VARCHAR(20) DEFAULT NULL,     -- relasi ke mahasiswa (null untuk admin)
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role (role),
  INDEX idx_users_nim (nim)
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: mahasiswa (Data Mahasiswa)
-- Digunakan oleh: KELOMPOK 1 (pemilik), semua kelompok (relasi)
-- ============================================================
CREATE TABLE IF NOT EXISTS mahasiswa (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nim VARCHAR(20) NOT NULL UNIQUE,
  nama VARCHAR(100) NOT NULL,
  email VARCHAR(100) DEFAULT NULL,
  no_telp VARCHAR(20) DEFAULT NULL,
  alamat TEXT DEFAULT NULL,
  jurusan VARCHAR(100) DEFAULT NULL,
  program_studi VARCHAR(100) DEFAULT NULL,
  angkatan YEAR DEFAULT NULL,
  semester INT DEFAULT 1,
  status ENUM('aktif', 'cuti', 'lulus', 'drop_out') NOT NULL DEFAULT 'aktif',
  foto_url VARCHAR(255) DEFAULT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_mahasiswa_jurusan (jurusan),
  INDEX idx_mahasiswa_angkatan (angkatan),
  INDEX idx_mahasiswa_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: mata_kuliah (Data Mata Kuliah)
-- Digunakan oleh: KELOMPOK 2 (pemilik), kelompok lain (relasi)
-- ============================================================
CREATE TABLE IF NOT EXISTS mata_kuliah (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode_mk VARCHAR(20) NOT NULL UNIQUE,
  nama_mk VARCHAR(100) NOT NULL,
  sks INT NOT NULL DEFAULT 2,
  semester INT NOT NULL,
  jurusan VARCHAR(100) DEFAULT NULL,
  dosen_pengampu VARCHAR(100) DEFAULT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_mk_semester (semester),
  INDEX idx_mk_jurusan (jurusan)
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: tahun_ajaran (Master Tahun Ajaran)
-- Digunakan oleh: Modul Akademik, Nilai, Absensi
-- ============================================================
CREATE TABLE IF NOT EXISTS tahun_ajaran (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tahun_ajaran VARCHAR(10) NOT NULL,
  semester ENUM('ganjil', 'genap') NOT NULL DEFAULT 'ganjil',
  tanggal_mulai DATE DEFAULT NULL,
  tanggal_selesai DATE DEFAULT NULL,
  status ENUM('draft', 'aktif', 'arsip') NOT NULL DEFAULT 'draft',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tahun_semester (tahun_ajaran, semester),
  INDEX idx_ta_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: bobot_nilai (Konfigurasi Bobot Nilai)
-- Digunakan oleh: Modul Input Nilai
-- ============================================================
CREATE TABLE IF NOT EXISTS bobot_nilai (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nama_config VARCHAR(100) NOT NULL UNIQUE,
  bobot_tugas DECIMAL(5,2) NOT NULL DEFAULT 30.00,
  bobot_uts DECIMAL(5,2) NOT NULL DEFAULT 30.00,
  bobot_uas DECIMAL(5,2) NOT NULL DEFAULT 40.00,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: jadwal (Jadwal Kuliah)
-- Digunakan oleh: KELOMPOK 3 (pemilik)
-- ============================================================
CREATE TABLE IF NOT EXISTS jadwal (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode_mk VARCHAR(20) NOT NULL,
  hari ENUM('senin', 'selasa', 'rabu', 'kamis', 'jumat', 'sabtu') NOT NULL,
  jam_mulai TIME NOT NULL,
  jam_selesai TIME NOT NULL,
  ruangan VARCHAR(50) DEFAULT NULL,
  dosen VARCHAR(100) DEFAULT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_jadwal_hari (hari),
  FOREIGN KEY (kode_mk) REFERENCES mata_kuliah(kode_mk) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: krs (Kartu Rencana Studi)
-- Digunakan oleh: KELOMPOK 4 (pemilik)
-- ============================================================
CREATE TABLE IF NOT EXISTS krs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nim VARCHAR(20) NOT NULL,
  kode_mk VARCHAR(20) NOT NULL,
  semester INT NOT NULL,
  tahun_ajaran VARCHAR(10) NOT NULL,  -- misal: 2025/2026
  status ENUM('diambil', 'batal', 'selesai') NOT NULL DEFAULT 'diambil',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_krs (nim, kode_mk, tahun_ajaran),
  FOREIGN KEY (nim) REFERENCES mahasiswa(nim) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (kode_mk) REFERENCES mata_kuliah(kode_mk) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: nilai (Nilai Mahasiswa)
-- Digunakan oleh: KELOMPOK 5 (pemilik)
-- ============================================================
CREATE TABLE IF NOT EXISTS nilai (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nim VARCHAR(20) NOT NULL,
  kode_mk VARCHAR(20) NOT NULL,
  nilai_tugas DECIMAL(5,2) DEFAULT 0,
  nilai_uts DECIMAL(5,2) DEFAULT 0,
  nilai_uas DECIMAL(5,2) DEFAULT 0,
  nilai_akhir DECIMAL(5,2) DEFAULT 0,
  grade CHAR(2) DEFAULT NULL,
  tahun_ajaran VARCHAR(10) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_nilai (nim, kode_mk, tahun_ajaran),
  FOREIGN KEY (nim) REFERENCES mahasiswa(nim) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (kode_mk) REFERENCES mata_kuliah(kode_mk) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: pembayaran (Pembayaran UKT)
-- Digunakan oleh: KELOMPOK PEMBAYARAN & LAPORAN (pemilik)
-- ============================================================
CREATE TABLE IF NOT EXISTS pembayaran (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nim VARCHAR(20) NOT NULL,
  jenis_pembayaran ENUM('ukt', 'spp', 'praktikum', 'wisuda', 'lainnya') NOT NULL DEFAULT 'ukt',
  jumlah DECIMAL(15,2) NOT NULL,
  tanggal_bayar DATE NOT NULL,
  metode_pembayaran ENUM('transfer_bank', 'virtual_account', 'tunai', 'qris') NOT NULL DEFAULT 'transfer_bank',
  bukti_pembayaran VARCHAR(255) DEFAULT NULL,
  nomor_referensi VARCHAR(50) DEFAULT NULL UNIQUE,
  semester INT NOT NULL,
  tahun_ajaran VARCHAR(10) NOT NULL,  -- misal: 2025/2026
  status ENUM('pending', 'lunas', 'gagal', 'refund') NOT NULL DEFAULT 'pending',
  keterangan TEXT DEFAULT NULL,
  verified_by INT DEFAULT NULL,        -- ID admin yang verifikasi
  verified_at TIMESTAMP NULL DEFAULT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_pembayaran_nim (nim),
  INDEX idx_pembayaran_status (status),
  INDEX idx_pembayaran_tahun (tahun_ajaran),
  INDEX idx_pembayaran_tanggal (tanggal_bayar),
  FOREIGN KEY (nim) REFERENCES mahasiswa(nim) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (verified_by) REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================
-- TABEL: laporan (Laporan Akademik & Keuangan)
-- Digunakan oleh: KELOMPOK PEMBAYARAN & LAPORAN (pemilik)
-- ============================================================
CREATE TABLE IF NOT EXISTS laporan (
  id INT AUTO_INCREMENT PRIMARY KEY,
  judul VARCHAR(200) NOT NULL,
  jenis_laporan ENUM('pembayaran', 'akademik', 'statistik', 'keuangan', 'mahasiswa') NOT NULL,
  deskripsi TEXT DEFAULT NULL,
  periode_mulai DATE DEFAULT NULL,
  periode_selesai DATE DEFAULT NULL,
  tahun_ajaran VARCHAR(10) DEFAULT NULL,
  data_laporan JSON DEFAULT NULL,      -- Data dinamis laporan dalam format JSON
  file_path VARCHAR(255) DEFAULT NULL, -- Path file PDF/CSV jika digenerate
  format_file ENUM('pdf', 'csv', 'excel') DEFAULT NULL,
  generated_by INT NOT NULL,            -- ID user yang generate
  total_records INT DEFAULT 0,
  status ENUM('draft', 'generated', 'archived') NOT NULL DEFAULT 'draft',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_laporan_jenis (jenis_laporan),
  INDEX idx_laporan_tahun (tahun_ajaran),
  INDEX idx_laporan_status (status),
  FOREIGN KEY (generated_by) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- DATA SEED: Admin default & sample data
-- ============================================================

-- Password: admin123 (bcrypt hash)
INSERT INTO users (username, password, role, nim) VALUES
  ('admin', '$2b$10$1u/G7PzFbGgx/HUu3mB.9e7eneGiWtXtVPBmdtFD.NPgwkTzD6tv6', 'admin', NULL)
ON DUPLICATE KEY UPDATE password = VALUES(password), role = VALUES(role), nim = VALUES(nim);

-- Sample mahasiswa
INSERT INTO mahasiswa (nim, nama, email, jurusan, program_studi, angkatan, semester, status) VALUES
  ('2024001', 'Ahmad Fauzan', 'ahmad@univ.ac.id', 'Teknik Informatika', 'S1 Informatika', 2024, 2, 'aktif'),
  ('2024002', 'Siti Nurhaliza', 'siti@univ.ac.id', 'Teknik Informatika', 'S1 Informatika', 2024, 2, 'aktif'),
  ('2024003', 'Budi Santoso', 'budi@univ.ac.id', 'Sistem Informasi', 'S1 Sistem Informasi', 2024, 2, 'aktif'),
  ('2024004', 'Dewi Lestari', 'dewi@univ.ac.id', 'Teknik Informatika', 'S1 Informatika', 2023, 4, 'aktif'),
  ('2024005', 'Rizki Pratama', 'rizki@univ.ac.id', 'Sistem Informasi', 'S1 Sistem Informasi', 2023, 4, 'aktif')
ON DUPLICATE KEY UPDATE nim = nim;

-- Sample user mahasiswa (password: mhs123)
INSERT INTO users (username, password, role, nim) VALUES
  ('2024001', '$2b$10$l/rSkV59cHSqmzUFH1Zgee5.LTgmBIXDzdFP5Z3DUElE6cq5Gvhwe', 'mahasiswa', '2024001'),
  ('2024002', '$2b$10$l/rSkV59cHSqmzUFH1Zgee5.LTgmBIXDzdFP5Z3DUElE6cq5Gvhwe', 'mahasiswa', '2024002'),
  ('2024003', '$2b$10$l/rSkV59cHSqmzUFH1Zgee5.LTgmBIXDzdFP5Z3DUElE6cq5Gvhwe', 'mahasiswa', '2024003'),
  ('2024004', '$2b$10$l/rSkV59cHSqmzUFH1Zgee5.LTgmBIXDzdFP5Z3DUElE6cq5Gvhwe', 'mahasiswa', '2024004'),
  ('2024005', '$2b$10$l/rSkV59cHSqmzUFH1Zgee5.LTgmBIXDzdFP5Z3DUElE6cq5Gvhwe', 'mahasiswa', '2024005')
ON DUPLICATE KEY UPDATE password = VALUES(password), role = VALUES(role), nim = VALUES(nim);

-- Sample mata kuliah untuk modul Nilai & Absensi
INSERT INTO mata_kuliah (kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu) VALUES
  ('IF101', 'Dasar Pemrograman', 3, 1, 'Teknik Informatika', 'Dr. Rina Kurnia'),
  ('IF202', 'Struktur Data', 3, 2, 'Teknik Informatika', 'Budi Hartono, M.Kom'),
  ('IF204', 'Basis Data', 3, 2, 'Teknik Informatika', 'Sari Prameswari, M.Kom'),
  ('SI201', 'Analisis Proses Bisnis', 3, 2, 'Sistem Informasi', 'Ahmad Wibowo, M.Kom'),
  ('SI304', 'Manajemen Proyek SI', 3, 4, 'Sistem Informasi', 'Dewi Anggraini, M.MSI')
ON DUPLICATE KEY UPDATE
  nama_mk = VALUES(nama_mk),
  sks = VALUES(sks),
  semester = VALUES(semester),
  jurusan = VALUES(jurusan),
  dosen_pengampu = VALUES(dosen_pengampu);

INSERT INTO tahun_ajaran (tahun_ajaran, semester, tanggal_mulai, tanggal_selesai, status) VALUES
  ('2024/2025', 'genap', '2025-01-13', '2025-06-30', 'aktif'),
  ('2025/2026', 'ganjil', '2025-08-18', '2025-12-20', 'draft')
ON DUPLICATE KEY UPDATE
  tanggal_mulai = VALUES(tanggal_mulai),
  tanggal_selesai = VALUES(tanggal_selesai),
  status = VALUES(status);

INSERT INTO bobot_nilai (nama_config, bobot_tugas, bobot_uts, bobot_uas, is_active) VALUES
  ('Default Akademik', 30.00, 30.00, 40.00, 1)
ON DUPLICATE KEY UPDATE
  bobot_tugas = VALUES(bobot_tugas),
  bobot_uts = VALUES(bobot_uts),
  bobot_uas = VALUES(bobot_uas),
  is_active = VALUES(is_active);

-- Sample pembayaran
INSERT INTO pembayaran (nim, jenis_pembayaran, jumlah, tanggal_bayar, metode_pembayaran, nomor_referensi, semester, tahun_ajaran, status) VALUES
  ('2024001', 'ukt', 3500000.00, '2025-02-15', 'transfer_bank', 'REF-2025-00001', 2, '2024/2025', 'lunas'),
  ('2024002', 'ukt', 3500000.00, '2025-02-20', 'virtual_account', 'REF-2025-00002', 2, '2024/2025', 'lunas'),
  ('2024003', 'ukt', 4000000.00, '2025-03-01', 'qris', 'REF-2025-00003', 2, '2024/2025', 'pending'),
  ('2024004', 'ukt', 3500000.00, '2025-02-10', 'transfer_bank', 'REF-2025-00004', 4, '2024/2025', 'lunas'),
  ('2024005', 'ukt', 4000000.00, '2025-03-05', 'tunai', 'REF-2025-00005', 4, '2024/2025', 'gagal')
ON DUPLICATE KEY UPDATE nomor_referensi = nomor_referensi;

SELECT 'Database siakad_db berhasil dibuat!' AS status;
