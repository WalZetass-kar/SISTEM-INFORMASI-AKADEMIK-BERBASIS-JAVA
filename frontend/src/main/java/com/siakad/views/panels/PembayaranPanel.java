package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.MahasiswaService;
import com.siakad.services.PembayaranService;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * PembayaranPanel - Input Pembayaran UKT & Status Pembayaran
 */
public class PembayaranPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatus, cmbTahunAjaran;
    private JLabel lblTotal;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 12;
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public PembayaranPanel() {
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    private void initUI() {
        // === HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Pembayaran UKT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(248, 250, 252));
        boolean isAdmin = JwtHelper.getInstance().isAdmin();
        JLabel lblSub = new JLabel(isAdmin
                ? "Input & verifikasi pembayaran mahasiswa"
                : "Riwayat & status pembayaran UKT Anda");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        titleBlock.add(lblTitle);
        titleBlock.add(lblSub);
        header.add(titleBlock, BorderLayout.WEST);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        txtSearch = makeField("Cari NIM/Nama...", 14);
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) { currentPage=1; loadData(); }}
        });

        cmbStatus = new JComboBox<>(new String[]{"Semua Status","pending","lunas","gagal","refund"});
        styleCombo(cmbStatus);
        cmbStatus.addActionListener(e -> { currentPage=1; loadData(); });

        cmbTahunAjaran = new JComboBox<>(new String[]{"Semua TA","2024/2025","2023/2024"});
        styleCombo(cmbTahunAjaran);
        cmbTahunAjaran.addActionListener(e -> { currentPage=1; loadData(); });

        JButton btnSearch = makeBtn("🔍", new Color(59,130,246));
        btnSearch.addActionListener(e -> { currentPage=1; loadData(); });

        JButton btnTambah = makeBtn("➕ Input Pembayaran", new Color(34,197,94));
        btnTambah.addActionListener(e -> showInputForm());
        btnTambah.setVisible(isAdmin);

        JButton btnStatus = makeBtn("📋 Cek Status UKT", new Color(168, 85, 247));
        btnStatus.addActionListener(e -> showStatusPembayaran());
        btnStatus.setVisible(!isAdmin);

        actions.add(txtSearch); actions.add(cmbStatus); actions.add(cmbTahunAjaran);
        actions.add(btnSearch);
        if (isAdmin) actions.add(btnTambah);
        else actions.add(btnStatus);
        header.add(actions, BorderLayout.EAST);

        // === TABLE ===
        String[] cols = isAdmin
                ? new String[]{"ID","NIM","Nama","Jenis","Jumlah","Tanggal","Metode","Semester","Status","Aksi"}
                : new String[]{"ID","NIM","Nama","Jenis","Jumlah","Tanggal","Metode","Semester","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return isAdmin && c == 9; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        if (isAdmin) {
            table.getColumnModel().getColumn(9).setMinWidth(140);
            table.getColumnModel().getColumn(9).setMaxWidth(140);
            table.getColumnModel().getColumn(9).setCellRenderer(new PembayaranActionRenderer());
            table.getColumnModel().getColumn(9).setCellEditor(new PembayaranActionEditor());
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(new Color(22,33,54));

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(new Color(22,33,54));
        tableCard.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0,28,0,28),
                BorderFactory.createLineBorder(new Color(30,41,59),1)));
        tableCard.add(sp);

        // === FOOTER ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8,28,12,28));
        lblTotal = new JLabel("Total: 0");
        lblTotal.setFont(new Font("Segoe UI",Font.PLAIN,11));
        lblTotal.setForeground(new Color(100,116,139));

        JPanel pag = new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0));
        pag.setOpaque(false);
        JButton btnPrev = makeBtn("◀",new Color(30,41,59));
        JButton btnNext = makeBtn("▶",new Color(30,41,59));
        btnPrev.addActionListener(e -> { if(currentPage>1){currentPage--;loadData();}});
        btnNext.addActionListener(e -> { currentPage++; loadData(); });
        pag.add(btnPrev); pag.add(btnNext);
        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(pag, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadData() {
        String search = txtSearch.getText().trim();
        String status = cmbStatus.getSelectedIndex()==0 ? null : (String)cmbStatus.getSelectedItem();
        String ta = cmbTahunAjaran.getSelectedIndex()==0 ? null : (String)cmbTahunAjaran.getSelectedItem();

        new SwingWorker<JsonObject,Void>() {
            protected JsonObject doInBackground() throws Exception {
                return PembayaranService.getAll(currentPage, PAGE_SIZE, search, status, ta);
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    tableModel.setRowCount(0);
                    if (resp.get("success").getAsBoolean()) {
                        JsonArray data = resp.getAsJsonArray("data");
                        JsonObject pg = resp.getAsJsonObject("pagination");
                        lblTotal.setText("Total: "+pg.get("total").getAsInt()+" | Hal "+currentPage+"/"+pg.get("totalPages").getAsInt());

                        boolean admin = JwtHelper.getInstance().isAdmin();
                        for (int i=0; i<data.size(); i++) {
                            JsonObject p = data.get(i).getAsJsonObject();
                            if (admin) {
                                tableModel.addRow(new Object[]{
                                    p.get("id").getAsInt(), p.get("nim").getAsString(),
                                    safe(p,"nama_mahasiswa"), p.get("jenis_pembayaran").getAsString(),
                                    RUPIAH.format(p.get("jumlah").getAsDouble()),
                                    p.get("tanggal_bayar").getAsString().substring(0,10),
                                    p.get("metode_pembayaran").getAsString(),
                                    p.get("semester").getAsInt(), p.get("status").getAsString(), "aksi"
                                });
                            } else {
                                tableModel.addRow(new Object[]{
                                    p.get("id").getAsInt(), p.get("nim").getAsString(),
                                    safe(p,"nama_mahasiswa"), p.get("jenis_pembayaran").getAsString(),
                                    RUPIAH.format(p.get("jumlah").getAsDouble()),
                                    p.get("tanggal_bayar").getAsString().substring(0,10),
                                    p.get("metode_pembayaran").getAsString(),
                                    p.get("semester").getAsInt(), p.get("status").getAsString()
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: "+e.getMessage());
                }
            }
        }.execute();
    }

    private void showInputForm() {
        JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Input Pembayaran UKT", true);
        d.setSize(440, 480);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(22,33,54));
        p.setBorder(new EmptyBorder(20,24,20,24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(5,4,5,4); g.weightx=1;

        JTextField fNim = makeField("",0), fJumlah = makeField("",0),
                   fTanggal = makeField("YYYY-MM-DD",0), fSemester = makeField("",0), fTA = makeField("2024/2025",0);
        JLabel lblMhsInfo = new JLabel("Ketik NIM lalu tekan Enter untuk validasi ke API Kelompok 1");
        lblMhsInfo.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblMhsInfo.setForeground(new Color(34, 197, 94));
        fNim.addActionListener(e -> lookupMahasiswa(fNim, lblMhsInfo, d));
        JComboBox<String> cJenis = new JComboBox<>(new String[]{"ukt","spp","praktikum","wisuda","lainnya"});
        JComboBox<String> cMetode = new JComboBox<>(new String[]{"transfer_bank","virtual_account","tunai","qris"});
        styleCombo(cJenis); styleCombo(cMetode);
        JTextArea fKet = new JTextArea(2,20);
        fKet.setBackground(new Color(30,41,59)); fKet.setForeground(Color.WHITE); fKet.setCaretColor(Color.WHITE);

        int r=0;
        addRow(p,g,r++,"NIM Mahasiswa *",fNim);
        g.gridx=1; g.gridy=r++; g.insets=new Insets(0,4,8,4); p.add(lblMhsInfo,g); g.insets=new Insets(5,4,5,4);
        addRow(p,g,r++,"Jenis",cJenis);
        addRow(p,g,r++,"Jumlah (Rp) *",fJumlah); addRow(p,g,r++,"Tanggal Bayar *",fTanggal);
        addRow(p,g,r++,"Metode",cMetode); addRow(p,g,r++,"Semester *",fSemester);
        addRow(p,g,r++,"Tahun Ajaran *",fTA);
        g.gridx=0; g.gridy=r; p.add(lbl("Keterangan"),g); g.gridx=1; p.add(new JScrollPane(fKet),g);

        JButton btnSave = makeBtn("💾 Simpan Pembayaran", new Color(59,130,246));
        btnSave.addActionListener(e -> {
            if (fNim.getText().isEmpty()||fJumlah.getText().isEmpty()||fTanggal.getText().isEmpty()
                ||fSemester.getText().isEmpty()||fTA.getText().isEmpty()) {
                JOptionPane.showMessageDialog(d,"Isi semua field wajib (*).","Validasi",JOptionPane.WARNING_MESSAGE);
                return;
            }
            double jumlah;
            int semester;
            try {
                jumlah = Double.parseDouble(fJumlah.getText().trim());
                semester = Integer.parseInt(fSemester.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d,
                        "Jumlah harus berupa angka dan semester harus berupa bilangan bulat.",
                        "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("nim", fNim.getText().trim());
            body.addProperty("jenis_pembayaran", (String)cJenis.getSelectedItem());
            body.addProperty("jumlah", jumlah);
            body.addProperty("tanggal_bayar", fTanggal.getText().trim());
            body.addProperty("metode_pembayaran", (String)cMetode.getSelectedItem());
            body.addProperty("semester", semester);
            body.addProperty("tahun_ajaran", fTA.getText().trim());
            body.addProperty("keterangan", fKet.getText().trim());

            new SwingWorker<JsonObject,Void>() {
                protected JsonObject doInBackground() throws Exception { return PembayaranService.create(body); }
                protected void done() {
                    try {
                        JsonObject r = get();
                        if (r.get("success").getAsBoolean()) {
                            JOptionPane.showMessageDialog(d, r.get("message").getAsString(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            d.dispose(); loadData();
                        } else {
                            JOptionPane.showMessageDialog(d, r.get("message").getAsString(), "Gagal", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error: "+ex.getMessage()); }
                }
            }.execute();
        });
        g.gridx=0; g.gridy=r+1; g.gridwidth=2; g.insets=new Insets(16,4,4,4);
        p.add(btnSave, g);

        d.add(new JScrollPane(p){{setBorder(null);}});
        d.setVisible(true);
    }

    private void lookupMahasiswa(JTextField fNim, JLabel lblInfo, JDialog parent) {
        String nim = fNim.getText().trim();
        if (nim.isEmpty()) return;
        lblInfo.setText("Memvalidasi NIM...");
        lblInfo.setForeground(new Color(234, 179, 8));
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getByNim(nim);
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        JsonObject m = resp.getAsJsonObject("data");
                        lblInfo.setText("✓ " + m.get("nama").getAsString() + " — " + m.get("jurusan").getAsString());
                        lblInfo.setForeground(new Color(34, 197, 94));
                    } else {
                        lblInfo.setText("✗ " + resp.get("message").getAsString());
                        lblInfo.setForeground(new Color(239, 68, 68));
                    }
                } catch (Exception ex) {
                    lblInfo.setText("✗ Gagal hubungi API mahasiswa: " + ex.getMessage());
                    lblInfo.setForeground(new Color(239, 68, 68));
                }
            }
        }.execute();
    }

    private void showStatusPembayaran() {
        String nim = JwtHelper.getInstance().getNim();
        if (nim == null || nim.isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIM tidak ditemukan di token login.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextField fSem = makeField("2", 0);
        JTextField fTA = makeField("2024/2025", 0);
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.setBackground(new Color(22, 33, 54));
        panel.add(lbl("Semester")); panel.add(fSem);
        panel.add(lbl("Tahun Ajaran")); panel.add(fTA);

        int ok = JOptionPane.showConfirmDialog(this, panel, "Cek Status UKT — NIM " + nim,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return PembayaranService.cekStatus(nim,
                        Integer.parseInt(fSem.getText().trim()), fTA.getText().trim());
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (!resp.get("success").getAsBoolean()) {
                        JOptionPane.showMessageDialog(PembayaranPanel.this, resp.get("message").getAsString(),
                                "Gagal", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JsonObject data = resp.getAsJsonObject("data");
                    String status = data.get("status_pembayaran").getAsString();
                    String mhs = data.getAsJsonObject("mahasiswa").get("nama").getAsString();
                    String msg = "Mahasiswa: " + mhs + "\nNIM: " + nim + "\nStatus UKT: " + status.toUpperCase();
                    if (data.has("detail") && !data.get("detail").isJsonNull()) {
                        JsonObject d = data.getAsJsonObject("detail");
                        msg += "\nReferensi: " + d.get("nomor_referensi").getAsString();
                        msg += "\nJumlah: " + RUPIAH.format(d.get("jumlah").getAsDouble());
                    }
                    JOptionPane.showMessageDialog(PembayaranPanel.this, msg, "Status Pembayaran",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void verifyPembayaran(int id, String newStatus) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ubah status pembayaran #"+id+" menjadi "+newStatus.toUpperCase()+"?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<JsonObject,Void>() {
            protected JsonObject doInBackground() throws Exception { return PembayaranService.updateStatus(id, newStatus); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(PembayaranPanel.this, r.get("message").getAsString());
                    loadData();
                } catch (Exception e) { JOptionPane.showMessageDialog(PembayaranPanel.this, "Error: "+e.getMessage()); }
            }
        }.execute();
    }

    // === Helpers ===
    private String safe(JsonObject o, String k) {
        return o.has(k)&&!o.get(k).isJsonNull() ? o.get(k).getAsString() : "-";
    }
    private JTextField makeField(String hint, int cols) {
        JTextField f = cols>0 ? new JTextField(cols) : new JTextField();
        f.setBackground(new Color(30,41,59)); f.setForeground(Color.WHITE); f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(51,65,85)),new EmptyBorder(6,10,6,10)));
        f.setFont(new Font("Segoe UI",Font.PLAIN,12));
        if (!hint.isEmpty()) f.setToolTipText(hint);
        return f;
    }
    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setForeground(Color.WHITE); b.setBackground(bg);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bg.darker()),new EmptyBorder(7,14,7,14)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setFocusPainted(false);
        return b;
    }
    private void styleCombo(JComboBox<String> c) {
        c.setBackground(new Color(30,41,59)); c.setForeground(new Color(203,213,225));
        c.setFont(new Font("Segoe UI",Font.PLAIN,12));
    }
    private void styleTable(JTable t) {
        t.setBackground(new Color(22,33,54)); t.setForeground(new Color(203,213,225));
        t.setSelectionBackground(new Color(37,99,235,80)); t.setGridColor(new Color(30,41,59));
        t.setRowHeight(38); t.setFont(new Font("Segoe UI",Font.PLAIN,12)); t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(15,23,42)); h.setForeground(new Color(148,163,184));
        h.setFont(new Font("Segoe UI",Font.BOLD,11)); h.setReorderingAllowed(false);
    }
    private JLabel lbl(String t) { JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setForeground(new Color(148,163,184)); return l; }
    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx=0; g.gridy=row; g.gridwidth=1; p.add(lbl(label),g); g.gridx=1; p.add(field,g);
    }

    // === Action Renderer/Editor ===
    class PembayaranActionRenderer extends JPanel implements TableCellRenderer {
        JButton b1=new JButton("✅"),b2=new JButton("❌");
        PembayaranActionRenderer(){
            setLayout(new FlowLayout(FlowLayout.CENTER,2,4)); setOpaque(true);
            for(JButton b:new JButton[]{b1,b2}){ b.setFont(new Font("Segoe UI",Font.PLAIN,11)); b.setBorderPainted(false); b.setFocusPainted(false); b.setForeground(Color.WHITE); add(b);}
            b1.setBackground(new Color(34,197,94)); b2.setBackground(new Color(239,68,68));
        }
        public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
            setBackground(s?new Color(37,99,235,40):new Color(22,33,54)); return this;
        }
    }
    class PembayaranActionEditor extends DefaultCellEditor {
        JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER,2,4));
        JButton bLunas=new JButton("✅ Lunas"), bGagal=new JButton("❌ Gagal");
        int rowId;
        PembayaranActionEditor(){
            super(new JCheckBox());
            panel.setBackground(new Color(22,33,54));
            bLunas.setFont(new Font("Segoe UI",Font.PLAIN,10)); bLunas.setBackground(new Color(34,197,94));
            bLunas.setForeground(Color.WHITE); bLunas.setBorderPainted(false); bLunas.setFocusPainted(false);
            bGagal.setFont(new Font("Segoe UI",Font.PLAIN,10)); bGagal.setBackground(new Color(239,68,68));
            bGagal.setForeground(Color.WHITE); bGagal.setBorderPainted(false); bGagal.setFocusPainted(false);
            panel.add(bLunas); panel.add(bGagal);
            bLunas.addActionListener(e->{ fireEditingStopped(); verifyPembayaran(rowId,"lunas"); });
            bGagal.addActionListener(e->{ fireEditingStopped(); verifyPembayaran(rowId,"gagal"); });
        }
        public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){
            rowId=(int)t.getValueAt(r,0); return panel;
        }
        public Object getCellEditorValue(){ return "aksi"; }
    }
}
