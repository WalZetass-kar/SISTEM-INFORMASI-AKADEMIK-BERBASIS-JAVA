// ============================================================
// MIDDLEWARE: Input Validation
// Validasi input untuk mencegah SQL Injection & data invalid
// ============================================================

const { validationResult } = require('express-validator');

/**
 * Middleware: Handle validation errors dari express-validator
 */
const handleValidation = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: 'Validasi gagal',
      errors: errors.array().map(err => ({
        field: err.path,
        message: err.msg
      }))
    });
  }
  next();
};

/**
 * Sanitize string input - mencegah XSS
 */
const sanitizeString = (str) => {
  if (typeof str !== 'string') return str;
  return str
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .trim();
};

module.exports = {
  handleValidation,
  sanitizeString
};
