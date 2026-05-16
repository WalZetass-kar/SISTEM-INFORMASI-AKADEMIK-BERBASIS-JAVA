// ============================================================
// CONTROLLER: Mahasiswa (CRUD + Search + Upload Foto)
// API: /api/mahasiswa
// Kelompok 1 - Data Mahasiswa
// ============================================================

const Mahasiswa = require('../models/Mahasiswa');
const User = require('../models/User');
const bcrypt = require('bcrypt');

const mahasiswaController = {
  // GET /api/mahasiswa
  getAll: async (req, res) => {
    try {
      const { page, limit, search, jurusan, status } = req.query;
      const result = await Mahasiswa.findAll({ page, limit, search, jurusan, status });
      res.json({ success: true, ...result });
    } catch (error) {
      console.error('Get mahasiswa error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/mahasiswa/:nim
  getByNim: async (req, res) => {
    try {
      const mhs = await Mahasiswa.findByNim(req.params.nim);
      if (!mhs) return res.status(404).json({ success: false, message: 'Mahasiswa tidak ditemukan.' });
      res.json({ success: true, data: mhs });
    } catch (error) {
      console.error('Get mahasiswa by NIM error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // POST /api/mahasiswa
  create: async (req, res) => {
    try {
      const { nim, nama, email, no_telp, alamat, jurusan, program_studi, angkatan, semester, status, password } = req.body;
      if (!nim || !nama) {
        return res.status(400).json({ success: false, message: 'NIM dan Nama wajib diisi.' });
      }

      const existing = await Mahasiswa.findByNim(nim);
      if (existing) return res.status(409).json({ success: false, message: 'NIM sudah terdaftar.' });

      const mhs = await Mahasiswa.create({ nim, nama, email, no_telp, alamat, jurusan, program_studi, angkatan, semester, status });

      // Auto-create user account untuk mahasiswa
      const salt = await bcrypt.genSalt(10);
      const hashedPassword = await bcrypt.hash(password || nim, salt); // default password = NIM
      await User.create({ username: nim, password: hashedPassword, role: 'mahasiswa', nim });

      res.status(201).json({ success: true, message: 'Mahasiswa berhasil ditambahkan.', data: mhs });
    } catch (error) {
      console.error('Create mahasiswa error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // PUT /api/mahasiswa/:nim
  update: async (req, res) => {
    try {
      const existing = await Mahasiswa.findByNim(req.params.nim);
      if (!existing) return res.status(404).json({ success: false, message: 'Mahasiswa tidak ditemukan.' });

      const updated = await Mahasiswa.update(req.params.nim, req.body);
      if (!updated) return res.status(400).json({ success: false, message: 'Tidak ada data yang diupdate.' });

      const mhs = await Mahasiswa.findByNim(req.params.nim);
      res.json({ success: true, message: 'Data mahasiswa berhasil diupdate.', data: mhs });
    } catch (error) {
      console.error('Update mahasiswa error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // DELETE /api/mahasiswa/:nim
  delete: async (req, res) => {
    try {
      const existing = await Mahasiswa.findByNim(req.params.nim);
      if (!existing) return res.status(404).json({ success: false, message: 'Mahasiswa tidak ditemukan.' });

      await Mahasiswa.delete(req.params.nim);
      res.json({ success: true, message: 'Mahasiswa berhasil dihapus.' });
    } catch (error) {
      console.error('Delete mahasiswa error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/mahasiswa/stats/jurusan
  statsByJurusan: async (req, res) => {
    try {
      const stats = await Mahasiswa.countByJurusan();
      res.json({ success: true, data: stats });
    } catch (error) {
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/mahasiswa/stats/status
  statsByStatus: async (req, res) => {
    try {
      const stats = await Mahasiswa.countByStatus();
      res.json({ success: true, data: stats });
    } catch (error) {
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/mahasiswa/jurusan/list
  getJurusanList: async (req, res) => {
    try {
      const list = await Mahasiswa.getJurusanList();
      res.json({ success: true, data: list });
    } catch (error) {
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = mahasiswaController;
