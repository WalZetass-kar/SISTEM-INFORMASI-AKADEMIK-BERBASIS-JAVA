package com.siakad.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ReportExporter - Cetak laporan ke PDF (iText)
 */
public class ReportExporter {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

    public static File exportToPdf(JsonObject laporan, Component parent) throws Exception {
        String judul = laporan.get("judul").getAsString();
        String jenis = laporan.get("jenis_laporan").getAsString();
        String safeName = judul.replaceAll("[^a-zA-Z0-9\\-_ ]", "").trim().replace(' ', '_');
        if (safeName.isEmpty()) safeName = "laporan_" + laporan.get("id").getAsInt();

        File outDir = new File(System.getProperty("user.home"), "SiakadLaporan");
        if (!outDir.exists()) outDir.mkdirs();

        File file = new File(outDir, safeName + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf");

        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        Paragraph title = new Paragraph("SISTEM INFORMASI AKADEMIK", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph(judul, HEADER_FONT));
        doc.add(new Paragraph("Jenis: " + jenis + "  |  ID: " + laporan.get("id").getAsInt(), BODY_FONT));
        if (laporan.has("tahun_ajaran") && !laporan.get("tahun_ajaran").isJsonNull()) {
            doc.add(new Paragraph("Tahun Ajaran: " + laporan.get("tahun_ajaran").getAsString(), BODY_FONT));
        }
        doc.add(new Paragraph("Total Records: " + laporan.get("total_records").getAsInt(), BODY_FONT));
        doc.add(new Paragraph("Dicetak: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), BODY_FONT));
        doc.add(Chunk.NEWLINE);

        if (laporan.has("data_laporan") && !laporan.get("data_laporan").isJsonNull()) {
            JsonElement dataEl = laporan.get("data_laporan");
            if (dataEl.isJsonObject()) {
                appendLaporanData(doc, dataEl.getAsJsonObject(), jenis);
            }
        }

        doc.close();

        int open = JOptionPane.showConfirmDialog(parent,
                "Laporan berhasil disimpan:\n" + file.getAbsolutePath() + "\n\nBuka file sekarang?",
                "Cetak Laporan", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
        return file;
    }

    private static void appendLaporanData(Document doc, JsonObject data, String jenis) throws DocumentException {
        switch (jenis) {
            case "pembayaran" -> appendPembayaranRecords(doc, data);
            case "mahasiswa" -> appendMahasiswaStats(doc, data);
            case "keuangan" -> appendKeuanganStats(doc, data);
            default -> doc.add(new Paragraph(data.toString(), BODY_FONT));
        }
    }

    private static void appendPembayaranRecords(Document doc, JsonObject data) throws DocumentException {
        if (data.has("summary")) {
            JsonObject s = data.getAsJsonObject("summary");
            doc.add(new Paragraph(String.format(
                    "Ringkasan: %d transaksi | Lunas: Rp %,.0f | Pending: Rp %,.0f",
                    s.get("total_transaksi").getAsInt(),
                    s.get("total_lunas").getAsDouble(),
                    s.get("total_pending").getAsDouble()), HEADER_FONT));
            doc.add(Chunk.NEWLINE);
        }
        if (!data.has("records")) return;
        JsonArray records = data.getAsJsonArray("records");
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 3f, 2f, 2f, 2f, 2f});
        addHeader(table, "NIM", "Nama", "Jumlah", "Tanggal", "Metode", "Status");
        for (int i = 0; i < records.size(); i++) {
            JsonObject r = records.get(i).getAsJsonObject();
            table.addCell(cell(r.get("nim").getAsString()));
            table.addCell(cell(r.has("nama_mahasiswa") && !r.get("nama_mahasiswa").isJsonNull()
                    ? r.get("nama_mahasiswa").getAsString() : "-"));
            table.addCell(cell(String.format("Rp %,.0f", r.get("jumlah").getAsDouble())));
            table.addCell(cell(r.get("tanggal_bayar").getAsString().substring(0, 10)));
            table.addCell(cell(r.get("metode_pembayaran").getAsString()));
            table.addCell(cell(r.get("status").getAsString()));
        }
        doc.add(table);
    }

    private static void appendMahasiswaStats(Document doc, JsonObject data) throws DocumentException {
        doc.add(new Paragraph("Total Mahasiswa: " + data.get("total").getAsInt(), HEADER_FONT));
        doc.add(Chunk.NEWLINE);
        if (data.has("per_jurusan")) {
            doc.add(new Paragraph("Per Jurusan & Status", HEADER_FONT));
            PdfPTable t = new PdfPTable(3);
            t.setWidthPercentage(100);
            addHeader(t, "Jurusan", "Status", "Jumlah");
            JsonArray arr = data.getAsJsonArray("per_jurusan");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject row = arr.get(i).getAsJsonObject();
                t.addCell(cell(row.get("jurusan").getAsString()));
                t.addCell(cell(row.get("status").getAsString()));
                t.addCell(cell(String.valueOf(row.get("total").getAsInt())));
            }
            doc.add(t);
        }
    }

    private static void appendKeuanganStats(Document doc, JsonObject data) throws DocumentException {
        doc.add(new Paragraph(String.format("Total Pendapatan: Rp %,.0f",
                data.get("total_pendapatan").getAsDouble()), HEADER_FONT));
        doc.add(Chunk.NEWLINE);
        if (data.has("pendapatan_bulanan")) {
            PdfPTable t = new PdfPTable(4);
            t.setWidthPercentage(100);
            addHeader(t, "Bulan", "Status", "Transaksi", "Jumlah");
            JsonArray arr = data.getAsJsonArray("pendapatan_bulanan");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject row = arr.get(i).getAsJsonObject();
                t.addCell(cell(row.get("bulan").getAsString()));
                t.addCell(cell(row.get("status").getAsString()));
                t.addCell(cell(String.valueOf(row.get("total").getAsInt())));
                t.addCell(cell(String.format("Rp %,.0f", row.get("jumlah").getAsDouble())));
            }
            doc.add(t);
        }
    }

    private static void addHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, HEADER_FONT));
            c.setBackgroundColor(new BaseColor(230, 236, 245));
            c.setPadding(5);
            table.addCell(c);
        }
    }

    private static PdfPCell cell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, BODY_FONT));
        c.setPadding(4);
        return c;
    }
}
