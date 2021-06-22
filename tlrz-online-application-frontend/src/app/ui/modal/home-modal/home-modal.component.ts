import {Component} from '@angular/core';
import {Router} from "@angular/router";

import {BsModalRef} from 'ngx-bootstrap';

import {applicationContext} from "../../../../environments/environment";

import {IndexedDatabaseService} from "../../../core/indexed-database/indexed-database.service";
import {SessionTimerService} from "../../../core/session-timer/session-timer.service";

@Component({
  selector: 'app-home-modal',
  templateUrl: './home-modal.component.html',
  styleUrls: ['./home-modal.component.scss']
})
export class HomeModalComponent {

    public toTheOnlineApplicationLink: string = applicationContext.toTheOnlineApplicationLink;
    public informationAboutGrantLink: string = applicationContext.informationAboutGrantLink;
    public privacyInformationLink: string = applicationContext.privacyInformationLink;

    constructor(
        public bsModalRef: BsModalRef,
        private router: Router,
        private storageService: IndexedDatabaseService,
        private sessionTimerService: SessionTimerService
    ) {
    }

    public navigateToApplicantForm() {
        localStorage.clear();
        this.storageService.clearData();
        this.bsModalRef.hide();
        this.router.navigate(['/antrag']);
        this.sessionTimerService.resetTimer();
    }

    public openLinkNewTab(link: string) {
        window.open(link, "_blank");
    }
}
