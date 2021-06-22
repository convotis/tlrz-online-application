import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {SessionTimerService} from "../../../core/session-timer/session-timer.service";

@Component({
  selector: 'tlrz-previos-next-btn',
  templateUrl: './previos-next-btn.component.html',
  styleUrls: ['./previos-next-btn.component.scss']
})
export class PreviosNextBtnComponent implements OnInit {

    @Input() disabledNextBtn: boolean;
    @Input() nextLabel: string;

    @Output() previous: EventEmitter<void> = new EventEmitter<void>();
    @Output() next: EventEmitter<void> = new EventEmitter<void>();

  constructor(private sessionTimerService: SessionTimerService) {

  }

  ngOnInit() {
  }

  previousBtn(): void {
      if (Liferay && Liferay.Session) {
          Liferay.Session.extend();
      }
      this.sessionTimerService.resetTimer();

      this.previous.emit();
  }

  nextBtn(): void {
      if (Liferay && Liferay.Session) {
          Liferay.Session.extend();
      }
      this.sessionTimerService.resetTimer();

      this.next.emit();
  }

  public get nextButtonDisabled(): boolean {
      return this.disabledNextBtn || (! this.sessionTimerService.sessionValid);
  }

}
