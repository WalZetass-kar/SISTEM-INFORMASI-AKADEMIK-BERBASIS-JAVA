// ============================================================
// SEED: Generate sample data dengan bcrypt hash yang valid
// Jalankan: npm run seed
// ============================================================

const bcrypt = require('bcrypt');
const { pool, testConnection } = require('../config/database');

const seed = async () => {
  try {
    await testConnection();
    console.log('🌱 Memulai seeding database...\n');

    await pool.execute(`
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
      ) ENGINE=InnoDB
    `);

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS bobot_nilai (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nama_config VARCHAR(100) NOT NULL UNIQUE,
        bobot_tugas DECIMAL(5,2) NOT NULL DEFAULT 30.00,
        bobot_uts DECIMAL(5,2) NOT NULL DEFAULT 30.00,
        bobot_uas DECIMAL(5,2) NOT NULL DEFAULT 40.00,
        is_active TINYINT(1) NOT NULL DEFAULT 1,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB
    `);

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS jurusan (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nama_jurusan VARCHAR(100) NOT NULL UNIQUE,
        is_active TINYINT(1) NOT NULL DEFAULT 1,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB
    `);

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS semester (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nomor INT NOT NULL UNIQUE,
        nama_semester VARCHAR(50) NOT NULL,
        is_active TINYINT(1) NOT NULL DEFAULT 1,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB
    `);

    // Hash passwords
    const adminHash = await bcrypt.hash('admin123', 10);
    const mhsHash = await bcrypt.hash('mhs123', 10);

    // Seed admin
    await pool.execute(
      `INSERT INTO users (username, password, role, nim) VALUES (?, ?, 'admin', NULL)
       ON DUPLICATE KEY UPDATE password = ?`,
      ['admin', adminHash, adminHash]
    );
    console.log('✅ Admin user created (admin / admin123)');

    // Seed mahasiswa
    const mahasiswaData = [
      ['2024001', 'Ahmad Fauzan', 'ahmad@univ.ac.id', '081234567890', 'Jl. Merdeka No.1', 'Teknik Informatika', 'S1 Informatika', 2024, 2, 'aktif'],
      ['2024002', 'Siti Nurhaliza', 'siti@univ.ac.id', '081234567891', 'Jl. Sudirman No.5', 'Teknik Informatika', 'S1 Informatika', 2024, 2, 'aktif'],
      ['2024003', 'Budi Santoso', 'budi@univ.ac.id', '081234567892', 'Jl. Gatot Subroto No.10', 'Sistem Informasi', 'S1 Sistem Informasi', 2024, 2, 'aktif'],
      ['2024004', 'Dewi Lestari', 'dewi@univ.ac.id', '081234567893', 'Jl. Ahmad Yani No.3', 'Teknik Informatika', 'S1 Informatika', 2023, 4, 'aktif'],
      ['2024005', 'Rizki Pratama', 'rizki@univ.ac.id', '081234567894', 'Jl. Diponegoro No.7', 'Sistem Informasi', 'S1 Sistem Informasi', 2023, 4, 'aktif'],
      ['2024006', 'Aisyah Putri', 'aisyah@univ.ac.id', '081234567895', 'Jl. Imam Bonjol No.2', 'Teknik Informatika', 'S1 Informatika', 2024, 2, 'aktif'],
      ['2024007', 'Dimas Prasetyo', 'dimas@univ.ac.id', '081234567896', 'Jl. Thamrin No.8', 'Manajemen Informatika', 'D3 Manajemen Informatika', 2023, 4, 'aktif'],
      ['2024008', 'Fitriani Rahma', 'fitri@univ.ac.id', '081234567897', 'Jl. Asia Afrika No.12', 'Sistem Informasi', 'S1 Sistem Informasi', 2024, 2, 'aktif'],
    ];

    const jurusanData = [...new Set(mahasiswaData.map(mhs => mhs[5]))];
    for (const jurusan of jurusanData) {
      await pool.execute(
        `INSERT INTO jurusan (nama_jurusan, is_active)
         VALUES (?, 1)
         ON DUPLICATE KEY UPDATE nama_jurusan = VALUES(nama_jurusan), is_active = VALUES(is_active)`,
        [jurusan]
      );
    }
    console.log(`✅ ${jurusanData.length} jurusan ready`);

    for (let nomor = 1; nomor <= 8; nomor++) {
      await pool.execute(
        `INSERT INTO semester (nomor, nama_semester, is_active)
         VALUES (?, ?, 1)
         ON DUPLICATE KEY UPDATE nama_semester = VALUES(nama_semester), is_active = VALUES(is_active)`,
        [nomor, `Semester ${nomor}`]
      );
    }
    console.log('✅ 8 semester ready');

    for (const mhs of mahasiswaData) {
      await pool.execute(
        `INSERT INTO mahasiswa (nim, nama, email, no_telp, alamat, jurusan, program_studi, angkatan, semester, status)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE nama = VALUES(nama)`,
        mhs
      );
      await pool.execute(
        `INSERT INTO users (username, password, role, nim) VALUES (?, ?, 'mahasiswa', ?)
         ON DUPLICATE KEY UPDATE password = ?`,
        [mhs[0], mhsHash, mhs[0], mhsHash]
      );
    }
    console.log(`✅ ${mahasiswaData.length} mahasiswa created (password: mhs123)`);

    // Seed mata kuliah untuk modul Nilai & Absensi
    const mataKuliahData = [
      ['IF101', 'Dasar Pemrograman', 3, 1, 'Teknik Informatika', 'Dr. Rina Kurnia'],
      ['IF202', 'Struktur Data', 3, 2, 'Teknik Informatika', 'Budi Hartono, M.Kom'],
      ['IF204', 'Basis Data', 3, 2, 'Teknik Informatika', 'Sari Prameswari, M.Kom'],
      ['SI201', 'Analisis Proses Bisnis', 3, 2, 'Sistem Informasi', 'Ahmad Wibowo, M.Kom'],
      ['SI304', 'Manajemen Proyek SI', 3, 4, 'Sistem Informasi', 'Dewi Anggraini, M.MSI'],
    ];

    for (const mk of mataKuliahData) {
      await pool.execute(
        `INSERT INTO mata_kuliah (kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu)
         VALUES (?, ?, ?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE
           nama_mk = VALUES(nama_mk),
           sks = VALUES(sks),
           semester = VALUES(semester),
           jurusan = VALUES(jurusan),
           dosen_pengampu = VALUES(dosen_pengampu)`,
        mk
      );
    }
    console.log(`✅ ${mataKuliahData.length} mata kuliah created`);

    const tahunAjaranData = [
      ['2024/2025', 'genap', '2025-01-13', '2025-06-30', 'aktif'],
      ['2025/2026', 'ganjil', '2025-08-18', '2025-12-20', 'draft'],
    ];

    for (const ta of tahunAjaranData) {
      await pool.execute(
        `INSERT INTO tahun_ajaran (tahun_ajaran, semester, tanggal_mulai, tanggal_selesai, status)
         VALUES (?, ?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE
           tanggal_mulai = VALUES(tanggal_mulai),
           tanggal_selesai = VALUES(tanggal_selesai),
           status = VALUES(status)`,
        ta
      );
    }
    console.log(`✅ ${tahunAjaranData.length} tahun ajaran created`);

    await pool.execute(
      `INSERT INTO bobot_nilai (nama_config, bobot_tugas, bobot_uts, bobot_uas, is_active)
       VALUES ('Default Akademik', 30.00, 30.00, 40.00, 1)
       ON DUPLICATE KEY UPDATE
         bobot_tugas = VALUES(bobot_tugas),
         bobot_uts = VALUES(bobot_uts),
         bobot_uas = VALUES(bobot_uas),
         is_active = VALUES(is_active)`
    );
    console.log('✅ Bobot nilai default ready');

    // Seed pembayaran
    const pembayaranData = [
      ['2024001', 'ukt', 3500000, '2025-02-15', 'transfer_bank', 'REF-2025-00000001', 2, '2024/2025', 'lunas'],
      ['2024002', 'ukt', 3500000, '2025-02-20', 'virtual_account', 'REF-2025-00000002', 2, '2024/2025', 'lunas'],
      ['2024003', 'ukt', 4000000, '2025-03-01', 'qris', 'REF-2025-00000003', 2, '2024/2025', 'pending'],
      ['2024004', 'ukt', 3500000, '2025-02-10', 'transfer_bank', 'REF-2025-00000004', 4, '2024/2025', 'lunas'],
      ['2024005', 'ukt', 4000000, '2025-03-05', 'tunai', 'REF-2025-00000005', 4, '2024/2025', 'gagal'],
      ['2024006', 'ukt', 3500000, '2025-02-25', 'virtual_account', 'REF-2025-00000006', 2, '2024/2025', 'lunas'],
      ['2024007', 'ukt', 3000000, '2025-01-20', 'transfer_bank', 'REF-2025-00000007', 4, '2024/2025', 'lunas'],
      ['2024008', 'ukt', 4000000, '2025-03-10', 'qris', 'REF-2025-00000008', 2, '2024/2025', 'pending'],
      ['2024001', 'praktikum', 500000, '2025-03-15', 'tunai', 'REF-2025-00000009', 2, '2024/2025', 'lunas'],
      ['2024002', 'praktikum', 500000, '2025-03-16', 'transfer_bank', 'REF-2025-00000010', 2, '2024/2025', 'pending'],
    ];

    for (const p of pembayaranData) {
      await pool.execute(
        `INSERT INTO pembayaran (nim, jenis_pembayaran, jumlah, tanggal_bayar, metode_pembayaran, nomor_referensi, semester, tahun_ajaran, status)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE nomor_referensi = VALUES(nomor_referensi)`,
        p
      );
    }
    console.log(`✅ ${pembayaranData.length} pembayaran created`);

    console.log('\n🎉 Seeding selesai!\n');
    console.log('Login credentials:');
    console.log('  Admin    → admin / admin123');
    console.log('  Mahasiswa → 2024001 / mhs123 (dst)');
    process.exit(0);
  } catch (error) {
    console.error('❌ Seeding error:', error);
    process.exit(1);
  }
};

seed();
