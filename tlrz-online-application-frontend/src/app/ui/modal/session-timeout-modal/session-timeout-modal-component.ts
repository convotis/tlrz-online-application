import { Component } from '@angular/core';
import { Router } from "@angular/router";

import { BsModalRef } from "ngx-bootstrap";

import {IndexedDatabaseService} from "../../../core/indexed-database/indexed-database.service";
import {applicationContext} from "../../../../environments/environment";

@Component({
    selector: 'tlrz-session-timeout-modal',
    templateUrl: './session-timeout-modal.component.html',
    styleUrls: ['./session-timeout-modal.component.scss']
})
export class SessionTimeoutModalComponent {

    public sessionTimeoutTitle: string = applicationContext.sessionTimeoutTitle;
    public sessionTimeoutText: string = applicationContext.sessionTimeoutText;
    public sessionTimeoutButton: string = applicationContext.sessionTimeoutButton;

    constructor(
        public bsModalRef: BsModalRef,
        private router: Router,
        private storageService: IndexedDatabaseService
    ) { }

    public navigateToHome() {
        this.bsModalRef.hide();
        localStorage.clear();
        this.storageService.clearData();

        if (Liferay && Liferay.Session) {
            Liferay.Session.expire();
        }

        window.location.href = applicationContext.sessionTimeoutLink;
    }
}
