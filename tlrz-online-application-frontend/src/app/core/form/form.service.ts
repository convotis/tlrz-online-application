import {Injectable} from '@angular/core';
import {HttpClient, HttpEventType} from "@angular/common/http";
import {FormArray, FormGroup} from "@angular/forms";

import {applicationContext} from "../../../environments/environment";
import {map} from "rxjs/operators";
import {Observable} from "rxjs";

interface Files {
    size: number;
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
                        return {type: 'progress', message: progress};
                    case HttpEventType.Response:
                        return {type: 'body', message: event.body, contentDisposition: event.headers.get('content-disposition'), xCreated: event.headers.get('x-created'), status: event.headers.get('x-status')};
                    default:
                        return {type: 'unhandled', message: `Unhandled event: ${event.type}`};
                }
            })
        )
    }

    public objectToFormData(formValue: any, formData: FormData, files: Files, namespace?: string, index?: number) {

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
                        this.objectToFormData(formValue[property], formData, files, formKey);
                        break;
                    case typeof formValue[property] === 'object' && !(formValue[property] instanceof File) && (formValue[property] instanceof Array):

                        let arrayIndex = 1;

                        for (const arrayValue of formValue[property]) {
                            this.objectToFormData(arrayValue, formData, files, formKey, arrayIndex);
                            arrayIndex++
                        }
                        break;
                    default:
                        if (formValue[property] instanceof File) {
                            files.size += formValue[property].size;
                        }
                        formData.append(formKey, formValue[property]);
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
