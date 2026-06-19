// ============================================================
// ROUTES: Pembayaran
// Modul Pembayaran & Laporan
// ============================================================

const express = require('express');
const router = express.Router();
const pembayaranController = require('../controllers/pembayaranController');
const { verifyToken, isAdmin } = require('../middleware/auth');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const uploadDir = path.join(__dirname, '..', 'uploads', 'pembayaran');
fs.mkdirSync(uploadDir, { recursive: true });

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => {
    cb(null, `bukti-${req.params.id}-${Date.now()}${path.extname(file.originalname)}`);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    const allowedMime = ['image/jpeg', 'image/png', 'image/webp', 'application/pdf'];
    const allowedExt = ['.jpg', '.jpeg', '.png', '.webp', '.pdf'];
    const ext = path.extname(file.originalname).toLowerCase();
    if (allowedMime.includes(file.mimetype) && allowedExt.includes(ext)) return cb(null, true);
    cb(new Error('Format bukti pembayaran harus JPG, PNG, WEBP, atau PDF.'));
  }
});

// Semua route butuh authentication
router.use(verifyToken);

// Dashboard & stats (harus di atas :id)
router.get('/dashboard/stats', isAdmin, pembayaranController.getDashboardStats);
router.get('/tahun-ajaran', pembayaranController.getTahunAjaran);
router.get('/tarif-ukt', pembayaranController.getTarifUkt);
router.get('/status/:nim', pembayaranController.cekStatus);
router.get('/mahasiswa/:nim', pembayaranController.getByNim);
router.get('/:id/bukti', pembayaranController.downloadBukti);
router.post('/:id/upload-bukti', isAdmin, upload.single('bukti'), pembayaranController.uploadBukti);

// CRUD
router.get('/', pembayaranController.getAll);
router.get('/:id', pembayaranController.getById);
router.post('/', isAdmin, pembayaranController.create);
router.put('/:id', isAdmin, pembayaranController.update);
router.put('/:id/status', isAdmin, pembayaranController.updateStatus);
router.delete('/:id', isAdmin, pembayaranController.delete);

module.exports = router;
