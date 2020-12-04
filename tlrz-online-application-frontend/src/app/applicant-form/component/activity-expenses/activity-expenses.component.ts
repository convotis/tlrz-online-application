import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {ErrMsgService} from "../../../core/err-msg/err-msg.service";

@Component({
  selector: 'tlrz-activity-expenses',
  templateUrl: './activity-expenses.component.html',
  styleUrls: ['./activity-expenses.component.scss']
})
export class ActivityExpensesComponent {

    @Input() public servicesUsageGroup: FormGroup;

    constructor(
        private errMsgService: ErrMsgService,
    ) { }

    public get activityExpenses(): FormControl {
        return <FormControl>this.servicesUsageGroup.get('activityExpenses');
    }

    public get activityPersonList(): FormArray {
        return <FormArray>this.servicesUsageGroup.get('activityPersonList');
    }

    public setListControlsValidators() {

        for (let group of this.activityPersonList.controls) {
            group.get('person').setValidators(Validators.required);
            group.get('person').updateValueAndValidity();
        }
    }

    public removeListControlsValidators() {

        for (let group of this.activityPersonList.controls) {
            group.get('person').setValidators(null);
            group.get('person').updateValueAndValidity();
        }
    }

    private removeFromList(controlIndex: number) {

        if (this.activityPersonList.length > 1) {
            this.activityPersonList.removeAt(controlIndex);
        }
    }

    private addToList(controlIndex: number) {
        Liferay.Session.extend();

        if (this.activityPersonList.length < 10) {
            this.activityPersonList.insert(controlIndex,
                new FormGroup({
                    person: new FormControl(null, Validators.required)
                })
            )
        }
    }
}
