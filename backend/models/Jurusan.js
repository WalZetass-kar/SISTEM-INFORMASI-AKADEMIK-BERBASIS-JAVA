const { pool } = require('../config/database');

class Jurusan {
  static async ensureTable() {
    await pool.execute(`
      CREATE TABLE IF NOT EXISTS jurusan (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nama_jurusan VARCHAR(100) NOT NULL UNIQUE,
        is_active TINYINT(1) NOT NULL DEFAULT 1,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB
    `);
    const [columns] = await pool.execute("SHOW COLUMNS FROM jurusan LIKE 'is_active'");
    if (columns.length === 0) {
      await pool.execute('ALTER TABLE jurusan ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1 AFTER nama_jurusan');
    }
    await pool.execute(`
      CREATE TABLE IF NOT EXISTS akademik_pertemuan_jurusan (
        id INT AUTO_INCREMENT PRIMARY KEY,
        jurusan VARCHAR(100) NOT NULL UNIQUE,
        jumlah_pertemuan INT NOT NULL DEFAULT 12,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB
    `);
  }

  static async findAll() {
    await this.ensureTable();
    await this.syncFromExistingData();
    const [rows] = await pool.execute(
      'SELECT id, nama_jurusan, is_active, created_at, updated_at FROM jurusan ORDER BY is_active DESC, nama_jurusan ASC'
    );
    return rows;
  }

  static async findAllNames() {
    await this.ensureTable();
    await this.syncFromExistingData();
    const [rows] = await pool.execute(
      'SELECT nama_jurusan FROM jurusan WHERE is_active = 1 ORDER BY nama_jurusan ASC'
    );
    return rows.map(row => row.nama_jurusan);
  }

  static async create(data) {
    await this.ensureTable();
    const nama = this.normalize(data?.nama_jurusan || data?.jurusan || data);
    if (!nama) {
      const error = new Error('Nama jurusan wajib diisi.');
      error.statusCode = 400;
      throw error;
    }
    await pool.execute(
      `INSERT INTO jurusan (nama_jurusan, is_active)
       VALUES (?, 1)
       ON DUPLICATE KEY UPDATE is_active = 1, updated_at = CURRENT_TIMESTAMP`,
      [nama]
    );
    const [rows] = await pool.execute(
      'SELECT id, nama_jurusan, is_active FROM jurusan WHERE nama_jurusan = ?',
      [nama]
    );
    return rows[0];
  }

  static async update(id, data) {
    await this.ensureTable();
    const nama = this.normalize(data?.nama_jurusan || data?.jurusan || data);
    const isActive = this.normalizeActive(data?.is_active);
    if (!nama) {
      const error = new Error('Nama jurusan wajib diisi.');
      error.statusCode = 400;
      throw error;
    }

    const connection = await pool.getConnection();
    try {
      await connection.beginTransaction();
      const [rows] = await connection.execute('SELECT nama_jurusan FROM jurusan WHERE id = ?', [id]);
      if (!rows[0]) {
        await connection.rollback();
        return false;
      }

      const oldName = rows[0].nama_jurusan;
      await connection.execute('UPDATE jurusan SET nama_jurusan = ?, is_active = ? WHERE id = ?', [nama, isActive, id]);
      await connection.execute('UPDATE mahasiswa SET jurusan = ? WHERE jurusan = ?', [nama, oldName]);
      await connection.execute('UPDATE mata_kuliah SET jurusan = ? WHERE jurusan = ?', [nama, oldName]);
      await connection.execute('UPDATE akademik_pertemuan_jurusan SET jurusan = ? WHERE jurusan = ?', [nama, oldName]);
      await connection.commit();
      return { id: Number(id), nama_jurusan: nama, is_active: isActive };
    } catch (error) {
      await connection.rollback();
      throw error;
    } finally {
      connection.release();
    }
  }

  static async delete(id) {
    await this.ensureTable();
    const [rows] = await pool.execute('SELECT nama_jurusan FROM jurusan WHERE id = ?', [id]);
    if (!rows[0]) return false;

    const [result] = await pool.execute('UPDATE jurusan SET is_active = 0 WHERE id = ?', [id]);
    return { deactivated: result.affectedRows > 0, deleted_mahasiswa: 0 };
  }

  static async syncFromExistingData() {
    await pool.execute(`
      INSERT IGNORE INTO jurusan (nama_jurusan)
      SELECT DISTINCT jurusan FROM mahasiswa WHERE jurusan IS NOT NULL AND jurusan <> ''
    `);
    await pool.execute(`
      INSERT IGNORE INTO jurusan (nama_jurusan)
      SELECT DISTINCT jurusan FROM mata_kuliah WHERE jurusan IS NOT NULL AND jurusan <> ''
    `);
    await pool.execute(`
      INSERT IGNORE INTO jurusan (nama_jurusan)
      SELECT DISTINCT jurusan FROM akademik_pertemuan_jurusan WHERE jurusan IS NOT NULL AND jurusan <> ''
    `);
  }

  static normalize(value) {
    return String(value || '').trim().replace(/\s+/g, ' ');
  }

  static normalizeActive(value) {
    if (value === undefined || value === null) return 1;
    if (typeof value === 'boolean') return value ? 1 : 0;
    const normalized = String(value).trim().toLowerCase();
    return ['1', 'true', 'aktif', 'active'].includes(normalized) ? 1 : 0;
  }
}

module.exports = Jurusan;
