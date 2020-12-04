package de.xdot.pdf.creation.service;

import de.xdot.pdf.creation.model.OnlineApplicationFormModel;
import de.xdot.pdf.creation.model.PDFGenerationResult;

import javax.mail.internet.InternetAddress;
import java.io.File;

public interface PDFGenerator {


    /**
     * This method processes a form data, generate PDF from the User input, then creates a ZIP archive with all attached files and submit it to WebDav directory.
     * Decoupling principles are broken here, because the files processing logic is a bit complex and at the moment I do not see a good option on how to diversify and split the logic into several independent services.
     *
     * @param onlineApplicationFormModel
     * @return
     * @throws Exception
     */
    PDFGenerationResult generatePDFAndEncryptedZip(OnlineApplicationFormModel onlineApplicationFormModel, InternetAddress from) throws Exception;

    void uploadToDav(File encryptedZipFile, String targetEncryptedZipFileName, InternetAddress from) throws Exception;
}
