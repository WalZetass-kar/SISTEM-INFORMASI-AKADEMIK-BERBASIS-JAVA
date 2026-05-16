package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.MahasiswaService;
import com.siakad.utils.JwtHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MahasiswaPanel - CRUD Data Mahasiswa
 * Menampilkan tabel mahasiswa dengan search & CRUD form
 */
public class MahasiswaPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblTotal;
    private int currentPage = 1;
    private final int PAGE_SIZE = 15;
    private JButton btnPrev, btnNext;

    public MahasiswaPanel() {
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Data Mahasiswa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(248, 250, 252));
        JLabel lblSub = new JLabel("Kelola data mahasiswa terdaftar");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        titleBlock.add(lblTitle);
        titleBlock.add(lblSub);

        // Search & actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        txtSearch = new JTextField(18);
        txtSearch.setBackground(new Color(30, 41, 59));
        txtSearch.setForeground(new Color(203, 213, 225));
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(6, 10, 6, 10)));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setToolTipText("Cari NIM, nama, atau email...");

        JButton btnSearch = createBtn("🔍 Cari", new Color(59, 130, 246));
        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { currentPage = 1; loadData(); }
            }
        });

        JButton btnRefresh = createBtn("🔄", new Color(30, 41, 59));
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); currentPage = 1; loadData(); });

        JButton btnTambah = createBtn("➕ Tambah", new Color(34, 197, 94));
        btnTambah.addActionListener(e -> showForm(null));
        btnTambah.setVisible(JwtHelper.getInstance().isAdmin());

        actions.add(txtSearch);
        actions.add(btnSearch);
        actions.add(btnRefresh);
        actions.add(btnTambah);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        // Table
        String[] columns = {"NIM", "Nama", "Jurusan", "Program Studi", "Angkatan", "Semester", "Status", "Aksi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 7; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        // Action column renderer & editor
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionButtonEditor(new JCheckBox(), true));
        table.getColumnModel().getColumn(7).setMinWidth(120);
        table.getColumnModel().getColumn(7).setMaxWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(new Color(22, 33, 54));
        scrollPane.getViewport().setBackground(new Color(22, 33, 54));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Footer: pagination
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 28, 12, 28));

        lblTotal = new JLabel("Total: 0 mahasiswa");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(new Color(100, 116, 139));

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pagination.setOpaque(false);
        btnPrev = createBtn("◀ Prev", new Color(30, 41, 59));
        btnNext = createBtn("Next ▶", new Color(30, 41, 59));
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); }});
        btnNext.addActionListener(e -> { currentPage++; loadData(); });
        pagination.add(btnPrev);
        pagination.add(btnNext);

        footer.add(lblTotal, BorderLayout.WEST);
        footer.add(pagination, BorderLayout.EAST);

        // Table container
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(new Color(22, 33, 54));
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 28, 0, 28),
                BorderFactory.createLineBorder(new Color(30, 41, 59), 1)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadData() {
        String search = txtSearch.getText().trim();
        SwingWorker<JsonObject, Void> worker = new SwingWorker<>() {
            @Override protected JsonObject doInBackground() throws Exception {
                return MahasiswaService.getAll(currentPage, PAGE_SIZE, search);
            }
            @Override protected void done() {
                try {
                    JsonObject resp = get();
                    tableModel.setRowCount(0);
                    if (resp.get("success").getAsBoolean()) {
                        JsonArray data = resp.getAsJsonArray("data");
                        JsonObject pag = resp.getAsJsonObject("pagination");
                        int total = pag.get("total").getAsInt();
                        int totalPages = pag.get("totalPages").getAsInt();

                        lblTotal.setText("Total: " + total + " mahasiswa | Halaman " + currentPage + " / " + totalPages);
                        btnPrev.setEnabled(currentPage > 1);
                        btnNext.setEnabled(currentPage < totalPages);

                        for (int i = 0; i < data.size(); i++) {
                            JsonObject m = data.get(i).getAsJsonObject();
                            tableModel.addRow(new Object[]{
                                    m.get("nim").getAsString(),
                                    m.get("nama").getAsString(),
                                    m.has("jurusan") && !m.get("jurusan").isJsonNull() ? m.get("jurusan").getAsString() : "-",
                                    m.has("program_studi") && !m.get("program_studi").isJsonNull() ? m.get("program_studi").getAsString() : "-",
                                    m.has("angkatan") && !m.get("angkatan").isJsonNull() ? m.get("angkatan").getAsString() : "-",
                                    m.has("semester") ? m.get("semester").getAsString() : "-",
                                    m.has("status") ? m.get("status").getAsString() : "aktif",
                                    "aksi"
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MahasiswaPanel.this,
                            "Gagal memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showForm(String nim) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                nim == null ? "Tambah Mahasiswa" : "Edit Mahasiswa", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(22, 33, 54));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(22, 33, 54));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1;

        JTextField fNim    = createFormField(); fNim.setEnabled(nim == null);
        JTextField fNama   = createFormField();
        JTextField fEmail  = createFormField();
        JTextField fTelp   = createFormField();
        JTextField fJurusan= createFormField();
        JTextField fProdi  = createFormField();
        JTextField fAngkatan = createFormField();
        JTextField fSemester = createFormField();
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"aktif", "cuti", "lulus", "drop_out"});
        cmbStatus.setBackground(new Color(30, 41, 59)); cmbStatus.setForeground(Color.WHITE);
        JTextField fPassword = nim == null ? createFormField() : null;

        // Pre-fill if edit
        if (nim != null) {
            SwingWorker<JsonObject, Void> w = new SwingWorker<>() {
                @Override protected JsonObject doInBackground() throws Exception { return MahasiswaService.getByNim(nim); }
                @Override protected void done() {
                    try {
                        JsonObject r = get();
                        if (r.get("success").getAsBoolean()) {
                            JsonObject d = r.getAsJsonObject("data");
                            fNim.setText(d.get("nim").getAsString());
                            fNama.setText(d.get("nama").getAsString());
                            if (!d.get("email").isJsonNull()) fEmail.setText(d.get("email").getAsString());
                            if (!d.get("no_telp").isJsonNull()) fTelp.setText(d.get("no_telp").getAsString());
                            if (!d.get("jurusan").isJsonNull()) fJurusan.setText(d.get("jurusan").getAsString());
                            if (!d.get("program_studi").isJsonNull()) fProdi.setText(d.get("program_studi").getAsString());
                            if (!d.get("angkatan").isJsonNull()) fAngkatan.setText(d.get("angkatan").getAsString());
                            fSemester.setText(d.get("semester").getAsString());
                            cmbStatus.setSelectedItem(d.get("status").getAsString());
                        }
                    } catch (Exception ignored) {}
                }
            };
            w.execute();
        }

        addFormRow(panel, gbc, 0, "NIM *", fNim);
        addFormRow(panel, gbc, 1, "Nama Lengkap *", fNama);
        addFormRow(panel, gbc, 2, "Email", fEmail);
        addFormRow(panel, gbc, 3, "No. Telp", fTelp);
        addFormRow(panel, gbc, 4, "Jurusan", fJurusan);
        addFormRow(panel, gbc, 5, "Program Studi", fProdi);
        addFormRow(panel, gbc, 6, "Angkatan", fAngkatan);
        addFormRow(panel, gbc, 7, "Semester", fSemester);
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(makeLabel("Status"), gbc);
        gbc.gridx = 1;
        panel.add(cmbStatus, gbc);
        if (nim == null) addFormRow(panel, gbc, 9, "Password (default=NIM)", fPassword);

        JButton btnSave = createBtn(nim == null ? "Simpan" : "Update", new Color(59, 130, 246));
        btnSave.addActionListener(e -> {
            JsonObject body = new JsonObject();
            body.addProperty("nim", fNim.getText().trim());
            body.addProperty("nama", fNama.getText().trim());
            body.addProperty("email", fEmail.getText().trim());
            body.addProperty("no_telp", fTelp.getText().trim());
            body.addProperty("jurusan", fJurusan.getText().trim());
            body.addProperty("program_studi", fProdi.getText().trim());
            body.addProperty("angkatan", fAngkatan.getText().trim());
            body.addProperty("semester", fSemester.getText().trim());
            body.addProperty("status", (String) cmbStatus.getSelectedItem());
            if (nim == null && fPassword != null) body.addProperty("password", fPassword.getText().trim());

            SwingWorker<JsonObject, Void> sw = new SwingWorker<>() {
                @Override protected JsonObject doInBackground() throws Exception {
                    return nim == null ? MahasiswaService.create(body) : MahasiswaService.update(nim, body);
                }
                @Override protected void done() {
                    try {
                        JsonObject r = get();
                        if (r.get("success").getAsBoolean()) {
                            JOptionPane.showMessageDialog(dialog, r.get("message").getAsString(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose(); loadData();
                        } else {
                            JOptionPane.showMessageDialog(dialog, r.get("message").getAsString(), "Gagal", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            sw.execute();
        });

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2; gbc.insets = new Insets(16, 4, 4, 4);
        panel.add(btnSave, gbc);

        dialog.add(new JScrollPane(panel) {{ setBorder(null); }});
        dialog.setVisible(true);
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(22, 33, 54));
        t.setForeground(new Color(203, 213, 225));
        t.setSelectionBackground(new Color(37, 99, 235, 80));
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(new Color(30, 41, 59));
        t.setRowHeight(40);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(1, 1));
        t.setFillsViewportHeight(true);

        JTableHeader th = t.getTableHeader();
        th.setBackground(new Color(15, 23, 42));
        th.setForeground(new Color(148, 163, 184));
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 41, 59)));
        th.setReorderingAllowed(false);
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(makeLabel(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }

    private JTextField createFormField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(30, 41, 59));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(6, 10, 6, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setPreferredSize(new Dimension(220, 32));
        return f;
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(7, 14, 7, 14)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    // ---- Action Button Renderer/Editor for table ----
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnDetail = new JButton("Detail");
        private final JButton btnEdit   = new JButton("Edit");
        ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 4));
            setOpaque(false);
            btnDetail.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnDetail.setBackground(new Color(30, 41, 59));
            btnDetail.setForeground(Color.WHITE); btnDetail.setBorderPainted(false); btnDetail.setFocusPainted(false);
            btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnEdit.setBackground(new Color(37, 99, 235));
            btnEdit.setForeground(Color.WHITE); btnEdit.setBorderPainted(false); btnEdit.setFocusPainted(false);
            add(btnDetail); add(btnEdit);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setBackground(sel ? new Color(37, 99, 235, 40) : new Color(22, 33, 54));
            return this;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 4));
        private final JButton btnDetail = new JButton("Detail");
        private final JButton btnEdit   = new JButton("Edit");
        private String nim;
        private final boolean isAdminUser;

        ActionButtonEditor(JCheckBox chk, boolean isAdmin) {
            super(chk);
            isAdminUser = isAdmin;
            panel.setBackground(new Color(22, 33, 54));
            styleActionBtn(btnDetail, new Color(30, 41, 59));
            styleActionBtn(btnEdit, new Color(37, 99, 235));
            panel.add(btnDetail);
            if (isAdminUser) panel.add(btnEdit);

            btnDetail.addActionListener(e -> {
                fireEditingStopped();
                if (nim != null) showForm(nim);
            });
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (nim != null) showForm(nim);
            });
        }

        void styleActionBtn(JButton btn, Color bg) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btn.setBackground(bg); btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false); btn.setFocusPainted(false);
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            nim = (String) t.getValueAt(r, 0);
            return panel;
        }
        @Override public Object getCellEditorValue() { return "aksi"; }
    }
}
