package de.xdot.pdf.creation.model.sub;

import java.util.List;

public class PartnerIncomeConfirmationModel {
    private Integer confirmation;
    private List<TaxFileModel> taxAssessmentFiles;
    private String taxAssessmentFileName;
    private Boolean preCalendarYear;
    private Boolean calendarYear;


    public Integer getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Integer confirmation) {
        this.confirmation = confirmation;
    }

    public List<TaxFileModel> getTaxAssessmentFiles() {
        return taxAssessmentFiles;
    }

    public void setTaxAssessmentFiles(List<TaxFileModel> taxAssessmentFile) {
        this.taxAssessmentFiles = taxAssessmentFile;
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
            ", taxAssessmentFile=" + taxAssessmentFiles +
            ", preCalendarYear=" + preCalendarYear +
            ", calendarYear=" + calendarYear +
            '}';
    }
}
