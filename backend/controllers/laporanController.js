// ============================================================
// CONTROLLER: Laporan (Generate & Cetak Laporan)
// API: /api/laporan
// Modul Pembayaran & Laporan
// ============================================================

const Laporan = require('../models/Laporan');

const laporanController = {
  // GET /api/laporan - List semua laporan
  getAll: async (req, res) => {
    try {
      const { page, limit, jenis, status } = req.query;
      const result = await Laporan.findAll({ page, limit, jenis, status });
      res.json({ success: true, ...result });
    } catch (error) {
      console.error('Get laporan error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/laporan/:id - Detail laporan
  getById: async (req, res) => {
    try {
      const laporan = await Laporan.findById(req.params.id);
      if (!laporan) return res.status(404).json({ success: false, message: 'Laporan tidak ditemukan.' });
      // Parse JSON data_laporan
      if (laporan.data_laporan && typeof laporan.data_laporan === 'string') {
        laporan.data_laporan = JSON.parse(laporan.data_laporan);
      }
      res.json({ success: true, data: laporan });
    } catch (error) {
      console.error('Get laporan by ID error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // POST /api/laporan/generate/pembayaran - Generate laporan pembayaran
  generatePembayaran: async (req, res) => {
    try {
      const { periode_mulai, periode_selesai, tahun_ajaran } = req.body;
      const result = await Laporan.generateLaporanPembayaran(
        periode_mulai, periode_selesai, tahun_ajaran, req.user.id
      );
      res.status(201).json({
        success: true, message: 'Laporan pembayaran berhasil digenerate.', data: result
      });
    } catch (error) {
      console.error('Generate laporan pembayaran error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // POST /api/laporan/generate/mahasiswa - Generate laporan mahasiswa
  generateMahasiswa: async (req, res) => {
    try {
      const result = await Laporan.generateLaporanMahasiswa(req.user.id);
      res.status(201).json({
        success: true, message: 'Laporan mahasiswa berhasil digenerate.', data: result
      });
    } catch (error) {
      console.error('Generate laporan mahasiswa error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // POST /api/laporan/generate/keuangan - Generate laporan keuangan
  generateKeuangan: async (req, res) => {
    try {
      const { tahun_ajaran } = req.body;
      if (!tahun_ajaran) {
        return res.status(400).json({ success: false, message: 'Tahun ajaran wajib diisi.' });
      }
      const result = await Laporan.generateLaporanKeuangan(tahun_ajaran, req.user.id);
      res.status(201).json({
        success: true, message: 'Laporan keuangan berhasil digenerate.', data: result
      });
    } catch (error) {
      console.error('Generate laporan keuangan error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // DELETE /api/laporan/:id
  delete: async (req, res) => {
    try {
      const existing = await Laporan.findById(req.params.id);
      if (!existing) return res.status(404).json({ success: false, message: 'Laporan tidak ditemukan.' });
      await Laporan.delete(req.params.id);
      res.json({ success: true, message: 'Laporan berhasil dihapus.' });
    } catch (error) {
      console.error('Delete laporan error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = laporanController;
