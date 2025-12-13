package com.artecomcarinho.service;

import com.artecomcarinho.model.Order;
import com.artecomcarinho.repository.OrderRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderPdfService {

    private final OrderRepository orderRepository;

    public byte[] generateOrderPdf(Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + orderId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font h = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font p = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // ===== Cabeçalho =====
        Paragraph header = new Paragraph("Pedido — Ficha de Produção/Conferência", title);
        header.setSpacingAfter(10);
        doc.add(header);

        // ===== Dados do pedido =====
        String orderNumber = safeOrderNumber(o);
        String customerName = safeCustomerName(o);

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingAfter(10);
        info.setWidths(new float[]{1.3f, 2.7f});

        addKeyValue(info, "Pedido", orderNumber, h, p);
        addKeyValue(info, "Cliente", customerName, h, p);

        addKeyValue(info, "Status", safeString(() -> String.valueOf(o.getStatus())), h, p);

        String createdAt = safeString(() -> {
            if (o.getOrderDate() != null) {
                return o.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return "-";
        });
        addKeyValue(info, "Data", createdAt, h, p);

        String total = safeString(() -> {
            if (o.getTotalAmount() == null) return "-";
            return "R$ " + o.getTotalAmount();
        });
        addKeyValue(info, "Total", total, h, p);

        doc.add(info);

        Paragraph itemsTitle = new Paragraph("Itens do pedido", h);
        itemsTitle.setSpacingAfter(6);
        doc.add(itemsTitle);

        PdfPTable items = new PdfPTable(4);
        items.setWidthPercentage(100);
        items.setSpacingAfter(10);
        items.setWidths(new float[]{3.2f, 1f, 1.2f, 1.2f});

        addHeader(items, "Produto", "Qtd", "Unit.", "Subtotal", h);

        if (o.getItems() != null && !o.getItems().isEmpty()) {
            o.getItems().forEach(it -> {
                String name = (it.getProduct() != null && it.getProduct().getName() != null)
                        ? it.getProduct().getName()
                        : "Item";

                String qty = it.getQuantity() != null ? String.valueOf(it.getQuantity()) : "1";

                // unit/subtotal: se tiver preço por item, ajuste conforme seu modelo
                String unit = safeString(() -> it.getUnitPrice() != null ? "R$ " + it.getUnitPrice() : "-");
                String sub = safeString(() -> it.getSubtotal() != null ? "R$ " + it.getSubtotal() : "-");

                items.addCell(cell(name, p));
                items.addCell(cell(qty, p));
                items.addCell(cell(unit, p));
                items.addCell(cell(sub, p));
            });
        } else {
            PdfPCell c = new PdfPCell(new Phrase("Sem itens (verifique o relacionamento no Order)", p));
            c.setColspan(4);
            c.setPadding(6);
            items.addCell(c);
        }

        doc.add(items);

        // ===== Observações (cliente / produção) =====
        Paragraph obsTitle = new Paragraph("Observações", h);
        obsTitle.setSpacingAfter(6);
        doc.add(obsTitle);

        String notes = safeString(() -> {
            if (o.getNotes() == null || o.getNotes().isBlank()) return "—";
            return o.getNotes();
        });

        PdfPTable notesTable = new PdfPTable(1);
        notesTable.setWidthPercentage(100);
        PdfPCell notesCell = new PdfPCell(new Phrase(notes, p));
        notesCell.setPadding(8);
        notesCell.setMinimumHeight(80);
        notesTable.addCell(notesCell);
        notesTable.setSpacingAfter(10);
        doc.add(notesTable);


        doc.close();
        return out.toByteArray();
    }

    private String safeOrderNumber(Order o) {
        try {
            if (o.getOrderNumber() != null && !o.getOrderNumber().isBlank()) return o.getOrderNumber();
        } catch (Exception ignored) {}
        return "#" + o.getId();
    }

    private String safeCustomerName(Order o) {
        try {
            if (o.getCustomer() != null && o.getCustomer().getName() != null) {
                return o.getCustomer().getName();
            }
        } catch (Exception ignored) {}
        return "Cliente";
    }

    private void addKeyValue(PdfPTable t, String k, String v, Font keyFont, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(k, keyFont));
        c1.setPadding(6);
        c1.setBackgroundColor(new Color(245, 245, 245));
        PdfPCell c2 = new PdfPCell(new Phrase(v == null ? "-" : v, valFont));
        c2.setPadding(6);
        t.addCell(c1);
        t.addCell(c2);
    }

    private void addHeader(PdfPTable t, String a, String b, String c, String d, Font f) {
        t.addCell(headerCell(a, f));
        t.addCell(headerCell(b, f));
        t.addCell(headerCell(c, f));
        t.addCell(headerCell(d, f));
    }

    private PdfPCell headerCell(String text, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setPadding(6);
        cell.setBackgroundColor(new Color(245, 245, 245));
        return cell;
    }

    private PdfPCell cell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "-" : text, f));
        c.setPadding(6);
        return c;
    }

    private String safeString(SupplierWithException supplier) {
        try {
            String v = supplier.get();
            return v == null ? "-" : v;
        } catch (Exception e) {
            return "-";
        }
    }

    @FunctionalInterface
    private interface SupplierWithException {
        String get() throws Exception;
    }
}
