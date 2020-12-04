package de.xdot.pdf.creation.model.sub;

import java.util.List;

public class ExpensesModel {
    private String invoiceDate;
    private String serviceType;
    private String invoiceAmount;
    private String reimbursement;
    private List<ApplicantFileModel> files;

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(String invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getReimbursement() {
        return reimbursement;
    }

    public void setReimbursement(String reimbursement) {
        this.reimbursement = reimbursement;
    }

    public List<ApplicantFileModel> getFiles() {
        return files;
    }

    public void setFiles(List<ApplicantFileModel> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "ExpensesModel{" +
            "invoiceDate='" + invoiceDate + '\'' +
            ", serviceType='" + serviceType + '\'' +
            ", invoiceAmount='" + invoiceAmount + '\'' +
            ", reimbursement='" + reimbursement + '\'' +
            ", files=" + files +
            '}';
    }
}
