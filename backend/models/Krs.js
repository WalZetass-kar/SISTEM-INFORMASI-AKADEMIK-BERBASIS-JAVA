// ============================================================
// MODEL: KRS
// Query database untuk tabel krs
// Digunakan oleh modul KRS & Jadwal Kuliah
// ============================================================

const { pool } = require('../config/database');

class Krs {
  static async findAll({ nim = '', tahun_ajaran = '', kode_mk = '' } = {}) {
    let query = `SELECT
        k.id AS id_krs,
        k.nim,
        m.nama AS nama_mahasiswa,
        k.kode_mk,
        mk.nama_mk,
        mk.sks,
        k.semester,
        mk.semester AS semester_mata_kuliah,
        k.tahun_ajaran,
        k.status,
        COALESCE(
          GROUP_CONCAT(
            DISTINCT CONCAT(
              UPPER(LEFT(j.hari, 1)),
              SUBSTRING(j.hari, 2),
              ' ',
              TIME_FORMAT(j.jam_mulai, '%H:%i'),
              '-',
              TIME_FORMAT(j.jam_selesai, '%H:%i')
            )
            ORDER BY FIELD(j.hari, 'senin', 'selasa', 'rabu', 'kamis', 'jumat', 'sabtu'), j.jam_mulai
            SEPARATOR ' | '
          ),
          '-'
        ) AS jadwal,
        COALESCE(
          GROUP_CONCAT(
            DISTINCT COALESCE(j.ruangan, '-')
            ORDER BY FIELD(j.hari, 'senin', 'selasa', 'rabu', 'kamis', 'jumat', 'sabtu'), j.jam_mulai
            SEPARATOR ', '
          ),
          '-'
        ) AS ruangan,
        COALESCE(
          GROUP_CONCAT(
            DISTINCT COALESCE(j.dosen, mk.dosen_pengampu, '-')
            ORDER BY FIELD(j.hari, 'senin', 'selasa', 'rabu', 'kamis', 'jumat', 'sabtu'), j.jam_mulai
            SEPARATOR ', '
          ),
          COALESCE(mk.dosen_pengampu, '-')
        ) AS dosen
      FROM krs k
      INNER JOIN mahasiswa m ON k.nim = m.nim
      INNER JOIN mata_kuliah mk ON k.kode_mk = mk.kode_mk
      LEFT JOIN jadwal j ON k.kode_mk = j.kode_mk
      WHERE 1=1`;
    const params = [];

    if (nim) {
      query += ' AND k.nim = ?';
      params.push(nim);
    }

    if (tahun_ajaran) {
      query += ' AND k.tahun_ajaran = ?';
      params.push(tahun_ajaran);
    }

    if (kode_mk) {
      query += ' AND k.kode_mk = ?';
      params.push(kode_mk);
    }

    query += ` GROUP BY
      k.id, k.nim, m.nama, k.kode_mk, mk.nama_mk, mk.sks, k.semester, mk.semester, k.tahun_ajaran, k.status
      ORDER BY k.tahun_ajaran DESC, k.semester ASC, k.kode_mk ASC`;

    const [rows] = await pool.execute(query, params);
    return rows;
  }

  static async findByUnique(nim, kode_mk, tahun_ajaran) {
    const [rows] = await pool.execute(
      `SELECT id AS id_krs, nim, kode_mk, semester, tahun_ajaran, status
       FROM krs WHERE nim = ? AND kode_mk = ? AND tahun_ajaran = ?`,
      [nim, kode_mk, tahun_ajaran]
    );
    return rows[0] || null;
  }

  static async create({ nim, kode_mk, semester, tahun_ajaran, status }) {
    const [result] = await pool.execute(
      `INSERT INTO krs (nim, kode_mk, semester, tahun_ajaran, status)
       VALUES (?, ?, ?, ?, ?)`,
      [nim, kode_mk, semester, tahun_ajaran, status || 'diambil']
    );

    const rows = await this.findAll({ nim, tahun_ajaran, kode_mk });
    return rows.find(item => item.id_krs === result.insertId) || null;
  }
}

module.exports = Krs;
