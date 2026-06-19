// ============================================================
// MODEL: Laporan
// Query database untuk tabel laporan
// Digunakan oleh: Modul Pembayaran & Laporan
// ============================================================

const { pool } = require('../config/database');
const { normalizePagination } = require('../utils/pagination');

class Laporan {
  static async findAll({ page = 1, limit = 10, jenis = '', status = '', tahun_ajaran = '', search = '' } = {}) {
    const pagination = normalizePagination({ page, limit });
    let query = `SELECT l.*, u.username as generated_by_name FROM laporan l
      LEFT JOIN users u ON l.generated_by = u.id WHERE 1=1`;
    let countQuery = 'SELECT COUNT(*) as total FROM laporan WHERE 1=1';
    const params = []; const countParams = [];

    if (jenis) {
      query += ' AND l.jenis_laporan = ?'; countQuery += ' AND jenis_laporan = ?';
      params.push(jenis); countParams.push(jenis);
    }
    if (status) {
      query += ' AND l.status = ?'; countQuery += ' AND status = ?';
      params.push(status); countParams.push(status);
    }
    if (tahun_ajaran) {
      query += ' AND l.tahun_ajaran = ?'; countQuery += ' AND tahun_ajaran = ?';
      params.push(tahun_ajaran); countParams.push(tahun_ajaran);
    }
    if (search) {
      query += ' AND (l.judul LIKE ? OR l.deskripsi LIKE ?)'; countQuery += ' AND (judul LIKE ? OR deskripsi LIKE ?)';
      const like = `%${search}%`;
      params.push(like, like); countParams.push(like, like);
    }

    const [countRows] = await pool.execute(countQuery, countParams);
    const total = countRows[0].total;
    query += ' ORDER BY l.created_at DESC LIMIT ? OFFSET ?';
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

  static async findById(id) {
    const [rows] = await pool.execute(
      `SELECT l.*, u.username as generated_by_name FROM laporan l
       LEFT JOIN users u ON l.generated_by = u.id WHERE l.id = ?`, [id]);
    return rows[0] || null;
  }

  static async create({ judul, jenis_laporan, deskripsi, periode_mulai, periode_selesai,
                         tahun_ajaran, data_laporan, file_path, format_file, generated_by, total_records }) {
    const [result] = await pool.execute(
      `INSERT INTO laporan (judul, jenis_laporan, deskripsi, periode_mulai, periode_selesai,
        tahun_ajaran, data_laporan, file_path, format_file, generated_by, total_records, status)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'generated')`,
      [judul, jenis_laporan, deskripsi || null, periode_mulai || null, periode_selesai || null,
       tahun_ajaran || null, JSON.stringify(data_laporan) || null, file_path || null,
       format_file || null, generated_by, total_records || 0]);
    return { id: result.insertId, judul, jenis_laporan };
  }

  static async update(id, data) {
    const fields = []; const params = [];
    const allowed = ['judul', 'deskripsi', 'status', 'file_path', 'format_file'];
    for (const f of allowed) { if (data[f] !== undefined) { fields.push(`${f} = ?`); params.push(data[f]); } }
    if (fields.length === 0) return false;
    params.push(id);
    const [result] = await pool.execute(`UPDATE laporan SET ${fields.join(', ')} WHERE id = ?`, params);
    return result.affectedRows > 0;
  }

  static async delete(id) {
    const [result] = await pool.execute('DELETE FROM laporan WHERE id = ?', [id]);
    return result.affectedRows > 0;
  }

  // Generate laporan pembayaran
  static async generateLaporanPembayaran(periode_mulai, periode_selesai, tahun_ajaran, generated_by) {
    let query = `SELECT p.*, m.nama as nama_mahasiswa, m.jurusan
      FROM pembayaran p LEFT JOIN mahasiswa m ON p.nim = m.nim WHERE 1=1`;
    const params = [];
    if (periode_mulai) { query += ' AND p.tanggal_bayar >= ?'; params.push(periode_mulai); }
    if (periode_selesai) { query += ' AND p.tanggal_bayar <= ?'; params.push(periode_selesai); }
    if (tahun_ajaran) { query += ' AND p.tahun_ajaran = ?'; params.push(tahun_ajaran); }
    query += ' ORDER BY p.tanggal_bayar DESC';
    const [rows] = await pool.execute(query, params);

    const totalLunas = rows.filter(r => r.status === 'lunas').reduce((s, r) => s + parseFloat(r.jumlah), 0);
    const totalPending = rows.filter(r => r.status === 'pending').reduce((s, r) => s + parseFloat(r.jumlah), 0);

    const laporan = await this.create({
      judul: `Laporan Pembayaran ${periode_mulai || ''} - ${periode_selesai || ''}`,
      jenis_laporan: 'pembayaran', deskripsi: `Laporan pembayaran periode ${periode_mulai} s/d ${periode_selesai}`,
      periode_mulai, periode_selesai, tahun_ajaran,
      data_laporan: { records: rows, summary: { total_transaksi: rows.length, total_lunas: totalLunas, total_pending: totalPending } },
      generated_by, total_records: rows.length
    });
    return { ...laporan, total_records: rows.length, summary: { total_transaksi: rows.length, total_lunas: totalLunas, total_pending: totalPending } };
  }

  // Generate laporan statistik mahasiswa
  static async generateLaporanMahasiswa(generated_by) {
    const [jurusanStats] = await pool.execute('SELECT jurusan, status, COUNT(*) as total FROM mahasiswa GROUP BY jurusan, status');
    const [angkatanStats] = await pool.execute('SELECT angkatan, COUNT(*) as total FROM mahasiswa GROUP BY angkatan ORDER BY angkatan DESC');
    const [totalMhs] = await pool.execute('SELECT COUNT(*) as total FROM mahasiswa');

    const laporan = await this.create({
      judul: `Laporan Statistik Mahasiswa - ${new Date().toISOString().split('T')[0]}`,
      jenis_laporan: 'mahasiswa', deskripsi: 'Laporan statistik data mahasiswa',
      data_laporan: { per_jurusan: jurusanStats, per_angkatan: angkatanStats, total: totalMhs[0].total },
      generated_by, total_records: totalMhs[0].total
    });
    return laporan;
  }

  // Generate laporan keuangan
  static async generateLaporanKeuangan(tahun_ajaran, generated_by) {
    const [pendapatan] = await pool.execute(
      `SELECT DATE_FORMAT(tanggal_bayar, '%Y-%m') as bulan, status,
        COUNT(*) as total, COALESCE(SUM(jumlah), 0) as jumlah
       FROM pembayaran WHERE tahun_ajaran = ? GROUP BY DATE_FORMAT(tanggal_bayar, '%Y-%m'), status ORDER BY bulan`,
      [tahun_ajaran]);
    const [totalAll] = await pool.execute(
      "SELECT COALESCE(SUM(jumlah), 0) as total FROM pembayaran WHERE status = 'lunas' AND tahun_ajaran = ?",
      [tahun_ajaran]);

    const laporan = await this.create({
      judul: `Laporan Keuangan TA ${tahun_ajaran}`,
      jenis_laporan: 'keuangan', deskripsi: `Laporan keuangan tahun ajaran ${tahun_ajaran}`,
      tahun_ajaran,
      data_laporan: { pendapatan_bulanan: pendapatan, total_pendapatan: totalAll[0].total },
      generated_by, total_records: pendapatan.length
    });
    return laporan;
  }
}

module.exports = Laporan;
