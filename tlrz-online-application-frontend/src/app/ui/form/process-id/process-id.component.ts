import {Component} from "@angular/core";
import {ProcessIdService} from "../../../core/process-id/process-id.service";

@Component({
    selector: 'tlrz-process-id',
    templateUrl: './process-id.component.html',
    styleUrls: ['./process-id.component.scss']
})
export class ProcessIdComponent {

    constructor() {
    }

    public get processId(): string {
        let currentProcessId = localStorage.getItem('processId');

        if (currentProcessId != null) {
            return currentProcessId;
        }

        currentProcessId = ProcessIdService.generateString(10);

        localStorage.setItem('processId', currentProcessId);

        return currentProcessId;
    }
}
