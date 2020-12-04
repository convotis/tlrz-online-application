import {Component, Input, OnInit} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {WizardComponent} from "angular-archwizard";

import {ErrMsgService} from "../../../../../core/err-msg/err-msg.service";
import {CustomValidator} from "../../../../../core/custom-validator/custom-validator";
import {FormService} from "../../../../../core/form/form.service";
import {map} from "rxjs/operators";
import {CardService} from "../../../../shared/card.service";

@Component({
  selector: 'tlrz-expenses-for-children',
  templateUrl: './expenses-for-children.component.html',
  styleUrls: ['./expenses-for-children.component.scss']
})
export class ExpensesForChildrenComponent implements OnInit {

    @Input() public expensesForChildrenGroup: FormGroup;
    @Input() public expensesForPartnerGroup: FormGroup;
    @Input() public applicantExpensesGroup: FormGroup;

    public showSubmitError: boolean[] = [false];
    @Input() private wizard: WizardComponent;

    constructor(
        private errMsgService: ErrMsgService,
        private formService: FormService,
        private cardService: CardService
    ) {
    }

    public get expensesForChildrenArray(): FormArray {
        return <FormArray>this.expensesForChildrenGroup.get('expensesForChildrenArray')
    }

    removeCardFromMainArray(controlIndex: number) {

        if (this.expensesForChildrenArray.length > 1) {
            this.expensesForChildrenArray.removeAt(controlIndex);
            this.showSubmitError.splice(controlIndex, 1);
        }
    }

    public submit() {

        for (let i = 0; i < this.showSubmitError.length; i++) {
            this.showSubmitError[i] = true;
        }

        if (!this.expensesForChildrenGroup.valid) {
            this.expensesForChildrenGroup.markAllAsTouched();
            this.formService.markAllAsDirty(this.expensesForChildrenGroup);
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
                        return value.pageTitle == 'expensesForChildren';
                    });
                })
            )
            .subscribe(value => {
                for (let index = 0; index < value.length; index++) {
                    if (this.expensesForChildrenArray.get(index.toString())) {
                        this.expensesForChildrenArray.get(index.toString()).get('index').patchValue(value[index].index + 1);
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
        Liferay.Session.extend();

        if (this.expensesForChildrenArray.length < 20) {

            this.expensesForChildrenArray.insert(controlIndex + 1,
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
