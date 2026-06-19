const express = require('express');
const router = express.Router();
const akademikController = require('../controllers/akademikController');
const { verifyToken, isAdmin, isMahasiswa, isAdminOrOwner } = require('../middleware/auth');

router.use(verifyToken);

router.get('/akademik/semester', akademikController.getSemester);
router.get('/akademik/tahun-ajaran', akademikController.getTahunAjaran);

// --- Modul Pengaturan Akademik (Admin Only) ---
router.get('/akademik/settings', isAdmin, akademikController.getSettings);
router.post('/akademik/tahun-ajaran', isAdmin, akademikController.createTahunAjaran);
router.put('/akademik/tahun-ajaran/:id', isAdmin, akademikController.updateTahunAjaran);
router.delete('/akademik/tahun-ajaran/:id', isAdmin, akademikController.deleteTahunAjaran);

router.post('/akademik/semester', isAdmin, akademikController.createSemester);
router.put('/akademik/semester/:id', isAdmin, akademikController.updateSemester);
router.delete('/akademik/semester/:id', isAdmin, akademikController.deleteSemester);

router.post('/akademik/mata-kuliah', isAdmin, akademikController.createMataKuliah);
router.put('/akademik/mata-kuliah/:kode_mk', isAdmin, akademikController.updateMataKuliah);
router.delete('/akademik/mata-kuliah/:kode_mk', isAdmin, akademikController.deleteMataKuliah);

router.post('/akademik/jurusan', isAdmin, akademikController.createJurusan);
router.put('/akademik/jurusan/:id', isAdmin, akademikController.updateJurusan);
router.delete('/akademik/jurusan/:id', isAdmin, akademikController.deleteJurusan);

router.put('/akademik/bobot-nilai', isAdmin, akademikController.updateBobotNilai);
router.put('/akademik/jumlah-pertemuan', isAdmin, akademikController.updateJumlahPertemuan);
router.put('/akademik/jumlah-pertemuan-jurusan', isAdmin, akademikController.updateJumlahPertemuanJurusan);
router.delete('/akademik/jumlah-pertemuan-jurusan', isAdmin, akademikController.deleteJumlahPertemuanJurusan);
router.get('/akademik/kehadiran/input-list', isAdmin, akademikController.getKehadiranInputList);
router.get('/akademik/kehadiran/rekap', isAdmin, akademikController.getKehadiranRekap);
router.post('/akademik/kehadiran/bulk', isAdmin, akademikController.bulkSaveKehadiran);
router.get('/akademik/kehadiran/saya', isMahasiswa, akademikController.getKehadiranSaya);
router.get('/akademik/info-saya', isMahasiswa, akademikController.getInfoAkademikSaya);

// --- Modul KRS & Jadwal Kuliah ---
router.get('/matakuliah', akademikController.getMatakuliah);
router.post('/matakuliah', isAdmin, akademikController.createMatakuliah);
router.put('/matakuliah/:kode_mk', isAdmin, akademikController.updateMatakuliah);
router.delete('/matakuliah/:kode_mk', isAdmin, akademikController.deleteMatakuliah);

router.get('/krs', akademikController.getKrs);
router.post('/krs', isAdminOrOwner, akademikController.createKrs);

router.get('/jadwal', akademikController.getJadwal);
router.post('/jadwal', isAdmin, akademikController.createJadwal);
router.put('/jadwal/:id', isAdmin, akademikController.updateJadwal);
router.delete('/jadwal/:id', isAdmin, akademikController.deleteJadwal);

module.exports = router;
