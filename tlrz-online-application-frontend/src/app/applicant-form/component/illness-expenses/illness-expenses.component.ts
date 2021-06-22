import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {ErrMsgService} from "../../../core/err-msg/err-msg.service";
import {SessionTimerService} from "../../../core/session-timer/session-timer.service";

@Component({
  selector: 'tlrz-illness-expenses',
  templateUrl: './illness-expenses.component.html',
  styleUrls: ['./illness-expenses.component.scss']
})
export class IllnessExpensesComponent {

    @Input() public servicesUsageGroup: FormGroup;

    constructor(
        private errMsgService: ErrMsgService,
        private sessionTimerService: SessionTimerService
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
        if (Liferay && Liferay.Session) {
            Liferay.Session.extend();
        }
        this.sessionTimerService.resetTimer();

        if (this.illnessPersonList.length < 10) {
            this.illnessPersonList.insert(controlIndex,
                new FormGroup({
                    person: new FormControl(null, Validators.required)
                })
            )
        }
    }
}
