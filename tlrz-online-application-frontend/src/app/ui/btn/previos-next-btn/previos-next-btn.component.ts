import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

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

  constructor() { }

  ngOnInit() {
  }

  previousBtn(): void {
      Liferay.Session.extend();

      this.previous.emit();
  }

  nextBtn(): void {
      Liferay.Session.extend();

      this.next.emit();
  }

}
