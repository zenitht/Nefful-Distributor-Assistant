package com.neffulapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.neffulapp.model.PreviewItemObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class Pdf {

    private static final int MAX_LENGTH = 55;
    private Context context;
    private Document document;
    private static Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
    private static Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    private static DecimalFormat formatter = new DecimalFormat("#,###");
    private File file;
    private String name;
    private String fileName;
    private SharedPreferences pref;
    private List<PreviewItemObject> data;

    public Pdf(Context context, String name, Bundle data) {
        this.context = context;
        this.data = data.getParcelableArrayList("PreviewItemObject");
        setNames(name);
        try {
            createPdf();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void createPdf() throws IOException, DocumentException {
        file = File.createTempFile(fileName, ".pdf", context.getExternalCacheDir());
        document = new Document(PageSize.A4, 15, 15, 15, 15);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        // ***************************************************************************
        PdfPTable table = new PdfPTable(6);
        table.setHeaderRows(1);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 7, 27, 49.5f, 9, 4.5f});
        // ***************************************************************************
        Time now = new Time();
        now.setToNow();
        String date = now.monthDay + "/" + (now.month + 1) + "/" + now.year;
        PdfPCell cName = new PdfPCell(new Phrase("Name : " + name, boldFont));
        PdfPCell cDate = new PdfPCell(new Phrase("Date : " + date, boldFont));
        cName.setColspan(4);
        cDate.setColspan(2);
        cName.setBorder(Rectangle.LEFT | Rectangle.TOP);
        cDate.setBorder(Rectangle.RIGHT | Rectangle.TOP);
        cDate.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cName);
        table.addCell(cDate);
        table.completeRow();
        // ***************************************************************************
        PdfPCell cell1 = new PdfPCell(new Paragraph("No", boldFont));
        PdfPCell cell2 = new PdfPCell(new Paragraph("Code", boldFont));
        PdfPCell cell3 = new PdfPCell(new Paragraph("Product Name", boldFont));
        PdfPCell cell4 = new PdfPCell(new Paragraph("Description", boldFont));
        PdfPCell cell5 = new PdfPCell(new Paragraph("Sum(RM)", boldFont));
        PdfPCell cell6 = new PdfPCell(new Paragraph("L/C", boldFont));
        // ***************************************************************************
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
        // ***************************************************************************
        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);
        table.addCell(cell4);
        table.addCell(cell5);
        table.addCell(cell6);
        table.completeRow();
        // ***************************************************************************
        int orderTotal = 0;
        int laborCostTotal = 0;
        for (int i = 0; i < data.size(); i++) {
            // **************************************************************************
            orderTotal = orderTotal + data.get(i).getSubtotal();
            laborCostTotal = laborCostTotal + data.get(i).getLabor();
            // ***************************************************************************
            String no = Integer.toString(i + 1);
            String productCode = data.get(i).getCode();
            String productName = data.get(i).getName();
            String remark = data.get(i).getRemark();
            // ***************************************************************************
            String description = "";
            int maxLength = 0;
            for (String string : data.get(i).getStackList()) {
                if (string.length() > maxLength) {
                    maxLength = string.length();
                }
            }
            int stringsPerRow = (int) Math.floor(MAX_LENGTH / maxLength);
            int j = 1;
            for (String bundle : data.get(i).getStackList()) {
                if (j % stringsPerRow == 0 && j < data.get(i).getStackList().size()) {
                    description = description + "(" + bundle + ")\n";
                } else {
                    description = description + "(" + bundle + ") ";
                }
                j++;
            }
            // ***************************************************************************
            if (remark != null && !remark.isEmpty()) {
                description = description + " * " + remark;
            }
            // ***************************************************************************
            String subTotal = Integer.toString(data.get(i).getSubtotal());
            String laborCost = "";
            if (data.get(i).getLabor() > 0) {
                laborCost = Integer.toString(data.get(i).getLabor());
            }
            // ***************************************************************************
            PdfPCell cNo = new PdfPCell(new Paragraph(no, normalFont));
            PdfPCell cProductCode = new PdfPCell(new Paragraph(productCode, normalFont));
            PdfPCell cProductName = new PdfPCell(new Paragraph(productName, normalFont));
            PdfPCell cDescription = new PdfPCell(new Paragraph(description, normalFont));
            PdfPCell cSum = new PdfPCell(new Paragraph(subTotal, normalFont));
            PdfPCell cLaborCost = new PdfPCell(new Paragraph(laborCost, normalFont));
            // ***************************************************************************
            cNo.setPaddingBottom(5);
            cProductCode.setPaddingBottom(5);
            cProductName.setPaddingBottom(5);
            cDescription.setPaddingBottom(5);
            cSum.setPaddingBottom(5);
            cLaborCost.setPaddingBottom(5);
            // ***************************************************************************
            cNo.setLeading(1, 1);
            cProductCode.setLeading(1, 1);
            cProductName.setLeading(1, 1);
            cDescription.setLeading(1, 1);
            cSum.setLeading(1, 1);
            cLaborCost.setLeading(1, 1);
            // ***************************************************************************
            cSum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cLaborCost.setHorizontalAlignment(Element.ALIGN_CENTER);
            // ***************************************************************************
            table.addCell(cNo);
            table.addCell(cProductCode);
            table.addCell(cProductName);
            table.addCell(cDescription);
            table.addCell(cSum);
            table.addCell(cLaborCost);
            table.completeRow();
        }
        // ***************************************************************************
        PdfPCell cOTLabel = new PdfPCell(new Paragraph("Order Total :\u00A0", boldFont));
        PdfPCell cOTvalue = new PdfPCell(new Paragraph(formatter.format(orderTotal), normalFont));
        PdfPCell cOTLC = new PdfPCell(new Paragraph((laborCostTotal == 0) ? "" : Integer.toString(laborCostTotal), normalFont));
        cOTLabel.setColspan(4);
        cOTLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cOTvalue.setHorizontalAlignment(Element.ALIGN_CENTER);
        cOTLC.setHorizontalAlignment(Element.ALIGN_CENTER);
        cOTLabel.setPaddingBottom(5);
        cOTvalue.setPaddingBottom(5);
        cOTLC.setPaddingBottom(5);
        table.addCell(cOTLabel);
        table.addCell(cOTvalue);
        table.addCell(cOTLC);
        table.completeRow();
        // **************************************************************************
        double memberDisc = orderTotal * 0.2;
        PdfPCell cMDLabel = new PdfPCell(new Paragraph("Member Disc :\u00A0", boldFont));
        PdfPCell cMDValue = new PdfPCell(new Paragraph(formatter.format(memberDisc), normalFont));
        cMDLabel.setColspan(4);
        cMDLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cMDValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        cMDLabel.setPaddingBottom(5);
        cMDValue.setPaddingBottom(5);
        table.addCell(cMDLabel);
        table.addCell(cMDValue);
        table.addCell("");
        table.completeRow();
        // **************************************************************************
        double grandTotal = orderTotal * 0.8;
        PdfPCell cGTLabel = new PdfPCell(new Paragraph("Grand Total :\u00A0", boldFont));
        PdfPCell cGTValue = new PdfPCell(new Paragraph(formatter.format(grandTotal), normalFont));
        cGTLabel.setColspan(4);
        cGTLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cGTValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        cGTLabel.setPaddingBottom(5);
        cGTValue.setPaddingBottom(5);
        table.addCell(cGTLabel);
        table.addCell(cGTValue);
        table.addCell("");
        table.completeRow();
        // **************************************************************************
        double totalPayable = grandTotal + laborCostTotal;
        PdfPCell cTPLabel = new PdfPCell(new Paragraph("Total Payable (GT+L/C) :\u00A0", boldFont));
        PdfPCell cTPValue = new PdfPCell(new Paragraph(formatter.format(totalPayable), normalFont));
        cTPLabel.setColspan(4);
        cTPLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cTPValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        cTPLabel.setPaddingBottom(5);
        cTPValue.setPaddingBottom(5);
        table.addCell(cTPLabel);
        table.addCell(cTPValue);
        table.addCell("");
        table.completeRow();
        // **************************************************************************
        document.add(table);
        document.close();
    }

    private void setNames(String name) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        String listName = pref.getString(context.getString(R.string.list_name), context.getString(R.string.default_name));
        String defaultListName = context.getString(R.string.default_list_name);
        if (name.isEmpty()) {
            if (!listName.equals(defaultListName)) {
                this.name = listName;
                fileName = listName + "_0";
            } else {
                this.name = "";
                fileName = defaultListName + "_0";
            }
        } else {
            this.name = name;
            fileName = name + "_0";
        }
    }

    public File getFile() {
        return file;
    }
}
