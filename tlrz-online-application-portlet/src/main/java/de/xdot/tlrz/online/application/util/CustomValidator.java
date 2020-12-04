package de.xdot.tlrz.online.application.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.Validator;
import de.xdot.pdf.creation.model.sub.ApplicantFileModel;
import de.xdot.pdf.creation.model.sub.ExpensesModel;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomValidator {

    public static void checkValidDate(String day, String month, String year) throws PortalException {
        if (! isValidDate(day, month, year)) {
            throw new PortalException("invalid date");
        }
    }

    public static void checkRequired(Object value) throws PortalException {
        if (Validator.isNull(value)) {
            throw new PortalException("required");
        }
    }

    public static void checkMaxSize(File file, long maxFileSize) throws PortalException {
        if (Validator.isNotNull(file) && file.exists()) {
            if (file.length() > maxFileSize) {
                throw new PortalException("file size too big");
            }
        }
    }

    public static void checkTotalMaxSize(List<ExpensesModel> expensesModels, long totalMaxSize) throws PortalException {
        List<File> files = new ArrayList<>();

        for (ExpensesModel expensesModel : expensesModels) {
            List<ApplicantFileModel> applicantFileModels = expensesModel.getFiles();
            for (ApplicantFileModel applicantFileModel : applicantFileModels) {
                files.add(applicantFileModel.getFile());
            }
        }

        long currentSize = 0;
        for (File file : files) {
            if (file.exists()) {
                currentSize += file.length();

                if (currentSize > totalMaxSize) {
                    throw new PortalException("total size of files too big");
                }
            }
        }
    }

    public static void checkRequiredFileExtension(String filename, String... extensions) throws PortalException {
        if (Validator.isNotNull(filename)) {
            String extension = FileUtil.getExtension(filename);
            if (Validator.isNull(extension)) {
                throw new PortalException("invalid file extension");
            }
            extension = "." + extension;
            boolean found = false;
            for (String check : extensions) {
                if (check.equalsIgnoreCase(extension)) {
                    found = true;

                    break;
                }
            }

            if (! found) {
                throw new PortalException("invalid file extension");
            }
        }
    }

    public static void checkValidCurrency(String value) throws PortalException {
        try {
            BigDecimal currency = parseCurrency(value);

            if (Validator.isNull(currency)) {
                throw new PortalException("invalid currency");
            }

            if (currency.compareTo(BigDecimal.valueOf(1000000L)) > 0) {
                throw new PortalException("invalid currency");
            }
        } catch (ParseException e) {
            throw new PortalException("invalid currency", e);
        }
    }

    public static void checkSmallerThan(BigDecimal value, BigDecimal compareTo) throws PortalException {
        if (Validator.isNotNull(value) && Validator.isNotNull(compareTo)) {
            if (value.compareTo(compareTo) >= 0) {
                throw new PortalException("value must be smaller");
            }
        }
    }


    public static BigDecimal parseCurrency(String value) throws ParseException {
        if (Validator.isNotNull(value)) {
            DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
            decimalFormatSymbols.setDecimalSeparator(',');
            decimalFormatSymbols.setGroupingSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
            decimalFormat.setParseBigDecimal(true);
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

            BigDecimal result = (BigDecimal)decimalFormat.parse(value);

            return result;
        }

        return null;
    }

    public static void checkValidPersonalnumber(String personalnumber) throws PortalException {
        if (Validator.isNotNull(personalnumber)) {
            if (! personalnumber.matches("\\d{8}")) {
                throw new PortalException("invalid personal number");
            }
        }
    }

    public static void checkValidEmailAddress(String emailAddress) throws PortalException {
        if (Validator.isNotNull(emailAddress)) {
            if (! emailAddress.matches("[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9-]+)*(?:\\.[a-zA-Z0-9]{2,}(?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)")) {
                throw new PortalException("invalid email address");
            }
        }
    }

    public static void checkAtLeastOneMustBeTrue(boolean... values) throws PortalException {
        if (Validator.isNotNull(values)) {
            for (boolean value : values) {
                if (value) {
                    return;
                }
            }
        }

        throw new PortalException("at least one must be true");
    }

    private static boolean isValidDate(String day, String month, String year) {
        if (Validator.isNull(day) || Validator.isNull(month) || Validator.isNull(year)) {
            return false;
        }
        try {
            int dayValue = Integer.parseInt(day);
            int monthValue = Integer.parseInt(month);
            int yearValue = Integer.parseInt(year);

            if (dayValue > 31 || dayValue < 1 || monthValue > 12 || monthValue < 1 || yearValue < 1000) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(yearValue, monthValue - 1, dayValue, 0, 0, 0);

            if (calendar.after(Calendar.getInstance())) {
                return false;
            }

            if (calendar.get(Calendar.DATE) != dayValue) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
