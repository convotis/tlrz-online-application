import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ExpandedRadioBtnComponent } from './expanded-radio-btn.component';

@NgModule({
    declarations: [ExpandedRadioBtnComponent],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule
    ],
    exports: [ExpandedRadioBtnComponent]
})
export class ExpandedRadioBtnModule {
}
