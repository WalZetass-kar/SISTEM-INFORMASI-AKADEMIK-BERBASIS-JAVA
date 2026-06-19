// ============================================================
// SERVER: Express.js - Sistem Informasi Akademik
// Entry point untuk backend API
// ============================================================

const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
require('dotenv').config();

const { testConnection } = require('./config/database');

// Import routes
const authRoutes = require('./routes/authRoutes');
const mahasiswaRoutes = require('./routes/mahasiswaRoutes');
const pembayaranRoutes = require('./routes/pembayaranRoutes');
const laporanRoutes = require('./routes/laporanRoutes');
const nilaiRoutes = require('./routes/nilaiRoutes');
const akademikRoutes = require('./routes/akademikRoutes');

const app = express();
const PORT = process.env.PORT || 3000;
const path = require('path');

const API_ENDPOINTS = [
  { method: 'GET', path: '/api', auth: 'public', description: 'Health check dan daftar endpoint' },
  { method: 'GET', path: '/api/routes', auth: 'public', description: 'Daftar lengkap endpoint API' },
  { method: 'POST', path: '/api/login', auth: 'public', description: 'Login' },
  { method: 'POST', path: '/api/logout', auth: 'user', description: 'Logout' },
  { method: 'GET', path: '/api/profile', auth: 'user', description: 'User profile' },
  { method: 'POST', path: '/api/register', auth: 'admin', description: 'Registrasi user' },

  { method: 'GET', path: '/api/mahasiswa', auth: 'user', description: 'List mahasiswa' },
  { method: 'GET', path: '/api/mahasiswa/:nim', auth: 'user', description: 'Detail mahasiswa' },
  { method: 'POST', path: '/api/mahasiswa', auth: 'admin', description: 'Tambah mahasiswa' },
  { method: 'PUT', path: '/api/mahasiswa/:nim', auth: 'admin', description: 'Update mahasiswa' },
  { method: 'DELETE', path: '/api/mahasiswa/:nim', auth: 'admin', description: 'Hapus mahasiswa' },
  { method: 'POST', path: '/api/mahasiswa/:nim/upload-foto', auth: 'admin', description: 'Upload foto mahasiswa' },
  { method: 'GET', path: '/api/mahasiswa/jurusan/list', auth: 'user', description: 'List jurusan mahasiswa' },
  { method: 'GET', path: '/api/mahasiswa/stats/jurusan', auth: 'admin', description: 'Statistik mahasiswa per jurusan' },
  { method: 'GET', path: '/api/mahasiswa/stats/status', auth: 'admin', description: 'Statistik mahasiswa per status' },

  { method: 'GET', path: '/api/pembayaran', auth: 'user', description: 'List pembayaran' },
  { method: 'GET', path: '/api/pembayaran/:id', auth: 'user', description: 'Detail pembayaran' },
  { method: 'POST', path: '/api/pembayaran', auth: 'admin', description: 'Input pembayaran' },
  { method: 'PUT', path: '/api/pembayaran/:id', auth: 'admin', description: 'Update pembayaran' },
  { method: 'PUT', path: '/api/pembayaran/:id/status', auth: 'admin', description: 'Verifikasi status pembayaran' },
  { method: 'DELETE', path: '/api/pembayaran/:id', auth: 'admin', description: 'Hapus pembayaran' },
  { method: 'POST', path: '/api/pembayaran/:id/upload-bukti', auth: 'admin', description: 'Upload bukti pembayaran' },
  { method: 'GET', path: '/api/pembayaran/:id/bukti', auth: 'user', description: 'Download bukti pembayaran' },
  { method: 'GET', path: '/api/pembayaran/dashboard/stats', auth: 'admin', description: 'Dashboard pembayaran' },
  { method: 'GET', path: '/api/pembayaran/tahun-ajaran', auth: 'user', description: 'List tahun ajaran pembayaran' },
  { method: 'GET', path: '/api/pembayaran/tarif-ukt', auth: 'user', description: 'Rekomendasi tarif UKT mahasiswa' },
  { method: 'GET', path: '/api/pembayaran/status/:nim', auth: 'user', description: 'Status pembayaran mahasiswa' },
  { method: 'GET', path: '/api/pembayaran/mahasiswa/:nim', auth: 'user', description: 'Riwayat pembayaran mahasiswa' },

  { method: 'GET', path: '/api/akademik/settings', auth: 'admin', description: 'Pengaturan akademik' },
  { method: 'GET', path: '/api/akademik/semester', auth: 'user', description: 'List semester aktif' },
  { method: 'GET', path: '/api/akademik/tahun-ajaran', auth: 'user', description: 'List tahun ajaran akademik' },
  { method: 'POST', path: '/api/akademik/tahun-ajaran', auth: 'admin', description: 'Tambah tahun ajaran' },
  { method: 'PUT', path: '/api/akademik/tahun-ajaran/:id', auth: 'admin', description: 'Update tahun ajaran' },
  { method: 'DELETE', path: '/api/akademik/tahun-ajaran/:id', auth: 'admin', description: 'Hapus tahun ajaran' },
  { method: 'POST', path: '/api/akademik/semester', auth: 'admin', description: 'Tambah semester' },
  { method: 'PUT', path: '/api/akademik/semester/:id', auth: 'admin', description: 'Update semester' },
  { method: 'DELETE', path: '/api/akademik/semester/:id', auth: 'admin', description: 'Hapus semester' },
  { method: 'POST', path: '/api/akademik/mata-kuliah', auth: 'admin', description: 'Tambah mata kuliah pengaturan' },
  { method: 'PUT', path: '/api/akademik/mata-kuliah/:kode_mk', auth: 'admin', description: 'Update mata kuliah pengaturan' },
  { method: 'DELETE', path: '/api/akademik/mata-kuliah/:kode_mk', auth: 'admin', description: 'Hapus mata kuliah pengaturan' },
  { method: 'POST', path: '/api/akademik/jurusan', auth: 'admin', description: 'Tambah jurusan' },
  { method: 'PUT', path: '/api/akademik/jurusan/:id', auth: 'admin', description: 'Update jurusan' },
  { method: 'DELETE', path: '/api/akademik/jurusan/:id', auth: 'admin', description: 'Hapus jurusan' },
  { method: 'PUT', path: '/api/akademik/bobot-nilai', auth: 'admin', description: 'Update bobot nilai' },
  { method: 'PUT', path: '/api/akademik/jumlah-pertemuan', auth: 'admin', description: 'Update jumlah pertemuan default' },
  { method: 'PUT', path: '/api/akademik/jumlah-pertemuan-jurusan', auth: 'admin', description: 'Update jumlah pertemuan per jurusan' },
  { method: 'GET', path: '/api/akademik/kehadiran/input-list', auth: 'admin', description: 'List input kehadiran' },
  { method: 'GET', path: '/api/akademik/kehadiran/rekap', auth: 'admin', description: 'Rekap kehadiran' },
  { method: 'POST', path: '/api/akademik/kehadiran/bulk', auth: 'admin', description: 'Simpan kehadiran massal' },
  { method: 'GET', path: '/api/akademik/kehadiran/saya', auth: 'mahasiswa', description: 'Kehadiran mahasiswa login' },
  { method: 'GET', path: '/api/akademik/info-saya', auth: 'mahasiswa', description: 'Info akademik mahasiswa login' },

  { method: 'GET', path: '/api/matakuliah', auth: 'user', description: 'List mata kuliah' },
  { method: 'POST', path: '/api/matakuliah', auth: 'admin', description: 'Tambah mata kuliah' },
  { method: 'GET', path: '/api/krs', auth: 'user', description: 'List KRS' },
  { method: 'POST', path: '/api/krs', auth: 'admin/owner', description: 'Tambah KRS' },
  { method: 'GET', path: '/api/jadwal', auth: 'user', description: 'List jadwal' },
  { method: 'POST', path: '/api/jadwal', auth: 'admin', description: 'Tambah jadwal' },

  { method: 'GET', path: '/api/nilai/mata-kuliah', auth: 'user', description: 'List mata kuliah untuk nilai' },
  { method: 'GET', path: '/api/nilai/input-list', auth: 'admin', description: 'List input nilai' },
  { method: 'GET', path: '/api/nilai/rekap', auth: 'admin', description: 'Rekap nilai' },
  { method: 'POST', path: '/api/nilai/bulk', auth: 'admin', description: 'Simpan nilai massal' },
  { method: 'DELETE', path: '/api/nilai', auth: 'admin', description: 'Hapus nilai' },
  { method: 'GET', path: '/api/nilai/saya', auth: 'mahasiswa', description: 'Nilai mahasiswa login' },

  { method: 'GET', path: '/api/laporan', auth: 'admin', description: 'List laporan' },
  { method: 'GET', path: '/api/laporan/:id', auth: 'admin', description: 'Detail laporan' },
  { method: 'POST', path: '/api/laporan/generate/pembayaran', auth: 'admin', description: 'Generate laporan pembayaran' },
  { method: 'POST', path: '/api/laporan/generate/mahasiswa', auth: 'admin', description: 'Generate laporan mahasiswa' },
  { method: 'POST', path: '/api/laporan/generate/keuangan', auth: 'admin', description: 'Generate laporan keuangan' },
  { method: 'DELETE', path: '/api/laporan/:id', auth: 'admin', description: 'Hapus laporan' }
];

const endpointSummary = API_ENDPOINTS.reduce((groups, endpoint) => {
  const group = endpoint.path.split('/')[2] || 'root';
  if (!groups[group]) groups[group] = [];
  groups[group].push(`${endpoint.method} ${endpoint.path}`);
  return groups;
}, {});

// ============================================================
// MIDDLEWARE
// ============================================================
app.use(cors({ origin: process.env.CORS_ORIGIN || '*' }));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));
app.use(morgan(process.env.LOG_LEVEL || 'dev'));

// Static files for uploads
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// ============================================================
// ROUTES
// ============================================================

// Health check
app.get('/api', (req, res) => {
  res.json({
    success: true,
    message: '🎓 Sistem Informasi Akademik API v1.0',
    timestamp: new Date().toISOString(),
    endpoints: endpointSummary,
    total_endpoints: API_ENDPOINTS.length
  });
});

app.get('/api/routes', (req, res) => {
  res.json({
    success: true,
    total: API_ENDPOINTS.length,
    data: API_ENDPOINTS
  });
});

// API Routes
app.use('/api', authRoutes);
app.use('/api/mahasiswa', mahasiswaRoutes);
app.use('/api/pembayaran', pembayaranRoutes);
app.use('/api', akademikRoutes);
app.use('/api/nilai', nilaiRoutes);
app.use('/api/laporan', laporanRoutes);

// ============================================================
// ERROR HANDLING
// ============================================================

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `Route ${req.method} ${req.originalUrl} tidak ditemukan.`
  });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('Server Error:', err);
  res.status(500).json({
    success: false,
    message: 'Internal Server Error',
    error: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
});

// ============================================================
// START SERVER
// ============================================================

const startServer = async () => {
  await testConnection();
  const server = app.listen(PORT);

  server.on('listening', () => {
    console.log('');
    console.log('============================================================');
    console.log('  🎓 SISTEM INFORMASI AKADEMIK - Backend API');
    console.log(`  🌐 Server running on http://localhost:${PORT}`);
    console.log(`  📡 API Base URL: http://localhost:${PORT}/api`);
    console.log(`  🔧 Environment: ${process.env.NODE_ENV || 'development'}`);
    console.log('============================================================');
    console.log('');
    console.log('  Available Endpoints:');
    API_ENDPOINTS.forEach(({ method, path: routePath, description }) => {
      console.log(`  ${method.padEnd(6)} ${routePath.padEnd(42)} - ${description}`);
    });
    console.log('');
  });

  server.on('error', (error) => {
    if (error.code === 'EADDRINUSE') {
      console.error(`❌ Port ${PORT} sudah digunakan. Ubah PORT di .env atau hentikan proses yang memakai port tersebut.`);
    } else {
      console.error('❌ Gagal menjalankan server:', error.message);
    }
    process.exit(1);
  });
};

if (require.main === module) {
  startServer().catch((error) => {
    console.error('❌ Gagal menjalankan server:', error.message);
    process.exit(1);
  });
}

module.exports = app;
