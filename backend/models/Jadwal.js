// ============================================================
// MODEL: Jadwal
// Query database untuk tabel jadwal
// Digunakan oleh modul KRS & Jadwal Kuliah
// ============================================================

const { pool } = require('../config/database');

class Jadwal {
  static async findAll({ kode_mk = '', hari = '', semester = '' } = {}) {
    let query = `SELECT
        j.id AS id_jadwal,
        j.kode_mk,
        mk.nama_mk,
        mk.sks,
        mk.semester,
        j.hari,
        TIME_FORMAT(j.jam_mulai, '%H:%i') AS jam_mulai,
        TIME_FORMAT(j.jam_selesai, '%H:%i') AS jam_selesai,
        CONCAT(TIME_FORMAT(j.jam_mulai, '%H:%i'), ' - ', TIME_FORMAT(j.jam_selesai, '%H:%i')) AS jam,
        j.ruangan,
        COALESCE(j.dosen, mk.dosen_pengampu) AS dosen
      FROM jadwal j
      INNER JOIN mata_kuliah mk ON j.kode_mk = mk.kode_mk
      WHERE 1=1`;
    const params = [];

    if (kode_mk) {
      query += ' AND j.kode_mk = ?';
      params.push(kode_mk);
    }

    if (hari) {
      query += ' AND j.hari = ?';
      params.push(hari);
    }

    if (semester) {
      query += ' AND mk.semester = ?';
      params.push(semester);
    }

    query += ` ORDER BY FIELD(j.hari, 'senin', 'selasa', 'rabu', 'kamis', 'jumat', 'sabtu'),
      j.jam_mulai ASC, j.kode_mk ASC`;

    const [rows] = await pool.execute(query, params);
    return rows;
  }

  static async create({ kode_mk, hari, jam_mulai, jam_selesai, ruangan, dosen }) {
    const [result] = await pool.execute(
      `INSERT INTO jadwal (kode_mk, hari, jam_mulai, jam_selesai, ruangan, dosen)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [kode_mk, hari, jam_mulai, jam_selesai, ruangan || null, dosen || null]
    );

    const [rows] = await pool.execute(
      `SELECT
          j.id AS id_jadwal,
          j.kode_mk,
          mk.nama_mk,
          mk.sks,
          mk.semester,
          j.hari,
          TIME_FORMAT(j.jam_mulai, '%H:%i') AS jam_mulai,
          TIME_FORMAT(j.jam_selesai, '%H:%i') AS jam_selesai,
          CONCAT(TIME_FORMAT(j.jam_mulai, '%H:%i'), ' - ', TIME_FORMAT(j.jam_selesai, '%H:%i')) AS jam,
          j.ruangan,
          COALESCE(j.dosen, mk.dosen_pengampu) AS dosen
        FROM jadwal j
        INNER JOIN mata_kuliah mk ON j.kode_mk = mk.kode_mk
        WHERE j.id = ?`,
      [result.insertId]
    );

    return rows[0] || null;
  }

  static async update(id, { kode_mk, hari, jam_mulai, jam_selesai, ruangan, dosen }) {
    const [result] = await pool.execute(
      `UPDATE jadwal
       SET kode_mk = ?, hari = ?, jam_mulai = ?, jam_selesai = ?, ruangan = ?, dosen = ?
       WHERE id = ?`,
      [kode_mk, hari, jam_mulai, jam_selesai, ruangan || null, dosen || null, id]
    );
    if (result.affectedRows === 0) return null;

    const [rows] = await pool.execute(
      `SELECT
          j.id AS id_jadwal,
          j.kode_mk,
          mk.nama_mk,
          mk.sks,
          mk.semester,
          j.hari,
          TIME_FORMAT(j.jam_mulai, '%H:%i') AS jam_mulai,
          TIME_FORMAT(j.jam_selesai, '%H:%i') AS jam_selesai,
          CONCAT(TIME_FORMAT(j.jam_mulai, '%H:%i'), ' - ', TIME_FORMAT(j.jam_selesai, '%H:%i')) AS jam,
          j.ruangan,
          COALESCE(j.dosen, mk.dosen_pengampu) AS dosen
        FROM jadwal j
        INNER JOIN mata_kuliah mk ON j.kode_mk = mk.kode_mk
        WHERE j.id = ?`,
      [id]
    );

    return rows[0] || null;
  }

  static async delete(id) {
    const [result] = await pool.execute('DELETE FROM jadwal WHERE id = ?', [id]);
    return result.affectedRows > 0;
  }
}

module.exports = Jadwal;
