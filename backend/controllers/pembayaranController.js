// ============================================================
// CONTROLLER: Pembayaran (Input UKT, Status, Dashboard)
// API: /api/pembayaran
// Modul Pembayaran & Laporan
// ============================================================

const Pembayaran = require('../models/Pembayaran');
const Mahasiswa = require('../models/Mahasiswa');
const { v4: uuidv4 } = require('uuid');

const pembayaranController = {
  // GET /api/pembayaran - Ambil semua pembayaran
  getAll: async (req, res) => {
    try {
      const { page, limit, search, status, tahun_ajaran, jenis } = req.query;
      const result = await Pembayaran.findAll({ page, limit, search, status, tahun_ajaran, jenis });
      res.json({ success: true, ...result });
    } catch (error) {
      console.error('Get pembayaran error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/pembayaran/:id - Detail pembayaran
  getById: async (req, res) => {
    try {
      const pembayaran = await Pembayaran.findById(req.params.id);
      if (!pembayaran) return res.status(404).json({ success: false, message: 'Pembayaran tidak ditemukan.' });
      res.json({ success: true, data: pembayaran });
    } catch (error) {
      console.error('Get pembayaran by ID error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/pembayaran/mahasiswa/:nim - History pembayaran per mahasiswa
  getByNim: async (req, res) => {
    try {
      // Validasi NIM dari API Kelompok 1
      const mhs = await Mahasiswa.findByNim(req.params.nim);
      if (!mhs) return res.status(404).json({ success: false, message: 'Mahasiswa tidak ditemukan.' });

      const pembayaran = await Pembayaran.findByNim(req.params.nim);
      res.json({ success: true, data: { mahasiswa: mhs, pembayaran } });
    } catch (error) {
      console.error('Get pembayaran by NIM error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // POST /api/pembayaran - Input pembayaran baru
  create: async (req, res) => {
    try {
      const { nim, jenis_pembayaran, jumlah, tanggal_bayar, metode_pembayaran,
              bukti_pembayaran, semester, tahun_ajaran, keterangan } = req.body;

      if (!nim || !jumlah || !tanggal_bayar || !semester || !tahun_ajaran) {
        return res.status(400).json({
          success: false,
          message: 'NIM, jumlah, tanggal bayar, semester, dan tahun ajaran wajib diisi.'
        });
      }

      // Validasi NIM - ambil data mahasiswa dari Kelompok 1
      const mhs = await Mahasiswa.findByNim(nim);
      if (!mhs) {
        return res.status(404).json({
          success: false,
          message: 'NIM tidak ditemukan di data mahasiswa. Pastikan data mahasiswa sudah diinput oleh Kelompok 1.'
        });
      }

      // Generate nomor referensi unik
      const nomor_referensi = `REF-${new Date().getFullYear()}-${uuidv4().substring(0, 8).toUpperCase()}`;

      const result = await Pembayaran.create({
        nim, jenis_pembayaran, jumlah, tanggal_bayar, metode_pembayaran,
        bukti_pembayaran, nomor_referensi, semester, tahun_ajaran, keterangan
      });

      res.status(201).json({
        success: true,
        message: `Pembayaran berhasil diinput untuk ${mhs.nama} (${nim}).`,
        data: { ...result, nama_mahasiswa: mhs.nama }
      });
    } catch (error) {
      console.error('Create pembayaran error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // PUT /api/pembayaran/:id/status - Update status pembayaran (Admin verifikasi)
  updateStatus: async (req, res) => {
    try {
      const { status } = req.body;
      const validStatuses = ['pending', 'lunas', 'gagal', 'refund'];
      if (!status || !validStatuses.includes(status)) {
        return res.status(400).json({
          success: false, message: `Status harus salah satu dari: ${validStatuses.join(', ')}`
        });
      }

      const pembayaran = await Pembayaran.findById(req.params.id);
      if (!pembayaran) return res.status(404).json({ success: false, message: 'Pembayaran tidak ditemukan.' });

      await Pembayaran.updateStatus(req.params.id, status, req.user.id);
      const updated = await Pembayaran.findById(req.params.id);
      res.json({ success: true, message: `Status pembayaran berhasil diubah menjadi ${status}.`, data: updated });
    } catch (error) {
      console.error('Update status error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // PUT /api/pembayaran/:id - Update data pembayaran
  update: async (req, res) => {
    try {
      const existing = await Pembayaran.findById(req.params.id);
      if (!existing) return res.status(404).json({ success: false, message: 'Pembayaran tidak ditemukan.' });
      if (existing.status === 'lunas') {
        return res.status(400).json({ success: false, message: 'Pembayaran yang sudah lunas tidak dapat diubah.' });
      }

      await Pembayaran.update(req.params.id, req.body);
      const updated = await Pembayaran.findById(req.params.id);
      res.json({ success: true, message: 'Data pembayaran berhasil diupdate.', data: updated });
    } catch (error) {
      console.error('Update pembayaran error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // DELETE /api/pembayaran/:id
  delete: async (req, res) => {
    try {
      const existing = await Pembayaran.findById(req.params.id);
      if (!existing) return res.status(404).json({ success: false, message: 'Pembayaran tidak ditemukan.' });
      if (existing.status === 'lunas') {
        return res.status(400).json({ success: false, message: 'Pembayaran yang sudah lunas tidak dapat dihapus.' });
      }

      await Pembayaran.delete(req.params.id);
      res.json({ success: true, message: 'Pembayaran berhasil dihapus.' });
    } catch (error) {
      console.error('Delete pembayaran error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/pembayaran/status/:nim - Cek status pembayaran mahasiswa
  cekStatus: async (req, res) => {
    try {
      const { semester, tahun_ajaran } = req.query;
      const mhs = await Mahasiswa.findByNim(req.params.nim);
      if (!mhs) return res.status(404).json({ success: false, message: 'Mahasiswa tidak ditemukan.' });

      if (semester && tahun_ajaran) {
        const status = await Pembayaran.getStatusByNimSemester(req.params.nim, semester, tahun_ajaran);
        return res.json({
          success: true,
          data: { mahasiswa: mhs, status_pembayaran: status ? status.status : 'belum_bayar', detail: status }
        });
      }

      const allPembayaran = await Pembayaran.findByNim(req.params.nim);
      res.json({ success: true, data: { mahasiswa: mhs, history: allPembayaran } });
    } catch (error) {
      console.error('Cek status error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/pembayaran/dashboard/stats - Dashboard statistik
  getDashboardStats: async (req, res) => {
    try {
      const { tahun_ajaran } = req.query;
      const stats = await Pembayaran.getDashboardStats(tahun_ajaran);
      res.json({ success: true, data: stats });
    } catch (error) {
      console.error('Dashboard stats error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/pembayaran/tahun-ajaran - List tahun ajaran
  getTahunAjaran: async (req, res) => {
    try {
      const list = await Pembayaran.getTahunAjaranList();
      res.json({ success: true, data: list });
    } catch (error) {
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = pembayaranController;
