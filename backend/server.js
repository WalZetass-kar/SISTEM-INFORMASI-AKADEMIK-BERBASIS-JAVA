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
    endpoints: {
      auth: '/api/login, /api/logout, /api/profile, /api/register',
      mahasiswa: '/api/mahasiswa',
      pembayaran: '/api/pembayaran',
      akademik: '/api/akademik',
      nilai: '/api/nilai',
      laporan: '/api/laporan'
    }
  });
});

// API Routes
app.use('/api', authRoutes);
app.use('/api/mahasiswa', mahasiswaRoutes);
app.use('/api/pembayaran', pembayaranRoutes);
app.use('/api/akademik', akademikRoutes);
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
    console.log('  POST   /api/login                    - Login');
    console.log('  POST   /api/logout                   - Logout');
    console.log('  GET    /api/profile                  - User Profile');
    console.log('  GET    /api/mahasiswa                - List Mahasiswa');
    console.log('  GET    /api/mahasiswa/:nim            - Detail Mahasiswa');
    console.log('  POST   /api/mahasiswa                - Tambah Mahasiswa');
    console.log('  PUT    /api/mahasiswa/:nim            - Update Mahasiswa');
    console.log('  DELETE /api/mahasiswa/:nim            - Hapus Mahasiswa');
    console.log('  GET    /api/pembayaran               - List Pembayaran');
    console.log('  POST   /api/pembayaran               - Input Pembayaran');
    console.log('  GET    /api/pembayaran/dashboard/stats - Dashboard Stats');
    console.log('  GET    /api/pembayaran/status/:nim    - Cek Status');
    console.log('  PUT    /api/pembayaran/:id/status     - Verifikasi');
    console.log('  GET    /api/nilai/input-list          - Input Nilai');
    console.log('  POST   /api/nilai/bulk                - Simpan Nilai');
    console.log('  GET    /api/laporan                  - List Laporan');
    console.log('  POST   /api/laporan/generate/*       - Generate Laporan');
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
