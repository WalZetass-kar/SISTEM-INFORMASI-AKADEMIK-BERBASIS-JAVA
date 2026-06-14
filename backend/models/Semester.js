const { pool } = require('../config/database');

class Semester {
  static async ensureTable() {
    await pool.execute(`
      CREATE TABLE IF NOT EXISTS semester (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nomor INT NOT NULL UNIQUE,
        nama_semester VARCHAR(50) NOT NULL,
        is_active TINYINT(1) NOT NULL DEFAULT 1,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB
    `);

    const [rows] = await pool.execute('SELECT COUNT(*) AS total FROM semester');
    if (Number(rows[0]?.total || 0) === 0) {
      const values = [];
      for (let i = 1; i <= 8; i++) {
        values.push([i, `Semester ${i}`]);
      }
      await pool.query('INSERT INTO semester (nomor, nama_semester) VALUES ?', [values]);
    }
  }

  static async findAll({ activeOnly = false } = {}) {
    await this.ensureTable();
    let query = 'SELECT id, nomor, nama_semester, is_active, created_at, updated_at FROM semester';
    const params = [];
    if (activeOnly) {
      query += ' WHERE is_active = 1';
    }
    query += ' ORDER BY nomor ASC';
    const [rows] = await pool.execute(query, params);
    return rows;
  }

  static async create({ nomor, nama_semester, is_active }) {
    await this.ensureTable();
    const normalizedNomor = Number(nomor);
    if (!Number.isInteger(normalizedNomor) || normalizedNomor < 1 || normalizedNomor > 20) {
      const error = new Error('Nomor semester harus berupa angka 1-20.');
      error.statusCode = 400;
      throw error;
    }

    const nama = String(nama_semester || `Semester ${normalizedNomor}`).trim();
    if (!nama) {
      const error = new Error('Nama semester wajib diisi.');
      error.statusCode = 400;
      throw error;
    }

    await pool.execute(
      `INSERT INTO semester (nomor, nama_semester, is_active)
       VALUES (?, ?, ?)
       ON DUPLICATE KEY UPDATE
         nama_semester = VALUES(nama_semester),
         is_active = VALUES(is_active)`,
      [normalizedNomor, nama, is_active === 0 ? 0 : 1]
    );

    const [rows] = await pool.execute('SELECT * FROM semester WHERE nomor = ?', [normalizedNomor]);
    return rows[0];
  }

  static async update(id, data) {
    await this.ensureTable();
    const fields = [];
    const params = [];

    if (data.nomor !== undefined) {
      const nomor = Number(data.nomor);
      if (!Number.isInteger(nomor) || nomor < 1 || nomor > 20) {
        const error = new Error('Nomor semester harus berupa angka 1-20.');
        error.statusCode = 400;
        throw error;
      }
      fields.push('nomor = ?');
      params.push(nomor);
    }
    if (data.nama_semester !== undefined) {
      const nama = String(data.nama_semester || '').trim();
      if (!nama) {
        const error = new Error('Nama semester wajib diisi.');
        error.statusCode = 400;
        throw error;
      }
      fields.push('nama_semester = ?');
      params.push(nama);
    }
    if (data.is_active !== undefined) {
      fields.push('is_active = ?');
      params.push(Number(data.is_active) === 1 ? 1 : 0);
    }

    if (!fields.length) return false;
    params.push(id);
    const [result] = await pool.execute(`UPDATE semester SET ${fields.join(', ')} WHERE id = ?`, params);
    return result.affectedRows > 0;
  }

  static async delete(id) {
    await this.ensureTable();
    const [result] = await pool.execute('UPDATE semester SET is_active = 0 WHERE id = ?', [id]);
    return result.affectedRows > 0;
  }
}

module.exports = Semester;
