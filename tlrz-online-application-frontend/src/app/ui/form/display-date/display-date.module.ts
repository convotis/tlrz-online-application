import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {ReactiveFormsModule} from "@angular/forms";

import {MomentModule} from 'ngx-moment';

import {DisplayDateComponent} from './display-date.component';

@NgModule({
    declarations: [DisplayDateComponent],
    exports: [DisplayDateComponent],
    imports: [MomentModule, ReactiveFormsModule, CommonModule]
})
export class DisplayDateModule {
}
