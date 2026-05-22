// ============================================================
// ROUTES: Akademik
// Modul KRS & Jadwal Kuliah
// ============================================================

const express = require('express');
const router = express.Router();
const akademikController = require('../controllers/akademikController');
const { verifyToken, isAdmin, isAdminOrOwner } = require('../middleware/auth');

router.use(verifyToken);

router.get('/matakuliah', akademikController.getMatakuliah);
router.post('/matakuliah', isAdmin, akademikController.createMatakuliah);

router.get('/krs', akademikController.getKrs);
router.post('/krs', isAdminOrOwner, akademikController.createKrs);

router.get('/jadwal', akademikController.getJadwal);
router.post('/jadwal', isAdmin, akademikController.createJadwal);

module.exports = router;
