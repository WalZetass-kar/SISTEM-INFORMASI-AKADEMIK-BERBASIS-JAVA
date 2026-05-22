// ============================================================
// ROUTES: Nilai
// Modul Akademik - Input Nilai
// ============================================================

const express = require('express');
const router = express.Router();
const nilaiController = require('../controllers/nilaiController');
const { verifyToken, isAdmin } = require('../middleware/auth');

router.use(verifyToken);

router.get('/mata-kuliah', nilaiController.getMataKuliah);
router.get('/saya', nilaiController.getMyNilai);
router.get('/input-list', isAdmin, nilaiController.getInputList);
router.post('/bulk', isAdmin, nilaiController.bulkSave);
router.delete('/', isAdmin, nilaiController.delete);

module.exports = router;
