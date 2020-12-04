import {Component, Input} from '@angular/core';
import {Observable} from "rxjs";

@Component({
  selector: 'app-total',
  templateUrl: './total.component.html',
  styleUrls: ['./total.component.scss']
})
export class TotalComponent {

    @Input() amount: Observable<number>;
    @Input() filesCount: Observable<number>;

    constructor() {
    }
}
