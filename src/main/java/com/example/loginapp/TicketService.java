package com.example.loginapp;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;

public class TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    public void generarTicketPDF(Factura factura, String rutaDestino) throws FileNotFoundException {
        // 1. Crear el escritor y el documento PDF
        PdfWriter writer = new PdfWriter(rutaDestino);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 2. Encabezado
        document.add(new Paragraph("MINIVET CORE")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Tu Veterinaria de Confianza\nDirección: Calle Ejemplo 123, Puebla\nTel: 555-0000")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("\n------------------------------------------------\n"));

        // 3. Datos de la Venta
        document.add(new Paragraph("Folio: " + factura.getNumeroFactura()));
        document.add(new Paragraph("Fecha: " + factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        document.add(new Paragraph("Cliente: " + factura.getCliente().getNombreCompleto()));
        document.add(new Paragraph("\n"));

        // 4. Tabla de Productos
        // Tabla con 3 columnas: Cantidad, Descripción, Importe
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 4, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados de tabla
        table.addHeaderCell(new Cell().add(new Paragraph("Cant").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Importe").setBold()).setTextAlignment(TextAlignment.RIGHT));

        // Llenar con los detalles
        if (factura.getDetalles() != null) {
            for (DetalleFactura detalle : factura.getDetalles()) {
                table.addCell(String.valueOf(detalle.getCantidad()));
                table.addCell(detalle.getProducto().getNombre());
                table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", detalle.getSubtotal())))
                        .setTextAlignment(TextAlignment.RIGHT));
            }
        }

        document.add(table);

        // 5. Totales
        document.add(new Paragraph("\n------------------------------------------------\n"));

        Table totales = new Table(UnitValue.createPercentArray(new float[]{4, 2}));
        totales.setWidth(UnitValue.createPercentValue(100));

        totales.addCell(new Cell().add(new Paragraph("SUBTOTAL:")).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
        totales.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getSubtotal()))).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

        totales.addCell(new Cell().add(new Paragraph("IVA (16%):")).setBorder(null).setTextAlignment(TextAlignment.RIGHT));
        totales.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getIva()))).setBorder(null).setTextAlignment(TextAlignment.RIGHT));

        totales.addCell(new Cell().add(new Paragraph("TOTAL:")).setBold().setBorder(null).setTextAlignment(TextAlignment.RIGHT));
        totales.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getTotal()))).setBold().setBorder(null).setTextAlignment(TextAlignment.RIGHT));

        document.add(totales);

        // 6. Pie de página
        document.add(new Paragraph("\n\n¡Gracias por su preferencia!")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic());

        // Cerrar el documento
        document.close();
        logger.info("PDF generado exitosamente en: {}", rutaDestino);

        // Intentar abrir el archivo automáticamente
        abrirArchivo(rutaDestino);
    }

    private void abrirArchivo(String ruta) {
        try {
            File archivo = new File(ruta);
            if (archivo.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
            }
        } catch (Exception e) {
            logger.error("No se pudo abrir el archivo automáticamente", e);
        }
    }
}