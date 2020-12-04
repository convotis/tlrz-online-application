import { Component } from '@angular/core';
import { Router } from "@angular/router";

import { BsModalRef } from "ngx-bootstrap";

import {IndexedDatabaseService} from "../../../core/indexed-database/indexed-database.service";

@Component({
  selector: 'tlrz-return-modal',
  templateUrl: './return-modal.component.html',
  styleUrls: ['./return-modal.component.scss']
})
export class ReturnModalComponent {

    constructor(
        public bsModalRef: BsModalRef,
        private router: Router,
        private storageService: IndexedDatabaseService
    ) { }

    public navigateToHome() {
        this.bsModalRef.hide();
        this.router.navigate(['/home']);
        localStorage.clear();
        this.storageService.clearData();
    }
}
