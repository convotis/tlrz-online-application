import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {DisplayCurrencyComponent} from './display-currency.component';
import {ReactiveFormsModule} from "@angular/forms";

@NgModule({
    declarations: [DisplayCurrencyComponent],
    imports: [
        CommonModule,
        ReactiveFormsModule
    ],
    exports: [DisplayCurrencyComponent]
})
export class DisplayCurrencyModule { }
