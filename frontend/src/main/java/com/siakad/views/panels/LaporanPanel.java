package com.siakad.views.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.siakad.services.LaporanService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;

/**
 * LaporanPanel - Generate & Cetak Laporan (Admin only)
 */
public class LaporanPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private int currentPage = 1;
    private CardLayout centerCard;
    private JPanel centerPanel;
    private StatePanel statePanel;

    // Warna tema
    private static final Color BG           = new Color(13, 19, 38);
    private static final Color CARD_BG      = new Color(18, 26, 48);
    private static final Color TABLE_BG     = new Color(15, 22, 42);
    private static final Color HEADER_BG    = new Color(10, 15, 30);
    private static final Color BORDER_COLOR = new Color(25, 36, 65);
    private static final Color ROW_ALT      = new Color(20, 29, 52);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_MUTED   = new Color(148, 163, 184);
    private static final Color TEXT_DIM     = new Color(71, 85, 105);
    private static final Color BLUE         = new Color(59, 130, 246);
    private static final Color GREEN        = new Color(34, 197, 94);
    private static final Color YELLOW       = new Color(234, 179, 8);
    private static final Color RED          = new Color(239, 68, 68);
    private static final Color PURPLE       = new Color(168, 85, 247);

    public LaporanPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        initUI();
        loadData();
    }

    private void initUI() {
        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Laporan & Cetak");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Generate dan unduh laporan akademik & keuangan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblSub);
        header.add(titleBlock, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnRefresh = buildBtn("🔄  Refresh", CARD_BG, 110);
        btnRefresh.addActionListener(e -> loadData());
        actions.add(btnRefresh);
        header.add(actions, BorderLayout.EAST);

        // ── Generate Cards ──
        JPanel genCards = new JPanel(new GridLayout(1, 3, 14, 0));
        genCards.setOpaque(false);
        genCards.setBorder(new EmptyBorder(0, 28, 20, 28));

        genCards.add(buildGenerateCard(
            "📊", "Laporan Pembayaran",
            "Generate laporan transaksi pembayaran UKT berdasarkan periode dan tahun ajaran.",
            BLUE, e -> showGeneratePembayaranDialog()
        ));
        genCards.add(buildGenerateCard(
            "👨‍🎓", "Laporan Mahasiswa",
            "Generate laporan data seluruh mahasiswa terdaftar beserta status akademik.",
            GREEN, e -> generateMahasiswa()
        ));
        genCards.add(buildGenerateCard(
            "💰", "Laporan Keuangan",
            "Generate ringkasan keuangan dan rekapitulasi pendapatan per tahun ajaran.",
            YELLOW, e -> showGenerateKeuanganDialog()
        ));

        // ── Table ──
        String[] cols = {"ID", "Judul", "Jenis", "Periode", "Tahun Ajaran", "Records", "Status", "Dibuat", "Aksi"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? TABLE_BG : ROW_ALT);
                return c;
            }
        };
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(8).setMinWidth(110);
        table.getColumnModel().getColumn(8).setMaxWidth(110);
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new ActionEditor());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(TABLE_BG);

        JPanel tableCard = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        tableCard.setOpaque(false);
        tableCard.setBorder(new EmptyBorder(0, 28, 0, 28));
        tableCard.add(sp);

        // ── Footer ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 28, 14, 28));
        lblTotal = new JLabel("Total: 0 laporan");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTotal.setForeground(TEXT_DIM);
        footer.add(lblTotal);

        // ── Body ──
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(genCards);
        body.add(tableCard);
        body.add(footer);

        centerCard = new CardLayout();
        centerPanel = new JPanel(centerCard);
        centerPanel.setOpaque(false);
        statePanel = new StatePanel();

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(scroll, "content");
        centerPanel.add(statePanel, "state");

        add(header, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildGenerateCard(String icon, String title, String desc, Color accent,
                                      java.awt.event.ActionListener action) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                // Top accent
                GradientPaint gp = new GradientPaint(0, 0, accent,
                        getWidth(), 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(0, 160));

        // Icon circle
        JLabel lblIcon = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
                g2.fillOval(0, 0, 44, 44);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(44, 44); }
            @Override public Dimension getMaximumSize() { return new Dimension(44, 44); }
        };
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);
        lblIcon.setOpaque(false);
        lblIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><body style='width:180px'>" + desc + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(TEXT_MUTED);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnGen = new JButton("Generate  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? accent.brighter() : accent;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnGen.setPreferredSize(new Dimension(140, 34));
        btnGen.setMaximumSize(new Dimension(140, 34));
        btnGen.setBorderPainted(false);
        btnGen.setContentAreaFilled(false);
        btnGen.setFocusPainted(false);
        btnGen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGen.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGen.addActionListener(action);

        card.add(lblIcon);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblDesc);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(12));
        card.add(btnGen);
        return card;
    }

    private void loadData() {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return LaporanService.getAll(currentPage, 20, null);
            }
            protected void done() {
                try {
                    JsonObject resp = get();
                    tableModel.setRowCount(0);
                    if (resp.get("success").getAsBoolean()) {
                        JsonArray data = resp.getAsJsonArray("data");
                        JsonObject pg = resp.getAsJsonObject("pagination");
                        lblTotal.setText("Total: " + pg.get("total").getAsInt() + " laporan tersimpan");
                        if (data.size() == 0) {
                            lblTotal.setText("Belum ada laporan tersimpan. Gunakan kartu generate di atas untuk membuat laporan.");
                            centerCard.show(centerPanel, "content");
                            return;
                        }

                        for (int i = 0; i < data.size(); i++) {
                            JsonObject l = data.get(i).getAsJsonObject();
                            String periode = "";
                            if (l.has("periode_mulai") && !l.get("periode_mulai").isJsonNull())
                                periode = l.get("periode_mulai").getAsString().substring(0, 10);
                            if (l.has("periode_selesai") && !l.get("periode_selesai").isJsonNull())
                                periode += " ~ " + l.get("periode_selesai").getAsString().substring(0, 10);

                            tableModel.addRow(new Object[]{
                                l.get("id").getAsInt(),
                                l.get("judul").getAsString(),
                                l.get("jenis_laporan").getAsString(),
                                periode.isEmpty() ? "-" : periode,
                                l.has("tahun_ajaran") && !l.get("tahun_ajaran").isJsonNull() ? l.get("tahun_ajaran").getAsString() : "-",
                                l.get("total_records").getAsInt(),
                                l.get("status").getAsString(),
                                l.get("created_at").getAsString().substring(0, 10),
                                "aksi"
                            });
                        }
                        centerCard.show(centerPanel, "content");
                    } else {
                        showStateError(resp.has("message") ? resp.get("message").getAsString() : "Gagal memuat laporan.");
                    }
                } catch (Exception e) {
                    showStateError("Gagal memuat laporan: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void showStateError(String message) {
        statePanel.showState("!", "Laporan tidak bisa dimuat", message, "Muat ulang", this::loadData);
        centerCard.show(centerPanel, "state");
    }

    private void showGeneratePembayaranDialog() {
        JTextField fMulai   = makeField("YYYY-MM-DD");
        JTextField fSelesai = makeField("YYYY-MM-DD");
        JTextField fTA      = makeField("2024/2025");

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(8, 4, 8, 4));
        panel.add(makeLabel("Periode Mulai")); panel.add(fMulai);
        panel.add(makeLabel("Periode Selesai")); panel.add(fSelesai);
        panel.add(makeLabel("Tahun Ajaran")); panel.add(fTA);

        int result = JOptionPane.showConfirmDialog(this, panel, "Generate Laporan Pembayaran",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception {
                return LaporanService.generatePembayaran(
                        fMulai.getText().trim().isEmpty() ? null : fMulai.getText().trim(),
                        fSelesai.getText().trim().isEmpty() ? null : fSelesai.getText().trim(),
                        fTA.getText().trim().isEmpty() ? null : fTA.getText().trim());
            }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString(),
                            r.get("success").getAsBoolean() ? "Sukses" : "Gagal",
                            r.get("success").getAsBoolean() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void generateMahasiswa() {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.generateMahasiswa(); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString(), "Info", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showGenerateKeuanganDialog() {
        String ta = JOptionPane.showInputDialog(this, "Masukkan Tahun Ajaran (contoh: 2024/2025):", "2024/2025");
        if (ta == null || ta.trim().isEmpty()) return;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.generateKeuangan(ta.trim()); }
            protected void done() {
                try {
                    JsonObject r = get();
                    JOptionPane.showMessageDialog(LaporanPanel.this, r.get("message").getAsString());
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void viewDetail(int id) {
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.getById(id); }
            protected void done() {
                try {
                    JsonObject resp = get();
                    if (resp.get("success").getAsBoolean()) {
                        JsonObject d = resp.getAsJsonObject("data");
                        String info = "Judul      : " + d.get("judul").getAsString() + "\n"
                                + "Jenis      : " + d.get("jenis_laporan").getAsString() + "\n"
                                + "Records    : " + d.get("total_records").getAsInt() + "\n"
                                + "Status     : " + d.get("status").getAsString() + "\n"
                                + "Dibuat     : " + d.get("created_at").getAsString() + "\n";
                        if (d.has("deskripsi") && !d.get("deskripsi").isJsonNull())
                            info += "Deskripsi  : " + d.get("deskripsi").getAsString();
                        JTextArea area = new JTextArea(info);
                        area.setEditable(false);
                        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        area.setBackground(CARD_BG);
                        area.setForeground(TEXT_PRIMARY);
                        area.setBorder(new EmptyBorder(8, 8, 8, 8));
                        JOptionPane.showMessageDialog(LaporanPanel.this, new JScrollPane(area),
                                "Detail Laporan #" + id, JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void deleteLaporan(int id) {
        int c = JOptionPane.showConfirmDialog(this, "Hapus laporan #" + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        new SwingWorker<JsonObject, Void>() {
            protected JsonObject doInBackground() throws Exception { return LaporanService.delete(id); }
            protected void done() {
                try { get(); loadData(); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private JButton buildBtn(String text, Color bg, int width) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(width, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField makeField(String hint) {
        JTextField f = new JTextField(14);
        f.setBackground(new Color(13, 19, 38));
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(6, 10, 6, 10)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        f.setToolTipText(hint);
        return f;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private void styleTable(JTable t) {
        t.setBackground(TABLE_BG);
        t.setForeground(TEXT_PRIMARY);
        t.setSelectionBackground(new Color(59, 130, 246, 60));
        t.setSelectionForeground(Color.WHITE);
        t.setGridColor(new Color(20, 30, 55));
        t.setRowHeight(42);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(HEADER_BG);
        h.setForeground(TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 40));
    }

    // ── Status Badge Renderer ─────────────────────────────────────────────────
    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String status = String.valueOf(v);
            Color badgeColor = switch (status.toLowerCase()) {
                case "selesai"    -> GREEN;
                case "diproses"   -> YELLOW;
                case "gagal"      -> RED;
                default           -> BLUE;
            };
            JLabel lbl = new JLabel(status.toUpperCase(), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 30));
                    g2.fillRoundRect(2, 5, getWidth() - 4, getHeight() - 10, 10, 10);
                    super.paintComponent(g);
                    g2.dispose();
                }
            };
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(badgeColor);
            lbl.setOpaque(false);
            lbl.setBackground(sel ? new Color(59, 130, 246, 60) : (r % 2 == 0 ? TABLE_BG : ROW_ALT));
            return lbl;
        }
    }

    // ── Action Renderer ───────────────────────────────────────────────────────
    class ActionRenderer extends JPanel implements TableCellRenderer {
        ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 6));
            setOpaque(false);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            removeAll();
            add(makeSmallBtn("👁 Detail", BLUE));
            add(makeSmallBtn("🗑", RED));
            setBackground(sel ? new Color(59, 130, 246, 40) : (r % 2 == 0 ? TABLE_BG : ROW_ALT));
            return this;
        }
        private JButton makeSmallBtn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 10));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1), new EmptyBorder(3, 6, 3, 6)));
            b.setFocusPainted(false);
            return b;
        }
    }

    // ── Action Editor ─────────────────────────────────────────────────────────
    class ActionEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private final JButton bView = makeSmallBtn("👁 Detail", BLUE);
        private final JButton bDel  = makeSmallBtn("🗑", RED);
        private int rowId;

        ActionEditor() {
            super(new JCheckBox());
            panel.setBackground(TABLE_BG);
            panel.add(bView);
            panel.add(bDel);
            bView.addActionListener(e -> { fireEditingStopped(); viewDetail(rowId); });
            bDel.addActionListener(e -> { fireEditingStopped(); deleteLaporan(rowId); });
        }

        private JButton makeSmallBtn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 10));
            b.setForeground(Color.WHITE);
            b.setBackground(bg);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1), new EmptyBorder(3, 6, 3, 6)));
            b.setFocusPainted(false);
            return b;
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            rowId = (int) t.getValueAt(r, 0);
            return panel;
        }
        @Override public Object getCellEditorValue() { return "aksi"; }
    }
}
