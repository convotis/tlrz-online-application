import {Injectable} from '@angular/core';
import {HttpClient, HttpEventType} from "@angular/common/http";
import {FormArray, FormGroup} from "@angular/forms";

import {applicationContext} from "../../../environments/environment";
import {map} from "rxjs/operators";
import {Observable} from "rxjs";

interface Files {
    size: number;
}

interface FormDataEntrySize {
    name: string;
    low, high: number;
}

interface FormDataEntryHolder {
    key: string;
    value: FormDataEntryValue
}

/* TODO: move Wizard from core to 'ui' or related feature module */
@Injectable({
  providedIn: 'root'
})
export class FormService {


    constructor(
        private http: HttpClient,
    ) {
    }

    /* TODO: remove 'any' type */
    public submitForm(data: any): Observable<any> {

        return this.http.post(applicationContext.submitFormUrl, data, {
            responseType: 'blob' as 'json',
            reportProgress: true,
            observe: 'events'
        }).pipe(
            map((event) => {

                switch (event.type) {

                    case HttpEventType.UploadProgress:
                        const progress = Math.round(100 * event.loaded / event.total);
                        return {type: 'progress', message: progress, bytesLoaded: event.loaded};
                    case HttpEventType.Response:
                        return {type: 'body', message: event.body, contentDisposition: event.headers.get('content-disposition'), xCreated: event.headers.get('x-created'), processId: event.headers.get('x-processId'), status: event.headers.get('x-status')};
                    default:
                        return {type: 'unhandled', message: `Unhandled event: ${event.type}`};
                }
            })
        )
    }

    public objectToFormData(formValue: any, formData: FormData, files: Files, formSizes: Array<FormDataEntrySize>) {
        const BOUNDARY_SIZE = 60;
        const CONTENT_DISPOSITION_SIZE = 39;

        let filesFormDataHolder: Array<FormDataEntryHolder> = [];

        let formSize = 0;
        let lastLow = 0;

        this.objectToFormDataInternal(formValue, formData, files, filesFormDataHolder, (value: FormDataEntryValue, key: string) => {
            let size = BOUNDARY_SIZE + CONTENT_DISPOSITION_SIZE;
            size += 6 //CR LF * 3
            size += key.length;

            if (typeof value === "string") {
                formSize += (size + value.length);
            }
        });

        for (let formDataEntryHolder of filesFormDataHolder) {
            let value = formDataEntryHolder.value as File;

            formData.append(formDataEntryHolder.key, value);

            let size = BOUNDARY_SIZE + CONTENT_DISPOSITION_SIZE;
            size += 6 //CR LF * 3
            size += formDataEntryHolder.key.length;

            if (lastLow === 0) {
                lastLow = formSize + 1;
            }

            let fileSize = size + value.size;

            let fileDataSize : FormDataEntrySize = { name: value.name, low: lastLow, high: lastLow + fileSize};

            lastLow += (fileSize + 1)

            formSizes.push(fileDataSize);
        }

        let formDataSize : FormDataEntrySize = { name: 'Form', low: 0, high: formSize};
        formSizes.push(formDataSize);
    }

    public objectToFormDataInternal(formValue: any, formData: FormData, files: Files, filesFormDataHolder: Array<FormDataEntryHolder>, callback: (value: FormDataEntryValue, key: string) => void, namespace?: string, index?: number) {

        let formKey;

        for(const property in formValue) {
            if(formValue.hasOwnProperty(property)) {

                if (namespace) {
                    formKey = namespace + (index ? '-' + index : '') + '-' + property;
                } else {
                    formKey = property;
                }

                switch (true) {
                    case typeof formValue[property] === 'object' && !(formValue[property] instanceof File) && !(formValue[property] instanceof Array):
                        this.objectToFormDataInternal(formValue[property], formData, files, filesFormDataHolder, callback, formKey);
                        break;
                    case typeof formValue[property] === 'object' && !(formValue[property] instanceof File) && (formValue[property] instanceof Array):

                        let arrayIndex = 1;

                        for (const arrayValue of formValue[property]) {
                            this.objectToFormDataInternal(arrayValue, formData, files, filesFormDataHolder, callback, formKey, arrayIndex);
                            arrayIndex++
                        }
                        break;
                    default:
                        if (formValue[property] instanceof File) {
                            files.size += formValue[property].size;

                            filesFormDataHolder.push({key: formKey, value: formValue[property]});
                        } else {
                            formData.append(formKey, formValue[property]);

                            callback.apply(null, [formValue[property], formKey]);
                        }
                }
            }
        }
    };

    public markAllAsDirty(abstractControl: any) {

        switch (true) {
            case abstractControl instanceof FormGroup:
                Object.keys(abstractControl.controls).forEach(key => {
                    this.markAllAsDirty(abstractControl.get(key));
                });
                break;
            case abstractControl instanceof FormArray:
                for (const control of abstractControl.controls) {
                    this.markAllAsDirty(control);
                }
                break;
            default:
                abstractControl.markAsDirty();
        }
    };
}
