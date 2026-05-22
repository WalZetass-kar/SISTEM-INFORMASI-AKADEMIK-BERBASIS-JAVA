// ============================================================
// CONTROLLER: Akademik (Matakuliah, KRS, Jadwal)
// API: /api/matakuliah, /api/krs, /api/jadwal
// ============================================================

const Mahasiswa = require('../models/Mahasiswa');
const Matakuliah = require('../models/Matakuliah');
const Jadwal = require('../models/Jadwal');
const Krs = require('../models/Krs');

const VALID_HARI = ['senin', 'selasa', 'rabu', 'kamis', 'jumat', 'sabtu'];
const TAHUN_AJARAN_REGEX = /^\d{4}\/\d{4}$/;
const JAM_REGEX = /^([01]\d|2[0-3]):([0-5]\d)$/;

const parseJam = (jamValue, jamMulaiValue, jamSelesaiValue) => {
  if (jamMulaiValue && jamSelesaiValue) {
    return { jam_mulai: jamMulaiValue, jam_selesai: jamSelesaiValue };
  }

  if (!jamValue) {
    return null;
  }

  const normalized = jamValue.replace(/\s+/g, '');
  const parts = normalized.split('-');
  if (parts.length !== 2) {
    return null;
  }

  return { jam_mulai: parts[0], jam_selesai: parts[1] };
};

const akademikController = {
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
        return res.status(400).json({
          success: false,
          message: 'Kode mata kuliah, nama mata kuliah, SKS, dan semester wajib diisi.'
        });
      }

      const sksNumber = Number.parseInt(sks, 10);
      const semesterNumber = Number.parseInt(semester, 10);
      if (!Number.isInteger(sksNumber) || sksNumber < 1) {
        return res.status(400).json({ success: false, message: 'SKS harus berupa angka lebih dari 0.' });
      }
      if (!Number.isInteger(semesterNumber) || semesterNumber < 1) {
        return res.status(400).json({ success: false, message: 'Semester harus berupa angka lebih dari 0.' });
      }

      const existing = await Matakuliah.findByKode(kode_mk);
      if (existing) {
        return res.status(409).json({ success: false, message: 'Kode mata kuliah sudah terdaftar.' });
      }

      const data = await Matakuliah.create({
        kode_mk: kode_mk.trim(),
        nama_mk: nama_mk.trim(),
        sks: sksNumber,
        semester: semesterNumber
      });

      res.status(201).json({
        success: true,
        message: 'Mata kuliah berhasil ditambahkan.',
        data
      });
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
      const { kode_mk, hari, jam, jam_mulai, jam_selesai, ruangan, dosen } = req.body;

      if (!kode_mk || !hari || (!jam && (!jam_mulai || !jam_selesai)) || !ruangan || !dosen) {
        return res.status(400).json({
          success: false,
          message: 'Kode mata kuliah, hari, jam, ruangan, dan dosen wajib diisi.'
        });
      }

      const normalizedHari = String(hari).trim().toLowerCase();
      if (!VALID_HARI.includes(normalizedHari)) {
        return res.status(400).json({
          success: false,
          message: `Hari harus salah satu dari: ${VALID_HARI.join(', ')}.`
        });
      }

      const parsedJam = parseJam(
        jam ? String(jam).trim() : '',
        jam_mulai ? String(jam_mulai).trim() : '',
        jam_selesai ? String(jam_selesai).trim() : ''
      );
      if (!parsedJam || !JAM_REGEX.test(parsedJam.jam_mulai) || !JAM_REGEX.test(parsedJam.jam_selesai)) {
        return res.status(400).json({
          success: false,
          message: 'Format jam tidak valid. Gunakan HH:MM-HH:MM atau isi jam_mulai/jam_selesai.'
        });
      }

      if (parsedJam.jam_mulai >= parsedJam.jam_selesai) {
        return res.status(400).json({
          success: false,
          message: 'Jam selesai harus lebih besar dari jam mulai.'
        });
      }

      const matakuliah = await Matakuliah.findByKode(String(kode_mk).trim());
      if (!matakuliah) {
        return res.status(404).json({ success: false, message: 'Mata kuliah tidak ditemukan.' });
      }

      const data = await Jadwal.create({
        kode_mk: String(kode_mk).trim(),
        hari: normalizedHari,
        jam_mulai: parsedJam.jam_mulai,
        jam_selesai: parsedJam.jam_selesai,
        ruangan: String(ruangan).trim(),
        dosen: String(dosen).trim()
      });

      res.status(201).json({
        success: true,
        message: 'Jadwal kuliah berhasil ditambahkan.',
        data
      });
    } catch (error) {
      console.error('Create jadwal error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  getKrs: async (req, res) => {
    try {
      const requestedNim = req.query.nim ? String(req.query.nim).trim() : '';
      const tahunAjaran = req.query.tahun_ajaran ? String(req.query.tahun_ajaran).trim() : '';
      const kodeMk = req.query.kode_mk ? String(req.query.kode_mk).trim() : '';

      let nim = requestedNim;
      if (req.user.role !== 'admin') {
        if (!req.user.nim) {
          return res.status(403).json({
            success: false,
            message: 'Akun mahasiswa ini belum terhubung dengan NIM.'
          });
        }
        if (requestedNim && requestedNim !== req.user.nim) {
          return res.status(403).json({
            success: false,
            message: 'Anda hanya dapat melihat data KRS milik sendiri.'
          });
        }
        nim = req.user.nim;
      }

      const data = await Krs.findAll({ nim, tahun_ajaran: tahunAjaran, kode_mk: kodeMk });
      const totalSks = data.reduce((sum, item) => sum + Number(item.sks || 0), 0);
      const uniqueNim = new Set(data.map(item => item.nim));
      const uniqueTahunAjaran = new Set(data.map(item => item.tahun_ajaran));
      const summary = data.length > 0 && uniqueNim.size === 1 && uniqueTahunAjaran.size === 1
        ? {
            nim: data[0].nim,
            nama_mahasiswa: data[0].nama_mahasiswa,
            tahun_ajaran: data[0].tahun_ajaran,
            total_matakuliah: data.length,
            total_sks: totalSks
          }
        : null;

      res.json({ success: true, data, summary });
    } catch (error) {
      console.error('Get KRS error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  createKrs: async (req, res) => {
    try {
      const { nim, kode_mk, tahun_ajaran } = req.body;

      if (!nim || !kode_mk || !tahun_ajaran) {
        return res.status(400).json({
          success: false,
          message: 'NIM, kode mata kuliah, dan tahun ajaran wajib diisi.'
        });
      }

      if (!TAHUN_AJARAN_REGEX.test(String(tahun_ajaran).trim())) {
        return res.status(400).json({
          success: false,
          message: 'Format tahun ajaran harus seperti 2024/2025.'
        });
      }

      const mahasiswa = await Mahasiswa.findByNim(String(nim).trim());
      if (!mahasiswa) {
        return res.status(404).json({ success: false, message: 'Mahasiswa tidak ditemukan.' });
      }

      const matakuliah = await Matakuliah.findByKode(String(kode_mk).trim());
      if (!matakuliah) {
        return res.status(404).json({ success: false, message: 'Mata kuliah tidak ditemukan.' });
      }

      const existing = await Krs.findByUnique(
        String(nim).trim(),
        String(kode_mk).trim(),
        String(tahun_ajaran).trim()
      );
      if (existing) {
        return res.status(409).json({
          success: false,
          message: 'Mata kuliah ini sudah diambil pada tahun ajaran tersebut.'
        });
      }

      const data = await Krs.create({
        nim: String(nim).trim(),
        kode_mk: String(kode_mk).trim(),
        semester: matakuliah.semester,
        tahun_ajaran: String(tahun_ajaran).trim(),
        status: 'diambil'
      });

      res.status(201).json({
        success: true,
        message: `KRS berhasil ditambahkan untuk ${mahasiswa.nama}.`,
        data
      });
    } catch (error) {
      console.error('Create KRS error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  }
};

module.exports = akademikController;
