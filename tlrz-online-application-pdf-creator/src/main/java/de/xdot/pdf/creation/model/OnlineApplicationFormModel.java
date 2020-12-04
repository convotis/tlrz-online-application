package de.xdot.pdf.creation.model;

import de.xdot.pdf.creation.model.sub.ApplicantAndFundsModel;
import de.xdot.pdf.creation.model.sub.ExpensesModel;
import de.xdot.pdf.creation.model.sub.ServiceUsageModel;

import java.util.List;

public class OnlineApplicationFormModel {
    private ApplicantAndFundsModel applicantAndFundsModel;
    private ServiceUsageModel serviceUsageModel;
    private List<ExpensesModel> applicantExpenses;
    private List<ExpensesModel> expensesForPartner;
    private List<ExpensesModel> expensesForChildren;
    private String amount;
    private String filesCount;
    private String pdfCreationTime;
    private String pdfCreationRowTime;

    public ApplicantAndFundsModel getApplicantAndFundsModel() {
        return applicantAndFundsModel;
    }

    public void setApplicantAndFundsModel(ApplicantAndFundsModel applicantAndFundsModel) {
        this.applicantAndFundsModel = applicantAndFundsModel;
    }

    public ServiceUsageModel getServiceUsageModel() {
        return serviceUsageModel;
    }

    public void setServiceUsageModel(ServiceUsageModel serviceUsageModel) {
        this.serviceUsageModel = serviceUsageModel;
    }

    public List<ExpensesModel> getApplicantExpenses() {
        return applicantExpenses;
    }

    public void setApplicantExpenses(List<ExpensesModel> applicantExpenses) {
        this.applicantExpenses = applicantExpenses;
    }

    public List<ExpensesModel> getExpensesForPartner() {
        return expensesForPartner;
    }

    public void setExpensesForPartner(List<ExpensesModel> expensesForPartner) {
        this.expensesForPartner = expensesForPartner;
    }

    public List<ExpensesModel> getExpensesForChildren() {
        return expensesForChildren;
    }

    public void setExpensesForChildren(List<ExpensesModel> expensesForChildren) {
        this.expensesForChildren = expensesForChildren;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(String filesCount) {
        this.filesCount = filesCount;
    }

    public String getPdfCreationTime() {
        return pdfCreationTime;
    }

    public void setPdfCreationTime(String pdfCreationTime) {
        this.pdfCreationTime = pdfCreationTime;
    }

    public String getPdfCreationRowTime() {
        return pdfCreationRowTime;
    }

    public void setPdfCreationRowTime(String pdfCreationRowTime) {
        this.pdfCreationRowTime = pdfCreationRowTime;
    }
}
