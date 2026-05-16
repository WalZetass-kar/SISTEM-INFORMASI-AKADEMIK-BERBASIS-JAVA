// ============================================================
// ROUTES: Pembayaran
// Modul Pembayaran & Laporan
// ============================================================

const express = require('express');
const router = express.Router();
const pembayaranController = require('../controllers/pembayaranController');
const { verifyToken, isAdmin, isAdminOrOwner } = require('../middleware/auth');

// Semua route butuh authentication
router.use(verifyToken);

// Dashboard & stats (harus di atas :id)
router.get('/dashboard/stats', isAdmin, pembayaranController.getDashboardStats);
router.get('/tahun-ajaran', pembayaranController.getTahunAjaran);
router.get('/status/:nim', isAdminOrOwner, pembayaranController.cekStatus);
router.get('/mahasiswa/:nim', isAdminOrOwner, pembayaranController.getByNim);

// CRUD
router.get('/', pembayaranController.getAll);
router.get('/:id', pembayaranController.getById);
router.post('/', isAdmin, pembayaranController.create);
router.put('/:id', isAdmin, pembayaranController.update);
router.put('/:id/status', isAdmin, pembayaranController.updateStatus);
router.delete('/:id', isAdmin, pembayaranController.delete);

module.exports = router;
