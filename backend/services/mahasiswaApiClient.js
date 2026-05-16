// ============================================================
// CLIENT: API Mahasiswa (Kelompok 1)
// Satu pintu integrasi modul Pembayaran & Laporan ke data mahasiswa.
// Endpoint Kelompok 1: GET /api/mahasiswa/:nim
// ============================================================

const Mahasiswa = require('../models/Mahasiswa');

class MahasiswaApiClient {
  /**
   * Ambil detail mahasiswa by NIM (kontrak sama dengan GET /api/mahasiswa/:nim)
   */
  static async getByNim(nim) {
    const data = await Mahasiswa.findByNim(nim);
    if (!data) return null;
    return data;
  }

  /**
   * Validasi NIM ada di sistem Kelompok 1
   */
  static async validateNim(nim) {
    const mhs = await this.getByNim(nim);
    return mhs !== null;
  }
}

module.exports = MahasiswaApiClient;
