package de.xdot.pdf.creation.model.sub;

import java.io.File;

public class PartnerIncomeConfirmationModel {
    private Integer confirmation;
    private File taxAssessmentFile;
    private String taxAssessmentFileName;
    private Boolean preCalendarYear;
    private Boolean calendarYear;


    public Integer getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Integer confirmation) {
        this.confirmation = confirmation;
    }

    public File getTaxAssessmentFile() {
        return taxAssessmentFile;
    }

    public void setTaxAssessmentFile(File taxAssessmentFile) {
        this.taxAssessmentFile = taxAssessmentFile;
    }

    public String getTaxAssessmentFileName() {
        return taxAssessmentFileName;
    }

    public void setTaxAssessmentFileName(String taxAssessmentFileName) {
        this.taxAssessmentFileName = taxAssessmentFileName;
    }

    public Boolean getPreCalendarYear() {
        return preCalendarYear;
    }

    public void setPreCalendarYear(Boolean preCalendarYear) {
        this.preCalendarYear = preCalendarYear;
    }

    public Boolean getCalendarYear() {
        return calendarYear;
    }

    public void setCalendarYear(Boolean calendarYear) {
        this.calendarYear = calendarYear;
    }

    @Override
    public String toString() {
        return "PartnerIncomeConfirmationModel{" +
            "confirmation=" + confirmation +
            ", taxAssessmentFile=" + taxAssessmentFile +
            ", preCalendarYear=" + preCalendarYear +
            ", calendarYear=" + calendarYear +
            '}';
    }
}
