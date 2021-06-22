package de.xdot.pdf.creation.service.impl;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.Validator;
import de.xdot.configuration.TlrzOnlineApplicationConfiguration;
import de.xdot.encryptor.service.PGEncryptor;
import de.xdot.onlineapplication.webdav.service.WebDavService;
import de.xdot.pdf.creation.ImageDrawer;
import de.xdot.pdf.creation.PDFAConfigs;
import de.xdot.pdf.creation.TextWriter;
import de.xdot.pdf.creation.constants.PDFCreationConstants;
import de.xdot.pdf.creation.model.OnlineApplicationFormModel;
import de.xdot.pdf.creation.model.PDFGenerationResult;
import de.xdot.pdf.creation.model.sub.ApplicantFileModel;
import de.xdot.pdf.creation.model.sub.ExpensesModel;
import de.xdot.pdf.creation.model.sub.TaxFileModel;
import de.xdot.pdf.creation.service.PDFGenerator;
import de.xdot.pdf.creation.status.ProcessIdHolder;
import de.xdot.pdf.creation.status.ProcessStepHolder;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static de.xdot.pdf.creation.constants.PDFCreationConstants.RECHNUNG;
import static de.xdot.pdf.creation.constants.PDFCreationConstants.REZEPT;

@SuppressWarnings("PackageAccessibility")
@Component(
    immediate = true,
    service = PDFGenerator.class
)
public class PDFGeneratorImpl implements PDFGenerator {

    private static final Log log = LogFactoryUtil.getLog(PDFGeneratorImpl.class);

    private final Map<String, String> documentType = new HashMap<String, String>() {{
        put("1", RECHNUNG);
        put("2", REZEPT);
    }};

    private final Map<String, String> documentFolderNames = new HashMap<String, String>() {{
        put("1", "Rechnungen");
        put("2", "Rezepte");
    }};

    @Reference
    private WebDavService webDavService;
    @Reference
    private MailService mailService;
    @Reference
    private PGEncryptor pgEncryptor;
    @Reference
    private TlrzOnlineApplicationConfiguration pdfConfiguration;

    private void generateZIP(List<AbstractMap.SimpleImmutableEntry<String, String>> srcFiles, FileOutputStream fos) throws IOException {

        ZipOutputStream zipOut = new ZipOutputStream(fos);

        Set<String> filenames = new HashSet<>();

        for (AbstractMap.SimpleImmutableEntry<String, String> srcFile : srcFiles) {
            if (log.isDebugEnabled()) {
                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Adding " + srcFile.getKey() + " to the ZIP file with filename " + srcFile.getValue());
            }
            File fileToZip = new File(srcFile.getKey());
            FileInputStream fis = new FileInputStream(fileToZip);

            String filename = srcFile.getValue();

            int index = 1;
            String name = FileUtil.stripExtension(srcFile.getValue());
            String extension = FileUtil.getExtension(srcFile.getValue());

            while (filenames.contains(filename)) {
                filename = name + " (" + String.valueOf(index) + ")" + "." + extension;

                index++;
            }

            filenames.add(filename);

            ZipEntry zipEntry = new ZipEntry(filename);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Writing final ZIP file");
        }

        zipOut.close();
    }

    public PDFGenerationResult generatePDFAndEncryptedZip(OnlineApplicationFormModel onlineApplicationFormModel, InternetAddress from) throws Exception {
        String typeOfPDFMerge = pdfConfiguration.typeOfPDFMerge();

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Generating PDF/ZIP with the configuration option " + typeOfPDFMerge);
        }

        ProcessStepHolder.processStep.set("Erzeugung der Quittungs-PDF-Datei");

        List<AbstractMap.SimpleImmutableEntry<String, String>> srcFiles = new ArrayList<>();
        List<String> filesTodelete = new ArrayList<>();
        StringBuilder fileName = new StringBuilder();
        StringBuilder zipFileName = new StringBuilder();
        fileName.append(onlineApplicationFormModel.getPdfCreationRowTime());
        zipFileName.append(fileName);
        fileName.append(".pdf");
        fileName.insert(0, onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() + onlineApplicationFormModel.getApplicantAndFundsModel().getLastName());
        zipFileName.append("_");
        zipFileName.append(ProcessIdHolder.processId.get());

        zipFileName.append(".zip");
        File file = FileUtil.createTempFile("pdf");
        ImageDrawer drawImage = new ImageDrawer();
        TextWriter textWriter = new TextWriter();

        List<TaxFileModel> taxAssessmentFiles = onlineApplicationFormModel.getApplicantAndFundsModel().getPartnerIncomeConfirmation().getTaxAssessmentFiles();

        Collection<PDDocument> mergedDocuments = new ArrayList<>();

        PDFGenerationResult pdfGenerationResult = new PDFGenerationResult();

        String basePath = "";
        if (typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {
            basePath = onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                "_" +
                onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                "_" +
                onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                "_" +
                onlineApplicationFormModel.getPdfCreationRowTime();
        }

        if (typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {
            File singleFormFile = FileUtil.createTempFile("pdf");

            TextWriter myTextWriter = new TextWriter();

            try (PDDocument doc = myTextWriter.writeText(10F, onlineApplicationFormModel)) {
                doc.save(singleFormFile);
            }

            String singleFormFileName = onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                "_" +
                onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                "_" +
                onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                "_" +
                onlineApplicationFormModel.getPdfCreationRowTime() +
                ".pdf";

            srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(singleFormFile.getAbsolutePath(), basePath + "/" + singleFormFileName));
        }

        try (PDDocument doc = textWriter.writeText(10F, onlineApplicationFormModel)) {

            if (typeOfPDFMerge.equals(PDFCreationConstants.MERGED_PDF) || typeOfPDFMerge.equals(PDFCreationConstants.MERGED_PDF_WITH_FILES) || typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {

                if (Validator.isNotNull(taxAssessmentFiles)) {
                    for (TaxFileModel taxAssessmentFile : taxAssessmentFiles) {
                        filesTodelete.add(taxAssessmentFile.getFile().getAbsolutePath());

                        if (taxAssessmentFile.getFile().getName().endsWith(".pdf")) {
                            PDDocument pdf = mergePDF(taxAssessmentFile.getFile(), doc, taxAssessmentFile.getFilename());

                            mergedDocuments.add(pdf);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Drawing " + taxAssessmentFile.getFile().getPath() + " from the Steuerbescheid into the final PDF");
                            }

                            drawImage.drawImage(taxAssessmentFile.getFile().getPath(), doc);
                        }
                    }

                    if (typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {
                        Collection<PDDocument> taxDocuments = new ArrayList<>();

                        PDDocument taxPdf = new PDDocument();

                        PDFAConfigs pdfaConfigs = new PDFAConfigs();
                        try {
                            pdfaConfigs.applyPDFAConfig(taxPdf);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        for (TaxFileModel taxAssessmentFile : taxAssessmentFiles) {
                            if (taxAssessmentFile.getFile().getName().endsWith(".pdf")) {
                                PDDocument pdf = mergePDF(taxAssessmentFile.getFile(), taxPdf, taxAssessmentFile.getFilename());

                                taxDocuments.add(pdf);
                            } else {
                                drawImage.drawImage(taxAssessmentFile.getFile().getPath(), taxPdf);
                            }
                        }

                        File tempFile = FileUtil.createTempFile("pdf");

                        taxPdf.save(tempFile);

                        filesTodelete.add(tempFile.getAbsolutePath());

                        //workaround for https://issues.apache.org/jira/browse/PDFBOX-3280. Close imported PDF after writing the target PDF file.
                        for (PDDocument pdf : taxDocuments) {
                            pdf.close();
                        }

                        String taxAssessmentFileName = onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                            "_" +
                            onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                            "_" +
                            onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                            "_" +
                            onlineApplicationFormModel.getPdfCreationRowTime() +
                            "_" +
                            "StB" +
                            ".pdf";
                        srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(tempFile.getPath(), basePath + "/" + taxAssessmentFileName));
                    } else {
                        for (TaxFileModel taxAssessmentFile : taxAssessmentFiles) {
                            srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(taxAssessmentFile.getFile().getPath(), taxAssessmentFile.getFilename()));
                        }
                    }
                }

                int index = 1;

                if (onlineApplicationFormModel.getApplicantAndFundsModel().isApplicantExpenses()) {
                    for (ExpensesModel value : onlineApplicationFormModel.getApplicantExpenses()) {
                        int attachmentIndex = 1;

                        for (ApplicantFileModel element : value.getFiles()) {
                            if (element.getFile().getName().endsWith(".pdf")) {
                                PDDocument pdf = mergePDF(element.getFile(), doc, element.getOriginalFilename());

                                mergedDocuments.add(pdf);
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Drawing " + element.getFile().getPath() + " from the Application Expenses into the final PDF");
                                }

                                drawImage.drawImage(element.getFile().getPath(), doc);
                            }

                            filesTodelete.add(element.getFile().getAbsolutePath());
                            if (typeOfPDFMerge.equals(PDFCreationConstants.MERGED_PDF_WITH_FILES)) {
                                srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), element.getFilename()));
                            } else if (typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {
                                String subfolder =
                                    basePath +
                                    "/" +
                                    onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                                    "_" +
                                    onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                                    "_" +
                                    onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                                    "_" +
                                    onlineApplicationFormModel.getPdfCreationRowTime() +
                                    "_" +
                                    documentFolderNames.get(element.getType());

                                String filename =
                                    String.format("%02d", index) +
                                    "_" +
                                    String.format("%02d", attachmentIndex) +
                                    "_" +
                                    FileUtil.stripExtension(element.getOriginalFilename()) +
                                    "_" +
                                    documentType.get(element.getType()) +
                                    "." +
                                    FileUtil.getExtension(element.getFile().getName());

                                srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), subfolder + "/" + filename));
                            }

                            attachmentIndex++;
                        }
                        index++;
                    }
                }
                if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForPartner()) {
                    for (ExpensesModel value : onlineApplicationFormModel.getExpensesForPartner()) {
                        int attachmentIndex = 1;

                        for (ApplicantFileModel element : value.getFiles()) {
                            if (element.getFile().getName().endsWith(".pdf")) {
                                PDDocument pdf = mergePDF(element.getFile(), doc, element.getOriginalFilename());

                                mergedDocuments.add(pdf);
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Drawing " + element.getFile().getPath() + " from the Expenses for Partner into the final PDF");
                                }

                                drawImage.drawImage(element.getFile().getPath(), doc);
                            }

                            filesTodelete.add(element.getFile().getAbsolutePath());
                            if (typeOfPDFMerge.equals(PDFCreationConstants.MERGED_PDF_WITH_FILES)) {
                                srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), element.getFilename()));
                            } else if (typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {
                                String subfolder =
                                    basePath +
                                        "/" +
                                        onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                                        "_" +
                                        onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                                        "_" +
                                        onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                                        "_" +
                                        onlineApplicationFormModel.getPdfCreationRowTime() +
                                        "_" +
                                        documentFolderNames.get(element.getType());

                                String filename =
                                    String.format("%02d", index) +
                                        "_" +
                                        String.format("%02d", attachmentIndex) +
                                        "_" +
                                        FileUtil.stripExtension(element.getOriginalFilename()) +
                                        "_" +
                                        documentType.get(element.getType()) +
                                        "." +
                                        FileUtil.getExtension(element.getFile().getName());

                                srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), subfolder + "/" + filename));
                            }

                            attachmentIndex++;
                        }
                        index++;
                    }
                }
                if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForChildren()) {
                    for (ExpensesModel value : onlineApplicationFormModel.getExpensesForChildren()) {
                        int attachmentIndex = 1;

                        for (ApplicantFileModel element : value.getFiles()) {
                            if (element.getFile().getName().endsWith(".pdf")) {
                                PDDocument pdf = mergePDF(element.getFile(), doc, element.getOriginalFilename());

                                mergedDocuments.add(pdf);
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Drawing " + element.getFile().getPath() + " from the Expenses for Children into the final PDF");
                                }

                                drawImage.drawImage(element.getFile().getPath(), doc);
                            }
                            filesTodelete.add(element.getFile().getAbsolutePath());

                            if (typeOfPDFMerge.equals(PDFCreationConstants.MERGED_PDF_WITH_FILES)) {
                                srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), element.getFilename()));
                            } else if (typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {
                                String subfolder =
                                    basePath +
                                        "/" +
                                        onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                                        "_" +
                                        onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                                        "_" +
                                        onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                                        "_" +
                                        onlineApplicationFormModel.getPdfCreationRowTime() +
                                        "_" +
                                        documentFolderNames.get(element.getType());

                                String filename =
                                    String.format("%02d", index) +
                                        "_" +
                                        String.format("%02d", attachmentIndex) +
                                        "_" +
                                        FileUtil.stripExtension(element.getOriginalFilename()) +
                                        "_" +
                                        documentType.get(element.getType()) +
                                        "." +
                                        FileUtil.getExtension(element.getFile().getName());

                                srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), subfolder + "/" + filename));
                            }

                            attachmentIndex++;
                        }
                        index++;
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Writing PDF file " + file.getAbsolutePath());
            }

            doc.save(file);
        } catch (Exception e) {
            if (file.exists()) {
                deleteWithOverwrite(file);
            }

            throw e;
        }
        //workaround for https://issues.apache.org/jira/browse/PDFBOX-3280. Close imported PDF after writing the target PDF file.
        for (PDDocument pdf : mergedDocuments) {
            pdf.close();
        }

        if (typeOfPDFMerge.equals(PDFCreationConstants.ORIGINAL_PDF_WITH_FILES)) {

            for (TaxFileModel taxAssessmentFile : taxAssessmentFiles) {
                srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(taxAssessmentFile.getFile().getPath(), taxAssessmentFile.getFilename()));
            }

            if (onlineApplicationFormModel.getApplicantAndFundsModel().isApplicantExpenses()) {
                for (ExpensesModel value : onlineApplicationFormModel.getApplicantExpenses()) {
                    for (ApplicantFileModel element : value.getFiles()) {
                        srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), element.getFilename()));
                    }
                }
            }
            if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForPartner()) {
                for (ExpensesModel value : onlineApplicationFormModel.getExpensesForPartner()) {
                    for (ApplicantFileModel element : value.getFiles()) {
                        srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), element.getFilename()));
                    }
                }
            }
            if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForChildren()) {
                for (ExpensesModel value : onlineApplicationFormModel.getExpensesForChildren()) {
                    for (ApplicantFileModel element : value.getFiles()) {
                        srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(element.getFile().getPath(), element.getFilename()));
                    }
                }
            }
        }
        if (typeOfPDFMerge.equals(PDFCreationConstants.ZIP_FILE_FOR_EKABHI)) {
            String allInOneFilename = onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                "_" +
                onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                "_" +
                onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                "_" +
                onlineApplicationFormModel.getPdfCreationRowTime() +
                "_" +
                "gesamt.pdf";

            srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(file.getAbsolutePath(), basePath + "/" + allInOneFilename));
        } else {
            srcFiles.add(new AbstractMap.SimpleImmutableEntry<>(file.getAbsolutePath(), fileName.toString()));
        }

        pdfGenerationResult.pdfFile = file;

        String targetZipFileName = zipFileName.toString().replaceAll(" ", "_");
        String targetEncryptedZipFileName = targetZipFileName + ".gpg";

        File zipFile = FileUtil.createTempFile("zip");

        File encryptedZipFile = null;

        try (FileOutputStream fos = new FileOutputStream(zipFile)) {
            if (log.isDebugEnabled()) {
                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Creating ZIP file " + zipFile.getAbsolutePath());
            }

            ProcessStepHolder.processStep.set("Erzeugung der ZIP-Datei");

            generateZIP(srcFiles, fos);

            if (log.isDebugEnabled()) {
                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Encrypting ZIP file " + zipFile.getAbsolutePath());
            }

            ProcessStepHolder.processStep.set("Verschlüsselung der ZIP-Datei mittels GPG");

            encryptedZipFile = pgEncryptor.encryptFile(zipFile);

            pdfGenerationResult.encryptedZipFile = encryptedZipFile;
            pdfGenerationResult.targetEncryptedZipFileName = targetEncryptedZipFileName;
        } catch (Exception e) {
            log.error("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Error while generating PDF or ZIP: " + e.getMessage(), e);

            deleteWithOverwrite(file);
            for (AbstractMap.SimpleImmutableEntry<String, String> element : srcFiles) {
                deleteWithOverwrite(new File(element.getKey()));
            }
            for (String element : filesTodelete) {
                if (!(element.equals(file.getAbsolutePath()))) {
                    deleteWithOverwrite(new File(element));
                }
            }
            mailService.sendEmail(generateErrorMailMessage(e, from));
        } finally {
            for (AbstractMap.SimpleImmutableEntry<String, String> element : srcFiles) {
                if (!(element.getKey().equals(file.getAbsolutePath()))) {
                    deleteWithOverwrite(new File(element.getKey()));
                }
            }
            for (String element : filesTodelete) {
                if (!(element.equals(file.getAbsolutePath()))) {
                    deleteWithOverwrite(new File(element));
                }
            }
            deleteWithOverwrite(zipFile);
        }

        return pdfGenerationResult;
    }

    @Override
    public void uploadToDav(File encryptedZipFile, String targetEncryptedZipFileName, InternetAddress from) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Uploading encrypted ZIP file " + encryptedZipFile.getAbsolutePath() + " to WebDAV");
        }
        try {
            ProcessStepHolder.processStep.set("Upload der ZIP-Datei in die DAP");

            webDavService.uploadFileToWebDav(encryptedZipFile, targetEncryptedZipFileName);
        } catch (Exception e) {
            log.error("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Error while uploading to WebDAV: " + e.getMessage(), e);

            mailService.sendEmail(generateErrorMailMessage(e, from));

            throw e;
        } finally {
            if (encryptedZipFile != null) {
                deleteWithOverwrite(encryptedZipFile);
            }
        }
    }

    private MailMessage generateErrorMailMessage(Throwable e, InternetAddress from) throws AddressException {

        MailMessage mailMessage = new MailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(new InternetAddress(pdfConfiguration.eMailRecipient()));
        mailMessage.setHTMLFormat(false);
        mailMessage.setSubject(pdfConfiguration.errorEMailSubject());
        mailMessage.setBody(pdfConfiguration.errorEMailText() + "\n\nVerarbeitungsschritt: " + ProcessStepHolder.processStep.get() + "\n\nFehlermeldung: " + e.getMessage() + "\n\nVorgangs-ID: " + ProcessIdHolder.processId.get());
        return mailMessage;
    }
    private void deleteWithOverwrite(File fileToDelete) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Deleting file with overwrite: " + fileToDelete.getAbsolutePath());
        }

        for (int i = 0; i < 3; i++) {
            fileToDelete.delete();
            fileToDelete.createNewFile();
            fileToDelete.delete();
        }
    }

    private PDDocument mergePDF(File pdfFile, PDDocument targetDocument, String originalFilename) throws IOException {
        PDDocument pdDocument = PDDocument.load(pdfFile, MemoryUsageSetting.setupTempFileOnly());

        int numberOfPages = pdDocument.getNumberOfPages();

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Adding " + numberOfPages + " pages from " + originalFilename + " to the final PDF");
        }

        for (int i = 0; i < numberOfPages; i++) {
            PDPage pdPage = pdDocument.getPage(i);

            targetDocument.importPage(pdPage);
        }

        return pdDocument;
    }

}
