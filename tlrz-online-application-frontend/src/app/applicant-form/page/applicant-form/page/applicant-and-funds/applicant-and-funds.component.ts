import {Component, Input} from '@angular/core';
import {FormGroup} from "@angular/forms";
import {BsModalRef, BsModalService, ModalOptions} from "ngx-bootstrap";
import {WizardComponent} from "angular-archwizard";

import {ReturnModalComponent} from "../../../../../ui/modal/return-modal/return-modal.component";
import {FormService} from "../../../../../core/form/form.service";
import {ErrMsgService} from "../../../../../core/err-msg/err-msg.service";

@Component({
    selector: 'tlrz-applicant-and-funds',
    templateUrl: './applicant-and-funds.component.html',
    styleUrls: ['./applicant-and-funds.component.scss']
})
export class ApplicantAndFundsComponent {

    @Input() public applicantAndFundsGroup: FormGroup;
    public showSubmitError: boolean = false;
    @Input() private wizard: WizardComponent;
    private bsModalRef: BsModalRef;

    constructor(
        private modalService: BsModalService,
        private formService: FormService,
        public errMsgService: ErrMsgService,
    ) {
    }

    public openReturnModal() {
        const config: ModalOptions = {
            class: 'return-modal',
            backdrop: 'static',
            keyboard: false
        };
        this.bsModalRef = this.modalService.show(ReturnModalComponent, config);
    }

    public submit() {

        this.showSubmitError = true;

        if (this.applicantAndFundsGroup.valid) {
            this.showSubmitError = false;
            localStorage.setItem('activePageIndex', (this.wizard.currentStepIndex + 1).toString());
            this.wizard.goToNextStep();
            this.scrollToTop();
        } else {
            this.applicantAndFundsGroup.markAllAsTouched();
            this.formService.markAllAsDirty(this.applicantAndFundsGroup);
        }
    }

    public scrollToTop(): void {
        var contentElement = document.getElementById('content');
        if (contentElement) {
            contentElement.scrollIntoView();
        }
    }
}
