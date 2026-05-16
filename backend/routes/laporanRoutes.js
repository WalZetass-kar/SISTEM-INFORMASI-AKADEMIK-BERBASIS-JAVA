// ============================================================
// ROUTES: Laporan
// Modul Pembayaran & Laporan
// ============================================================

const express = require('express');
const router = express.Router();
const laporanController = require('../controllers/laporanController');
const { verifyToken, isAdmin } = require('../middleware/auth');

// Semua route butuh authentication + admin only
router.use(verifyToken);
router.use(isAdmin);

// List & detail
router.get('/', laporanController.getAll);
router.get('/:id', laporanController.getById);

// Generate laporan
router.post('/generate/pembayaran', laporanController.generatePembayaran);
router.post('/generate/mahasiswa', laporanController.generateMahasiswa);
router.post('/generate/keuangan', laporanController.generateKeuangan);

// Delete
router.delete('/:id', laporanController.delete);

module.exports = router;
