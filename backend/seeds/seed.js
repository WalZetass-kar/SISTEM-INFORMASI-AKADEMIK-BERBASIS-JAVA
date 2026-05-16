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
