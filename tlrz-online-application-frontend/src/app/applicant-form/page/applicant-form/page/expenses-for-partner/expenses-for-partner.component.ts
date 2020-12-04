import {Component, Input, OnInit} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {WizardComponent} from "angular-archwizard";
import {CustomValidator} from "../../../../../core/custom-validator/custom-validator";
import {ErrMsgService} from "../../../../../core/err-msg/err-msg.service";
import {FormService} from "../../../../../core/form/form.service";
import {CardService} from "../../../../shared/card.service";
import {map} from "rxjs/operators";

@Component({
    selector: 'tlrz-expenses-for-partner',
    templateUrl: './expenses-for-partner.component.html',
    styleUrls: ['./expenses-for-partner.component.scss']
})
export class ExpensesForPartnerComponent implements OnInit {

    @Input() public expensesForPartnerGroup: FormGroup;
    @Input() public expensesForChildrenGroup: FormGroup;
    @Input() public applicantExpensesGroup: FormGroup;
    public showSubmitError: boolean[] = [false];
    @Input() private wizard: WizardComponent;

    constructor(
        private errMsgService: ErrMsgService,
        private formService: FormService,
        private cardService: CardService
    ) {
    }

    public get expensesForPartnerArray(): FormArray {
        return <FormArray>this.expensesForPartnerGroup.get('expensesForPartnerArray');
    }

    removeCardFromMainArray( controlIndex: number ) {

        if (this.expensesForPartnerArray.length > 1) {
            this.expensesForPartnerArray.removeAt(controlIndex);
            this.showSubmitError.splice(controlIndex, 1);
        }
    }

    public submit() {

        for (let i = 0; i < this.showSubmitError.length; i++) {
            this.showSubmitError[i] = true;
        }

        if (!this.expensesForPartnerGroup.valid) {
            this.expensesForPartnerGroup.markAllAsTouched();
            this.formService.markAllAsDirty(this.expensesForPartnerGroup);
        } else {
            for (let i = 0; i < this.showSubmitError.length; i++) {
                this.showSubmitError[i] = false;
            }
            localStorage.setItem('activePageIndex', (this.wizard.currentStepIndex + 1).toString());
            this.wizard.goToNextStep();
            this.scrollToTop();
        }
    }

    public scrollToTop(): void {
        var contentElement = document.getElementById('content');
        if (contentElement) {
            contentElement.scrollIntoView();
        }
    }


    ngOnInit(): void {

        this.cardService.cards$
            .pipe(
                map((cards: any) => {
                    return cards.map((value, index) => {
                        return {pageTitle: value, index: index}
                    }).filter((value) => {
                        return value.pageTitle == 'expensesForPartner';
                    });
                })
            )
            .subscribe(value => {
                for (let index = 0; index < value.length; index++) {
                    if (this.expensesForPartnerArray.get(index.toString())) {
                        this.expensesForPartnerArray.get(index.toString()).get('index').patchValue(value[index].index + 1);
                    }
                }
            });
    }

    public return() {
        localStorage.setItem('activePageIndex', (this.wizard.currentStepIndex - 1).toString());
        this.wizard.goToPreviousStep();
        this.scrollToTop();
    }

    private addCardToMainArray(controlIndex: number) {

        if (this.expensesForPartnerArray.length < 20) {

            this.expensesForPartnerArray.insert(controlIndex + 1,
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
