import {Component} from '@angular/core';

import {BsModalRef, BsModalService, ModalOptions} from "ngx-bootstrap";

import {HomeModalComponent} from "../../../ui/modal/home-modal/home-modal.component";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent {

    private bsModalRef: BsModalRef;

    constructor(
        private modalService: BsModalService,
    ) {
    }

    public openHomeModal() {
        const config: ModalOptions = {
            backdrop: 'static',
            keyboard: false
        };


        this.bsModalRef = this.modalService.show(HomeModalComponent, config);
    }
}
