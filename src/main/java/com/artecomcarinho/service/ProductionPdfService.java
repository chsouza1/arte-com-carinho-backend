package com.artecomcarinho.service;

import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.ProductionOrder;
import com.artecomcarinho.model.enums.ProductionStage;
import com.artecomcarinho.model.enums.ProductionStatus;
import com.artecomcarinho.repository.OrderRepository;
import com.artecomcarinho.repository.ProductionOrderRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ProductionPdfService {

    private final OrderRepository orderRepository;
    private final ProductionOrderRepository productionOrderRepository;

    public byte[] generateProductionPdf(Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + orderId));

        ProductionOrder po = productionOrderRepository.findById(orderId)
                .orElseGet(() -> productionOrderRepository.save(
                        ProductionOrder.builder()
                                .order(o)
                                .stage(ProductionStage.BORDADO)
                                .status(ProductionStatus.PENDING)
                                .updatedAt(java.time.LocalDateTime.now())
                                .build()
                ));

        String customerName =
                (o.getCustomer() != null && o.getCustomer().getName() != null)
                        ? o.getCustomer().getName()
                        : "Cliente";

        String orderNumber =
                (o.getOrderNumber() != null) ? o.getOrderNumber() : "#" + o.getId();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);

        doc.open();

        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font h = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font p = FontFactory.getFont(FontFactory.HELVETICA, 10);

        Paragraph header = new Paragraph("Ficha de Produção", title);
        header.setSpacingAfter(10);
        doc.add(header);

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingAfter(10);
        info.setWidths(new float[]{1.2f, 2.8f});

        addKeyValue(info, "Pedido", orderNumber, h, p);
        addKeyValue(info, "Cliente", customerName, h, p);
        addKeyValue(info, "Etapa atual", po.getStage().name(), h, p);
        addKeyValue(info, "Status", po.getStatus().name(), h, p);

        String updatedAt = po.getUpdatedAt() == null
                ? "-"
                : po.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        addKeyValue(info, "Atualizado em", updatedAt, h, p);

        doc.add(info);

        // Itens
        Paragraph itemsTitle = new Paragraph("Itens do pedido", h);
        itemsTitle.setSpacingAfter(6);
        doc.add(itemsTitle);

        PdfPTable items = new PdfPTable(2);
        items.setWidthPercentage(100);
        items.setWidths(new float[]{3.5f, 1f});
        items.setSpacingAfter(10);

        addHeader(items, "Item", "Qtd", h);

        if (o.getItems() != null && !o.getItems().isEmpty()) {
            o.getItems().forEach(it -> {
                String name =
                        (it.getProduct() != null && it.getProduct().getName() != null)
                                ? it.getProduct().getName()
                                : "Item";
                String qty = (it.getQuantity() != null) ? String.valueOf(it.getQuantity()) : "1";

                items.addCell(cell(name, p));
                items.addCell(cell(qty, p));
            });
        } else {
            PdfPCell c = new PdfPCell(new Phrase("Sem itens (verifique modelo do pedido)", p));
            c.setColspan(2);
            c.setPadding(6);
            items.addCell(c);
        }

        doc.add(items);

        // Observações
        Paragraph notesTitle = new Paragraph("Observações da produção", h);
        notesTitle.setSpacingAfter(6);
        doc.add(notesTitle);

        PdfPCell notesCell = new PdfPCell(new Phrase(
                (po.getNotes() == null || po.getNotes().isBlank()) ? "—" : po.getNotes(), p
        ));
        notesCell.setPadding(8);
        notesCell.setMinimumHeight(80);

        PdfPTable notesTable = new PdfPTable(1);
        notesTable.setWidthPercentage(100);
        notesTable.addCell(notesCell);
        notesTable.setSpacingAfter(12);
        doc.add(notesTable);

        // Checklist
        Paragraph chkTitle = new Paragraph("Checklist (conferência)", h);
        chkTitle.setSpacingAfter(6);
        doc.add(chkTitle);

        PdfPTable chk = new PdfPTable(2);
        chk.setWidthPercentage(100);
        chk.setWidths(new float[]{3f, 1f});
        chk.setSpacingAfter(10);

        addHeader(chk, "Etapa", "OK", h);

        addChecklistRow(chk, "Bordado", p);
        addChecklistRow(chk, "Costura", p);
        addChecklistRow(chk, "Acabamento", p);
        addChecklistRow(chk, "Embalagem", p);

        doc.add(chk);

        doc.close();

        return out.toByteArray();
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

    private void addHeader(PdfPTable t, String a, String b, Font f) {
        PdfPCell h1 = new PdfPCell(new Phrase(a, f));
        PdfPCell h2 = new PdfPCell(new Phrase(b, f));
        h1.setPadding(6);
        h2.setPadding(6);
        h1.setBackgroundColor(new Color(245, 245, 245));
        h2.setBackgroundColor(new Color(245, 245, 245));
        t.addCell(h1);
        t.addCell(h2);
    }

    private PdfPCell cell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "-" : text, f));
        c.setPadding(6);
        return c;
    }

    private void addChecklistRow(PdfPTable t, String label, Font f) {
        t.addCell(cell(label, f));
        PdfPCell box = new PdfPCell(new Phrase("   ", f));
        box.setPadding(6);
        t.addCell(box);
    }
}
