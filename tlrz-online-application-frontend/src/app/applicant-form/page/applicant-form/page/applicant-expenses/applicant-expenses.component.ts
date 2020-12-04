import {Component, Input, OnInit} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {WizardComponent} from "angular-archwizard";
import {CustomValidator} from "../../../../../core/custom-validator/custom-validator";
import {FormService} from "../../../../../core/form/form.service";
import {CardService} from "../../../../shared/card.service";
import {map} from "rxjs/operators";
import {ErrMsgService} from "../../../../../core/err-msg/err-msg.service";

@Component({
  selector: 'tlrz-applicant-expenses',
  templateUrl: './applicant-expenses.component.html',
  styleUrls: ['./applicant-expenses.component.scss']
})
export class ApplicantExpensesComponent implements OnInit {

    @Input() public applicantExpensesGroup: FormGroup;
    @Input() public expensesForPartnerGroup: FormGroup;
    @Input() public expensesForChildrenGroup: FormGroup;
    public showSubmitError: boolean[] = [false];
    @Input() private wizard: WizardComponent;

    constructor(
        private formService: FormService,
        private cardService: CardService,
        public errMsgService: ErrMsgService,
    ) {
    }

    public get applicantExpensesArray(): FormArray {
        return <FormArray>this.applicantExpensesGroup.get('applicantExpensesArray');
    }

    public ngOnInit(): void {

        this.cardService.cards$
            .pipe(
                map((cards: any) => {
                    return cards.map((value, index) => {
                        return {pageTitle: value, index: index}
                    }).filter((value) => {
                        return value.pageTitle == 'applicantExpenses';
                    });
                })
            )
            .subscribe(value => {
                for (let index = 0; index < value.length; index++) {
                    if (this.applicantExpensesArray.get(index.toString())) {
                        this.applicantExpensesArray.get(index.toString()).get('index').patchValue(value[index].index + 1);
                    }
                }
            });
    }

    /* TODO: move to helper */
    public removeCardFromMainArray(controlIndex: number) {

        if (this.applicantExpensesArray.length > 1) {
            this.applicantExpensesArray.removeAt(controlIndex);
            this.showSubmitError.splice(controlIndex, 1);
        }
    }

    public submit() {

        for (let i = 0; i < this.showSubmitError.length; i++) {
            this.showSubmitError[i] = true;
        }

        if (!this.applicantExpensesGroup.valid) {
            this.applicantExpensesGroup.markAllAsTouched();
            this.formService.markAllAsDirty(this.applicantExpensesGroup);
        } else {
            for (let i = 0; i < this.showSubmitError.length; i++) {
                this.showSubmitError[i] = false;
            }
            localStorage.setItem('activePageIndex', (this.wizard.currentStepIndex + 1).toString());
            this.wizard.goToNextStep();
            this.scrollToTop();
        }
    }

    public return() {
        localStorage.setItem('activePageIndex', (this.wizard.currentStepIndex - 1).toString());
        this.wizard.goToPreviousStep();
        this.scrollToTop();
    }

    public scrollToTop(): void {
        var contentElement = document.getElementById('content');
        if (contentElement) {
            contentElement.scrollIntoView();
        }
    }

    private addCardToMainArray(controlIndex: number) {

        /* TODO: move magic numbers to enum */
        if (this.applicantExpensesArray.length < 20) {
            /* TODO: move FormGroup definition on the top */
            this.applicantExpensesArray.insert(controlIndex + 1,
                new FormGroup({
                    index: new FormControl(null),
                    invoiceDate: new FormControl(null, [
                        CustomValidator.validDate()
                    ]),
                    serviceType: new FormControl(null, Validators.required),
                    invoiceAmount: new FormControl(null, [
                        Validators.required,
                        CustomValidator.requiredIntValues(3),
                    ]),
                    reimbursement: new FormControl(null),
                    files: new FormArray([
                        new FormGroup({
                            file: new FormControl(null, [
                                Validators.required,
                                CustomValidator.requiredFileExtension(".pdf, .jpg, .jpeg, .gif, .bmp, .png"),
                                CustomValidator.maxSize(10485760)]
                            ),
                            type: new FormControl(null, Validators.required)
                        })
                    ])
                }, [
                    CustomValidator.validCardFields(), CustomValidator.testValidator()
                ])
            );

            this.showSubmitError.splice(controlIndex + 1, 0, false)
        }
    }
}
