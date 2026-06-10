// ============================================================
// CONTROLLER: Nilai
// API untuk modul Akademik - Input Nilai
// ============================================================

const Nilai = require('../models/Nilai');

const nilaiController = {
  getMataKuliah: async (req, res) => {
    try {
      const { semester, search } = req.query;
      const data = await Nilai.getMataKuliahList({ semester, search });
      res.json({ success: true, data });
    } catch (error) {
      console.error('Get mata kuliah error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  getInputList: async (req, res) => {
    try {
      const { kode_mk, tahun_ajaran, search, jurusan } = req.query;
      if (!kode_mk || !tahun_ajaran) {
        return res.status(400).json({
          success: false,
          message: 'Kode mata kuliah dan tahun ajaran wajib diisi.'
        });
      }

      const result = await Nilai.getInputList({ kode_mk, tahun_ajaran, search, jurusan });
      if (!result.mata_kuliah) {
        return res.status(404).json({ success: false, message: 'Mata kuliah tidak ditemukan.' });
      }

      res.json({
        success: true,
        data: result.data,
        mata_kuliah: result.mata_kuliah,
        bobot_nilai: result.bobot_nilai
      });
    } catch (error) {
      console.error('Get input nilai error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  bulkSave: async (req, res) => {
    try {
      const { kode_mk, tahun_ajaran, nilai } = req.body;
      if (!kode_mk || !tahun_ajaran || !Array.isArray(nilai)) {
        return res.status(400).json({
          success: false,
          message: 'Kode mata kuliah, tahun ajaran, dan data nilai wajib diisi.'
        });
      }

      if (nilai.length === 0) {
        return res.status(400).json({ success: false, message: 'Data nilai masih kosong.' });
      }

      for (const item of nilai) {
        if (!item.nim) {
          return res.status(400).json({ success: false, message: 'Setiap data nilai wajib memiliki NIM.' });
        }
      }

      const saved = await Nilai.bulkUpsert({ kode_mk, tahun_ajaran, nilai });
      res.json({
        success: true,
        message: `${saved.length} nilai mahasiswa berhasil disimpan.`,
        data: saved
      });
    } catch (error) {
      console.error('Bulk save nilai error:', error);
      res.status(error.statusCode || 500).json({ success: false, message: error.message || 'Terjadi kesalahan server.' });
    }
  },

  delete: async (req, res) => {
    try {
      const { nim, kode_mk, tahun_ajaran } = req.body;
      if (!nim || !kode_mk || !tahun_ajaran) {
        return res.status(400).json({
          success: false,
          message: 'NIM, kode mata kuliah, dan tahun ajaran wajib diisi.'
        });
      }

      const deleted = await Nilai.deleteByKey({ nim, kode_mk, tahun_ajaran });
      if (!deleted) {
        return res.status(404).json({ success: false, message: 'Data nilai tidak ditemukan.' });
      }

      res.json({ success: true, message: 'Nilai mahasiswa berhasil dihapus.' });
    } catch (error) {
      console.error('Delete nilai error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  getMyNilai: async (req, res) => {
    try {
      if (!req.user.nim) {
        return res.status(403).json({ success: false, message: 'Akun ini tidak terhubung dengan NIM mahasiswa.' });
      }

      const data = await Nilai.findByNim(req.user.nim);
      const totalNilai = data.reduce((sum, item) => sum + Number(item.nilai_akhir || 0), 0);
      const totalSks = data.reduce((sum, item) => sum + Number(item.sks || 0), 0);
      const rataRata = data.length > 0 ? Number((totalNilai / data.length).toFixed(2)) : 0;

      res.json({
        success: true,
        data,
        summary: {
          total_mata_kuliah: data.length,
          total_sks: totalSks,
          rata_rata: rataRata
        }
      });
    } catch (error) {
      console.error('Get my nilai error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = nilaiController;
