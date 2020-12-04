import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {ErrMsgService} from "../../../core/err-msg/err-msg.service";

@Component({
  selector: 'tlrz-illness-expenses',
  templateUrl: './illness-expenses.component.html',
  styleUrls: ['./illness-expenses.component.scss']
})
export class IllnessExpensesComponent {

    @Input() public servicesUsageGroup: FormGroup;

    constructor(
        private errMsgService: ErrMsgService,
    ) { }

    public get illnessExpenses(): FormControl {
        return <FormControl>this.servicesUsageGroup.get('illnessExpenses');
    }

    public get illnessPersonList(): FormArray {
        return <FormArray>this.servicesUsageGroup.get('illnessPersonList');
    }

    public setListControlsValidators() {

        for (let group of this.illnessPersonList.controls) {
            group.get('person').setValidators(Validators.required);
            group.get('person').updateValueAndValidity();
        }
    }

    public removeListControlsValidators() {

        for (let group of this.illnessPersonList.controls) {
            group.get('person').setValidators(null);
            group.get('person').updateValueAndValidity();
        }
    }

    private removeFromList(controlIndex: number){

        if (this.illnessPersonList.length > 1) {
            this.illnessPersonList.removeAt(controlIndex);
        }
    }

    private addToList(controlIndex: number) {
        Liferay.Session.extend();

        if (this.illnessPersonList.length < 10) {
            this.illnessPersonList.insert(controlIndex,
                new FormGroup({
                    person: new FormControl(null, Validators.required)
                })
            )
        }
    }
}
