import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {Component, Input, OnInit} from '@angular/core';
import {WizardComponent} from "angular-archwizard";
import {BsModalRef, BsModalService, ModalOptions} from 'ngx-bootstrap';
import {Observable} from "rxjs";
import {takeUntil} from "rxjs/operators";

import {SubmitModalComponent} from "../../../../../ui/modal/submit-modal/submit-modal.component";
import {ServiceInformation} from "../../../../shared/service-information";
import {ErrMsgService} from '../../../../../core/err-msg/err-msg.service';
import {AutoUnsubscribe} from "../../../../../core/auto-unsubscribe/auto-unsubscribe";

@Component({
    selector: 'tlrz-summary',
    templateUrl: './summary.component.html',
    styleUrls: ['./summary.component.scss'],
})
export class SummaryComponent extends AutoUnsubscribe implements OnInit {

    @Input() public applicantReactiveForm: FormGroup;
    @Input() private wizard: WizardComponent;

    public amount: Observable<number> = new Observable(this.amountSubscribe());
    public filesCount: Observable<number> = new Observable(this.filesCountSubscribe());

    private amountToSend: number = 0;
    private filesCountToSend: number = 0;

    private bsModalRef: BsModalRef;

    public get applicantAndFundsGroup(): FormGroup {
        return <FormGroup>this.applicantReactiveForm.get('applicantAndFunds');
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

    public get servicesUsageGroup(): FormGroup {
        return <FormGroup>this.applicantReactiveForm.get('servicesUsage');
    }

    public get applicantExpensesGroup(): FormGroup {
        return <FormGroup>this.applicantReactiveForm.get('applicantExpenses');
    }

    public get expensesForChildrenGroup(): FormGroup {
        return <FormGroup>this.applicantReactiveForm.get('expensesForChildren');
    }

    public get expensesForPartnerGroup(): FormGroup {
        return <FormGroup>this.applicantReactiveForm.get('expensesForPartner');
    }

    private get expensesForChildrenArray(): FormArray {
        return <FormArray>this.expensesForChildrenGroup.get('expensesForChildrenArray');
    }

    private get expensesForPartnerArray(): FormArray {
        return <FormArray>this.expensesForPartnerGroup.get('expensesForPartnerArray');
    }

    private get applicantExpensesArray(): FormArray {
        return <FormArray>this.applicantExpensesGroup.get('applicantExpensesArray');
    }

    constructor(
        public errMsgService: ErrMsgService,
        private modalService: BsModalService
    ) {
        super();
    }

    public ngOnInit(): void {

        this.amount.pipe(takeUntil(this.componentDestroyed$)).subscribe(value => {
            this.amountToSend = value;
        });

        this.filesCount.pipe(takeUntil(this.componentDestroyed$)).subscribe(value => {
            this.filesCountToSend = value;
        });
    }

    /* TODO: rename variables on meaning explaintaition name */
    public filesCountSubscribe() {

        let applicantExpensesFilesCount = 0;
        let expensesForPartnerFilesCount = 0;
        let expensesForChildrenFilesCount = 0;

        return (observer) => {

            applicantExpensesFilesCount = this.applicantExpenses.value == 'true' ? this.countFiles(this.applicantExpensesArray.value) : 0;
            expensesForPartnerFilesCount = this.expensesForPartner.value == 'true' ? this.countFiles(this.expensesForPartnerArray.value) : 0;
            expensesForChildrenFilesCount = this.expensesForChildren.value == 'true' ? this.countFiles(this.expensesForChildrenArray.value) : 0;
            observer.next(applicantExpensesFilesCount + expensesForPartnerFilesCount + expensesForChildrenFilesCount);

            this.applicantExpensesArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(applicantExpensesArray => {
                applicantExpensesFilesCount = this.countFiles(applicantExpensesArray);
                observer.next(applicantExpensesFilesCount + expensesForPartnerFilesCount + expensesForChildrenFilesCount);
            });

            this.expensesForPartnerArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(expensesForPartnerArray => {
                expensesForPartnerFilesCount = this.countFiles(expensesForPartnerArray);
                observer.next(applicantExpensesFilesCount + expensesForPartnerFilesCount + expensesForChildrenFilesCount);
            });

            this.expensesForChildrenArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(expensesForChildrenArray => {
                expensesForChildrenFilesCount = this.countFiles(expensesForChildrenArray);
                observer.next(applicantExpensesFilesCount + expensesForPartnerFilesCount + expensesForChildrenFilesCount);
            });

            this.expensesForChildren.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
                expensesForChildrenFilesCount = radioValue == 'true' ? this.countFiles(this.expensesForChildrenArray.value) : 0;
                observer.next(applicantExpensesFilesCount + expensesForPartnerFilesCount + expensesForChildrenFilesCount);
            });

            this.applicantExpenses.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
                applicantExpensesFilesCount = radioValue == 'true' ? this.countFiles(this.applicantExpensesArray.value) : 0;
                observer.next(applicantExpensesFilesCount + expensesForPartnerFilesCount + expensesForChildrenFilesCount);
            });

            this.expensesForPartner.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
                expensesForPartnerFilesCount = radioValue == 'true' ? this.countFiles(this.expensesForPartnerArray.value) : 0;
                observer.next(applicantExpensesFilesCount + expensesForPartnerFilesCount + expensesForChildrenFilesCount);
            });

            return {
                unsubscribe() {
                }
            };
        }
    }

    public amountSubscribe() {

        let applicantExpensesAmount = 0;
        let expensesForPartnerAmount = 0;
        let expensesForChildrenAmount = 0;

        return (observer) => {

            applicantExpensesAmount = this.applicantExpenses.value == 'true' ? this.countAmount(this.applicantExpensesArray.value) : 0;
            expensesForPartnerAmount = this.expensesForPartner.value == 'true' ? this.countAmount(this.expensesForPartnerArray.value) : 0;
            expensesForChildrenAmount = this.expensesForChildren.value == 'true' ? this.countAmount(this.expensesForChildrenArray.value) : 0;
            observer.next(this.numberToCurrency((applicantExpensesAmount + expensesForChildrenAmount + expensesForPartnerAmount).toString()) + " €");

            this.applicantExpensesArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(applicantExpensesArray => {
                applicantExpensesAmount = this.countAmount(applicantExpensesArray);
                observer.next(this.numberToCurrency((applicantExpensesAmount + expensesForChildrenAmount + expensesForPartnerAmount).toString()) + " €");
            });

            this.expensesForPartnerArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(expensesForPartnerArray => {
                expensesForPartnerAmount = this.countAmount(expensesForPartnerArray);
                observer.next(this.numberToCurrency((applicantExpensesAmount + expensesForChildrenAmount + expensesForPartnerAmount).toString()) + " €");
            });

            this.expensesForChildrenArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(expensesForChildrenArray => {
                expensesForChildrenAmount = this.countAmount(expensesForChildrenArray);
                observer.next(this.numberToCurrency((applicantExpensesAmount + expensesForChildrenAmount + expensesForPartnerAmount).toString()) + " €");
            });

            this.applicantExpenses.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
                applicantExpensesAmount = radioValue == 'true' ? this.countAmount(this.applicantExpensesArray.value) : 0;
                observer.next(this.numberToCurrency((applicantExpensesAmount + expensesForChildrenAmount + expensesForPartnerAmount).toString()) + " €");
            });

            this.expensesForChildren.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
                expensesForChildrenAmount = radioValue == 'true' ? this.countAmount(this.expensesForChildrenArray.value) : 0;
                observer.next(this.numberToCurrency((applicantExpensesAmount + expensesForChildrenAmount + expensesForPartnerAmount).toString()) + " €");
            });

            this.expensesForPartner.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
                expensesForPartnerAmount = radioValue == 'true' ? this.countAmount(this.expensesForPartnerArray.value) : 0;
                observer.next(this.numberToCurrency((applicantExpensesAmount + expensesForChildrenAmount + expensesForPartnerAmount).toString()) + " €");
            });

            return {
                unsubscribe() {
                }
            };
        }
    }

    public countFiles(serviceArray: ServiceInformation[]): number {

        let filesCount = 0;

        for (let service of serviceArray) {
            for (let file of service.files) {
                if (file.file != null) {
                    filesCount++;
                }
            }
        }
        return filesCount;
    }

    public countAmount(serviceArray: ServiceInformation[]): number {

        let amount = 0;

        for (let service of serviceArray) {
            amount += service.invoiceAmount ?
                parseFloat(service.invoiceAmount.split('.').join('')) : 0;
        }
        return amount;
    }

    public openSubmitModal() {

        if (this.applicantReactiveForm.get('termsCheckbox').value) {

            const configs: ModalOptions = {
                initialState: {
                    step: 1,
                    formData: this.applicantReactiveForm.value,
                    amount: this.amountToSend,
                    filesCount: this.filesCountToSend,
                },
                backdrop: 'static',
                keyboard: false
            };

            this.bsModalRef = this.modalService.show(SubmitModalComponent, configs);
        } else {
            this.applicantReactiveForm.markAllAsTouched();
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

    private numberToCurrency(value: string): string {

        let euro = value.slice(0, -2);
        let cent = value.slice(-2);

        euro = euro.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

        return euro + ',' + cent;
    }
}
