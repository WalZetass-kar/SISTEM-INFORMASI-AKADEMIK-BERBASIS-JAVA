// ============================================================
// CONTROLLER: Pembayaran (Input UKT, Status, Dashboard)
// API: /api/pembayaran
// Modul Pembayaran & Laporan
// ============================================================

const Pembayaran = require('../models/Pembayaran');
const Mahasiswa = require('../models/Mahasiswa');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');

const VALID_JENIS = ['ukt', 'spp', 'praktikum', 'wisuda', 'lainnya'];
const VALID_METODE = ['transfer_bank', 'virtual_account', 'tunai', 'qris'];
const VALID_STATUS = ['pending', 'lunas', 'gagal', 'refund'];
const DATE_REGEX = /^\d{4}-\d{2}-\d{2}$/;
const TAHUN_AJARAN_REGEX = /^\d{4}\/\d{4}$/;

const isAdmin = (req) => req.user?.role === 'admin';
const isOwner = (req, nim) => req.user?.role === 'mahasiswa' && req.user?.nim === nim;

const isValidDate = (value) => {
  if (!DATE_REGEX.test(String(value || ''))) return false;
  const date = new Date(`${value}T00:00:00.000Z`);
  return !Number.isNaN(date.getTime()) && date.toISOString().slice(0, 10) === value;
};

const normalizePaymentPayload = (body, existing = {}) => {
  const data = {
    jenis_pembayaran: body.jenis_pembayaran ?? existing.jenis_pembayaran ?? 'ukt',
    jumlah: body.jumlah ?? existing.jumlah,
    tanggal_bayar: body.tanggal_bayar ?? existing.tanggal_bayar,
    metode_pembayaran: body.metode_pembayaran ?? existing.metode_pembayaran ?? 'transfer_bank',
    semester: body.semester ?? existing.semester,
    tahun_ajaran: body.tahun_ajaran ?? existing.tahun_ajaran,
    keterangan: body.keterangan ?? existing.keterangan ?? null
  };

  data.jumlah = Number(data.jumlah);
  data.semester = Number(data.semester);
  if (data.tanggal_bayar instanceof Date) data.tanggal_bayar = data.tanggal_bayar.toISOString().slice(0, 10);
  if (typeof data.tanggal_bayar === 'string' && data.tanggal_bayar.length > 10) {
    data.tanggal_bayar = data.tanggal_bayar.slice(0, 10);
  }
  return data;
};

const validatePaymentPayload = (data, { requireAll = true } = {}) => {
  if (requireAll && (!data.jumlah || !data.tanggal_bayar || !data.semester || !data.tahun_ajaran)) {
    return 'Jumlah, tanggal bayar, semester, dan tahun ajaran wajib diisi.';
  }
  if (!VALID_JENIS.includes(data.jenis_pembayaran)) {
    return `Jenis pembayaran harus salah satu dari: ${VALID_JENIS.join(', ')}.`;
  }
  if (!VALID_METODE.includes(data.metode_pembayaran)) {
    return `Metode pembayaran harus salah satu dari: ${VALID_METODE.join(', ')}.`;
  }
  if (!Number.isFinite(data.jumlah) || data.jumlah <= 0) {
    return 'Jumlah pembayaran harus berupa angka lebih dari 0.';
  }
  if (!Number.isInteger(data.semester) || data.semester < 1 || data.semester > 20) {
    return 'Semester harus berupa angka 1-20.';
  }
  if (!isValidDate(data.tanggal_bayar)) {
    return 'Tanggal bayar harus format YYYY-MM-DD.';
  }
  if (!TAHUN_AJARAN_REGEX.test(String(data.tahun_ajaran || ''))) {
    return 'Tahun ajaran harus format YYYY/YYYY, contoh 2024/2025.';
  }
  return null;
};

const toUploadsPath = (file) => `/uploads/pembayaran/${file.filename}`;

const resolveUploadPath = (storedPath) => {
  if (!storedPath || !storedPath.startsWith('/uploads/pembayaran/')) return null;
  const filename = path.basename(storedPath);
  return path.join(__dirname, '..', 'uploads', 'pembayaran', filename);
};

const pembayaranController = {
  // GET /api/pembayaran - Ambil semua pembayaran
  getAll: async (req, res) => {
    try {
      const { page, limit, search, status, tahun_ajaran, jenis } = req.query;
      const result = await Pembayaran.findAll({
        page,
        limit,
        search: isAdmin(req) ? search : req.user.nim,
        status,
        tahun_ajaran,
        jenis
      });
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
      if (!isAdmin(req) && !isOwner(req, pembayaran.nim)) {
        return res.status(403).json({ success: false, message: 'Akses ditolak. Anda hanya dapat melihat pembayaran sendiri.' });
      }
      res.json({ success: true, data: pembayaran });
    } catch (error) {
      console.error('Get pembayaran by ID error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  // GET /api/pembayaran/mahasiswa/:nim - History pembayaran per mahasiswa
  getByNim: async (req, res) => {
    try {
      if (!isAdmin(req) && !isOwner(req, req.params.nim)) {
        return res.status(403).json({ success: false, message: 'Akses ditolak. Anda hanya dapat melihat riwayat pembayaran sendiri.' });
      }
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

      const paymentData = normalizePaymentPayload({
        jenis_pembayaran,
        jumlah,
        tanggal_bayar,
        metode_pembayaran,
        semester,
        tahun_ajaran,
        keterangan
      });
      const validationError = validatePaymentPayload(paymentData);
      if (validationError) return res.status(400).json({ success: false, message: validationError });

      // Validasi NIM - ambil data mahasiswa dari Kelompok 1
      const mhs = await Mahasiswa.findByNim(nim);
      if (!mhs) {
        return res.status(404).json({
          success: false,
          message: 'NIM tidak ditemukan di data mahasiswa. Pastikan data mahasiswa sudah diinput oleh Kelompok 1.'
        });
      }

      if (paymentData.jenis_pembayaran === 'ukt') {
        const duplicate = await Pembayaran.findDuplicateUkt({
          nim,
          semester: paymentData.semester,
          tahun_ajaran: paymentData.tahun_ajaran
        });
        if (duplicate) {
          return res.status(409).json({
            success: false,
            message: `UKT semester ${paymentData.semester} tahun ajaran ${paymentData.tahun_ajaran} sudah tercatat (${duplicate.status}).`
          });
        }
      }

      // Generate nomor referensi unik
      const nomor_referensi = `REF-${new Date().getFullYear()}-${uuidv4().substring(0, 8).toUpperCase()}`;

      const result = await Pembayaran.create({
        nim,
        ...paymentData,
        bukti_pembayaran,
        nomor_referensi
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
      if (!status || !VALID_STATUS.includes(status)) {
        return res.status(400).json({
          success: false, message: `Status harus salah satu dari: ${VALID_STATUS.join(', ')}`
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

      const paymentData = normalizePaymentPayload(req.body, existing);
      const validationError = validatePaymentPayload(paymentData);
      if (validationError) return res.status(400).json({ success: false, message: validationError });

      if (paymentData.jenis_pembayaran === 'ukt') {
        const duplicate = await Pembayaran.findDuplicateUkt({
          nim: existing.nim,
          semester: paymentData.semester,
          tahun_ajaran: paymentData.tahun_ajaran,
          excludeId: req.params.id
        });
        if (duplicate) {
          return res.status(409).json({
            success: false,
            message: `UKT semester ${paymentData.semester} tahun ajaran ${paymentData.tahun_ajaran} sudah tercatat di transaksi lain.`
          });
        }
      }

      await Pembayaran.update(req.params.id, paymentData);
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
      if (!isAdmin(req) && !isOwner(req, req.params.nim)) {
        return res.status(403).json({ success: false, message: 'Akses ditolak. Anda hanya dapat mengecek pembayaran sendiri.' });
      }
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
  },

  getTarifUkt: async (req, res) => {
    try {
      const { nim, semester, tahun_ajaran } = req.query;
      if (!nim) return res.status(400).json({ success: false, message: 'NIM wajib diisi.' });
      if (!isAdmin(req) && !isOwner(req, nim)) {
        return res.status(403).json({ success: false, message: 'Akses ditolak. Anda hanya dapat melihat tarif UKT sendiri.' });
      }
      if (semester && (!Number.isInteger(Number(semester)) || Number(semester) < 1 || Number(semester) > 20)) {
        return res.status(400).json({ success: false, message: 'Semester harus berupa angka 1-20.' });
      }
      if (tahun_ajaran && !TAHUN_AJARAN_REGEX.test(String(tahun_ajaran))) {
        return res.status(400).json({ success: false, message: 'Tahun ajaran harus format YYYY/YYYY.' });
      }

      const result = await Pembayaran.getTarifUkt({ nim, semester, tahun_ajaran });
      if (!result) return res.status(404).json({ success: false, message: 'Mahasiswa tidak ditemukan.' });
      if (!result.tarif) {
        return res.json({
          success: true,
          message: 'Tarif UKT belum tersedia untuk mahasiswa ini.',
          data: result
        });
      }
      res.json({ success: true, data: result });
    } catch (error) {
      console.error('Get tarif UKT error:', error);
      res.status(500).json({ success: false, message: 'Terjadi kesalahan server.' });
    }
  },

  uploadBukti: async (req, res) => {
    try {
      const pembayaran = await Pembayaran.findById(req.params.id);
      if (!pembayaran) {
        if (req.file) fs.unlink(req.file.path, () => {});
        return res.status(404).json({ success: false, message: 'Pembayaran tidak ditemukan.' });
      }
      if (!req.file) return res.status(400).json({ success: false, message: 'File bukti pembayaran tidak ditemukan.' });

      const oldFile = resolveUploadPath(pembayaran.bukti_pembayaran);
      const buktiPath = toUploadsPath(req.file);
      await Pembayaran.updateBukti(req.params.id, buktiPath);
      if (oldFile && oldFile !== req.file.path) fs.unlink(oldFile, () => {});

      res.json({
        success: true,
        message: 'Bukti pembayaran berhasil diupload.',
        data: { bukti_pembayaran: buktiPath }
      });
    } catch (error) {
      if (req.file) fs.unlink(req.file.path, () => {});
      console.error('Upload bukti pembayaran error:', error);
      res.status(500).json({ success: false, message: 'Gagal upload bukti pembayaran.' });
    }
  },

  downloadBukti: async (req, res) => {
    try {
      const pembayaran = await Pembayaran.findById(req.params.id);
      if (!pembayaran) return res.status(404).json({ success: false, message: 'Pembayaran tidak ditemukan.' });
      if (!isAdmin(req) && !isOwner(req, pembayaran.nim)) {
        return res.status(403).json({ success: false, message: 'Akses ditolak. Anda hanya dapat melihat bukti pembayaran sendiri.' });
      }
      const filePath = resolveUploadPath(pembayaran.bukti_pembayaran);
      if (!filePath || !fs.existsSync(filePath)) {
        return res.status(404).json({ success: false, message: 'Bukti pembayaran belum tersedia.' });
      }
      res.download(filePath);
    } catch (error) {
      console.error('Download bukti pembayaran error:', error);
      res.status(500).json({ success: false, message: 'Gagal download bukti pembayaran.' });
    }
  }
};

module.exports = pembayaranController;
