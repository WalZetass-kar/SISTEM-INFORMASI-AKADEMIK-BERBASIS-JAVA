const Mahasiswa = require('../models/Mahasiswa');
const Matakuliah = require('../models/Matakuliah');
const Jadwal = require('../models/Jadwal');
const Krs = require('../models/Krs');
const AkademikSettings = require('../models/AkademikSettings');

const VALID_HARI = ['senin', 'selasa', 'rabu', 'kamis', 'jumat', 'sabtu'];
const TAHUN_AJARAN_REGEX = /^\d{4}\/\d{4}$/;
const JAM_REGEX = /^([01]\d|2[0-3]):[0-5]\d$/;

const normalizeJam = (value) => {
  if (typeof value !== 'string') return '';
  return value.trim();
};

const normalizeText = (value) => {
  if (typeof value !== 'string') return '';
  return value.trim();
};

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
      res.status(500).json(`{ success: false, message: 'Terjadi kesalahan server.' }`);
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
  },
  getMatakuliah: async (req, res) => {
    try {
      const { search, semester } = req.query;
      const data = await Matakuliah.findAll({ search, semester });
      res.json({ success: true, data });
    } catch (error) {
      console.error('Get matakuliah error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  createMatakuliah: async (req, res) => {
    try {
      const { kode_mk, nama_mk, sks, semester } = req.body;
      if (!kode_mk || !nama_mk || sks === undefined || semester === undefined) {
        return res.status(400).json({ success: false, message: 'Kode mata kuliah, nama mata kuliah, SKS, dan semester wajib diisi.' });
      }
      const data = await Matakuliah.create({ kode_mk, nama_mk, sks, semester });
      res.status(201).json({ success: true, message: 'Mata kuliah berhasil ditambahkan.', data });
    } catch (error) {
      console.error('Create matakuliah error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  getJadwal: async (req, res) => {
    try {
      const { kode_mk, hari, semester } = req.query;
      const data = await Jadwal.findAll({ kode_mk, hari, semester });
      res.json({ success: true, data });
    } catch (error) {
      console.error('Get jadwal error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  createJadwal: async (req, res) => {
    try {
      const { kode_mk, hari, ruangan, dosen } = req.body;
      const jam_mulai = normalizeJam(req.body.jam_mulai);
      const jam_selesai = normalizeJam(req.body.jam_selesai);

      if (!kode_mk || !hari || !jam_mulai || !jam_selesai || !ruangan || !dosen) {
        return res.status(400).json({ success: false, message: 'Semua field jadwal wajib diisi.' });
      }

      if (!VALID_HARI.includes(hari)) {
        return res.status(400).json({ success: false, message: `Hari harus salah satu dari: ${VALID_HARI.join(', ')}.` });
      }

      if (!JAM_REGEX.test(jam_mulai) || !JAM_REGEX.test(jam_selesai)) {
        return res.status(400).json({ success: false, message: 'Format jam harus HH:mm, contoh 08:00.' });
      }

      if (jam_mulai >= jam_selesai) {
        return res.status(400).json({ success: false, message: 'Jam selesai harus lebih besar dari jam mulai.' });
      }

      const data = await Jadwal.create({ kode_mk, hari, jam_mulai, jam_selesai, ruangan, dosen });
      res.status(201).json({ success: true, message: 'Jadwal berhasil ditambahkan.', data });
    } catch (error) {
      console.error('Create jadwal error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  getKrs: async (req, res) => {
    try {
      const { tahun_ajaran: tahunAjaran, kode_mk: kodeMk } = req.query;
      const requestedNim = normalizeText(req.query.nim);
      let nim = requestedNim;
      if (req.user.role !== 'admin') {
        if (!req.user.nim) return res.status(403).json({ success: false, message: 'Akun belum terhubung dengan NIM.' });
        if (requestedNim && requestedNim !== req.user.nim) return res.status(403).json({ success: false, message: 'Akses ditolak.' });
        nim = req.user.nim;
      }
      const data = await Krs.findAll({ nim, tahun_ajaran: tahunAjaran, kode_mk: kodeMk });
      res.json({ success: true, data });
    } catch (error) {
      console.error('Get KRS error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  createKrs: async (req, res) => {
    try {
      const nim = normalizeText(req.body.nim);
      const { kode_mk, tahun_ajaran } = req.body;
      if (!nim || !kode_mk || !tahun_ajaran) return res.status(400).json({ success: false, message: 'NIM, kode MK, dan tahun ajaran wajib diisi.' });

      if (req.user.role !== 'admin') {
        if (!req.user.nim) return res.status(403).json({ success: false, message: 'Akun belum terhubung dengan NIM.' });
        if (nim !== req.user.nim) return res.status(403).json({ success: false, message: 'Akses ditolak. Anda hanya dapat mengelola KRS milik sendiri.' });
      }
      
      const matakuliah = await Matakuliah.findByKode(kode_mk);
      if (!matakuliah) return res.status(404).json({ success: false, message: 'Mata kuliah tidak ditemukan.' });

      const data = await Krs.create({
        nim,
        kode_mk,
        semester: matakuliah.semester,
        tahun_ajaran,
        status: 'diambil'
      });
      res.status(201).json({ success: true, message: 'KRS berhasil ditambahkan.', data });
    } catch (error) {
      console.error('Create KRS error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = akademikController;
