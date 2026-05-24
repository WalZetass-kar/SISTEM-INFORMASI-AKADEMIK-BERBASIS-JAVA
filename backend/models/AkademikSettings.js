const { pool } = require('../config/database');

class AkademikSettings {
  static async getTahunAjaran() {
    const [rows] = await pool.execute(
      'SELECT * FROM tahun_ajaran ORDER BY status = "aktif" DESC, tahun_ajaran DESC, semester ASC'
    );
    return rows;
  }

  static async createTahunAjaran({ tahun_ajaran, semester, tanggal_mulai, tanggal_selesai, status }) {
    if (status === 'aktif') await this.clearActiveTahunAjaran();
    const [result] = await pool.execute(
      `INSERT INTO tahun_ajaran (tahun_ajaran, semester, tanggal_mulai, tanggal_selesai, status)
       VALUES (?, ?, ?, ?, ?)`,
      [tahun_ajaran, semester || 'ganjil', tanggal_mulai || null, tanggal_selesai || null, status || 'draft']
    );
    return { id: result.insertId, tahun_ajaran, semester: semester || 'ganjil' };
  }

  static async updateTahunAjaran(id, data) {
    if (data.status === 'aktif') await this.clearActiveTahunAjaran();
    const fields = [];
    const params = [];
    for (const field of ['tahun_ajaran', 'semester', 'tanggal_mulai', 'tanggal_selesai', 'status']) {
      if (data[field] !== undefined) {
        fields.push(`${field} = ?`);
        params.push(data[field] || null);
      }
    }
    if (!fields.length) return false;
    params.push(id);
    const [result] = await pool.execute(`UPDATE tahun_ajaran SET ${fields.join(', ')} WHERE id = ?`, params);
    return result.affectedRows > 0;
  }

  static async clearActiveTahunAjaran() {
    await pool.execute("UPDATE tahun_ajaran SET status = 'arsip' WHERE status = 'aktif'");
  }

  static async deleteTahunAjaran(id) {
    const [result] = await pool.execute('DELETE FROM tahun_ajaran WHERE id = ?', [id]);
    return result.affectedRows > 0;
  }

  static async getMataKuliah() {
    const [rows] = await pool.execute('SELECT * FROM mata_kuliah ORDER BY semester ASC, kode_mk ASC');
    return rows;
  }

  static async createMataKuliah(data) {
    const [result] = await pool.execute(
      `INSERT INTO mata_kuliah (kode_mk, nama_mk, sks, semester, jurusan, dosen_pengampu)
       VALUES (?, ?, ?, ?, ?, ?)`,
      [data.kode_mk, data.nama_mk, data.sks || 2, data.semester || 1, data.jurusan || null, data.dosen_pengampu || null]
    );
    return { id: result.insertId, kode_mk: data.kode_mk, nama_mk: data.nama_mk };
  }

  static async updateMataKuliah(kodeMk, data) {
    const fields = [];
    const params = [];
    for (const field of ['nama_mk', 'sks', 'semester', 'jurusan', 'dosen_pengampu']) {
      if (data[field] !== undefined) {
        fields.push(`${field} = ?`);
        params.push(data[field] || null);
      }
    }
    if (!fields.length) return false;
    params.push(kodeMk);
    const [result] = await pool.execute(`UPDATE mata_kuliah SET ${fields.join(', ')} WHERE kode_mk = ?`, params);
    return result.affectedRows > 0;
  }

  static async deleteMataKuliah(kodeMk) {
    const [result] = await pool.execute('DELETE FROM mata_kuliah WHERE kode_mk = ?', [kodeMk]);
    return result.affectedRows > 0;
  }

  static async getBobotNilai() {
    const [rows] = await pool.execute('SELECT * FROM bobot_nilai WHERE is_active = 1 ORDER BY id ASC LIMIT 1');
    return rows[0] || null;
  }

  static async updateBobotNilai({ bobot_tugas, bobot_uts, bobot_uas }) {
    const total = Number(bobot_tugas) + Number(bobot_uts) + Number(bobot_uas);
    if (Math.round(total * 100) / 100 !== 100) {
      const error = new Error('Total bobot harus 100%.');
      error.statusCode = 400;
      throw error;
    }

    await pool.execute(
      `INSERT INTO bobot_nilai (nama_config, bobot_tugas, bobot_uts, bobot_uas, is_active)
       VALUES ('Default Akademik', ?, ?, ?, 1)
       ON DUPLICATE KEY UPDATE bobot_tugas = VALUES(bobot_tugas), bobot_uts = VALUES(bobot_uts), bobot_uas = VALUES(bobot_uas), is_active = 1`,
      [bobot_tugas, bobot_uts, bobot_uas]
    );
    return this.getBobotNilai();
  }
}

module.exports = AkademikSettings;
