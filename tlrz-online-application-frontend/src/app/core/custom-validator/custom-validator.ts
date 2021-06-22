import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";
import {Input} from "@angular/core";
import {ServiceInformation} from "../../applicant-form/shared/service-information";
import {ApplicantFormComponent} from "../../applicant-form/page/applicant-form/applicant-form.component";

export class CustomValidator {

    constructor() {}

    /*TODO:: rename validation function according to usage
       (checks if inputs have more than two required validation errors)*/
    static testValidator(): any {
        return (control: AbstractControl) => {

            let requiredErrors = 0;

            if (this.getErrors(control, requiredErrors) > 1) {
                return {
                    testValidator: true
                }
            }
            return null;
        }
    }

    static getErrors(abstractControl: any, errorsCounter: number): number {

        switch (true) {
            case abstractControl instanceof FormGroup:
                Object.keys(abstractControl.controls).forEach(key => {
                    errorsCounter = this.getErrors(abstractControl.get(key), errorsCounter);
                });
                break;
            case abstractControl instanceof FormArray:
                for (const control of abstractControl.controls) {
                    errorsCounter = this.getErrors(control, errorsCounter);
                }
                break;
            default:
                if (abstractControl.errors) {
                    if (abstractControl.errors['required']) {
                        errorsCounter++;
                    }
                }

        }
        return errorsCounter;
    };

    static smallerThan(comparedControl: AbstractControl): any {
        return (comparingControl: AbstractControl) => {

            let comparedValue = comparedControl.value;
            let comparingValue = comparingControl.value;

            if (comparingValue && comparedValue) {

                comparingValue = parseInt(comparingValue.split('.').join(''));
                comparedValue = parseInt(comparedValue.split('.').join(''));

                if (comparedValue <= comparingValue) {
                    return {
                        largerThan: true
                    }
                }
            }
            return null;
        }
    }

    static requiredRadio(radioControl: AbstractControl): any {
        return (fileControl: AbstractControl) => {

            const radioValue = radioControl.value;
            const file = fileControl.value;

            if (file && radioValue != '2') {
                return {
                    requiredRadio: true
                }
            }

            if (file && radioValue && !radioControl.valid && fileControl.valid) {
                radioControl.updateValueAndValidity({onlySelf: true, emitEvent: false});
            }
            return null;
        }
    }

    static requiredFile(files: FormArray): any {
        return (radioControl: AbstractControl) => {

            const radioValue = radioControl.value;
            let filePresent = false;
            let fileControl;

            for (let control of files.controls) {
                if (control.get('file')) {
                    fileControl = control.get('file');

                    if (fileControl.value) {
                        filePresent = true;
                    }

                    break;
                }
            }

            /* TODO: move magic numbers to enum */
            if (radioValue == '2' && !filePresent) {
                return {
                    requiredFile: true
                }
            }

            /* TODO: move magic number to enum */
            if (radioValue != '2' && filePresent) {
                fileControl.updateValueAndValidity({onlySelf: true, emitEvent: false});
            }

            if (filePresent && radioValue && radioControl.valid && !fileControl.valid) {
                fileControl.updateValueAndValidity({onlySelf: true, emitEvent: false});
            }
            return null;
        }
    }

    static requiredFileExtension(type: string): any {
        return (control: AbstractControl) => {

            const file = control.value;

            if (file) {
                //TODO:: fix dirty hack on page reload
                if (file.name) {
                    const fileByDotArr = file.name.split('.');
                    const extension = '.' + fileByDotArr.pop();

                    const typeList: string[] = type.replace(/\s/g, '').split(',')
                        .map((item) => {
                            return item.toLowerCase().trim();
                        });

                    if (!extension ||
                        !fileByDotArr ||
                        !fileByDotArr.length ||
                        typeList.indexOf(extension.toLowerCase()) === -1) {
                        return {
                            requiredFileExtension: true
                        };
                    }
                }
            }
            return null;
        };
    }

    /**
     * @param maxSize variable is presented in bytes
     */
    static maxSize(maxSize: number): any {
        return (control: AbstractControl) => {

            const file = control.value;

            if (file) {
                if (file.size > maxSize) {
                    return {
                        maxSize:true
                    }
                }
            }
            return null;
        }
    }

    /* TODO: rename to more obviously name. */
    static requiredIntValues(maxSeparatedValues: number) {
        return (control: AbstractControl) => {

            const value = control.value;

            if (value) {

                const euroAndCent = value.split('.');

                if (!this.onlyDigitsAndDelimiter(value) ||
                    euroAndCent.length > maxSeparatedValues ||
                    value.includes('-')
                ) return {
                    requiredIntValues: true
                };

                for (let i of euroAndCent) {
                    if (i == '') {
                        return {
                            requiredIntValues: true
                        };
                    }
                }
            }
            return null;
        }
    }

    static validDate(): any {
        return (control: AbstractControl) => {

            const date = control.value;

            if (!date) {
                return {
                    validDate: true
                };
            }

            if (date.day.match(/^[0-9]{2}$/) == null ||
                date.month.match(/^[0-9]{2}$/) == null ||
                date.year.match(/^[0-9]{4}$/) == null ||
                date.day > 31 || date.day < 1 ||
                date.month > 12 || date.month < 1 ||
                date.year < 1000) {
                return {
                    validDate: true
                };
            }

            let calcDate = new Date(date.year, (date.month - 1), date.day, 0, 0, 0, 0);
            if (calcDate > new Date()) {
                return {
                    validDate: true
                };
            }

            //if you provide an invalid date like 2019-02-31 the "day" of the created Date Object will be different from the passed day
            if (calcDate.getDate() != date.day) {
                return {
                    validDate: true
                };
            }

            return null;
        }
    }

    static validPersonalNumber(): any {
        return (control: AbstractControl) => {

            const personalNumber = control.value;

            if (personalNumber) {
                if (personalNumber.match(/^[0-9]{8}$/) == null) {
                    return {
                        validPersonalNumber: true
                    };
                }
            }
            return null;
        }
    }

    public static onlyDigitsAndDelimiter(value: string): boolean {
        const regExp = /^[0-9]{1,}([.][0-9]{1,}){1,}?$/;
        return !!value.match(regExp);
    }

    static validCardFields() {
        return (abstractControl: AbstractControl) => {

            let fieldsCheck: {
                fileFilled: boolean,
                inputsFilled: boolean
            } = {
                fileFilled: true,
                inputsFilled: true
            };

            this.checkCard(abstractControl, '', fieldsCheck);

            switch (true) {
                case fieldsCheck.fileFilled == false && fieldsCheck.inputsFilled == true:
                    return {
                        fillFile: true
                    };
                case fieldsCheck.fileFilled == true && fieldsCheck.inputsFilled == false:
                    return {
                        fillFields: true
                    };
                default:
                    return null;
            }
        }
    }

    private static checkCard(abstractControl: any, controlName: string, fieldsCheck: any) {

        switch (true) {
            case abstractControl instanceof FormGroup:
                Object.keys(abstractControl.controls).forEach(key => {
                    this.checkCard(abstractControl.get(key), key, fieldsCheck);
                });
                break;
            case abstractControl instanceof FormArray:

                let index = 0;

                for (const control of abstractControl.controls) {
                    this.checkCard(control, index.toString(), fieldsCheck);
                    index++;
                }
                break;
            default:
                if (abstractControl.value == '' || abstractControl.value == null) {

                    if (controlName == 'file') {
                        fieldsCheck.fileFilled = false;
                    } else {
                        if (controlName != 'reimbursement') {
                            fieldsCheck.inputsFilled = false;
                        }
                    }
                }
        }
    }

    static atLeastOneMustBeTrue(applicantFormComponent: ApplicantFormComponent) {
        return () => {
            const applicantReactiveForm = applicantFormComponent.applicantReactiveForm;
            if (applicantReactiveForm) {
                const applicantExpenses = applicantFormComponent.applicantExpenses;
                const expensesForChildren = applicantFormComponent.expensesForChildren;
                const expensesForPartner = applicantFormComponent.expensesForPartner;

                if (
                    (applicantExpenses.value == 'true') ||
                    (expensesForChildren.value == 'true') ||
                    (expensesForPartner.value == 'true')
                ) {
                    return null;
                }

                return {
                    atLeastOneMustBeTrue: true
                };
            }

            return null;
        };
    }

    static maxTotalSize(applicantFormComponent: ApplicantFormComponent, maxTotalSize: number, checkChildren: boolean, checkPartner: boolean): any {
        return () => {
            const applicantReactiveForm = applicantFormComponent.applicantReactiveForm;
            if (applicantReactiveForm) {
                const applicantAndFundsGroup = applicantFormComponent.applicantAndFundsGroup;
                if (applicantAndFundsGroup) {
                    const applicantExpenses = applicantFormComponent.applicantExpenses;
                    const expensesForChildren = applicantFormComponent.expensesForChildren;
                    const expensesForPartner = applicantFormComponent.expensesForPartner;
                    const expensesForChildrenArray = applicantFormComponent.expensesForChildrenArray;
                    const expensesForPartnerArray = applicantFormComponent.expensesForPartnerArray;
                    const applicantExpensesArray = applicantFormComponent.applicantExpensesArray;

                    let applicantExpensesFileSizes = (applicantExpenses.value == 'true') ? this.countFileSizes(applicantExpensesArray.value) : 0;
                    let expensesForPartnerFileSizes = (checkPartner && expensesForPartner.value == 'true') ? this.countFileSizes(expensesForPartnerArray.value) : 0;
                    let expensesForChildrenFilesSizes = (checkChildren && expensesForChildren.value == 'true') ? this.countFileSizes(expensesForChildrenArray.value) : 0;

                    if ((applicantExpensesFileSizes + expensesForChildrenFilesSizes + expensesForPartnerFileSizes) > maxTotalSize) {
                        return {
                            maxTotalSize: true
                        };
                    }
                }
            }
            return null;
        }
    }

    private static countFileSizes(serviceArray: ServiceInformation[]): number {

        let fileSizes = 0;

        let i = 1;

        for (let service of serviceArray) {
            for (let file of service.files) {
                if (file.file != null) {
                    fileSizes += file.file.size;
                }
            }
        }
        return fileSizes;
    }
}
