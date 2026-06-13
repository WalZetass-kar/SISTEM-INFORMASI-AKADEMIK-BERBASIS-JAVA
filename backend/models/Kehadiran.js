// ============================================================
// MODEL: Kehadiran
// Query database untuk input absensi/kehadiran mahasiswa
// ============================================================

const { pool } = require('../config/database');

class Kehadiran {
  static validStatuses() {
    return ['hadir', 'izin', 'sakit', 'alpha'];
  }

  static normalizeStatus(value) {
    const status = String(value || '').trim().toLowerCase();
    return this.validStatuses().includes(status) ? status : 'hadir';
  }

  static async getInputList({ kode_mk, tahun_ajaran, tanggal, search = '', jurusan = '' }) {
    const [mkRows] = await pool.execute(
      'SELECT kode_mk, nama_mk, semester, jurusan, dosen_pengampu FROM mata_kuliah WHERE kode_mk = ?',
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
        kh.id AS id_kehadiran,
        kh.tanggal,
        kh.pertemuan,
        COALESCE(kh.status, 'hadir') AS status_kehadiran,
        kh.keterangan,
        kh.updated_at
      FROM krs k
      INNER JOIN mahasiswa m ON m.nim = k.nim
      LEFT JOIN kehadiran kh
        ON kh.nim = m.nim
        AND kh.kode_mk = k.kode_mk
        AND kh.tahun_ajaran = k.tahun_ajaran
        AND kh.tanggal = ?
      WHERE m.status = 'aktif'
        AND k.kode_mk = ?
        AND k.tahun_ajaran = ?
        AND k.status <> 'batal'
    `;
    const params = [tanggal, kode_mk, tahun_ajaran];

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
    return { mata_kuliah: mataKuliah, data: rows };
  }

  static async bulkUpsert({ kode_mk, tahun_ajaran, tanggal, pertemuan, items, input_by }) {
    const connection = await pool.getConnection();
    try {
      await connection.beginTransaction();

      const nimList = items.map(item => item.nim).filter(Boolean);
      if (nimList.length === 0) {
        const error = new Error('Data kehadiran masih kosong.');
        error.statusCode = 400;
        throw error;
      }

      const placeholders = nimList.map(() => '?').join(', ');
      const [krsRows] = await connection.execute(
        `SELECT nim FROM krs
         WHERE kode_mk = ? AND tahun_ajaran = ? AND status <> 'batal' AND nim IN (${placeholders})`,
        [kode_mk, tahun_ajaran, ...nimList]
      );
      const allowedNim = new Set(krsRows.map(row => row.nim));

      const saved = [];
      for (const item of items) {
        if (!allowedNim.has(item.nim)) {
          const error = new Error(`Mahasiswa ${item.nim} belum mengambil mata kuliah ini di KRS.`);
          error.statusCode = 400;
          throw error;
        }

        const status = this.normalizeStatus(item.status);
        const keterangan = String(item.keterangan || '').trim() || null;

        await connection.execute(
          `INSERT INTO kehadiran
            (nim, kode_mk, tahun_ajaran, tanggal, pertemuan, status, keterangan, input_by)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?)
           ON DUPLICATE KEY UPDATE
             pertemuan = VALUES(pertemuan),
             status = VALUES(status),
             keterangan = VALUES(keterangan),
             input_by = VALUES(input_by)`,
          [item.nim, kode_mk, tahun_ajaran, tanggal, pertemuan || null, status, keterangan, input_by || null]
        );

        saved.push({ nim: item.nim, status, keterangan });
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

  static async getRekap({ tahun_ajaran, kode_mk = '', tanggal_mulai = '', tanggal_selesai = '', search = '', jurusan = '' }) {
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
        COUNT(kh.id) AS total_pertemuan,
        SUM(CASE WHEN kh.status = 'hadir' THEN 1 ELSE 0 END) AS hadir,
        SUM(CASE WHEN kh.status = 'izin' THEN 1 ELSE 0 END) AS izin,
        SUM(CASE WHEN kh.status = 'sakit' THEN 1 ELSE 0 END) AS sakit,
        SUM(CASE WHEN kh.status = 'alpha' THEN 1 ELSE 0 END) AS alpha,
        MAX(kh.updated_at) AS updated_at
      FROM krs k
      INNER JOIN mahasiswa m ON m.nim = k.nim
      INNER JOIN mata_kuliah mk ON mk.kode_mk = k.kode_mk
      LEFT JOIN kehadiran kh
        ON kh.nim = k.nim
        AND kh.kode_mk = k.kode_mk
        AND kh.tahun_ajaran = k.tahun_ajaran
    `;
    const params = [];

    if (tanggal_mulai) {
      query += ' AND kh.tanggal >= ?';
      params.push(tanggal_mulai);
    }

    if (tanggal_selesai) {
      query += ' AND kh.tanggal <= ?';
      params.push(tanggal_selesai);
    }

    query += `
      WHERE m.status = 'aktif'
        AND k.tahun_ajaran = ?
        AND k.status <> 'batal'
    `;
    params.push(tahun_ajaran);

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

    query += `
      GROUP BY
        m.nim, m.nama, m.jurusan, m.program_studi,
        k.kode_mk, mk.nama_mk, mk.sks, mk.semester, k.tahun_ajaran
      ORDER BY m.nim ASC, mk.semester ASC, k.kode_mk ASC
    `;

    const [rows] = await pool.execute(query, params);
    const data = rows.map(row => {
      const total = Number(row.total_pertemuan || 0);
      const hadir = Number(row.hadir || 0);
      const persentase = total > 0 ? Number(((hadir / total) * 100).toFixed(2)) : 0;
      let status = 'Belum Ada Data';
      if (total > 0 && persentase >= 75) status = 'Aman';
      else if (total > 0 && persentase >= 60) status = 'Perhatian';
      else if (total > 0) status = 'Bermasalah';

      return {
        ...row,
        total_pertemuan: total,
        hadir,
        izin: Number(row.izin || 0),
        sakit: Number(row.sakit || 0),
        alpha: Number(row.alpha || 0),
        persentase_hadir: persentase,
        status_kehadiran: status
      };
    });

    const summary = {
      total_records: data.length,
      total_pertemuan_tercatat: data.reduce((sum, item) => sum + item.total_pertemuan, 0),
      total_hadir: data.reduce((sum, item) => sum + item.hadir, 0),
      total_izin: data.reduce((sum, item) => sum + item.izin, 0),
      total_sakit: data.reduce((sum, item) => sum + item.sakit, 0),
      total_alpha: data.reduce((sum, item) => sum + item.alpha, 0),
      bermasalah: data.filter(item => item.status_kehadiran === 'Bermasalah').length,
      perhatian: data.filter(item => item.status_kehadiran === 'Perhatian').length,
      aman: data.filter(item => item.status_kehadiran === 'Aman').length
    };
    const rowsWithData = data.filter(item => item.total_pertemuan > 0);
    summary.rata_rata_persentase = rowsWithData.length > 0
      ? Number((rowsWithData.reduce((sum, item) => sum + item.persentase_hadir, 0) / rowsWithData.length).toFixed(2))
      : 0;

    return { data, summary };
  }

  static async findByNim(nim) {
    const [rows] = await pool.execute(
      `SELECT
        kh.id,
        kh.nim,
        kh.kode_mk,
        mk.nama_mk,
        mk.sks,
        mk.semester,
        kh.tahun_ajaran,
        kh.tanggal,
        kh.pertemuan,
        kh.status,
        kh.keterangan,
        COALESCE(jadwal.dosen, mk.dosen_pengampu, '-') AS dosen,
        kh.updated_at
       FROM kehadiran kh
       INNER JOIN mata_kuliah mk ON mk.kode_mk = kh.kode_mk
       LEFT JOIN (
         SELECT
           kode_mk,
           GROUP_CONCAT(DISTINCT dosen ORDER BY dosen SEPARATOR ', ') AS dosen
         FROM jadwal
         WHERE dosen IS NOT NULL AND dosen <> ''
         GROUP BY kode_mk
       ) jadwal ON jadwal.kode_mk = kh.kode_mk
       WHERE kh.nim = ?
       ORDER BY kh.tahun_ajaran DESC, kh.tanggal DESC, kh.pertemuan DESC, kh.kode_mk ASC`,
      [nim]
    );

    const counts = rows.reduce((acc, row) => {
      const status = this.normalizeStatus(row.status);
      acc[status] += 1;
      return acc;
    }, { hadir: 0, izin: 0, sakit: 0, alpha: 0 });

    const total = rows.length;
    return {
      data: rows,
      summary: {
        total_pertemuan: total,
        hadir: counts.hadir,
        izin: counts.izin,
        sakit: counts.sakit,
        alpha: counts.alpha,
        persentase_hadir: total > 0 ? Number(((counts.hadir / total) * 100).toFixed(2)) : 0
      }
    };
  }
}

module.exports = Kehadiran;
