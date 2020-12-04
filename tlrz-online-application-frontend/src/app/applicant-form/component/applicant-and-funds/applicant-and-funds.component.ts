import {Component, Input} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';

import { DateFormat } from '../../../ui/form/display-date/shared/date-format';

@Component({
    selector: 'app-applicant-and-funds',
    templateUrl: './applicant-and-funds.component.html',
    styleUrls: ['./applicant-and-funds.component.scss']
})
export class ApplicantAndFundsComponent {

    @Input() public applicantAndFundsGroup: FormGroup;

    constructor() {
    }

    public get lastName(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('lastName');
    }

    public get birthday(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('birthday');
    }

    public get firstName(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('firstName');
    }

    public get personalNumber(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('personalNumber');
    }

    public get privatePhone(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('privatePhone');
    }

    public get privateEmail(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('privateEmail');
    }

    public get applicantExpenses(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('applicantExpenses');
    }

    public get expensesForChildren(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('expensesForChildren');
    }

    public get expensesForPartner(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('expensesForPartner');
    }

    public get partnerFirstName(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('partnerFirstName');
    }

    public get differentLastName(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('differentLastName');
    }

    public get partnerTotalIncome(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('partnerTotalIncome');
    }

    public get partnerIncomeConfirmation(): FormGroup {
        return <FormGroup>this.applicantAndFundsGroup.get('partnerIncomeConfirmation');
    }

    public get confirmation(): FormControl {
        return <FormControl>this.partnerIncomeConfirmation.get('confirmation');
    }

    public millisToDate(date: DateFormat) {
        if (!date) return '';
        return date.day+'.'+date.month+'.'+date.year ;
    }
}
