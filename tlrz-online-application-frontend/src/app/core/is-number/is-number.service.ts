import {Injectable} from '@angular/core';

/* TODO: rename class to more general name */
@Injectable({
    providedIn: 'root'
})
export class IsNumberService {

    constructor() { }

    public static isNumber(value: string): boolean {
        return typeof parseFloat(value) == "number" && isNaN(parseFloat(value)) === false;
    }
}
