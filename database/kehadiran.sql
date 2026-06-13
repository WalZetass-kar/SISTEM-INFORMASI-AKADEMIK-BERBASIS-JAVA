USE siakad_db;

CREATE TABLE IF NOT EXISTS akademik_config (
  config_key VARCHAR(50) PRIMARY KEY,
  config_value VARCHAR(100) NOT NULL,
  description VARCHAR(255) DEFAULT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO akademik_config (config_key, config_value, description) VALUES
  ('jumlah_pertemuan', '12', 'Jumlah pertemuan default untuk input kehadiran')
ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value),
  description = VALUES(description);

CREATE TABLE IF NOT EXISTS kehadiran (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nim VARCHAR(20) NOT NULL,
  kode_mk VARCHAR(20) NOT NULL,
  tahun_ajaran VARCHAR(10) NOT NULL,
  tanggal DATE NOT NULL,
  pertemuan INT DEFAULT NULL,
  status ENUM('hadir', 'izin', 'sakit', 'alpha') NOT NULL DEFAULT 'hadir',
  keterangan TEXT DEFAULT NULL,
  input_by INT DEFAULT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_kehadiran (nim, kode_mk, tahun_ajaran, tanggal),
  INDEX idx_kehadiran_kelas (kode_mk, tahun_ajaran, tanggal),
  INDEX idx_kehadiran_status (status),
  FOREIGN KEY (nim) REFERENCES mahasiswa(nim) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (kode_mk) REFERENCES mata_kuliah(kode_mk) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (input_by) REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

INSERT INTO krs (nim, kode_mk, semester, tahun_ajaran, status) VALUES
  ('2024001', 'IF202', 2, '2024/2025', 'diambil'),
  ('2024002', 'IF202', 2, '2024/2025', 'diambil'),
  ('2024001', 'IF204', 2, '2024/2025', 'diambil'),
  ('2024002', 'IF204', 2, '2024/2025', 'diambil'),
  ('2024003', 'SI201', 2, '2024/2025', 'diambil'),
  ('2024004', 'SI304', 4, '2024/2025', 'diambil'),
  ('2024005', 'SI304', 4, '2024/2025', 'diambil')
ON DUPLICATE KEY UPDATE status = VALUES(status);
