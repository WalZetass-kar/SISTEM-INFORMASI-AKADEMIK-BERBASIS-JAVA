const express = require('express');
const router = express.Router();
const akademikController = require('../controllers/akademikController');
const { verifyToken, isAdmin } = require('../middleware/auth');

router.use(verifyToken);
router.use(isAdmin);

router.get('/settings', akademikController.getSettings);

router.post('/tahun-ajaran', akademikController.createTahunAjaran);
router.put('/tahun-ajaran/:id', akademikController.updateTahunAjaran);
router.delete('/tahun-ajaran/:id', akademikController.deleteTahunAjaran);

router.post('/mata-kuliah', akademikController.createMataKuliah);
router.put('/mata-kuliah/:kode_mk', akademikController.updateMataKuliah);
router.delete('/mata-kuliah/:kode_mk', akademikController.deleteMataKuliah);

router.put('/bobot-nilai', akademikController.updateBobotNilai);

module.exports = router;
