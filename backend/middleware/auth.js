// ============================================================
// MIDDLEWARE: JWT Authentication & Authorization
// Digunakan oleh SEMUA route yang membutuhkan autentikasi
// ============================================================

const jwt = require('jsonwebtoken');
const jwtConfig = require('../config/jwt');

// Daftar token yang sudah di-blacklist (logout)
const tokenBlacklist = new Set();

/**
 * Middleware: Verifikasi JWT Token
 * Mengecek header Authorization: Bearer <token>
 */
const verifyToken = (req, res, next) => {
  try {
    const authHeader = req.headers['authorization'];

    if (!authHeader) {
      return res.status(401).json({
        success: false,
        message: 'Token tidak ditemukan. Silakan login terlebih dahulu.'
      });
    }

    const token = authHeader.split(' ')[1]; // Bearer <token>

    if (!token) {
      return res.status(401).json({
        success: false,
        message: 'Format token tidak valid. Gunakan: Bearer <token>'
      });
    }

    // Cek apakah token sudah di-blacklist (logout)
    if (tokenBlacklist.has(token)) {
      return res.status(401).json({
        success: false,
        message: 'Token sudah tidak valid. Silakan login kembali.'
      });
    }

    // Verifikasi token
    const decoded = jwt.verify(token, jwtConfig.secret);
    req.user = decoded; // { id, username, role, nim }
    req.token = token;
    next();
  } catch (error) {
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        message: 'Token sudah expired. Silakan login kembali.'
      });
    }
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        success: false,
        message: 'Token tidak valid.'
      });
    }
    return res.status(500).json({
      success: false,
      message: 'Terjadi kesalahan saat verifikasi token.'
    });
  }
};

/**
 * Middleware: Cek Role Admin
 * Hanya admin yang bisa mengakses
 */
const isAdmin = (req, res, next) => {
  if (req.user && req.user.role === 'admin') {
    next();
  } else {
    return res.status(403).json({
      success: false,
      message: 'Akses ditolak. Hanya admin yang dapat mengakses.'
    });
  }
};

/**
 * Middleware: Cek Role Mahasiswa
 * Hanya mahasiswa yang bisa mengakses
 */
const isMahasiswa = (req, res, next) => {
  if (req.user && req.user.role === 'mahasiswa') {
    next();
  } else {
    return res.status(403).json({
      success: false,
      message: 'Akses ditolak. Hanya mahasiswa yang dapat mengakses.'
    });
  }
};

/**
 * Middleware: Cek Role Admin ATAU Mahasiswa pemilik data
 * Admin bisa akses semua, mahasiswa hanya data miliknya
 */
const isAdminOrOwner = (req, res, next) => {
  if (req.user.role === 'admin') {
    return next();
  }
  // Mahasiswa hanya bisa akses data miliknya berdasarkan NIM
  const nimParam = req.params.nim || req.body.nim || req.query.nim;
  if (req.user.nim === nimParam) {
    return next();
  }
  return res.status(403).json({
    success: false,
    message: 'Akses ditolak. Anda hanya dapat mengakses data milik sendiri.'
  });
};

/**
 * Blacklist token saat logout
 */
const blacklistToken = (token) => {
  tokenBlacklist.add(token);
};

/**
 * Cek apakah token di-blacklist
 */
const isTokenBlacklisted = (token) => {
  return tokenBlacklist.has(token);
};

module.exports = {
  verifyToken,
  isAdmin,
  isMahasiswa,
  isAdminOrOwner,
  blacklistToken,
  isTokenBlacklisted
};
