import {ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

import {WizardComponent} from "angular-archwizard";

import {applicationContext} from "../../../../environments/environment";
import {CustomValidator} from "../../../core/custom-validator/custom-validator";
import {CardService} from "../../shared/card.service";
import {FileStorageService} from '../../shared/file-storage.service';
import {AutoUnsubscribe} from "../../../core/auto-unsubscribe/auto-unsubscribe";
import {Applicant} from "./shared/applicant";
import {IndexedDatabaseService} from "../../../core/indexed-database/indexed-database.service";

@Component({
    selector: 'tlrz-applicant-form',
    templateUrl: './applicant-form.component.html',
    styleUrls: ['./applicant-form.component.scss'],
})
export class ApplicantFormComponent extends AutoUnsubscribe implements OnInit {

    @ViewChild(WizardComponent, {static: true}) public wizard: WizardComponent;

    public applicantReactiveForm: FormGroup;

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

    public get applicantExpensesArray(): FormArray {
        return <FormArray>this.applicantExpensesGroup.get('applicantExpensesArray');
    }

    public get applicantExpensesRadio(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('applicantExpenses');
    }

    public get expensesForPartnerRadio(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('expensesForPartner');
    }

    public get expensesForChildrenRadio(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('expensesForChildren');
    }

    public get expensesForPartnerArray(): FormArray {
        return <FormArray>this.expensesForPartnerGroup.get('expensesForPartnerArray');
    }

    public get expensesForChildrenArray(): FormArray {
        return <FormArray>this.expensesForChildrenGroup.get('expensesForChildrenArray');
    }

    public get activePageIndex(): number {

        let activePageIndex = localStorage.getItem('activePageIndex');

        if (activePageIndex != null) {
            return parseInt(activePageIndex);
        }
        return 0;
    }

    private get currentApplicant(): Applicant {
        return JSON.parse(applicationContext.user);
    }

    private get optionalPageArrayGroup() {
        return new FormGroup({
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
        })
    }

    private get optionalPageFilesGroup() {
        return new FormGroup({
            file: new FormControl(null, [
                Validators.required,
                CustomValidator.requiredFileExtension(".pdf, .jpg, .jpeg, .gif, .bmp, .png"),
                CustomValidator.maxSize(10485760)]
            ),
            type: new FormControl(null, Validators.required)
        })
    }

    constructor(
        private formBuilder: FormBuilder,
        private cardService: CardService,
        private fileStorage: FileStorageService,
        private storageService: IndexedDatabaseService,
        private _changeDetectionRef: ChangeDetectorRef
    ) {
        super();

        /* TODO: replace FormGroup instead of FormBuilder */
        this.applicantReactiveForm = this.formBuilder.group({

            // Applicant and Funds page
            applicantAndFunds: new FormGroup({

                // Applicant Details

                lastName: new FormControl({value: this.currentApplicant.lastName, disabled: true}),
                birthday: new FormControl({value: this.currentApplicant.birthday, disabled: (this.currentApplicant.birthday != null)}, [
                    CustomValidator.validDate()
                ]),
                firstName: new FormControl({value: this.currentApplicant.firstName, disabled: true}),
                personalNumber: new FormControl(null, [
                    Validators.required,
                    CustomValidator.validPersonalNumber()
                ]),
                privatePhone: new FormControl(null),
                privateEmail: new FormControl(null,
                    Validators.pattern(/^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9-]+)*(?:\.[a-zA-Z0-9]{2,}(?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)$/)
                ),

                //Funds Detail

                applicantExpenses: new FormControl(null, Validators.required),
                expensesForChildren: new FormControl(null, Validators.required),
                expensesForPartner: new FormControl(null, Validators.required),

                partnerFirstName: new FormControl(null),
                differentLastName: new FormControl(null),
                partnerTotalIncome: new FormGroup({
                    preCurrentCalendarYear: new FormControl(false),
                    currentCalendarYear: new FormControl(false)
                }),
                partnerIncomeConfirmation: new FormGroup({
                    confirmation: new FormControl(null),
                    taxAssessmentFile: new FormControl(null)
                })
            }, [CustomValidator.atLeastOneMustBeTrue(this), CustomValidator.testValidator()]),

            // Services Usage page

            servicesUsage: new FormGroup({

                // Activity Expenses

                activityExpenses: new FormControl(null, Validators.required),
                activityPersonList: new FormArray([
                    new FormGroup({
                        person: new FormControl(null)
                    })
                ]),

                // Illness Expenses

                illnessExpenses: new FormControl(null, Validators.required),
                illnessPersonList: new FormArray([
                    new FormGroup({
                        person: new FormControl(null)
                    })
                ]),

                // Insurance Benefits

                insuranceBenefits: new FormControl(null, Validators.required),
                insurancePersonList: new FormArray([
                    new FormGroup({
                        person: new FormControl(null)
                    })
                ]),
            }, CustomValidator.testValidator()),

            // Applicant Expenses

            applicantExpenses: new FormGroup({
                applicantExpensesArray: new FormArray([
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
                    ])])
            }, CustomValidator.maxTotalSize(this, 104857600, false, false)),

            // Expenses for Partner

            expensesForPartner: new FormGroup({
                expensesForPartnerArray: new FormArray([
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
                    ])]
                )
            }, CustomValidator.maxTotalSize(this, 104857600, false, true)),

            // Expenses for Children

            expensesForChildren: new FormGroup({
                expensesForChildrenArray: new FormArray([
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
                    ])]
                )
            }, CustomValidator.maxTotalSize(this, 104857600, true, true)),

            termsCheckbox: new FormControl(false, Validators.pattern('true'))
        });
    }

    ngOnInit(): void {

        this.storageService.getItem().subscribe(applicantForm => {

            if (applicantForm != null) {

                this.makeFitToStoredArray(this.applicantExpensesArray, applicantForm.applicantExpenses.applicantExpensesArray);
                this.makeFitToStoredArray(this.expensesForPartnerArray, applicantForm.expensesForPartner.expensesForPartnerArray);
                this.makeFitToStoredArray(this.expensesForChildrenArray, applicantForm.expensesForChildren.expensesForChildrenArray);

                this.applicantReactiveForm.patchValue(applicantForm);
            }

            this.storageService.storeOnChange(this.applicantReactiveForm);

            this._changeDetectionRef.detectChanges();

            this.wizard.goToStep(this.activePageIndex);
        });
    }

    private makeFitToStoredArray(formArray: FormArray, storedArray: any[]) {

        while (storedArray.length > formArray.length) {
            formArray.push(this.optionalPageArrayGroup);
        }

        for (let i = 0; i < storedArray.length; i++) {
            while (storedArray[i].files.length >
            (<FormArray>formArray.get(i.toString()).get('files')).length) {

                (<FormArray>formArray.get(i.toString()).get('files')).push(this.optionalPageFilesGroup);
            }
        }
    }
}
