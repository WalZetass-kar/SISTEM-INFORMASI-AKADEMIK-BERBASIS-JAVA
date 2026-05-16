// ============================================================
// MODEL: User
// Query database untuk tabel users
// Digunakan oleh: Modul Login & Authentication
// ============================================================

const { pool } = require('../config/database');

class User {
  /**
   * Cari user berdasarkan username
   */
  static async findByUsername(username) {
    const [rows] = await pool.execute(
      'SELECT id, username, password, role, nim, is_active FROM users WHERE username = ?',
      [username]
    );
    return rows[0] || null;
  }

  /**
   * Cari user berdasarkan ID
   */
  static async findById(id) {
    const [rows] = await pool.execute(
      'SELECT id, username, role, nim, is_active, created_at FROM users WHERE id = ?',
      [id]
    );
    return rows[0] || null;
  }

  /**
   * Cari user berdasarkan NIM
   */
  static async findByNim(nim) {
    const [rows] = await pool.execute(
      'SELECT id, username, role, nim, is_active, created_at FROM users WHERE nim = ?',
      [nim]
    );
    return rows[0] || null;
  }

  /**
   * Buat user baru
   */
  static async create({ username, password, role, nim }) {
    const [result] = await pool.execute(
      'INSERT INTO users (username, password, role, nim) VALUES (?, ?, ?, ?)',
      [username, password, role, nim || null]
    );
    return { id: result.insertId, username, role, nim };
  }

  /**
   * Update password user
   */
  static async updatePassword(id, hashedPassword) {
    const [result] = await pool.execute(
      'UPDATE users SET password = ? WHERE id = ?',
      [hashedPassword, id]
    );
    return result.affectedRows > 0;
  }

  /**
   * Ambil semua user (admin view)
   */
  static async findAll() {
    const [rows] = await pool.execute(
      'SELECT id, username, role, nim, is_active, created_at FROM users ORDER BY created_at DESC'
    );
    return rows;
  }

  /**
   * Nonaktifkan user
   */
  static async deactivate(id) {
    const [result] = await pool.execute(
      'UPDATE users SET is_active = 0 WHERE id = ?',
      [id]
    );
    return result.affectedRows > 0;
  }

  /**
   * Aktifkan user
   */
  static async activate(id) {
    const [result] = await pool.execute(
      'UPDATE users SET is_active = 1 WHERE id = ?',
      [id]
    );
    return result.affectedRows > 0;
  }
}

module.exports = User;
