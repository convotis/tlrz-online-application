import {Component, Input} from '@angular/core';
import {FormGroup} from "@angular/forms";

import {WizardComponent} from "angular-archwizard";
import {FormService} from "../../../../../core/form/form.service";
import {ErrMsgService} from "../../../../../core/err-msg/err-msg.service";

@Component({
  selector: 'tlrz-services-usage',
  templateUrl: './services-usage.component.html',
  styleUrls: ['./services-usage.component.scss']
})
export class ServicesUsageComponent {

    @Input() public servicesUsageGroup: FormGroup;
    public showSubmitError: boolean = false;
    @Input() private wizard: WizardComponent;

    constructor(
        private formService: FormService,
        public errMsgService: ErrMsgService,
    ) {
    }

    public submit() {

        this.showSubmitError = true;

        if (this.servicesUsageGroup.valid) {
            this.showSubmitError = false;
            localStorage.setItem('activePageIndex', (this.wizard.currentStepIndex + 1).toString());
            this.wizard.goToNextStep();
            this.scrollToTop();
        } else {
            this.servicesUsageGroup.markAllAsTouched();
            this.formService.markAllAsDirty(this.servicesUsageGroup);
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
}
