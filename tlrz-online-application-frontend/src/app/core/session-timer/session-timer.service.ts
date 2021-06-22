import {EventEmitter, Injectable} from '@angular/core';
import {applicationContext} from "../../../environments/environment";

@Injectable({
    providedIn: 'root'
})
export class SessionTimerService {

    private sessionAliveUntil;

    constructor() {
        this.resetTimer();
    }

    ngOnInit(){
        setInterval(()=>{this.remainingTime = 0}, 1000);
    }

    public sessionTimerReset = new EventEmitter<Object>();

    public resetTimer(): void {
        this.sessionAliveUntil = Date.now() + applicationContext.sessionLength;

        this.remainingTime = 0;

        this.sessionTimerReset.emit("");
    }

    public get remainingTime(): number {
        return this.sessionAliveUntil - Date.now()
    }

    public set remainingTime(remain: number) {
        //do nothing
    }

    public get sessionValid(): boolean {
        return (this.remainingTime > 0);
    }
}
