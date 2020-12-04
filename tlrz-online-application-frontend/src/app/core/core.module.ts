import {NgModule, Optional, SkipSelf} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";

import {CustomValidator} from "./custom-validator/custom-validator";
import {throwIfAlreadyLoaded} from './module-import-guard';
import {ErrMsgService} from "./err-msg/err-msg.service";
import {IsNumberService} from "./is-number/is-number.service";
import {FormService} from "./form/form.service";
import {IndexedDatabaseService} from "./indexed-database/indexed-database.service";

@NgModule({
    declarations: [],
    imports: [
        ReactiveFormsModule,
        HttpClientModule,
        CommonModule,
    ],
    providers: [
        CustomValidator,
        ErrMsgService,
        IsNumberService,
        FormService,
        IndexedDatabaseService
    ]
})
export class CoreModule {
  constructor( @Optional() @SkipSelf() parentModule: CoreModule ) {
    throwIfAlreadyLoaded(parentModule, 'CoreModule');
  }
}
