// ============================================================
// MODEL: Pembayaran
// Query database untuk tabel pembayaran
// Relasi ke mahasiswa via nim
// ============================================================

const { pool } = require('../config/database');
const { normalizePagination } = require('../utils/pagination');

class Pembayaran {
  static async findAll({ page = 1, limit = 10, search = '', status = '', tahun_ajaran = '', jenis = '' } = {}) {
    const pagination = normalizePagination({ page, limit });
    let query = `SELECT p.*, m.nama as nama_mahasiswa, m.jurusan, m.program_studi
      FROM pembayaran p LEFT JOIN mahasiswa m ON p.nim = m.nim WHERE 1=1`;
    let countQuery = `SELECT COUNT(*) as total FROM pembayaran p
      LEFT JOIN mahasiswa m ON p.nim = m.nim WHERE 1=1`;
    const params = [];
    const countParams = [];

    if (search) {
      const s = ' AND (p.nim LIKE ? OR m.nama LIKE ? OR p.nomor_referensi LIKE ?)';
      const sp = `%${search}%`;
      query += s; countQuery += s;
      params.push(sp, sp, sp); countParams.push(sp, sp, sp);
    }
    if (status) {
      query += ' AND p.status = ?'; countQuery += ' AND p.status = ?';
      params.push(status); countParams.push(status);
    }
    if (tahun_ajaran) {
      query += ' AND p.tahun_ajaran = ?'; countQuery += ' AND p.tahun_ajaran = ?';
      params.push(tahun_ajaran); countParams.push(tahun_ajaran);
    }
    if (jenis) {
      query += ' AND p.jenis_pembayaran = ?'; countQuery += ' AND p.jenis_pembayaran = ?';
      params.push(jenis); countParams.push(jenis);
    }

    const [countRows] = await pool.execute(countQuery, countParams);
    const total = countRows[0].total;
    query += ' ORDER BY p.created_at DESC LIMIT ? OFFSET ?';
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
      `SELECT p.*, m.nama as nama_mahasiswa, m.jurusan, m.program_studi, m.email, m.angkatan
       FROM pembayaran p LEFT JOIN mahasiswa m ON p.nim = m.nim WHERE p.id = ?`, [id]);
    return rows[0] || null;
  }

  static async findByNim(nim) {
    const [rows] = await pool.execute(
      `SELECT p.*, m.nama as nama_mahasiswa, m.jurusan
       FROM pembayaran p LEFT JOIN mahasiswa m ON p.nim = m.nim
       WHERE p.nim = ? ORDER BY p.created_at DESC`, [nim]);
    return rows;
  }

  static async findByReferensi(nomor_referensi) {
    const [rows] = await pool.execute('SELECT * FROM pembayaran WHERE nomor_referensi = ?', [nomor_referensi]);
    return rows[0] || null;
  }

  static async create({ nim, jenis_pembayaran, jumlah, tanggal_bayar, metode_pembayaran,
                         bukti_pembayaran, nomor_referensi, semester, tahun_ajaran, keterangan }) {
    const [result] = await pool.execute(
      `INSERT INTO pembayaran (nim, jenis_pembayaran, jumlah, tanggal_bayar, metode_pembayaran,
        bukti_pembayaran, nomor_referensi, semester, tahun_ajaran, status, keterangan)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending', ?)`,
      [nim, jenis_pembayaran || 'ukt', jumlah, tanggal_bayar, metode_pembayaran || 'transfer_bank',
       bukti_pembayaran || null, nomor_referensi, semester, tahun_ajaran, keterangan || null]);
    return { id: result.insertId, nim, jumlah, nomor_referensi };
  }

  static async updateStatus(id, status, verified_by) {
    const verified_at = status === 'lunas' ? new Date() : null;
    const [result] = await pool.execute(
      'UPDATE pembayaran SET status = ?, verified_by = ?, verified_at = ? WHERE id = ?',
      [status, verified_by, verified_at, id]);
    return result.affectedRows > 0;
  }

  static async update(id, data) {
    const fields = []; const params = [];
    const allowed = ['jenis_pembayaran', 'jumlah', 'tanggal_bayar', 'metode_pembayaran',
      'bukti_pembayaran', 'semester', 'tahun_ajaran', 'keterangan'];
    for (const f of allowed) { if (data[f] !== undefined) { fields.push(`${f} = ?`); params.push(data[f]); } }
    if (fields.length === 0) return false;
    params.push(id);
    const [result] = await pool.execute(`UPDATE pembayaran SET ${fields.join(', ')} WHERE id = ?`, params);
    return result.affectedRows > 0;
  }

  static async delete(id) {
    const [result] = await pool.execute('DELETE FROM pembayaran WHERE id = ?', [id]);
    return result.affectedRows > 0;
  }

  static async getStatusByNimSemester(nim, semester, tahun_ajaran) {
    const [rows] = await pool.execute(
      `SELECT * FROM pembayaran WHERE nim = ? AND semester = ? AND tahun_ajaran = ? AND jenis_pembayaran = 'ukt'
       ORDER BY created_at DESC LIMIT 1`, [nim, semester, tahun_ajaran]);
    return rows[0] || null;
  }

  // === STATISTIK DASHBOARD ===
  static async getTotalPendapatan(tahun_ajaran = null) {
    let q = "SELECT COALESCE(SUM(jumlah), 0) as total FROM pembayaran WHERE status = 'lunas'";
    const p = [];
    if (tahun_ajaran) { q += ' AND tahun_ajaran = ?'; p.push(tahun_ajaran); }
    const [rows] = await pool.execute(q, p);
    return rows[0].total;
  }

  static async countByStatus(tahun_ajaran = null) {
    let q = 'SELECT status, COUNT(*) as total, COALESCE(SUM(jumlah), 0) as total_jumlah FROM pembayaran';
    const p = [];
    if (tahun_ajaran) { q += ' WHERE tahun_ajaran = ?'; p.push(tahun_ajaran); }
    q += ' GROUP BY status';
    const [rows] = await pool.execute(q, p);
    return rows;
  }

  static async getPendapatanPerBulan(tahun_ajaran = null) {
    let q = `SELECT DATE_FORMAT(tanggal_bayar, '%Y-%m') as bulan, COUNT(*) as total_transaksi,
      COALESCE(SUM(jumlah), 0) as total_jumlah FROM pembayaran WHERE status = 'lunas'`;
    const p = [];
    if (tahun_ajaran) { q += ' AND tahun_ajaran = ?'; p.push(tahun_ajaran); }
    q += " GROUP BY DATE_FORMAT(tanggal_bayar, '%Y-%m') ORDER BY bulan ASC";
    const [rows] = await pool.execute(q, p);
    return rows;
  }

  static async countByMetode(tahun_ajaran = null) {
    let q = `SELECT metode_pembayaran, COUNT(*) as total, COALESCE(SUM(jumlah), 0) as total_jumlah
      FROM pembayaran WHERE status = 'lunas'`;
    const p = [];
    if (tahun_ajaran) { q += ' AND tahun_ajaran = ?'; p.push(tahun_ajaran); }
    q += ' GROUP BY metode_pembayaran';
    const [rows] = await pool.execute(q, p);
    return rows;
  }

  static async countByJurusan(tahun_ajaran = null) {
    let q = `SELECT m.jurusan, COUNT(*) as total, COALESCE(SUM(p.jumlah), 0) as total_jumlah
      FROM pembayaran p LEFT JOIN mahasiswa m ON p.nim = m.nim WHERE p.status = 'lunas'`;
    const p = [];
    if (tahun_ajaran) { q += ' AND p.tahun_ajaran = ?'; p.push(tahun_ajaran); }
    q += ' GROUP BY m.jurusan ORDER BY total_jumlah DESC';
    const [rows] = await pool.execute(q, p);
    return rows;
  }

  static async getDashboardStats(tahun_ajaran = null) {
    const [totalPendapatan, statusStats, perBulan, perMetode, perJurusan] = await Promise.all([
      this.getTotalPendapatan(tahun_ajaran), this.countByStatus(tahun_ajaran),
      this.getPendapatanPerBulan(tahun_ajaran), this.countByMetode(tahun_ajaran),
      this.countByJurusan(tahun_ajaran)
    ]);
    const totalTransaksi = statusStats.reduce((sum, s) => sum + s.total, 0);
    const lunas = statusStats.find(s => s.status === 'lunas')?.total || 0;
    const pending = statusStats.find(s => s.status === 'pending')?.total || 0;
    const gagal = statusStats.find(s => s.status === 'gagal')?.total || 0;
    return {
      ringkasan: { total_pendapatan: totalPendapatan, total_transaksi: totalTransaksi,
        lunas, pending, gagal, persentase_lunas: totalTransaksi > 0 ? ((lunas / totalTransaksi) * 100).toFixed(1) : 0 },
      chart_pendapatan_bulanan: perBulan, chart_metode_pembayaran: perMetode,
      chart_per_jurusan: perJurusan, status_detail: statusStats
    };
  }

  static async getTahunAjaranList() {
    const [rows] = await pool.execute('SELECT DISTINCT tahun_ajaran FROM pembayaran ORDER BY tahun_ajaran DESC');
    return rows.map(r => r.tahun_ajaran);
  }
}

module.exports = Pembayaran;
