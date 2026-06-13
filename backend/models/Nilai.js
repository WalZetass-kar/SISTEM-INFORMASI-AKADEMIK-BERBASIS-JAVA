// ============================================================
// MODEL: Nilai
// Query database untuk input dan rekap nilai mahasiswa
// ============================================================

const { pool } = require('../config/database');

class Nilai {
  static calculateNilaiAkhir(nilaiTugas = 0, nilaiUts = 0, nilaiUas = 0, bobot = null) {
    const activeBobot = bobot || { bobot_tugas: 30, bobot_uts: 30, bobot_uas: 40 };
    return Number((
      (nilaiTugas * (Number(activeBobot.bobot_tugas) / 100)) +
      (nilaiUts * (Number(activeBobot.bobot_uts) / 100)) +
      (nilaiUas * (Number(activeBobot.bobot_uas) / 100))
    ).toFixed(2));
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

  static async getActiveBobotNilai(connection = pool) {
    const [rows] = await connection.execute(
      'SELECT bobot_tugas, bobot_uts, bobot_uas FROM bobot_nilai WHERE is_active = 1 ORDER BY id ASC LIMIT 1'
    );
    return rows[0] || { bobot_tugas: 30, bobot_uts: 30, bobot_uas: 40 };
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

  static async getInputList({ kode_mk, tahun_ajaran, search = '', jurusan = '' }) {
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
        k.status AS status_krs,
        n.id AS id_nilai,
        n.nilai_tugas,
        n.nilai_uts,
        n.nilai_uas,
        n.nilai_akhir,
        n.grade,
        n.tahun_ajaran,
        n.updated_at
      FROM krs k
      INNER JOIN mahasiswa m ON m.nim = k.nim
      LEFT JOIN nilai n
        ON n.nim = m.nim
        AND n.kode_mk = k.kode_mk
        AND n.tahun_ajaran = k.tahun_ajaran
      WHERE m.status = 'aktif'
        AND k.kode_mk = ?
        AND k.tahun_ajaran = ?
        AND k.status <> 'batal'
    `;
    const params = [kode_mk, tahun_ajaran];

    const jurusanFilter = String(jurusan || '').trim();
    if (jurusanFilter) {
      query += ' AND m.jurusan = ?';
      params.push(jurusanFilter);
    }

    if (search) {
      query += ' AND (m.nim LIKE ? OR m.nama LIKE ?)';
      const like = `%${search}%`;
      params.push(like, like);
    }

    query += ' ORDER BY m.nim ASC';
    const [rows] = await pool.execute(query, params);
    const bobotNilai = await this.getActiveBobotNilai();
    return { mata_kuliah: mataKuliah, bobot_nilai: bobotNilai, data: rows };
  }

  static async getRekap({ tahun_ajaran, kode_mk = '', search = '', jurusan = '' }) {
    let query = `
      SELECT
        m.nim,
        m.nama,
        m.jurusan,
        m.program_studi,
        k.kode_mk,
        mk.nama_mk,
        mk.sks,
        mk.semester,
        k.tahun_ajaran,
        k.status AS status_krs,
        n.id AS id_nilai,
        n.nilai_tugas,
        n.nilai_uts,
        n.nilai_uas,
        n.nilai_akhir,
        n.grade,
        n.updated_at,
        CASE
          WHEN n.id IS NULL THEN 'Belum Diinput'
          ELSE 'Sudah Diinput'
        END AS status_nilai
      FROM krs k
      INNER JOIN mahasiswa m ON m.nim = k.nim
      INNER JOIN mata_kuliah mk ON mk.kode_mk = k.kode_mk
      LEFT JOIN nilai n
        ON n.nim = k.nim
        AND n.kode_mk = k.kode_mk
        AND n.tahun_ajaran = k.tahun_ajaran
      WHERE m.status = 'aktif'
        AND k.tahun_ajaran = ?
        AND k.status <> 'batal'
    `;
    const params = [tahun_ajaran];

    if (kode_mk) {
      query += ' AND k.kode_mk = ?';
      params.push(kode_mk);
    }

    const jurusanFilter = String(jurusan || '').trim();
    if (jurusanFilter) {
      query += ' AND m.jurusan = ?';
      params.push(jurusanFilter);
    }

    if (search) {
      query += ' AND (m.nim LIKE ? OR m.nama LIKE ? OR k.kode_mk LIKE ? OR mk.nama_mk LIKE ?)';
      const like = `%${search}%`;
      params.push(like, like, like, like);
    }

    query += ' ORDER BY m.nim ASC, mk.semester ASC, k.kode_mk ASC';
    const [rows] = await pool.execute(query, params);

    const summary = {
      total_records: rows.length,
      sudah_diinput: 0,
      belum_diinput: 0,
      rata_rata: 0,
      grade_a: 0,
      grade_b: 0,
      grade_c: 0,
      grade_d: 0,
      grade_e: 0
    };

    let nilaiTotal = 0;
    for (const row of rows) {
      if (row.id_nilai) {
        summary.sudah_diinput += 1;
        nilaiTotal += Number(row.nilai_akhir || 0);
        const grade = String(row.grade || '').toUpperCase();
        if (grade === 'A') summary.grade_a += 1;
        if (grade === 'B') summary.grade_b += 1;
        if (grade === 'C') summary.grade_c += 1;
        if (grade === 'D') summary.grade_d += 1;
        if (grade === 'E') summary.grade_e += 1;
      } else {
        summary.belum_diinput += 1;
      }
    }

    summary.rata_rata = summary.sudah_diinput > 0
      ? Number((nilaiTotal / summary.sudah_diinput).toFixed(2))
      : 0;

    return { data: rows, summary };
  }

  static async bulkUpsert({ kode_mk, tahun_ajaran, nilai }) {
    const connection = await pool.getConnection();
    try {
      await connection.beginTransaction();
      const bobotNilai = await this.getActiveBobotNilai(connection);

      const nimList = nilai.map(item => item.nim);
      const placeholders = nimList.map(() => '?').join(', ');
      const [krsRows] = await connection.execute(
        `SELECT nim FROM krs
         WHERE kode_mk = ? AND tahun_ajaran = ? AND status <> 'batal' AND nim IN (${placeholders})`,
        [kode_mk, tahun_ajaran, ...nimList]
      );
      const allowedNim = new Set(krsRows.map(row => row.nim));

      const saved = [];
      for (const item of nilai) {
        if (!allowedNim.has(item.nim)) {
          const error = new Error(`Mahasiswa ${item.nim} belum mengambil mata kuliah ini di KRS.`);
          error.statusCode = 400;
          throw error;
        }

        const nilaiTugas = this.normalizeScore(item.nilai_tugas);
        const nilaiUts = this.normalizeScore(item.nilai_uts);
        const nilaiUas = this.normalizeScore(item.nilai_uas);
        const nilaiAkhir = this.calculateNilaiAkhir(nilaiTugas, nilaiUts, nilaiUas, bobotNilai);
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
