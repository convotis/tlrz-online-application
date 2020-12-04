package de.xdot.pdf.creation.model.sub;

public class ApplicantAndFundsModel {
    private String lastName;
    private String firstName;
    private String birthday;
    private String personalNumber;
    private String privatePhone;
    private String privateEmail;
    private boolean applicantExpenses;
    private boolean expensesForChildren;
    private boolean expensesForPartner;
    private String partnerFirstName;
    private String differentLastName;
    private Integer partnerTotalIncome;
    private PartnerIncomeConfirmationModel partnerIncomeConfirmation;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getPrivatePhone() {
        return privatePhone;
    }

    public void setPrivatePhone(String privatePhone) {
        this.privatePhone = privatePhone;
    }

    public String getPrivateEmail() {
        return privateEmail;
    }

    public void setPrivateEmail(String privateEmail) {
        this.privateEmail = privateEmail;
    }

    public boolean isApplicantExpenses() {
        return applicantExpenses;
    }

    public void setApplicantExpenses(boolean applicantExpenses) {
        this.applicantExpenses = applicantExpenses;
    }

    public boolean isExpensesForChildren() {
        return expensesForChildren;
    }

    public void setExpensesForChildren(boolean expensesForChildren) {
        this.expensesForChildren = expensesForChildren;
    }

    public boolean isExpensesForPartner() {
        return expensesForPartner;
    }

    public void setExpensesForPartner(boolean expensesForPartner) {
        this.expensesForPartner = expensesForPartner;
    }

    public String getPartnerFirstName() {
        return partnerFirstName;
    }

    public void setPartnerFirstName(String partnerFirstName) {
        this.partnerFirstName = partnerFirstName;
    }

    public String getDifferentLastName() {
        return differentLastName;
    }

    public void setDifferentLastName(String differentLastName) {
        this.differentLastName = differentLastName;
    }

    public Integer getPartnerTotalIncome() {
        return partnerTotalIncome;
    }

    public void setPartnerTotalIncome(Integer partnerTotalIncome) {
        this.partnerTotalIncome = partnerTotalIncome;
    }

    public PartnerIncomeConfirmationModel getPartnerIncomeConfirmation() {
        return partnerIncomeConfirmation;
    }

    public void setPartnerIncomeConfirmation(PartnerIncomeConfirmationModel partnerIncomeConfirmation) {
        this.partnerIncomeConfirmation = partnerIncomeConfirmation;
    }

    @Override
    public String toString() {
        return "ApplicantAndFundsModel{" +
            "lastName='" + lastName + '\'' +
            ", firstName='" + firstName + '\'' +
            ", birthday='" + birthday + '\'' +
            ", personalNumber='" + personalNumber + '\'' +
            ", privatePhone='" + privatePhone + '\'' +
            ", privateEmail='" + privateEmail + '\'' +
            ", expensesForChildren=" + expensesForChildren +
            ", expensesForPartner=" + expensesForPartner +
            ", partnerFirstName='" + partnerFirstName + '\'' +
            ", differentLastName='" + differentLastName + '\'' +
            ", partnerTotalIncome=" + partnerTotalIncome +
            ", partnerIncomeConfirmation=" + partnerIncomeConfirmation +
            '}';
    }
}
