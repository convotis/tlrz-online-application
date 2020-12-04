import {AbstractControl} from "@angular/forms";
import {Injectable} from '@angular/core';
import {ErrMsg} from "./err-msg.enum";

@Injectable({
  providedIn: 'root'
})
export class ErrMsgService {

    constructor() { }

    getErrMsg(control: AbstractControl): string {
        return control.errors ?
            ErrMsg[Object.keys(control.errors)[0]] : '';
    }

    getErrMsgByKey(messageKey: string): string {
        return ErrMsg[messageKey];
    }
}
