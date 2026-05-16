// ============================================================
// ROUTES: Authentication
// ============================================================

const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { verifyToken, isAdmin } = require('../middleware/auth');

// Public routes
router.post('/login', authController.login);

// Protected routes
router.post('/logout', verifyToken, authController.logout);
router.get('/profile', verifyToken, authController.getProfile);
router.post('/register', verifyToken, isAdmin, authController.register);

module.exports = router;
