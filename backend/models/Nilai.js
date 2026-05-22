// ============================================================
// MODEL: Nilai
// Query database untuk input dan rekap nilai mahasiswa
// ============================================================

const { pool } = require('../config/database');

class Nilai {
  static calculateNilaiAkhir(nilaiTugas = 0, nilaiUts = 0, nilaiUas = 0) {
    return Number(((nilaiTugas * 0.3) + (nilaiUts * 0.3) + (nilaiUas * 0.4)).toFixed(2));
  }

  static calculateGrade(nilaiAkhir = 0) {
    if (nilaiAkhir >= 85) return 'A';
    if (nilaiAkhir >= 70) return 'B';
    if (nilaiAkhir >= 60) return 'C';
    if (nilaiAkhir >= 50) return 'D';
    return 'E';
  }

  static normalizeScore(value) {
    const score = Number(value);
    if (!Number.isFinite(score)) return 0;
    if (score < 0) return 0;
    if (score > 100) return 100;
    return Number(score.toFixed(2));
  }

  static async getMataKuliahList({ semester = '', search = '' } = {}) {
    let query = 'SELECT kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu FROM mata_kuliah WHERE 1=1';
    const params = [];

    if (semester) {
      query += ' AND semester = ?';
      params.push(semester);
    }

    if (search) {
      query += ' AND (kode_mk LIKE ? OR nama_mk LIKE ? OR jurusan LIKE ?)';
      const like = `%${search}%`;
      params.push(like, like, like);
    }

    query += ' ORDER BY semester ASC, kode_mk ASC';
    const [rows] = await pool.execute(query, params);
    return rows;
  }

  static async getInputList({ kode_mk, tahun_ajaran, search = '' }) {
    const [mkRows] = await pool.execute(
      'SELECT kode_mk, nama_mk, semester, jurusan FROM mata_kuliah WHERE kode_mk = ?',
      [kode_mk]
    );
    const mataKuliah = mkRows[0] || null;
    if (!mataKuliah) return { mata_kuliah: null, data: [] };

    let query = `
      SELECT
        m.nim,
        m.nama,
        m.jurusan,
        m.program_studi,
        m.semester,
        n.id AS id_nilai,
        n.nilai_tugas,
        n.nilai_uts,
        n.nilai_uas,
        n.nilai_akhir,
        n.grade,
        n.tahun_ajaran,
        n.updated_at
      FROM mahasiswa m
      LEFT JOIN nilai n
        ON n.nim = m.nim
        AND n.kode_mk = ?
        AND n.tahun_ajaran = ?
      WHERE m.status = 'aktif'
    `;
    const params = [kode_mk, tahun_ajaran];

    if (mataKuliah.jurusan) {
      query += ' AND m.jurusan = ?';
      params.push(mataKuliah.jurusan);
    }

    if (search) {
      query += ' AND (m.nim LIKE ? OR m.nama LIKE ?)';
      const like = `%${search}%`;
      params.push(like, like);
    }

    query += ' ORDER BY m.nim ASC';
    const [rows] = await pool.execute(query, params);
    return { mata_kuliah: mataKuliah, data: rows };
  }

  static async bulkUpsert({ kode_mk, tahun_ajaran, nilai }) {
    const connection = await pool.getConnection();
    try {
      await connection.beginTransaction();

      const saved = [];
      for (const item of nilai) {
        const nilaiTugas = this.normalizeScore(item.nilai_tugas);
        const nilaiUts = this.normalizeScore(item.nilai_uts);
        const nilaiUas = this.normalizeScore(item.nilai_uas);
        const nilaiAkhir = this.calculateNilaiAkhir(nilaiTugas, nilaiUts, nilaiUas);
        const grade = this.calculateGrade(nilaiAkhir);

        await connection.execute(
          `INSERT INTO nilai (nim, kode_mk, nilai_tugas, nilai_uts, nilai_uas, nilai_akhir, grade, tahun_ajaran)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?)
           ON DUPLICATE KEY UPDATE
             nilai_tugas = VALUES(nilai_tugas),
             nilai_uts = VALUES(nilai_uts),
             nilai_uas = VALUES(nilai_uas),
             nilai_akhir = VALUES(nilai_akhir),
             grade = VALUES(grade)`,
          [item.nim, kode_mk, nilaiTugas, nilaiUts, nilaiUas, nilaiAkhir, grade, tahun_ajaran]
        );

        saved.push({
          nim: item.nim,
          nilai_tugas: nilaiTugas,
          nilai_uts: nilaiUts,
          nilai_uas: nilaiUas,
          nilai_akhir: nilaiAkhir,
          grade
        });
      }

      await connection.commit();
      return saved;
    } catch (error) {
      await connection.rollback();
      throw error;
    } finally {
      connection.release();
    }
  }

  static async deleteByKey({ nim, kode_mk, tahun_ajaran }) {
    const [result] = await pool.execute(
      'DELETE FROM nilai WHERE nim = ? AND kode_mk = ? AND tahun_ajaran = ?',
      [nim, kode_mk, tahun_ajaran]
    );
    return result.affectedRows > 0;
  }

  static async findByNim(nim) {
    const [rows] = await pool.execute(
      `SELECT
        n.id,
        n.nim,
        n.kode_mk,
        mk.nama_mk,
        mk.sks,
        mk.semester,
        mk.jurusan,
        n.nilai_tugas,
        n.nilai_uts,
        n.nilai_uas,
        n.nilai_akhir,
        n.grade,
        n.tahun_ajaran,
        n.updated_at
       FROM nilai n
       LEFT JOIN mata_kuliah mk ON mk.kode_mk = n.kode_mk
       WHERE n.nim = ?
       ORDER BY n.tahun_ajaran DESC, mk.semester ASC, n.kode_mk ASC`,
      [nim]
    );
    return rows;
  }
}

module.exports = Nilai;
