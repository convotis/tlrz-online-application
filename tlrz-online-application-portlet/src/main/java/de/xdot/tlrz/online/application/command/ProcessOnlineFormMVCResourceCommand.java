
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
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.ParamUtil;
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
import de.xdot.pdf.creation.model.sub.TaxFileModel;
import de.xdot.pdf.creation.service.PDFGenerator;
import de.xdot.pdf.creation.status.ProcessIdHolder;
import de.xdot.pdf.creation.status.ProcessStepHolder;
import de.xdot.tlrz.online.application.constants.OnlineAppPortletKeys;
import de.xdot.tlrz.online.application.util.CustomValidator;
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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
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
    private static final int MAX_TAX_FILES = 20;

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

        String processId = "";

        try {
            ProcessStepHolder.processStep.set("Auslesen der hochgeladenen Dateien");

            UploadPortletRequest uploadPortletRequest = portal.getUploadPortletRequest(resourceRequest);

            processId = ParamUtil.getString(uploadPortletRequest, "processId");

            ProcessIdHolder.processId.set(processId);

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
            httpRes.setHeader("x-processId", processId);

            ServletResponseUtil.sendFile(httpReq, httpRes, fileName.toString(), in, ContentTypes.APPLICATION_PDF);

            pdfGenerator.uploadToDav(pdfGenerationResult.encryptedZipFile, pdfGenerationResult.targetEncryptedZipFileName, from);
        } catch (Exception e) {
            HttpServletResponse httpRes = PortalUtil.getHttpServletResponse(resourceResponse);
            httpRes.setHeader("x-created", pdfCreationTime);
            httpRes.setHeader("x-status", "error");
            httpRes.setHeader("x-processId", processId);

            log.error("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Error occurred during processing form", e);
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
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Starting to generate Application Form Model from request data");
        }

        ProcessStepHolder.processStep.set("Auslesen und Verarbeiten der eingegebenen Formulardaten");

        UploadPortletRequest uploadPortletRequest = portal.getUploadPortletRequest(resourceRequest);

        OnlineApplicationFormModel onlineApplicationFormModel = new OnlineApplicationFormModel();
        try {
            onlineApplicationFormModel.setApplicantAndFundsModel(getApplicantAndFundsModel(uploadPortletRequest));
            onlineApplicationFormModel.setServiceUsageModel(getServiceUsageModel(uploadPortletRequest));
            onlineApplicationFormModel.setApplicantExpenses(getApplicantExpenses(uploadPortletRequest));
            onlineApplicationFormModel.setExpensesForPartner(getExpensesForPartner(uploadPortletRequest));
            onlineApplicationFormModel.setExpensesForChildren(getExpensesForChildren(uploadPortletRequest));

            List<ExpensesModel> expensesModels = new ArrayList<>();
            if (onlineApplicationFormModel.getApplicantAndFundsModel().isApplicantExpenses()) {
                expensesModels.addAll(onlineApplicationFormModel.getApplicantExpenses());
            }
            if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForChildren()) {
                expensesModels.addAll(onlineApplicationFormModel.getExpensesForChildren());
            }
            if (onlineApplicationFormModel.getApplicantAndFundsModel().isExpensesForPartner()) {
                expensesModels.addAll(onlineApplicationFormModel.getExpensesForPartner());
            }

            CustomValidator.checkTotalMaxSize(expensesModels, 104857600);

            boolean termsCheckbox = GetterUtil.getBoolean(uploadPortletRequest.getParameter("termsCheckbox"));
            if (! termsCheckbox) {
                throw new PortalException("terms must be checked");
            }

            String amount = uploadPortletRequest.getParameter("amount");
            if (log.isDebugEnabled()) {
                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Application Form Model.Amount = " + amount);
            }
            onlineApplicationFormModel.setAmount(amount);

            String filesCount = uploadPortletRequest.getParameter("filesCount");
            if (log.isDebugEnabled()) {
                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Application Form Model.FilesCount = " + filesCount);
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
        mailMessage.setHTMLFormat(false);
        mailMessage.setSubject(pdfConfiguration.errorEMailSubject());
        mailMessage.setBody(pdfConfiguration.errorEMailText() + "\n\nVerarbeitungsschritt: " + ProcessStepHolder.processStep.get() + "\n\nFehlermeldung: " + e.getMessage() + "\n\nVorgangs-ID: " + ProcessIdHolder.processId.get());
        return mailMessage;
    }

    private ApplicantAndFundsModel getApplicantAndFundsModel(UploadPortletRequest uploadPortletRequest) throws PortalException, IOException {
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
            String day = uploadPortletRequest.getParameter("applicantAndFunds-birthday-day");
            String month = uploadPortletRequest.getParameter("applicantAndFunds-birthday-month");
            String year = uploadPortletRequest.getParameter("applicantAndFunds-birthday-year");

            CustomValidator.checkValidDate(day, month, year);

            applicantAndFundsModel.setBirthday(day + "." + month + "." + year);
        }

        String personalNumber = uploadPortletRequest.getParameter("applicantAndFunds-personalNumber");
        CustomValidator.checkRequired(personalNumber);
        CustomValidator.checkValidPersonalnumber(personalNumber);
        applicantAndFundsModel.setPersonalNumber(personalNumber);

        applicantAndFundsModel.setPrivatePhone(uploadPortletRequest.getParameter("applicantAndFunds-privatePhone"));

        String privateEmail = uploadPortletRequest.getParameter("applicantAndFunds-privateEmail");
        CustomValidator.checkValidEmailAddress(privateEmail);
        applicantAndFundsModel.setPrivateEmail(privateEmail);

        boolean applicantExpenses = Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-applicantExpenses"));
        boolean expensesForChildren = Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-expensesForChildren"));
        boolean expensesForPartner = Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-expensesForPartner"));
        CustomValidator.checkAtLeastOneMustBeTrue(applicantExpenses, expensesForChildren, expensesForPartner);
        applicantAndFundsModel.setExpensesForChildren(expensesForChildren);
        applicantAndFundsModel.setApplicantExpenses(applicantExpenses);
        applicantAndFundsModel.setExpensesForPartner(expensesForPartner);

        String partnerFirstName = uploadPortletRequest.getParameter("applicantAndFunds-partnerFirstName");
        if (expensesForPartner) {
            CustomValidator.checkRequired(partnerFirstName);
        }
        applicantAndFundsModel.setPartnerFirstName(partnerFirstName);

        applicantAndFundsModel.setDifferentLastName(uploadPortletRequest.getParameter("applicantAndFunds-differentLastName"));
        applicantAndFundsModel.setPartnerTotalIncome(Integer.valueOf(Objects.toString(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome"), "0")));

        PartnerIncomeConfirmationModel partnerIncomeConfirmationModel = new PartnerIncomeConfirmationModel();
        Integer confirmation = Integer.valueOf(Objects.toString(uploadPortletRequest.getParameter("applicantAndFunds-partnerIncomeConfirmation-confirmation"), "0"));
        partnerIncomeConfirmationModel.setConfirmation(confirmation);

        partnerIncomeConfirmationModel.setCalendarYear(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome-currentCalendarYear")));
        boolean preCalendarYear = Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome-preCurrentCalendarYear"));

        partnerIncomeConfirmationModel.setPreCalendarYear(preCalendarYear);

        if (expensesForPartner && confirmation == 2 && preCalendarYear) {
            List<TaxFileModel> taxFileModels = createTaxFileModels(uploadPortletRequest);

            partnerIncomeConfirmationModel.setTaxAssessmentFiles(taxFileModels);
        }

        partnerIncomeConfirmationModel.setCalendarYear(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome-currentCalendarYear")));
        partnerIncomeConfirmationModel.setPreCalendarYear(Boolean.parseBoolean(uploadPortletRequest.getParameter("applicantAndFunds-partnerTotalIncome-preCurrentCalendarYear")));
        applicantAndFundsModel.setPartnerIncomeConfirmation(partnerIncomeConfirmationModel);

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Generated Applicant and Funds Model = " + applicantAndFundsModel);
        }

        return applicantAndFundsModel;
    }

    private ServiceUsageModel getServiceUsageModel(UploadPortletRequest uploadPortletRequest) throws PortalException {

        ServiceUsageModel serviceUsageModel = new ServiceUsageModel();

        boolean activityExpenses = Boolean.parseBoolean(uploadPortletRequest.getParameter("servicesUsage-activityExpenses"));
        serviceUsageModel.setActivityExpenses(activityExpenses);
        if (activityExpenses) {
            serviceUsageModel.setActivityPersonModels(getServiceUsageExpencesForEntity("servicesUsage-activityPersonList", uploadPortletRequest));
        }

        boolean illnessExpenses = Boolean.parseBoolean(uploadPortletRequest.getParameter("servicesUsage-illnessExpenses"));
        serviceUsageModel.setIllnessExpenses(illnessExpenses);
        if (illnessExpenses) {
            serviceUsageModel.setIllnessPersonList(getServiceUsageExpencesForEntity("servicesUsage-illnessPersonList", uploadPortletRequest));
        }

        boolean insuranceBenefits = Boolean.parseBoolean(uploadPortletRequest.getParameter("servicesUsage-insuranceBenefits"));
        serviceUsageModel.setInsuranceBenefits(insuranceBenefits);
        if (insuranceBenefits) {
            serviceUsageModel.setInsurancePersonList(getServiceUsageExpencesForEntity("servicesUsage-insurancePersonList", uploadPortletRequest));
        }

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Generated Service Usage model = " + serviceUsageModel);
        }

        return serviceUsageModel;
    }

    private List<ExpensesModel> getApplicantExpenses(UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException, PortalException {
        return getExpensesModelForEntity("applicantExpenses", "applicantExpensesArray", uploadPortletRequest);
    }

    private List<ExpensesModel> getExpensesForPartner(UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException, PortalException {
        return getExpensesModelForEntity("expensesForPartner", "expensesForPartnerArray", uploadPortletRequest);
    }

    private List<ExpensesModel> getExpensesForChildren(UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException, PortalException {
        return getExpensesModelForEntity("expensesForChildren", "expensesForChildrenArray", uploadPortletRequest);
    }

    private List<ExpensesModel> getExpensesModelForEntity(String entityExpenses, String entityExpensesArray, UploadPortletRequest uploadPortletRequest) throws IOException, InterruptedException, PortalException {
        List<ExpensesModel> expensesModels = new ArrayList<>();

        List<String> paramKeys = new ArrayList<>(uploadPortletRequest.getParameterMap().keySet());
        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Request keys = " + paramKeys);
        }
        Collections.sort(paramKeys);
        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Sorted Request keys = " + paramKeys);
        }

        List<String> entityExpensesParamKeys = paramKeys
            .stream()
            .filter(s -> s.startsWith(entityExpenses + "-" + entityExpensesArray))
            .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Filtered entity Param keys = " + entityExpensesParamKeys);
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

    private ExpensesModel createExpensesModel(UploadPortletRequest uploadPortletRequest, List<String> entityExpensesParamKeys, String paramPrefix) throws IOException, PortalException {
        ExpensesModel expensesModel = null;

        String paramInvoiceDateDay = paramPrefix + "invoiceDate-day";
        String paramInvoiceDateMonth = paramPrefix + "invoiceDate-month";
        String paramInvoiceDateYear = paramPrefix + "invoiceDate-year";
        String paramServiceType = paramPrefix + "serviceType";
        String paramInvoiceAmount = paramPrefix + "invoiceAmount";
        String paramReimbursment = paramPrefix + "reimbursement";
        if (entityExpensesParamKeys.contains(paramInvoiceDateDay)) {
            expensesModel = new ExpensesModel();

            String invoiceDateDay = uploadPortletRequest.getParameter(paramInvoiceDateDay);
            String invoiceDateMonth = uploadPortletRequest.getParameter(paramInvoiceDateMonth);
            String invoiceDateYear = uploadPortletRequest.getParameter(paramInvoiceDateYear);
            CustomValidator.checkValidDate(invoiceDateDay, invoiceDateMonth, invoiceDateYear);
            expensesModel.setInvoiceDate(invoiceDateDay + "." + invoiceDateMonth + "." + invoiceDateYear);

            String serviceType = uploadPortletRequest.getParameter(paramServiceType);
            CustomValidator.checkRequired(serviceType);
            expensesModel.setServiceType(serviceType);

            String invoiceAmount = Objects.toString(formatCurrency(uploadPortletRequest.getParameter(paramInvoiceAmount)), "");
            expensesModel.setInvoiceAmount(invoiceAmount);

            CustomValidator.checkValidCurrency(invoiceAmount);

            String reimbursement = Objects.toString(formatCurrency(uploadPortletRequest.getParameter(paramReimbursment)), "");
            if (Validator.isNotNull(reimbursement)) {
                CustomValidator.checkValidCurrency(reimbursement);

                try {
                    BigDecimal invoiceAmountValue = CustomValidator.parseCurrency(invoiceAmount);
                    BigDecimal reimbursementValue = CustomValidator.parseCurrency(reimbursement);

                    CustomValidator.checkSmallerThan(reimbursementValue, invoiceAmountValue);
                } catch (ParseException e) {
                    //ignore
                }
            }
            expensesModel.setReimbursement(reimbursement);

            List<ApplicantFileModel> applicantFileModels = createApplicantFileModels(uploadPortletRequest, entityExpensesParamKeys, paramPrefix);

            expensesModel.setFiles(applicantFileModels);
        }

        return expensesModel;
    }

    private List<ApplicantFileModel> createApplicantFileModels(UploadPortletRequest uploadPortletRequest, List<String> entityExpensesParamKeys, String paramPrefix) throws IOException, PortalException {
        List<ApplicantFileModel> applicantFileModels = new ArrayList<>();

        int count = 0;

        for (int i = 1; i <= MAX_FILES; i++) {
            String innerParamPrefix = paramPrefix + "files-" + i + "-";

            String paramFile = innerParamPrefix + "file";
            String paramFileDataURI = innerParamPrefix + "file-dataURI";
            if ( (entityExpensesParamKeys.contains(paramFile)) || (entityExpensesParamKeys.contains(paramFileDataURI)) ) {
                ApplicantFileModel applicantFileModel = createApplicantFileModel(uploadPortletRequest, innerParamPrefix);

                applicantFileModels.add(applicantFileModel);

                count++;
            }
        }

        if (count == 0) {
            throw new PortalException("at least one file is required");
        }

        return applicantFileModels;
    }

    private ApplicantFileModel createApplicantFileModel(UploadPortletRequest uploadPortletRequest, String innerParamPrefix) throws IOException, PortalException {
        String paramType = innerParamPrefix + "type";

        ApplicantFileModel applicantFileModel = new ApplicantFileModel();

        File uploadedFile = getUploadedFile(uploadPortletRequest, innerParamPrefix);

        String uploadedFileName = getUploadedFileName(uploadPortletRequest, innerParamPrefix);

        CustomValidator.checkRequired(uploadedFile);
        CustomValidator.checkMaxSize(uploadedFile, 10485760);
        CustomValidator.checkRequiredFileExtension(uploadedFileName, ".pdf", ".jpg", ".jpeg", ".gif", ".bmp", ".png");

        StringBuilder prefix = new StringBuilder(uploadedFileName);

        String originalFilename = prefix.toString();

        String type = uploadPortletRequest.getParameter(paramType);

        CustomValidator.checkRequired(type);

        prefix.insert((prefix.lastIndexOf(".")), documentType.get(type));

        String filename = prefix.toString();

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Found uploaded file for Expenses: [uploadedFileName = " + uploadedFileName + ", uploadedFile = " + uploadedFile + ", originalFilename = " + originalFilename + "]");
        }

        applicantFileModel.setFile(uploadedFile);
        applicantFileModel.setFilename(filename);
        applicantFileModel.setOriginalFilename(originalFilename);
        applicantFileModel.setType(type);

        return applicantFileModel;
    }

    private List<TaxFileModel> createTaxFileModels(UploadPortletRequest uploadPortletRequest) throws IOException, PortalException {
        List<TaxFileModel> taxFileModels = new ArrayList<>();

        final String paramPrefix = "applicantAndFunds-partnerIncomeConfirmation-";

        List<String> paramKeys = new ArrayList<>(uploadPortletRequest.getParameterMap().keySet());

        for (int i = 1; i <= MAX_TAX_FILES; i++) {
            String innerParamPrefix = paramPrefix + "files-" + i + "-";

            String paramFile = innerParamPrefix + "file";
            String paramFileDataURI = innerParamPrefix + "file-dataURI";
            if ( (paramKeys.contains(paramFile)) || (paramKeys.contains(paramFileDataURI)) ) {
                TaxFileModel taxFileModel = createTaxFileModel(uploadPortletRequest, innerParamPrefix);

                taxFileModels.add(taxFileModel);
            }
        }

        return taxFileModels;
    }

    private TaxFileModel createTaxFileModel(UploadPortletRequest uploadPortletRequest, String innerParamPrefix) throws IOException, PortalException {
        TaxFileModel taxFileModel = new TaxFileModel();

        File uploadedFile = getUploadedFile(uploadPortletRequest, innerParamPrefix);

        String uploadedFileName = getUploadedFileName(uploadPortletRequest, innerParamPrefix);

        CustomValidator.checkRequired(uploadedFile);
        CustomValidator.checkMaxSize(uploadedFile, 10485760);
        CustomValidator.checkRequiredFileExtension(uploadedFileName, ".pdf", ".jpg", ".jpeg", ".gif", ".bmp", ".png");

        if (log.isDebugEnabled()) {
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Found uploaded tax file: [uploadedFileName = " + uploadedFileName + ", uploadedFile = " + uploadedFile + "]");
        }

        taxFileModel.setFile(uploadedFile);
        taxFileModel.setFilename(uploadedFileName);

        return taxFileModel;
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
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Uploaded binary file for key " + paramFile + " = " + file);
        }

        if (Validator.isNull(file)) {
            String dataURI = uploadPortletRequest.getParameter(paramFileDataURI);

            if (log.isDebugEnabled()) {
                log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Uploaded data URI file for key " + paramFileDataURI + " = " + (dataURI == null ? "null" : "[length = " + dataURI.length() + "]"));
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

    private List<String> getServiceUsageExpencesForEntity(String entity, UploadPortletRequest uploadPortletRequest) throws PortalException {
        Map<String, String[]> parameterMap = uploadPortletRequest.getParameterMap();

        int count = 0;

        List<String> person = new ArrayList<>();
        for (String paramKey : parameterMap.keySet()) {
            if (paramKey.contains(entity)) {
                String personName = uploadPortletRequest.getParameter(paramKey);

                CustomValidator.checkRequired(personName);

                person.add(personName);

                count++;
            }
        }

        if (count > 10) {
            throw new PortalException("too many names");
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
            log.debug("(Vorgangs-ID: " + ProcessIdHolder.processId.get() + ") " + "Deleting file with overwrite: " + fileToDelete.getAbsolutePath());
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
