import {Component, Input} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";

import {ErrMsgService} from "../../../core/err-msg/err-msg.service";

@Component({
  selector: 'tlrz-applicant-details',
  templateUrl: './applicant-details.component.html',
  styleUrls: ['./applicant-details.component.scss']
})
export class ApplicantDetailsComponent {

    @Input() public applicantAndFundsGroup: FormGroup;

    constructor(
        public errMsgService: ErrMsgService,
    ) {}

    public get personalNumber(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('personalNumber');
    }

    public get privateEmail(): FormControl {
        return <FormControl>this.applicantAndFundsGroup.get('privateEmail');
    }
}
