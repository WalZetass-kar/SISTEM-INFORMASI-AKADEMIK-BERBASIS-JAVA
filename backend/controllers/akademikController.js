const AkademikSettings = require('../models/AkademikSettings');

const akademikController = {
  getSettings: async (req, res) => {
    try {
      const [tahunAjaran, mataKuliah, bobotNilai] = await Promise.all([
        AkademikSettings.getTahunAjaran(),
        AkademikSettings.getMataKuliah(),
        AkademikSettings.getBobotNilai()
      ]);
      res.json({ success: true, data: { tahun_ajaran: tahunAjaran, mata_kuliah: mataKuliah, bobot_nilai: bobotNilai } });
    } catch (error) {
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  createTahunAjaran: async (req, res) => {
    try {
      if (!req.body.tahun_ajaran) return res.status(400).json({ success: false, message: 'Tahun ajaran wajib diisi.' });
      const data = await AkademikSettings.createTahunAjaran(req.body);
      res.status(201).json({ success: true, message: 'Tahun ajaran berhasil ditambahkan.', data });
    } catch (error) {
      res.status(500).json({ success: false, message: error.message || 'Terjadi kesalahan server.' });
    }
  },

  updateTahunAjaran: async (req, res) => {
    try {
      const updated = await AkademikSettings.updateTahunAjaran(req.params.id, req.body);
      if (!updated) return res.status(404).json({ success: false, message: 'Tahun ajaran tidak ditemukan.' });
      res.json({ success: true, message: 'Tahun ajaran berhasil diperbarui.' });
    } catch (error) {
      res.status(500).json({ success: false, message: error.message || 'Terjadi kesalahan server.' });
    }
  },

  deleteTahunAjaran: async (req, res) => {
    try {
      const deleted = await AkademikSettings.deleteTahunAjaran(req.params.id);
      if (!deleted) return res.status(404).json({ success: false, message: 'Tahun ajaran tidak ditemukan.' });
      res.json({ success: true, message: 'Tahun ajaran berhasil dihapus.' });
    } catch (error) {
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  createMataKuliah: async (req, res) => {
    try {
      if (!req.body.kode_mk || !req.body.nama_mk) return res.status(400).json({ success: false, message: 'Kode dan nama mata kuliah wajib diisi.' });
      const data = await AkademikSettings.createMataKuliah(req.body);
      res.status(201).json({ success: true, message: 'Mata kuliah berhasil ditambahkan.', data });
    } catch (error) {
      res.status(500).json({ success: false, message: error.message || 'Terjadi kesalahan server.' });
    }
  },

  updateMataKuliah: async (req, res) => {
    try {
      const updated = await AkademikSettings.updateMataKuliah(req.params.kode_mk, req.body);
      if (!updated) return res.status(404).json({ success: false, message: 'Mata kuliah tidak ditemukan.' });
      res.json({ success: true, message: 'Mata kuliah berhasil diperbarui.' });
    } catch (error) {
      res.status(500).json({ success: false, message: error.message || 'Terjadi kesalahan server.' });
    }
  },

  deleteMataKuliah: async (req, res) => {
    try {
      const deleted = await AkademikSettings.deleteMataKuliah(req.params.kode_mk);
      if (!deleted) return res.status(404).json({ success: false, message: 'Mata kuliah tidak ditemukan.' });
      res.json({ success: true, message: 'Mata kuliah berhasil dihapus.' });
    } catch (error) {
      res.status(500).json({ success: false, message: 'Data tidak dapat dihapus karena masih digunakan modul lain.' });
    }
  },

  updateBobotNilai: async (req, res) => {
    try {
      const data = await AkademikSettings.updateBobotNilai(req.body);
      res.json({ success: true, message: 'Bobot nilai berhasil disimpan.', data });
    } catch (error) {
      res.status(error.statusCode || 500).json({ success: false, message: error.message || 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = akademikController;
