package de.xdot.pdf.creation;


import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import de.xdot.pdf.creation.model.OnlineApplicationFormModel;
import de.xdot.pdf.creation.model.sub.ExpensesModel;
import de.xdot.pdf.creation.service.impl.PDFGeneratorImpl;
import de.xdot.pdf.creation.status.ProcessIdHolder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static de.xdot.pdf.creation.constants.PDFCreationConstants.AID;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.DENTAL_TREATMENT;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.HEALER;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.HOSPITAL;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.MEDICAL_TREATMENT;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.NO;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.OTHER;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.RECHNUNG;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.REZEPT;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.TRAVEL_EXPENSES;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.YES;

public class TextWriter {
    private static final Log _LOG = LogFactoryUtil.getLog(TextWriter.class);
    private final Float ROW_HEIGHT = 15F;
    private final Float MARGIN = 33f;
    private final Float TABLE_MARGIN = 27f;
    private final Float AFTER_YES_MARGIN = 48F;
    private final Float AFTER_NO_MARGIN = 55F;
    private static final float A_4_WIDTH = 595;
    private static final float A_4_HEIGHT = 815F;

    private static final float TEXT_FONT_SIZE = 8f;
    private static final float TABLE_FONT_SIZE = 10f;
    private static final float HEADER_FONT_SIZE = 16f;

    private PDPage page = new PDPage(PDRectangle.A4);
    private PDDocument doc = null;
    private Integer rowNumber = 1;
    private PDPageContentStream contentStream = null;
    private final Map<String, String> serviceType = new HashMap<String, String>() {{
        put("1", MEDICAL_TREATMENT);
        put("2", DENTAL_TREATMENT);
        put("3", TRAVEL_EXPENSES);
        put("4", HEALER);
        put("5", AID);
        put("6", HOSPITAL);
        put("7", REZEPT);
        put("8", OTHER);
    }};
    private final Map<String, String> documentType = new HashMap<String, String>() {{
        put("1", RECHNUNG);
        put("2", REZEPT);
    }};
    private List<String> resultTableHeaders = new LinkedList<String>() {{
        add("Belegnr.");
        add("Datum der Rechnung");
        add("Art der Leistung");
        add("Rechnungs-betrag");
        add("Kosten-erstattung von anderer Seite");
        add("Dokument");
        add("Dokumententyp");
    }};

    private List<Float> columnSizes = Arrays.asList(
        7f,
        12f,
        12f,
        12f,
        12f,
        20f,
        16f
    );

    public PDDocument writeText(Float fontSize, OnlineApplicationFormModel onlineApplicationFormModel) throws IOException {
        this.doc = new PDDocument();
        PDFAConfigs pdfaConfigs = new PDFAConfigs();
        try {
            pdfaConfigs.applyPDFAConfig(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        doc.addPage(page);
        InputStream fontStream = PDFGeneratorImpl.class.getResourceAsStream("/FreeSans.ttf");
        InputStream fontBoldStream = PDFGeneratorImpl.class.getResourceAsStream("/arial_bold.ttf");
        this.contentStream = new PDPageContentStream(doc, page);
        PDFont font = PDType0Font.load(doc, fontStream);
        PDFont boldFont = PDType0Font.load(doc, fontBoldStream);

        writeBoldText(font, boldFont, HEADER_FONT_SIZE, MARGIN, contentStream, "Online-Kurzantrag auf Beihilfe");
        writeAdaptingText(font, TEXT_FONT_SIZE, MARGIN, contentStream, "Der Online-Kurzantrag auf Beihilfe wurde am " + onlineApplicationFormModel.getPdfCreationTime()+ " Uhr (Ortszeit Erfurt) mit der Vorgangs-ID " + ProcessIdHolder.processId.get() + " angenommen.");

        writeNewLine(contentStream);

        writeBoldText(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, "Angaben des Antragstellers");
        drawUnderlinedElement(onlineApplicationFormModel.getApplicantAndFundsModel().getLastName(), "Name", onlineApplicationFormModel.getApplicantAndFundsModel().getBirthday(), "Geburtsdatum", font, TEXT_FONT_SIZE, contentStream);
        drawUnderlinedElement(onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName(), "Vorname", onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber(), "Personalnummer", font, TEXT_FONT_SIZE, contentStream);
        drawUnderlinedElement(onlineApplicationFormModel.getApplicantAndFundsModel().getPrivatePhone(), "Telefon privat", onlineApplicationFormModel.getApplicantAndFundsModel().getPrivateEmail(), "E-Mail privat", font, TEXT_FONT_SIZE, contentStream);

        writeNewLine(contentStream);

        if (onlineApplicationFormModel.getApplicantAndFundsModel().isApplicantExpenses()) {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);

            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "es sollen Aufwendungen für den Antragsteller geltend gemacht werden.");
        } else {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, NO);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_NO_MARGIN, contentStream, "es sollen keine Aufwendungen für den Antragsteller geltend gemacht werden.");
        }

        if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForChildren()) {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);

            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "es sollen Aufwendungen für Kinder gemacht werden.");
        } else {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, NO);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_NO_MARGIN, contentStream, "es sollen keine Aufwendungen für Kinder gemacht werden.");
        }

        if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForPartner()) {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "es sollen Aufwendungen für den/die Ehegatten/in oder Lebenspartner/in (nach § 1 LPartG) geltend gemacht werden:");
            if (!Validator.isBlank(onlineApplicationFormModel.getApplicantAndFundsModel().getDifferentLastName())) {
                drawUnderlinedElement(onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerFirstName() + " " + onlineApplicationFormModel.getApplicantAndFundsModel().getDifferentLastName(), "Vor- und Nachname, ggf, abweichender Familienname", font, TEXT_FONT_SIZE, contentStream);
            } else {
                drawUnderlinedElement(onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerFirstName(), "Vor- und Nachname, ggf. abweichender Familienname", font, TEXT_FONT_SIZE, contentStream);
            }
            if (onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getCalendarYear()) {
                writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);
                writeAdaptingConditionalBoldText(font, boldFont, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "der Gesamtbetrag der Einkünfte (§ 2 Abs. 3 und 5a EStG) der/des Ehegattin/en bzw. der/des Lebenspartnerin/s unterschreitet den Betrag von 18.000 Euro **voraussichtlich im laufenden Kalenderjahr**.");
            }
            if (onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getPreCalendarYear() && (onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getConfirmation() == 1)) {
                writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);
                writeAdaptingConditionalBoldText(font, boldFont, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "der Gesamtbetrag der Einkünfte (§ 2 Abs. 3 und 5a EStG) der/des Ehegattin/en bzw. der/des Lebenspartnerin/s unterschreitet den Betrag von 18.000 Euro **im Vorvorkalenderjahr der Antragstellung**. Der **Steuerbescheid liegt vor**.");
            }
            if (onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getPreCalendarYear() && (onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getConfirmation() == 2)) {
                writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);
                writeAdaptingConditionalBoldText(font, boldFont, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "der Gesamtbetrag der Einkünfte (§ 2 Abs. 3 und 5a EStG) der/des Ehegattin/ en bzw. der/des Lebenspartnerin/s unterschreitet den Betrag von 18.000 Euro **im Vorvorkalenderjahr der Antragstellung**. Der **Steuerbescheid** ist **beigefügt**.");
            }
            if ((!onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getPreCalendarYear()) && (!onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getCalendarYear())) {
                writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, NO);
                writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_NO_MARGIN, contentStream, "der Gesamtbetrag der Einkünfte (§ 2 Abs. 3 und 5a EStG) der/des Ehegattin/en bzw. der/des Lebenspartnerin/s unterschreitet nicht den Betragvon 18.000 Euro.");
            }
        } else {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, NO);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_NO_MARGIN, contentStream, "es sollen keine Aufwendungen für den/die Ehegatten/in oder Lebenspartner/in (nach § 1 LPartG) geltend gemacht werden.");
        }

        writeNewLine(contentStream);

        writeBoldText(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, "Weitere Angaben des Antragstellers");

        if (onlineApplicationFormModel.getServiceUsageModel().isActivityExpenses()) {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "es werden Aufwendungen für die persönliche Tätigkeit (als Arzt, Zahnarzt, Heilpraktiker, Physiotherapeut usw.) eines nahen Angehörigen (Ehegatte/in, Lebenspartner/in, Eltern, Kinder der behandelnden Person) für folgende Personen geltend gemacht:");

            for (String value : onlineApplicationFormModel.getServiceUsageModel().getActivityPersonModels()) {
                drawUnderlinedElement(value, "Vor- und Nachname, ggf. abweichender Familienname", font, TEXT_FONT_SIZE, contentStream);
            }
        } else {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, NO);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_NO_MARGIN, contentStream, "es werden keine Aufwendungen für die persönliche Tätigkeit (als Arzt, Zahnarzt, Heilpraktiker, Physiotherapeut usw.) eines nahen Angehörigen (Ehegatte/in, Lebenspartner/in, Eltern, Kinder der behandelnden Person) geltend gemacht.");
        }
        if (onlineApplicationFormModel.getServiceUsageModel().isIllnessExpenses()) {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, " es werden Aufwendungen für Krankheiten, für die Versicherungsleistungen ausgeschlossen oder eingestellt worden sind, für folgende Personen geltend gemacht:");
            for (String value : onlineApplicationFormModel.getServiceUsageModel().getIllnessPersonList()) {
                drawUnderlinedElement(value, "Vor- und Nachname, ggf. abweichender Familienname", font, TEXT_FONT_SIZE, contentStream);
            }
        } else {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, NO);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_NO_MARGIN, contentStream, "es werden keine Aufwendungen für Krankheiten, für die Versicherungsleistungen ausgeschlossen oder eingestellt worden sind, geltend gemacht.");
        }
        if (onlineApplicationFormModel.getServiceUsageModel().isInsuranceBenefits()) {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, YES);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_YES_MARGIN, contentStream, "es werden Leistungen einer Auslandskranken- bzw. Rücktransportversicherung für folgende Personen in Anspruch genommen:");
            for (String value : onlineApplicationFormModel.getServiceUsageModel().getInsurancePersonList()) {
                drawUnderlinedElement(value, "Vor- und Nachname, ggf. abweichender Familienname", font, TEXT_FONT_SIZE, contentStream);
            }
        } else {
            writeBoldTextWithoutMarging(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, NO);
            writeAdaptingText(font, TEXT_FONT_SIZE, AFTER_NO_MARGIN, contentStream, "es werden keine Leistungen einer Auslandskranken- bzw. Rücktransportversicherung in Anspruch genommen.");
        }

        writeNewLine(contentStream);

        writeBoldText(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, "Zusammenstellung der Antragsdaten");
        drawUnderlinedElement(onlineApplicationFormModel.getFilesCount(), "Anzahl der beigefügten Dateien", onlineApplicationFormModel.getAmount(), "Antragssumme", font, TEXT_FONT_SIZE, contentStream);

        writeNewLine(contentStream);

        writeBoldText(font, boldFont, TEXT_FONT_SIZE, MARGIN, contentStream, "Aufwendungen");

        writeExpensesTable(font, TABLE_FONT_SIZE, boldFont, onlineApplicationFormModel);

        contentStream.saveGraphicsState();
        contentStream.close();
        fontStream.close();
        return doc;
    }

    private void writeBoldText(PDFont font, PDFont boldFont, Float fontSize, Float xPosition, PDPageContentStream contentStream, String text) throws IOException {
        Float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(xPosition, yPosition);
        contentStream.setFont(boldFont, fontSize);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.setFont(font, fontSize);
        this.rowNumber++;
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private void writeBoldTextWithoutMarging(PDFont font, PDFont boldFont, Float fontSize, Float xPosition, PDPageContentStream contentStream, String text) throws IOException {
        Float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(xPosition, yPosition);
        contentStream.setFont(boldFont, fontSize);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.setFont(font, fontSize);
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private void writeText(PDFont font, Float fontSize, Float xPosition, PDPageContentStream contentStream, String text) throws IOException {
        Float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(xPosition, yPosition);
        contentStream.setFont(font, fontSize);
        contentStream.showText(text);
        contentStream.endText();
        this.rowNumber++;
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private void writeConditionalBoldText(PDFont font, PDFont boldFont, Float fontSize, Float xPosition, PDPageContentStream contentStream, String text) throws IOException {
        Float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;
        contentStream.beginText();
        contentStream.newLineAtOffset(xPosition, yPosition);

        String[] parts = StringUtil.split(text, "**");

        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 0) {
                contentStream.setFont(font, fontSize);
            } else {
                contentStream.setFont(boldFont, fontSize);
            }

            contentStream.showText(parts[i]);
        }

        contentStream.endText();

        this.rowNumber++;
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private void writeNewLine(PDPageContentStream contentStream) throws IOException {
        this.rowNumber++;
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private void drawUnderlinedElement(String leftElementValue, String leftElementName, String rightElementValue, String rightElementName, PDFont font, Float fontSize, PDPageContentStream contentStream) throws IOException {
        final float tableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
        final float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;
        contentStream.setFont(font, fontSize);
        contentStream.drawLine(MARGIN, yPosition - 2, tableWidth / 2, yPosition - 2);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(Objects.toString(leftElementValue, ""));
        contentStream.endText();
        contentStream.setFont(font, 2 + fontSize / 2);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition - ROW_HEIGHT + 5);
        contentStream.showText(leftElementName);
        contentStream.endText();
        contentStream.drawLine(2 * MARGIN + tableWidth / 2, yPosition - 2, MARGIN + tableWidth, yPosition - 2);
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(2 * MARGIN + tableWidth / 2, yPosition);
        contentStream.showText(Objects.toString(rightElementValue, ""));
        contentStream.endText();
        contentStream.setFont(font, 2 + fontSize / 2);
        contentStream.beginText();
        contentStream.newLineAtOffset(2 * MARGIN + tableWidth / 2, yPosition - ROW_HEIGHT + 5);
        contentStream.showText(rightElementName);
        contentStream.endText();
        contentStream.setFont(font, fontSize);
        this.rowNumber += 2;
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private void drawUnderlinedElement(String leftElementValue, String leftElementName, PDFont font, Float fontSize, PDPageContentStream contentStream) throws IOException {
        final float tableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
        final float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;
        contentStream.setFont(font, fontSize);
        contentStream.drawLine(MARGIN, yPosition - 2, tableWidth / 2, yPosition - 2);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(Objects.toString(leftElementValue, ""));
        contentStream.endText();
        contentStream.setFont(font, 2 + fontSize / 2);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition - ROW_HEIGHT + 5);
        contentStream.showText(leftElementName);
        contentStream.endText();
        contentStream.setFont(font, fontSize);
        this.rowNumber += 2;
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private void writeExpensesTable(PDFont font, float fontSize, PDFont boldFont, OnlineApplicationFormModel onlineApplicationFormModel) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();

        float tableWidth = mediaBox.getWidth();
        float pageHeight = mediaBox.getHeight() - (2 * MARGIN);

        final float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;

        BaseTable baseTable = new BaseTable(
            yPosition,
            pageHeight,
            MARGIN,
            tableWidth,
            TABLE_MARGIN,
            doc,
            page,
            false,
            true
        );

        Row<PDPage> headerRow = addTableRow(baseTable, resultTableHeaders, font, fontSize);
        baseTable.addHeaderRow(headerRow);

        int index = 1;

        if (onlineApplicationFormModel.getApplicantAndFundsModel().isApplicantExpenses()) {
            addTableSpanningRown(baseTable, "Antragsteller", font, fontSize);

            if (onlineApplicationFormModel.getApplicantExpenses().size() > 0) {
                for (ExpensesModel expensesModel : onlineApplicationFormModel.getApplicantExpenses()) {
                    addExpensesModel(expensesModel, baseTable, index++, font, fontSize);
                }
            }
        }

        if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForPartner()) {
            addTableSpanningRown(baseTable, "Ehegatte", font, fontSize);

            if (onlineApplicationFormModel.getExpensesForPartner().size() > 0) {
                for (ExpensesModel expensesModel : onlineApplicationFormModel.getExpensesForPartner()) {
                    addExpensesModel(expensesModel, baseTable, index++, font, fontSize);
                }
            }
        }
        if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForChildren()) {
            addTableSpanningRown(baseTable, "Kinder", font, fontSize);

            for (ExpensesModel expensesModel : onlineApplicationFormModel.getExpensesForChildren()) {
                addExpensesModel(expensesModel, baseTable, index++, font, fontSize);
            }
        }

        addSummaryRow(baseTable, onlineApplicationFormModel.getAmount(), font, fontSize, boldFont);

        baseTable.draw();
    }

    private void addSummaryRow(BaseTable table, String amount, PDFont font, float fontSize, PDFont boldFont) {
        Row<PDPage> row = table.createRow(ROW_HEIGHT);

        Cell<PDPage> titleCell = row.createCell(columnSizes.get(0) + columnSizes.get(1) + columnSizes.get(2), "Summe der Rechnungsbeträge:");
        titleCell.setFont(boldFont);
        titleCell.setFontSize(fontSize);


        Cell<PDPage> summaryCell = row.createCell(columnSizes.get(3), amount);
        summaryCell.setFont(font);
        summaryCell.setFontSize(fontSize);
    }

    private Row<PDPage> addTableRow(BaseTable table, List<String> values, PDFont font, float fontSize) {
        Row<PDPage> row = table.createRow(ROW_HEIGHT);

        for (int i = 0; i < values.size(); i++) {
            float columnSize = columnSizes.get(i);

            Cell<PDPage> cell = row.createCell(columnSize, values.get(i));

            cell.setFont(font);
            cell.setFontSize(fontSize);
        }

        return row;
    }

    private Row<PDPage> addTableSpanningRown(BaseTable table, String value, PDFont font, float fontSize) {
        Row<PDPage> row = table.createRow(ROW_HEIGHT);

        Cell<PDPage> cell = row.createCell(100f, value);

        cell.setFont(font);
        cell.setFontSize(fontSize);

        return row;
    }

    private void addExpensesModel(ExpensesModel expensesModel, BaseTable baseTable, int index, PDFont font, float fontSize) {
        List<String> expenses = new LinkedList<>();
        expenses.add(String.valueOf(index));
        expenses.add(expensesModel.getInvoiceDate());
        expenses.add(serviceType.get(expensesModel.getServiceType()));
        expenses.add(Objects.toString(expensesModel.getInvoiceAmount(), "0,00") + " €");
        expenses.add(Validator.isNull(expensesModel.getReimbursement()) ? "" : (Objects.toString(expensesModel.getReimbursement(), "0,00") + " €"));
        expenses.add(expensesModel.getFiles().get(0).getOriginalFilename());
        expenses.add(documentType.get(expensesModel.getFiles().get(0).getType()));

        addTableRow(baseTable, expenses, font, fontSize);

        if (expensesModel.getFiles().size() > 1) {
            for (int i = 1; i < expensesModel.getFiles().size(); i++) {
                List<String> additionalFiles = new LinkedList<>();
                additionalFiles.add("");
                additionalFiles.add("");
                additionalFiles.add("");
                additionalFiles.add("");
                additionalFiles.add("");
                additionalFiles.add(expensesModel.getFiles().get(i).getOriginalFilename());
                additionalFiles.add(documentType.get(expensesModel.getFiles().get(i).getType()));

                addTableRow(baseTable, additionalFiles, font, fontSize);
            }
        }
    }

    private void drawResultTable(List<String> tableData, List<Float> columnSize, StringBuilder totalString, PDFont font, Float fontSize, Float tableFontSize, PDPageContentStream contentStream) throws IOException {
        final float tableWidth = page.getMediaBox().getWidth();
        final float yPosition = A_4_HEIGHT - rowNumber * ROW_HEIGHT;
        contentStream.setFont(font, fontSize);
        int columnNumber = 0;
        Float xPosition = MARGIN;
        for (String value : tableData) {
            contentStream.beginText();
            contentStream.newLineAtOffset(xPosition, yPosition);
            contentStream.showText(value);
            contentStream.endText();

            xPosition += columnSize.get(columnNumber) / stringSize(totalString.toString(), font, tableFontSize) * tableWidth;
            columnNumber++;
        }
        this.rowNumber++;
        if ((A_4_HEIGHT - rowNumber * ROW_HEIGHT) < MARGIN) {
            this.rowNumber = 0;
            this.page = new PDPage(PDRectangle.A4);
            contentStream.saveGraphicsState();
            contentStream.close();
            this.doc.addPage(page);
            this.contentStream = new PDPageContentStream(doc, page);
        }
    }

    private Float stringSize(String value, PDFont font, Float fontSize) throws IOException {
        return fontSize * font.getStringWidth(value);
    }

    private void writeAdaptingText(PDFont font, Float fontSize, Float xPosition, PDPageContentStream contentStream, String text) throws IOException {
        StringBuilder sb = new StringBuilder();
        LinkedList<String> separatedText = new LinkedList(Arrays.asList(text.split(" ")));
        for (String separatedString : separatedText) {
            sb.append(separatedString + " ");
            if (separatedText.indexOf(separatedString) < separatedText.size() - 1) {
                if (A_4_WIDTH - xPosition - MARGIN - (stringSize(sb.toString(), font, fontSize) / 1000) < (stringSize(separatedText.get(separatedText.indexOf(separatedString) + 1), font, fontSize)) / 1000) {
                    writeText(font, fontSize, xPosition, contentStream, sb.toString());
                    xPosition = MARGIN;
                    sb = new StringBuilder();
                }
                separatedText.set(separatedText.indexOf(separatedString), "");
            }
        }
        writeText(font, fontSize, xPosition, contentStream, sb.toString());
    }
    private void writeAdaptingConditionalBoldText(PDFont font, PDFont boldFont, Float fontSize, Float xPosition, PDPageContentStream contentStream, String text) throws IOException {
        StringBuilder sb = new StringBuilder();
        LinkedList<String> separatedText = new LinkedList(Arrays.asList(text.split(" ")));
        for (String separatedString : separatedText) {
            sb.append(separatedString + " ");
            if (separatedText.indexOf(separatedString) < separatedText.size() - 1) {
                if (A_4_WIDTH - xPosition - MARGIN - (stringSize(sb.toString(), font, fontSize) / 1000) < (stringSize(separatedText.get(separatedText.indexOf(separatedString) + 1), font, fontSize)) / 1000) {
                    writeConditionalBoldText(font, boldFont, fontSize, xPosition, contentStream, sb.toString());
                    xPosition = MARGIN;
                    sb = new StringBuilder();
                }
                separatedText.set(separatedText.indexOf(separatedString), "");
            }
        }
        writeConditionalBoldText(font, boldFont, fontSize, xPosition, contentStream, sb.toString());
    }

    private void setTableData(List<ExpensesModel> model, Integer count, List<Float> columnSize, StringBuilder totalString, PDFont font, Float fontSize, Float tableFontSize, PDPageContentStream contentStream) throws IOException {
        for (ExpensesModel value : model) {
            {
                List<String> applicantExpenses = new LinkedList<>();
                applicantExpenses.add(count.toString());
                applicantExpenses.add(value.getInvoiceDate());
                applicantExpenses.add(serviceType.get(value.getServiceType()));
                applicantExpenses.add(Objects.toString(value.getInvoiceAmount(), "0,00") + " €");
                applicantExpenses.add(Validator.isNull(value.getReimbursement()) ? "" : (Objects.toString(value.getReimbursement(), "0,00") + " €"));
                applicantExpenses.add(value.getFiles().get(0).getOriginalFilename());
                applicantExpenses.add(documentType.get(value.getFiles().get(0).getType()));
                drawResultTable(applicantExpenses, columnSize, totalString, font, fontSize, tableFontSize, contentStream);
                count++;
            }
            if (value.getFiles().size() > 1) {
                for (int i = 1; i < value.getFiles().size(); i++) {
                    List<String> applicantExpenses = new LinkedList<>();
                    applicantExpenses.add("");
                    applicantExpenses.add("");
                    applicantExpenses.add("");
                    applicantExpenses.add("");
                    applicantExpenses.add("");
                    applicantExpenses.add(value.getFiles().get(i).getOriginalFilename());
                    applicantExpenses.add(documentType.get(value.getFiles().get(i).getType()));
                    drawResultTable(applicantExpenses, columnSize, totalString, font, fontSize, tableFontSize, contentStream);
                }
            }
        }
    }
}

