
package de.xdot.tlrz.online.application.command;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.FileItem;
import com.liferay.portal.kernel.upload.UploadException;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import de.xdot.configuration.TlrzOnlineApplicationConfiguration;
import de.xdot.pdf.creation.constants.PDFCreationConstants;
import de.xdot.pdf.creation.model.OnlineApplicationFormModel;
import de.xdot.pdf.creation.model.PDFGenerationResult;
import de.xdot.pdf.creation.model.sub.ApplicantAndFundsModel;
import de.xdot.pdf.creation.model.sub.ApplicantFileModel;
import de.xdot.pdf.creation.model.sub.ExpensesModel;
import de.xdot.pdf.creation.model.sub.PartnerIncomeConfirmationModel;
import de.xdot.pdf.creation.model.sub.ServiceUsageModel;
import de.xdot.pdf.creation.service.PDFGenerator;
import de.xdot.tlrz.online.application.constants.OnlineAppPortletKeys;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static de.xdot.tlrz.online.application.constants.OnlineAppPortletKeys.RECHNUNG;
import static de.xdot.tlrz.online.application.constants.OnlineAppPortletKeys.REZEPT;

@Component(immediate = true,
    property = {"javax.portlet.name=" + OnlineAppPortletKeys.ONLINEAPP, "mvc.command.name=" + OnlineAppPortletKeys.SUBMIT_FORM},
    service = MVCResourceCommand.class)
public class ProcessOnlineFormMVCResourceCommand extends BaseMVCResourceCommand {

    private static final Log log = LogFactoryUtil.getLog(ProcessOnlineFormMVCResourceCommand.class);
    private final Map<String, String> documentType = new HashMap<String, String>() {{
        put("1", RECHNUNG);
        put("2", REZEPT);
    }};

    private static final int MAX_BELEG = 20;
    private static final int MAX_FILES = 50;

    @Reference
    private Portal portal;
    @Reference
    private MailService mailService;
    @Reference
    private PDFGenerator pdfGenerator;


    @Reference
    private TlrzOnlineApplicationConfiguration pdfConfiguration;

    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortalException, IOException, AddressException, InterruptedException {
        InternetAddress from = new InternetAddress(PrefsPropsUtil.getString(PortalUtil.getDefaultCompanyId(), PropsKeys.ADMIN_EMAIL_FROM_ADDRESS));

        File pdfFile = null;
        InputStream in = null;

        ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);

        Calendar calendar = Calendar.getInstance(themeDisplay.getTimeZone(), themeDisplay.getLocale());
        String pdfCreationTime = FastDateFormatFactoryUtil.getSimpleDateFormat("dd.MM.yyyy', um 'HH:mm", themeDisplay.getLocale(), themeDisplay.getTimeZone()).format(calendar.getTime());
        String pdfCreationRowTime = FastDateFormatFactoryUtil.getSimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", themeDisplay.getLocale(), themeDisplay.getTimeZone()).format(calendar.getTime());

        try {
            portal.getUploadPortletRequest(resourceRequest);

            UploadException uploadException = (UploadException)resourceRequest.getAttribute(WebKeys.UPLOAD_EXCEPTION);

            if (Validator.isNotNull(uploadException)) {
                mailService.sendEmail(generateErrorMailMessage(uploadException, from));

                throw uploadException;
            }

            OnlineApplicationFormModel onlineApplicationFormModel = generateOnlineApplicationFormModel(resourceRequest, from, pdfCreationTime, pdfCreationRowTime);

            PDFGenerationResult pdfGenerationResult = pdfGenerator.generatePDFAndEncryptedZip(onlineApplicationFormModel, from);

            pdfFile = pdfGenerationResult.pdfFile;

            in = new FileInputStream(pdfFile);

            StringBuilder fileName = new StringBuilder();
            if (pdfConfiguration.typeOfPDFMerge() == PDFCreationConstants.ZIP_FILE_FOR_EKABHI) {
                String allInOneFilename = onlineApplicationFormModel.getApplicantAndFundsModel().getLastName() +
                    "_" +
                    onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() +
                    "_" +
                    onlineApplicationFormModel.getApplicantAndFundsModel().getPersonalNumber() +
                    "_" +
                    onlineApplicationFormModel.getPdfCreationRowTime() +
                    "_" +
                    "gesamt.pdf";

                fileName.append(allInOneFilename);
            } else {
                fileName.append(onlineApplicationFormModel.getPdfCreationRowTime());
                fileName.append(".pdf");
                fileName.insert(0, onlineApplicationFormModel.getApplicantAndFundsModel().getFirstName() + onlineApplicationFormModel.getApplicantAndFundsModel().getLastName());
            }

            HttpServletResponse httpRes = PortalUtil.getHttpServletResponse(resourceResponse);
            HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(resourceRequest);

            httpRes.setHeader("x-created", pdfCreationTime);
            httpRes.setHeader("x-status", "ok");

            ServletResponseUtil.sendFile(httpReq, httpRes, fileName.toString(), in, ContentTypes.APPLICATION_PDF);

            pdfGenerator.uploadToDav(pdfGenerationResult.encryptedZipFile, pdfGenerationResult.targetEncryptedZipFileName, from);
        } catch (Exception e) {
            HttpServletResponse httpRes = PortalUtil.getHttpServletResponse(resourceResponse);
            httpRes.setHeader("x-created", pdfCreationTime);
            httpRes.setHeader("x-status", "error");

            log.error("Error occurred during processing form", e);
            throw new PortalException(e);
        } finally {
            if (pdfFile != null) {
                deleteWithOverwrite(pdfFile);
            }
            UploadPortletRequest uploadPortletRequest = portal.getUploadPortletRequest(resourceRequest);
            Map<String, FileItem[]> multipartParameterMap = uploadPortletRequest.getMultipartParameterMap();
            for (Map.Entry<String, FileItem[]> entry : multipartParameterMap.entrySet()) {
                File file = uploadPortletRequest.getFile(entry.getKey());
                if (Validator.isNotNull(file)) {
                    if (file.exists()) {
                        deleteWithOverwrite(file);
                    }
                }
            }

            if (in != null) {
                in.close();
            }
        }
    }

    private OnlineApplicationFormModel generateOnlineApplicationFormModel(ResourceRequest resourceRequest, InternetAddress from, String pdfCreationTime, String pdfCreationRowTime) throws AddressException, IOException, InterruptedException, PortalException {
        if (log.isDebugEnabled()) {
            log.debug("Starting to generate Application Form Model from request data");
        }

        UploadPortletRequest uploadPortletRequest = portal.getUploadPortletRequest(resourceRequest);

        OnlineApplicationFormModel onlineApplicationFormModel = new OnlineApplicationFormModel();
        try {
            onlineApplicationFormModel.setApplicantAndFundsModel(getApplicantAndFundsModel(uploadPortletRequest));
            onlineApplicationFormModel.setServiceUsageModel(getServiceUsageModel(uploadPortletRequest));
            onlineApplicationFormModel.setApplicantExpenses(getApplicantExpenses(uploadPortletRequest));
            onlineApplicationFormModel.setExpensesForPartner(getExpensesForPartner(uploadPortletRequest));
            onlineApplicationFormModel.setExpensesForChildren(getExpensesForChildren(uploadPortletRequest));

            String amount = uploadPortletRequest.getParameter("amount");
            if (log.isDebugEnabled()) {
                log.debug("Application Form Model.Amount = " + amount);
            }
            onlineApplicationFormModel.setAmount(amount);

            String filesCount = uploadPortletRequest.getParameter("filesCount");
            if (log.isDebugEnabled()) {
                log.debug("Application Form Model.FilesCount = " + filesCount);
            }
            onlineApplicationFormModel.setFilesCount(filesCount);

            onlineApplicationFormModel.setPdfCreationTime(pdfCreationTime);
            onlineApplicationFormModel.setPdfCreationRowTime(pdfCreationRowTime);
        } catch (NullPointerException | PortalException e) {
            mailService.sendEmail(generateErrorMailMessage(e, from));

            throw new PortalException(e.getMessage(), e);
        }
        return onlineApplicationFormModel;
    }
    private MailMessage generateErrorMailMessage(Throwable e, InternetAddress from) throws AddressException {

        MailMessage mailMessage = new MailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(new InternetAddress(pdfConfiguration.eMailRecipient()));
        mailMessage.setHTMLFormat(true);
        mailMessage.setSubject(pdfConfiguration.errorEMailSubject());
        mailMessage.setBody(pdfConfiguration.errorEMailText() + "Error occurring during extraction data from submitted form");
        return mailMessage;
    }

    private ApplicantAndFundsModel getApplicantAndFundsModel(UploadPortletRequest uploadPortletRequest) throws PortalException {
        ApplicantAndFundsModel applicantAndFundsModel = new ApplicantAndFundsModel();
        User user = portal.getUser(uploadPortletRequest);

        applicantAndFundsModel.setFirstName(user.getFirstName());
        applicantAndFundsModel.setLastName(user.getLastName());
        long birthday = user.getBirthday().getTime();

        TimeZone timeZone = TimeZone.getDefault();

        long utcBirthday = birthday + timeZone.getOffset(Calendar.DST_OFFSET);

        if (utcBirthday != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(user.getBirthday());

            String day = String.format("%02d", calendar.get(Calendar.DATE));
            String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
            String year = String.format("%04d", calendar.get(Calendar.YEAR));

            applicantAndFundsModel.setBirthday(day + "." + month + "." + year);
        } else {
            applicantAndFundsModel.setBirthday(uploadPortletRequest.getParameter("applicantAndFunds-birthday-day") + "." + uploadPortletRequest.getParameter("applicantAndFunds-birthday-month") + "." + uploadPortletRequest.getParameter("applicantAndFunds-birthday-year"));
        }
        applicantAndFundsModel.setPersonalNumber(uploadPortletRequest.getParameter("applicantAndFunds-personalNumber"));
        applicantAndFundsModel.setPrivatePhone(uploadPortletRequest.getParameter("applicantAndFunds-privatePhone"));
        applicantAndFundsModel.setPrivateEmail(uploadPortletRequest.getParameter("applicantAndFunds-privateEmail"));
        applicantAndFundsModel.setApplicantExpenses(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-applicantExpenses")));
        applicantAndFundsModel.setExpensesForChildren(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-expensesForChildren")));
        applicantAndFundsModel.setExpensesForPartner(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-expensesForPartner")));
        applicantAndFundsModel.setPartnerFirstName(uploadPortletRequest.getParameter("applicantAndFunds-partnerFirstName"));
        applicantAndFundsModel.setDifferentLastName(uploadPortletRequest.getParameter("applicantAndFunds-differentLastName"));
        applicantAndFundsModel.setPartnerTotalIncome(Integer.valueOf(Objects.toString(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome"), "0")));

        PartnerIncomeConfirmationModel partnerIncomeConfirmationModel = new PartnerIncomeConfirmationModel();
        partnerIncomeConfirmationModel.setConfirmation(Integer.valueOf(Objects.toString(uploadPortletRequest.getParameter("applicantAndFunds-partnerIncomeConfirmation-confirmation"), "0")));

        File taxAssessmentFile = uploadPortletRequest.getFile("applicantAndFunds-partnerIncomeConfirmation-taxAssessmentFile");
        String taxAssessmentFileName = uploadPortletRequest.getFileName("applicantAndFunds-partnerIncomeConfirmation-taxAssessmentFile");

        if (log.isDebugEnabled()) {
            log.debug("Uploaded tax assessment file = " + taxAssessmentFile);
        }
        partnerIncomeConfirmationModel.setTaxAssessmentFile(taxAssessmentFile);
        partnerIncomeConfirmationModel.setTaxAssessmentFileName(taxAssessmentFileName);

        partnerIncomeConfirmationModel.setCalendarYear(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome-currentCalendarYear")));
        partnerIncomeConfirmationModel.setPreCalendarYear(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome-preCurrentCalendarYear")));
        applicantAndFundsModel.setPartnerIncomeConfirmation(partnerIncomeConfirmationModel);

        if (log.isDebugEnabled()) {
            log.debug("Generated Applicant and Funds Model = " + applicantAndFundsModel);
        }

        return applicantAndFundsModel;
    }

    private ServiceUsageModel getServiceUsageModel(UploadPortletRequest uploadPortletRequest) {

        ServiceUsageModel serviceUsageModel = new ServiceUsageModel();

        serviceUsageModel.setActivityExpenses(Boolean.parseBoolean(uploadPortletRequest.getParameter("servicesUsage-activityExpenses")));
        serviceUsageModel.setActivityPersonModels(getServiceUsageExpencesForEntity("servicesUsage-activityPersonList", uploadPortletRequest));

        serviceUsageModel.setIllnessExpenses(Boolean.parseBoolean(uploadPortletRequest.getParameter("servicesUsage-illnessExpenses")));
        serviceUsageModel.setIllnessPersonList(getServiceUsageExpencesForEntity("servicesUsage-illnessPersonList", uploadPortletRequest));

        serviceUsageModel.setInsuranceBenefits(Boolean.parseBoolean(uploadPortletRequest.getParameter("servicesUsage-insuranceBenefits")));
        serviceUsageModel.setInsurancePersonList(getServiceUsageExpencesForEntity("servicesUsage-insurancePersonList", uploadPortletRequest));

        if (log.isDebugEnabled()) {
            log.debug("Generated Service Usage model = " + serviceUsageModel);
        }

        return serviceUsageModel;
    }

    private List<ExpensesModel> getApplicantExpenses(UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException {
        return getExpensesModelForEntity("applicantExpenses", "applicantExpensesArray", uploadPortletRequest);
    }

    private List<ExpensesModel> getExpensesForPartner(UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException {
        return getExpensesModelForEntity("expensesForPartner", "expensesForPartnerArray", uploadPortletRequest);

    }

    private List<ExpensesModel> getExpensesForChildren(UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException {
        return getExpensesModelForEntity("expensesForChildren", "expensesForChildrenArray", uploadPortletRequest);
    }

    private List<ExpensesModel> getExpensesModelForEntity(String entityExpenses, String entityExpensesArray, UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException {
        List<ExpensesModel> expensesModels = new ArrayList<>();

        List<String> paramKeys = new ArrayList<>(uploadPortletRequest.getParameterMap().keySet());
        if (log.isDebugEnabled()) {
            log.debug("Request keys = " + paramKeys);
        }
        Collections.sort(paramKeys);
        if (log.isDebugEnabled()) {
            log.debug("Sorted Request keys = " + paramKeys);
        }

        List<String> entityExpensesParamKeys = paramKeys
            .stream()
            .filter(s -> s.startsWith(entityExpenses + "-" + entityExpensesArray))
            .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("Filtered entity Param keys = " + entityExpensesParamKeys);
        }

        for (int paramFirstindex = 1; paramFirstindex <= MAX_BELEG; paramFirstindex++) {
            String paramPrefix = entityExpenses + "-" + entityExpensesArray + "-" + paramFirstindex + "-";
            ExpensesModel expensesModel = createExpensesModel(uploadPortletRequest, entityExpensesParamKeys, paramPrefix);

            if (expensesModel != null) {
                expensesModels.add(expensesModel);
            }
        }

        return expensesModels;
    }

    private ExpensesModel createExpensesModel(UploadPortletRequest uploadPortletRequest, List<String> entityExpensesParamKeys, String paramPrefix) throws IOException {
        ExpensesModel expensesModel = null;

        String paramInvoiceDateDay = paramPrefix + "invoiceDate-day";
        String paramInvoiceDateMonth = paramPrefix + "invoiceDate-month";
        String paramInvoiceDateYear = paramPrefix + "invoiceDate-year";
        String paramServiceType = paramPrefix + "serviceType";
        String paramInvoiceAmount = paramPrefix + "invoiceAmount";
        String paramReimbursment = paramPrefix + "reimbursement";
        if (entityExpensesParamKeys.contains(paramInvoiceDateDay)) {
            expensesModel = new ExpensesModel();

            expensesModel.setInvoiceDate(uploadPortletRequest.getParameter(paramInvoiceDateDay) + "." + uploadPortletRequest.getParameter(paramInvoiceDateMonth) + "." + uploadPortletRequest.getParameter(paramInvoiceDateYear));
            expensesModel.setServiceType(uploadPortletRequest.getParameter(paramServiceType));
            expensesModel.setInvoiceAmount(Objects.toString(formatCurrency(uploadPortletRequest.getParameter(paramInvoiceAmount)), ""));
            expensesModel.setReimbursement(Objects.toString(formatCurrency(uploadPortletRequest.getParameter(paramReimbursment)), ""));

            List<ApplicantFileModel> applicantFileModels = createApplicantFileModels(uploadPortletRequest, entityExpensesParamKeys, paramPrefix);

            expensesModel.setFiles(applicantFileModels);
        }

        return expensesModel;
    }

    private List<ApplicantFileModel> createApplicantFileModels(UploadPortletRequest uploadPortletRequest, List<String> entityExpensesParamKeys, String paramPrefix) throws IOException {
        List<ApplicantFileModel> applicantFileModels = new ArrayList<>();

        for (int i = 1; i <= MAX_FILES; i++) {
            String innerParamPrefix = paramPrefix + "files-" + i + "-";

            String paramFile = innerParamPrefix + "file";
            String paramFileDataURI = innerParamPrefix + "file-dataURI";
            if ( (entityExpensesParamKeys.contains(paramFile)) || (entityExpensesParamKeys.contains(paramFileDataURI)) ) {
                ApplicantFileModel applicantFileModel = createApplicantFileModel(uploadPortletRequest, innerParamPrefix);

                applicantFileModels.add(applicantFileModel);
            }

        }

        return applicantFileModels;
    }

    private ApplicantFileModel createApplicantFileModel(UploadPortletRequest uploadPortletRequest, String innerParamPrefix) throws IOException {
        String paramType = innerParamPrefix + "type";

        ApplicantFileModel applicantFileModel = new ApplicantFileModel();

        File uploadedFile = getUploadedFile(uploadPortletRequest, innerParamPrefix);

        String uploadedFileName = getUploadedFileName(uploadPortletRequest, innerParamPrefix);

        StringBuilder prefix = new StringBuilder(uploadedFileName);

        String originalFilename = prefix.toString();

        prefix.insert((prefix.lastIndexOf(".")), documentType.get(uploadPortletRequest.getParameter(paramType)));

        String filename = prefix.toString();

        if (log.isDebugEnabled()) {
            log.debug("Found uploaded file for Expenses: [uploadedFileName = " + uploadedFileName + ", uploadedFile = " + uploadedFile + ", originalFilename = " + originalFilename + "]");
        }

        applicantFileModel.setFile(uploadedFile);
        applicantFileModel.setFilename(filename);
        applicantFileModel.setOriginalFilename(originalFilename);
        applicantFileModel.setType(uploadPortletRequest.getParameter(paramType));

        return applicantFileModel;
    }

    private String getUploadedFileName(UploadPortletRequest uploadPortletRequest, String innerParamPrefix) {
        String paramFile = innerParamPrefix + "file";
        String paramFileName = innerParamPrefix + "file-name";

        String uploadedFileName = uploadPortletRequest.getFileName(paramFile);
        if (Validator.isNull(uploadedFileName)) {
            uploadedFileName = uploadPortletRequest.getParameter(paramFileName);
        }

        return uploadedFileName;
    }

    private File getUploadedFile(UploadPortletRequest uploadPortletRequest, String innerParamPrefix) throws IOException {
        String paramFile = innerParamPrefix + "file";
        String paramFileDataURI = innerParamPrefix + "file-dataURI";

        File file = uploadPortletRequest.getFile(paramFile);
        if (log.isDebugEnabled()) {
            log.debug("Uploaded binary file for key " + paramFile + " = " + file);
        }

        if (Validator.isNull(file)) {
            String dataURI = uploadPortletRequest.getParameter(paramFileDataURI);

            if (log.isDebugEnabled()) {
                log.debug("Uploaded data URI file for key " + paramFileDataURI + " = " + (dataURI == null ? "null" : "[length = " + dataURI.length() + "]"));
            }

            if (Validator.isNotNull(dataURI)) {
                String mimeType = dataURI.substring(dataURI.indexOf(":") + 1, dataURI.indexOf(";"));
                Set<String> extensions = MimeTypesUtil.getExtensions(mimeType);

                String extension = "";
                if (! extensions.isEmpty()) {
                    extension = extensions.iterator().next();
                    if (extension.startsWith(".")) {
                        extension = extension.substring(1);
                    }
                }

                String base64EncodedData = dataURI.substring(dataURI.indexOf(',') + 1);
                byte[] bytes = Base64.getDecoder().decode(base64EncodedData);

                File tempFile = FileUtil.createTempFile(extension);
                FileUtil.write(tempFile, bytes);

                return tempFile;
            }
        }

        return file;
    }

    private List<String> getServiceUsageExpencesForEntity(String entity, UploadPortletRequest uploadPortletRequest) {
        Map<String, String[]> parameterMap = uploadPortletRequest.getParameterMap();

        List<String> person = new ArrayList<>();
        for (String paramKey : parameterMap.keySet()) {
            if (paramKey.contains(entity)) {
                person.add(uploadPortletRequest.getParameter(paramKey));
            }
        }
        return person;
    }

    private void renameFile(File uploadedFile, File tmpFile) throws InterruptedException {

        Thread mainThread = Thread.currentThread();
        final Boolean[] threadFlag = {false};
        synchronized (mainThread) {
            new Thread() {
                @Override
                public synchronized void start() {
                    try {
                        Files.move(Paths.get(uploadedFile.getPath()), tmpFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                        while (true) {
                            if (0L == uploadedFile.getUsableSpace()) {
                                mainThread.notify();
                                threadFlag[0] = true;

                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("error during renaming tmp files", e);
                    }
                }
            }.start();
            if (!threadFlag[0]) {
                mainThread.wait();
            }

        }
    }

    private void deleteWithOverwrite(File fileToDelete) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting file with overwrite: " + fileToDelete.getAbsolutePath());
        }

        for (int i = 0; i < 3; i++) {

            fileToDelete.delete();
            fileToDelete.createNewFile();
            fileToDelete.delete();
        }
    }

    private String formatCurrency(String value) {
        if (!Validator.isBlank(value)) {
            StringBuilder currency = new StringBuilder(value);
            currency.replace(value.lastIndexOf("."), value.lastIndexOf("."), ",").deleteCharAt(currency.lastIndexOf("."));
            return currency.toString();
        } else return null;
    }

}
