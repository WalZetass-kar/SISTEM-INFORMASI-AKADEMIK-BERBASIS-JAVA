// ============================================================
// MODEL: Mahasiswa
// Query database untuk tabel mahasiswa
// Digunakan oleh: Kelompok 1 (CRUD), semua kelompok (read)
// ============================================================

const { pool } = require('../config/database');
const { normalizePagination } = require('../utils/pagination');

class Mahasiswa {
  /**
   * Ambil semua mahasiswa dengan pagination & search
   */
  static async findAll({ page = 1, limit = 10, search = '', jurusan = '', status = '' } = {}) {
    const pagination = normalizePagination({ page, limit });
    let query = 'SELECT * FROM mahasiswa WHERE 1=1';
    let countQuery = 'SELECT COUNT(*) as total FROM mahasiswa WHERE 1=1';
    const params = [];
    const countParams = [];

    if (search) {
      const searchClause = ' AND (nim LIKE ? OR nama LIKE ? OR email LIKE ?)';
      const searchParam = `%${search}%`;
      query += searchClause;
      countQuery += searchClause;
      params.push(searchParam, searchParam, searchParam);
      countParams.push(searchParam, searchParam, searchParam);
    }

    if (jurusan) {
      query += ' AND jurusan = ?';
      countQuery += ' AND jurusan = ?';
      params.push(jurusan);
      countParams.push(jurusan);
    }

    if (status) {
      query += ' AND status = ?';
      countQuery += ' AND status = ?';
      params.push(status);
      countParams.push(status);
    }

    // Count total
    const [countRows] = await pool.execute(countQuery, countParams);
    const total = countRows[0].total;

    // Pagination
    query += ' ORDER BY created_at DESC LIMIT ? OFFSET ?';
    params.push(pagination.limit, pagination.offset);

    const [rows] = await pool.execute(query, params);

    return {
      data: rows,
      pagination: {
        page: pagination.page,
        limit: pagination.limit,
        total,
        totalPages: Math.ceil(total / pagination.limit)
      }
    };
  }

  /**
   * Cari mahasiswa berdasarkan NIM
   */
  static async findByNim(nim) {
    const [rows] = await pool.execute(
      'SELECT * FROM mahasiswa WHERE nim = ?',
      [nim]
    );
    return rows[0] || null;
  }

  /**
   * Cari mahasiswa berdasarkan ID
   */
  static async findById(id) {
    const [rows] = await pool.execute(
      'SELECT * FROM mahasiswa WHERE id = ?',
      [id]
    );
    return rows[0] || null;
  }

  /**
   * Buat mahasiswa baru
   */
  static async create({ nim, nama, email, no_telp, alamat, jurusan, program_studi, angkatan, semester, status }) {
    const [result] = await pool.execute(
      `INSERT INTO mahasiswa (nim, nama, email, no_telp, alamat, jurusan, program_studi, angkatan, semester, status)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [nim, nama, email || null, no_telp || null, alamat || null, jurusan || null,
       program_studi || null, angkatan || null, semester || 1, status || 'aktif']
    );
    return { id: result.insertId, nim, nama };
  }

  /**
   * Update data mahasiswa
   */
  static async update(nim, data) {
    const fields = [];
    const params = [];

    const allowedFields = ['nama', 'email', 'no_telp', 'alamat', 'jurusan',
      'program_studi', 'angkatan', 'semester', 'status', 'foto_url'];

    for (const field of allowedFields) {
      if (data[field] !== undefined) {
        fields.push(`${field} = ?`);
        params.push(data[field]);
      }
    }

    if (fields.length === 0) {
      return false;
    }

    params.push(nim);
    const [result] = await pool.execute(
      `UPDATE mahasiswa SET ${fields.join(', ')} WHERE nim = ?`,
      params
    );
    return result.affectedRows > 0;
  }

  /**
   * Hapus mahasiswa berdasarkan NIM
   */
  static async delete(nim) {
    const [result] = await pool.execute(
      'DELETE FROM mahasiswa WHERE nim = ?',
      [nim]
    );
    return result.affectedRows > 0;
  }

  /**
   * Hitung total mahasiswa per jurusan (untuk statistik)
   */
  static async countByJurusan() {
    const [rows] = await pool.execute(
      'SELECT jurusan, COUNT(*) as total FROM mahasiswa GROUP BY jurusan ORDER BY total DESC'
    );
    return rows;
  }

  /**
   * Hitung total mahasiswa per status
   */
  static async countByStatus() {
    const [rows] = await pool.execute(
      'SELECT status, COUNT(*) as total FROM mahasiswa GROUP BY status'
    );
    return rows;
  }

  /**
   * Hitung total mahasiswa per angkatan
   */
  static async countByAngkatan() {
    const [rows] = await pool.execute(
      'SELECT angkatan, COUNT(*) as total FROM mahasiswa GROUP BY angkatan ORDER BY angkatan DESC'
    );
    return rows;
  }

  /**
   * Ambil semua jurusan unik
   */
  static async getJurusanList() {
    const [rows] = await pool.execute(
      'SELECT DISTINCT jurusan FROM mahasiswa WHERE jurusan IS NOT NULL ORDER BY jurusan'
    );
    return rows.map(r => r.jurusan);
  }
}

module.exports = Mahasiswa;
