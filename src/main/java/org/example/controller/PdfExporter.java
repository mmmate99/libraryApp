package org.example.controller;

import com.sun.prism.paint.Color;
import org.example.model.Book;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.FileOutputStream;
import java.util.List;

public class PdfExporter {
    public static void export(List<Book> books, String filename) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();

        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
        Font font = new Font(bf, 12);
        document.add(new Paragraph("Könyvlista", font));


        //document.add(new Paragraph("Könyvlista", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        //document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.addCell("Cím");
        table.addCell("Szerző");
        table.addCell("ISBN");
        table.addCell("Zsáner");
        table.addCell("Állapot");
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 3, 2, 2, 2});

        for (Book b : books) {
            table.addCell(b.getTitle());
            table.addCell(b.getAuthor());
            table.addCell(b.getIsbn());
            table.addCell(b.getGenre());
            table.addCell(b.isBorrowed() ? "Kölcsönzött" : "Elérhető");
        }

        document.add(table);
        document.close();
    }
}

