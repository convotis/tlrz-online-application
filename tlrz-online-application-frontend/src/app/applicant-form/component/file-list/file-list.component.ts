import {Component, Input} from "@angular/core";
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";
import {ErrMsgService} from "../../../core/err-msg/err-msg.service";
import {CustomValidator} from "../../../core/custom-validator/custom-validator";

@Component({
    selector: 'tlrz-file-list',
    templateUrl: './file-list.component.html',
    styleUrls: ['./file-list.component.scss']
})
export class FileListComponent {

    @Input() parentGroup: FormGroup;
    @Input() uniqueId: string;

    constructor(
        public errMsgService: ErrMsgService,
    ) {
    }

    public get files(): FormArray {
        return <FormArray>this.parentGroup.get('files');
    }

    public get confirmation(): FormControl {
        return <FormControl>this.parentGroup.get('confirmation');
    }

    public updateValidity() {
        this.confirmation.updateValueAndValidity();
    }

    public addFileToFileArray(array: FormArray, fileIndex: number) {
        /* TODO: move magic numbers to enum */
        if (array.length < 20) {
            array.insert(fileIndex, new FormGroup({
                file: new FormControl(null, [
                    CustomValidator.requiredFileExtension(".pdf, .jpg, .jpeg, .gif, .bmp, .png"),
                    CustomValidator.maxSize(10485760)
                    ]
                )
            }));

            this.confirmation.updateValueAndValidity();

            this.parentGroup.updateValueAndValidity();
        }
    }

    public removeFileFromFileArray(array: FormArray, fileIndex: number) {

        if (array.length > 1) {
            array.removeAt(fileIndex);

            this.confirmation.updateValueAndValidity();

            this.parentGroup.updateValueAndValidity();
        }
    }

}

