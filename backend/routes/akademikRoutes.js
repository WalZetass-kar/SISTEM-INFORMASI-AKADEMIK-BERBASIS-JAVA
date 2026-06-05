const express = require('express');
const router = express.Router();
const akademikController = require('../controllers/akademikController');
const { verifyToken, isAdmin, isAdminOrOwner } = require('../middleware/auth');

router.use(verifyToken);

// --- Modul Pengaturan Akademik (Admin Only) ---
router.get('/akademik/settings', isAdmin, akademikController.getSettings);
router.post('/akademik/tahun-ajaran', isAdmin, akademikController.createTahunAjaran);
router.put('/akademik/tahun-ajaran/:id', isAdmin, akademikController.updateTahunAjaran);
router.delete('/akademik/tahun-ajaran/:id', isAdmin, akademikController.deleteTahunAjaran);

router.post('/akademik/mata-kuliah', isAdmin, akademikController.createMataKuliah);
router.put('/akademik/mata-kuliah/:kode_mk', isAdmin, akademikController.updateMataKuliah);
router.delete('/akademik/mata-kuliah/:kode_mk', isAdmin, akademikController.deleteMataKuliah);

router.put('/akademik/bobot-nilai', isAdmin, akademikController.updateBobotNilai);

// --- Modul KRS & Jadwal Kuliah ---
router.get('/matakuliah', akademikController.getMatakuliah);
router.post('/matakuliah', isAdmin, akademikController.createMatakuliah);

router.get('/krs', akademikController.getKrs);
router.post('/krs', isAdminOrOwner, akademikController.createKrs);

router.get('/jadwal', akademikController.getJadwal);
router.post('/jadwal', isAdmin, akademikController.createJadwal);

module.exports = router;
