import {BrowserModule} from '@angular/platform-browser';
import {APP_BASE_HREF} from "@angular/common";
import {NgModule} from '@angular/core';
import {AgGridModule} from 'ag-grid-angular';
import {BsModalRef, ModalModule, ProgressbarConfig, ProgressbarModule, TooltipContainerComponent} from "ngx-bootstrap";

import {AppRoutingModule} from './app-routing.module';
import {CoreModule} from './core/core.module';
import {AppComponent} from './app.component';

import {ReturnModalComponent} from "./ui/modal/return-modal/return-modal.component";
import {HomeModalComponent} from "./ui/modal/home-modal/home-modal.component";
import {SubmitModalComponent} from './ui/modal/submit-modal/submit-modal.component';

@NgModule({
    declarations: [
        ReturnModalComponent,
        HomeModalComponent,
        AppComponent,
        SubmitModalComponent,
    ],
    imports: [
        ModalModule.forRoot(),
        ProgressbarModule,
        AppRoutingModule,
        BrowserModule,
        CoreModule,
        AgGridModule.withComponents([])
    ],
    providers: [
        {
            provide: APP_BASE_HREF,
            useValue: '/'
        },
        ProgressbarConfig,
        BsModalRef,
    ],
    entryComponents: [
        TooltipContainerComponent,
        ReturnModalComponent,
        HomeModalComponent,
        SubmitModalComponent
    ],
    bootstrap: [AppComponent],
})
export class AppModule { }
