package de.xdot.pdf.creation.model.sub;

import java.util.List;

public class ServiceUsageModel {
    private boolean activityExpenses;
    private List<String> activityPersonModels;
    private boolean illnessExpenses;
    private List<String> illnessPersonList;
    private boolean insuranceBenefits;
    private List<String> insurancePersonList;

    public boolean isActivityExpenses() {
        return activityExpenses;
    }

    public void setActivityExpenses(boolean activityExpenses) {
        this.activityExpenses = activityExpenses;
    }

    public List<String> getActivityPersonModels() {
        return activityPersonModels;
    }

    public void setActivityPersonModels(List<String> activityPersonModels) {
        this.activityPersonModels = activityPersonModels;
    }

    public boolean isIllnessExpenses() {
        return illnessExpenses;
    }

    public void setIllnessExpenses(boolean illnessExpenses) {
        this.illnessExpenses = illnessExpenses;
    }

    public List<String> getIllnessPersonList() {
        return illnessPersonList;
    }

    public void setIllnessPersonList(List<String> illnessPersonList) {
        this.illnessPersonList = illnessPersonList;
    }

    public boolean isInsuranceBenefits() {
        return insuranceBenefits;
    }

    public void setInsuranceBenefits(boolean insuranceBenefits) {
        this.insuranceBenefits = insuranceBenefits;
    }

    public List<String> getInsurancePersonList() {
        return insurancePersonList;
    }

    public void setInsurancePersonList(List<String> insurancePersonList) {
        this.insurancePersonList = insurancePersonList;
    }

    @Override
    public String toString() {
        return "ServiceUsageModel{" +
            "activityExpenses=" + activityExpenses +
            ", activityPersonModels=" + activityPersonModels +
            ", illnessExpenses=" + illnessExpenses +
            ", illnessPersonList=" + illnessPersonList +
            ", insuranceBenefits=" + insuranceBenefits +
            ", insurancePersonList=" + insurancePersonList +
            '}';
    }
}
