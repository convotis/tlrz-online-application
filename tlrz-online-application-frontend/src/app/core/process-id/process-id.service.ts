import {Injectable} from '@angular/core';

/* TODO: rename class to more general name */
@Injectable({
    providedIn: 'root'
})
export class ProcessIdService {

    constructor() { }

    public static generateString(length: number): string {
        const characters ='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

        let result = ' ';
        const charactersLength = characters.length;
        for ( let i = 0; i < length; i++ ) {
            result += characters.charAt(Math.floor(Math.random() * charactersLength));
        }

        return result;
    }

}





