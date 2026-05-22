// ============================================================
// MODEL: Matakuliah
// Query database untuk tabel mata_kuliah
// Digunakan oleh modul KRS & Jadwal Kuliah
// ============================================================

const { pool } = require('../config/database');

class Matakuliah {
  static async findAll({ search = '', semester = '' } = {}) {
    let query = `SELECT kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu
      FROM mata_kuliah WHERE 1=1`;
    const params = [];

    if (search) {
      query += ' AND (kode_mk LIKE ? OR nama_mk LIKE ?)';
      const searchParam = `%${search}%`;
      params.push(searchParam, searchParam);
    }

    if (semester) {
      query += ' AND semester = ?';
      params.push(semester);
    }

    query += ' ORDER BY semester ASC, kode_mk ASC';

    const [rows] = await pool.execute(query, params);
    return rows;
  }

  static async findByKode(kode_mk) {
    const [rows] = await pool.execute(
      `SELECT kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu
       FROM mata_kuliah WHERE kode_mk = ?`,
      [kode_mk]
    );
    return rows[0] || null;
  }

  static async create({ kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu }) {
    await pool.execute(
      `INSERT INTO mata_kuliah (kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [kode_mk, nama_mk, sks, semester, jurusan || null, dosen_pengampu || null]
    );

    return this.findByKode(kode_mk);
  }
}

module.exports = Matakuliah;
