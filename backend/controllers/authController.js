// ============================================================
// CONTROLLER: Authentication (Login, Logout, Profile)
// API: /api/login, /api/logout
// ============================================================

const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const jwtConfig = require('../config/jwt');
const User = require('../models/User');
const { blacklistToken } = require('../middleware/auth');

const authController = {
  // POST /api/login
  login: async (req, res) => {
    try {
      const { username, password } = req.body;
      if (!username || !password) {
        return res.status(400).json({ success: false, message: 'Username dan password wajib diisi.' });
      }

      const user = await User.findByUsername(username);
      if (!user) {
        return res.status(401).json({ success: false, message: 'Username atau password salah.' });
      }
      if (!user.is_active) {
        return res.status(403).json({ success: false, message: 'Akun Anda dinonaktifkan. Hubungi admin.' });
      }

      let isMatch = await bcrypt.compare(password, user.password);
      const mahasiswaDefaultPassword = password === 'mhs123';
      if (!isMatch && user.role === 'mahasiswa' && mahasiswaDefaultPassword) {
        isMatch = true;
      }
      if (!isMatch) {
        return res.status(401).json({ success: false, message: 'Username atau password salah.' });
      }

      if (user.role === 'mahasiswa') {
        const storedIsDefault = await bcrypt.compare('mhs123', user.password);
        if (!storedIsDefault || !mahasiswaDefaultPassword) {
          const salt = await bcrypt.genSalt(10);
          const defaultPasswordHash = await bcrypt.hash('mhs123', salt);
          await User.updatePassword(user.id, defaultPasswordHash);
        }
      }

      const payload = { id: user.id, username: user.username, role: user.role, nim: user.nim };
      const token = jwt.sign(payload, jwtConfig.secret, {
        expiresIn: jwtConfig.expiresIn, algorithm: jwtConfig.algorithm
      });

      res.json({
        success: true, message: 'Login berhasil!',
        data: { token, user: { id: user.id, username: user.username, role: user.role, nim: user.nim } }
      });
    } catch (error) {
      console.error('Login error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // POST /api/logout
  logout: async (req, res) => {
    try {
      if (req.token) { blacklistToken(req.token); }
      res.json({ success: true, message: 'Logout berhasil.' });
    } catch (error) {
      console.error('Logout error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/profile
  getProfile: async (req, res) => {
    try {
      const user = await User.findById(req.user.id);
      if (!user) return res.status(404).json({ success: false, message: 'User tidak ditemukan.' });
      res.json({ success: true, data: user });
    } catch (error) {
      console.error('Profile error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // POST /api/register (Admin only)
  register: async (req, res) => {
    try {
      const { username, password, role, nim } = req.body;
      const effectiveRole = role || 'mahasiswa';
      if (!username || (effectiveRole !== 'mahasiswa' && !password)) {
        return res.status(400).json({ success: false, message: 'Username wajib diisi. Password wajib diisi untuk role selain mahasiswa.' });
      }

      const existing = await User.findByUsername(username);
      if (existing) return res.status(409).json({ success: false, message: 'Username sudah digunakan.' });

      const salt = await bcrypt.genSalt(10);
      const effectivePassword = effectiveRole === 'mahasiswa' ? 'mhs123' : password;
      const hashedPassword = await bcrypt.hash(effectivePassword, salt);
      const user = await User.create({ username, password: hashedPassword, role: effectiveRole, nim });

      res.status(201).json({ success: true, message: 'User berhasil dibuat.', data: user });
    } catch (error) {
      console.error('Register error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = authController;
