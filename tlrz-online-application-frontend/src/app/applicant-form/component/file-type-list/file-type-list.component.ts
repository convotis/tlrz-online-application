import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

import {ErrMsgService} from "../../../core/err-msg/err-msg.service";
import {CustomValidator} from "../../../core/custom-validator/custom-validator";

@Component({
  selector: 'tlrz-file-type-list',
  templateUrl: './file-type-list.component.html',
  styleUrls: ['./file-type-list.component.scss']
})
export class FileTypeListComponent {

    @Input() parentGroup: FormGroup;
    @Input() group1: FormGroup;
    @Input() group2: FormGroup;
    @Input() uniqueId: string;

    constructor(
        public errMsgService: ErrMsgService,
    ) {
    }

    public get files(): FormArray {
        return <FormArray>this.parentGroup.get('files');
    }

    public addFileToFileArray(array: FormArray, fileIndex: number) {

        /* TODO: move magic numbers to enum */
        if (array.length < 50) {
            array.insert(fileIndex, new FormGroup({
                file: new FormControl(null, [
                    Validators.required,
                    CustomValidator.requiredFileExtension(".pdf, .jpg, .jpeg, .gif, .bmp, .png"),
                    CustomValidator.maxSize(10485760)]
                ),
                type: new FormControl(null, Validators.required)
            }));

            this.parentGroup.updateValueAndValidity();
            if (this.group1) {
                this.group1.updateValueAndValidity();
            }
            if (this.group2) {
                this.group2.updateValueAndValidity();
            }
        }
    }

    public removeFileFromFileArray(array: FormArray, fileIndex: number) {

        if (array.length > 1) {
            array.removeAt(fileIndex);

            this.parentGroup.updateValueAndValidity();
            if (this.group1) {
                this.group1.updateValueAndValidity();
            }
            if (this.group2) {
                this.group2.updateValueAndValidity();
            }
        }
    }
}
