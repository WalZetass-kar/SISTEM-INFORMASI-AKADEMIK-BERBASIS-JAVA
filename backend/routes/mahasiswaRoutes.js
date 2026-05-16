// ============================================================
// ROUTES: Mahasiswa
// Kelompok 1 - CRUD Data Mahasiswa
// ============================================================

const express = require('express');
const router = express.Router();
const mahasiswaController = require('../controllers/mahasiswaController');
const { verifyToken, isAdmin } = require('../middleware/auth');
const multer = require('multer');
const path = require('path');

// Konfigurasi Multer untuk Upload Foto
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/mahasiswa/');
  },
  filename: (req, file, cb) => {
    const nim = req.params.nim || 'unknown';
    cb(null, `foto-${nim}-${Date.now()}${path.extname(file.originalname)}`);
  }
});

const upload = multer({ 
  storage: storage,
  limits: { fileSize: 2 * 1024 * 1024 }, // Max 2MB
  fileFilter: (req, file, cb) => {
    const filetypes = /jpeg|jpg|png/;
    const mimetype = filetypes.test(file.mimetype);
    const extname = filetypes.test(path.extname(file.originalname).toLowerCase());
    if (mimetype && extname) return cb(null, true);
    cb(new Error('Format file harus JPG/PNG!'));
  }
});

// Semua route butuh authentication
router.use(verifyToken);

// Stats
router.get('/stats/jurusan', isAdmin, mahasiswaController.statsByJurusan);
router.get('/stats/status', isAdmin, mahasiswaController.statsByStatus);
router.get('/jurusan/list', mahasiswaController.getJurusanList);

// CRUD
router.get('/', mahasiswaController.getAll);
router.get('/:nim', mahasiswaController.getByNim);
router.post('/', isAdmin, mahasiswaController.create);
router.put('/:nim', isAdmin, mahasiswaController.update);
router.delete('/:nim', isAdmin, mahasiswaController.delete);

// Route khusus upload foto
router.post('/:nim/upload-foto', isAdmin, upload.single('foto'), (req, res) => {
  if (!req.file) return res.status(400).json({ success: false, message: 'File tidak ditemukan.' });
  
  // Simpan path ke DB
  const fotoUrl = `/uploads/mahasiswa/${req.file.filename}`;
  const Mahasiswa = require('../models/Mahasiswa');
  
  Mahasiswa.update(req.params.nim, { foto_url: fotoUrl })
    .then(updated => {
      res.json({ success: true, message: 'Foto berhasil diupload!', foto_url: fotoUrl });
    })
    .catch(err => {
      res.status(500).json({ success: false, message: 'Gagal update database.' });
    });
});

module.exports = router;
