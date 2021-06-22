import {Component} from "@angular/core";
import {SessionTimerService} from "../../../core/session-timer/session-timer.service";
import {BsModalRef, BsModalService, ModalOptions} from "ngx-bootstrap";
import {SessionTimeoutModalComponent} from "../../modal/session-timeout-modal/session-timeout-modal-component";

@Component({
    selector: 'tlrz-session-timer',
    templateUrl: './session-timer.component.html',
    styleUrls: ['./session-timer.component.scss']
})
export class SessionTimerComponent {

    private bsModalRef: BsModalRef;

    constructor(private sessionTimerService: SessionTimerService, private modalService: BsModalService) {
        this.remainingTime = "";
    }

    ngOnInit(){
        setInterval(()=>{this.remainingTime = ""}, 1000);

        this.sessionTimerService.sessionTimerReset.subscribe(
            v => {
                this.remainingTime = "";
            }
        )
    }

    public get remainingTime(): string {
        return this.millisToMinutesAndSeconds(this.sessionTimerService.remainingTime )
    }

    public set remainingTime(remain: string) {
        if (! this.sessionTimerService.sessionValid) {
            this.openSessionTimeoutModal();
        }
    }

    public get warningLevel(): boolean {
        return (this.sessionTimerService.remainingTime < 60000);
    }

    public get cssClass(): string {
        let value = 'session-timer';

        if (this.warningLevel) {
            value += ' warn';
        }

        return value
    }

    public extendSession(): void {
        if (Liferay && Liferay.Session) {
            Liferay.Session.extend();
        }
        this.sessionTimerService.resetTimer();
    }

    private millisToMinutesAndSeconds(millis: number): string {
        let minutes = Math.floor((millis / 1000 / 60) % 60);
        let minutesStr = minutes < 0 ? "00" : (minutes < 10 ? "0" : "") + minutes;
        let seconds = Math.floor((millis / 1000) % 60);
        let secondsStr = seconds < 0 ? "00" : (seconds < 10 ? "0" : "") + seconds;
        return minutesStr + ":" + secondsStr;
    }

    private openSessionTimeoutModal() {
        if (! this.bsModalRef) {
            const config: ModalOptions = {
                class: 'session-timeout-modal',
                backdrop: 'static',
                keyboard: false
            };
            this.bsModalRef = this.modalService.show(SessionTimeoutModalComponent, config);
        }
    }
}
