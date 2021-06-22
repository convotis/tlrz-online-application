import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {ErrMsgService} from "../../../core/err-msg/err-msg.service";
import {SessionTimerService} from "../../../core/session-timer/session-timer.service";

@Component({
  selector: 'tlrz-insurance-benefits',
  templateUrl: './insurance-benefits.component.html',
  styleUrls: ['./insurance-benefits.component.scss']
})
export class InsuranceBenefitsComponent {

    @Input() public servicesUsageGroup: FormGroup;

    constructor(
        private errMsgService: ErrMsgService,
        private sessionTimerService: SessionTimerService
    ) { }

    public get insuranceBenefits(): FormControl {
        return <FormControl>this.servicesUsageGroup.get('insuranceBenefits');
    }

    public get insurancePersonList(): FormArray {
        return <FormArray>this.servicesUsageGroup.get('insurancePersonList');
    }

    public setListControlsValidators() {

        for (let group of this.insurancePersonList.controls) {
            group.get('person').setValidators(Validators.required);
            group.get('person').updateValueAndValidity();
        }
    }

    public removeListControlsValidators() {

        for (let group of this.insurancePersonList.controls) {
            group.get('person').setValidators(null);
            group.get('person').updateValueAndValidity();
        }
    }

    private removeFromList(controlIndex: number){

        if (this.insurancePersonList.length > 1) {
            this.insurancePersonList.removeAt(controlIndex);
        }
    }

    private addToList(controlIndex: number) {
        if (Liferay && Liferay.Session) {
            Liferay.Session.extend();
        }
        this.sessionTimerService.resetTimer();

        if (this.insurancePersonList.length < 10) {
            this.insurancePersonList.insert(controlIndex,
                new FormGroup({
                    person: new FormControl(null, Validators.required)
                })
            )
        }
    }
}
