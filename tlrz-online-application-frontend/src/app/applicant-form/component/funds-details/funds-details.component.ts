import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";
import {Component, Input, OnInit} from '@angular/core';
import {distinctUntilChanged, takeUntil} from 'rxjs/operators';

import {AutoUnsubscribe} from '../../../core/auto-unsubscribe/auto-unsubscribe';
import {ErrMsgService} from "../../../core/err-msg/err-msg.service";
import {CustomValidator} from "../../../core/custom-validator/custom-validator";

@Component({
    selector: 'tlrz-funds-details',
    templateUrl: './funds-details.component.html',
    styleUrls: ['./funds-details.component.scss']
})
export class FundsDetailsComponent extends AutoUnsubscribe implements OnInit {

    @Input() public applicantAndFundsGroup: FormGroup;

    public get partnerFirstName(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('partnerFirstName');
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

    public get partnerIncomeConfirmation(): FormGroup {
        return <FormGroup>this.applicantAndFundsGroup.get('partnerIncomeConfirmation');
    }

    public get partnerTotalIncome(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('partnerTotalIncome');
    }

    public get confirmation(): FormControl {
        return <FormControl>this.partnerIncomeConfirmation.get('confirmation');
    }

    public get files(): FormArray {
        return <FormArray>this.partnerIncomeConfirmation.get('files');
    }

    constructor(
        public errMsgService: ErrMsgService,
    ) {
        super();
    }

    public ngOnInit(): void {

        this.switchConfirmationValidators(this.partnerTotalIncome.get('preCurrentCalendarYear').value);

        this.partnerTotalIncome.get('preCurrentCalendarYear').valueChanges
            .pipe(
                distinctUntilChanged(),
                takeUntil(this.componentDestroyed$)
            )
            .subscribe((value: boolean) => {
                this.switchConfirmationValidators(value);
            });
    }

    public setPartnerControlsValidators() {
        this.partnerFirstName.setValidators(Validators.required);
        this.partnerFirstName.updateValueAndValidity();

        this.partnerTotalIncome.setValidators(Validators.required);
        this.partnerTotalIncome.updateValueAndValidity();
    }

    public removePartnerControlsValidators() {

        this.partnerFirstName.setValidators(null);
        this.partnerFirstName.updateValueAndValidity();

        this.partnerTotalIncome.setValidators(null);
        this.partnerTotalIncome.updateValueAndValidity();

        this.removeIncomeConfirmationValidators();
    }

    public switchConfirmationValidators(fieldValue: boolean) {
        fieldValue
            ? this.setIncomeConfirmationValidators()
            : this.removeIncomeConfirmationValidators();
    }

    public setIncomeConfirmationValidators() {
        this.confirmation.setValidators([
            Validators.required,
            CustomValidator.requiredFile(this.files)
        ]);
        this.confirmation.updateValueAndValidity();

        for (let control of this.files.controls) {
            control.get('file').setValidators([
                CustomValidator.requiredFileExtension(".pdf, .jpg, .jpeg, .gif, .bmp, .png"),
                CustomValidator.maxSize(10485760),
            ]);

            control.get('file').updateValueAndValidity();
        }
    }

    public removeIncomeConfirmationValidators() {
        for (let control of this.files.controls) {
            control.get('file').setValidators(null);

            control.get('file').updateValueAndValidity();
        }

        this.confirmation.setValidators(null);
        this.confirmation.updateValueAndValidity();
    }
}
