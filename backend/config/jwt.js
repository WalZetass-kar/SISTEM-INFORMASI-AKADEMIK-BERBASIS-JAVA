// ============================================================
// JWT CONFIGURATION
// Shared JWT config untuk semua modul
// ============================================================

require('dotenv').config();

module.exports = {
  secret: process.env.JWT_SECRET || 'siakad-default-secret-key',
  expiresIn: process.env.JWT_EXPIRES_IN || '24h',
  algorithm: 'HS256'
};
