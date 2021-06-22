import {Component, Input, OnInit} from '@angular/core';
import {formatDate} from "@angular/common";

import {BsModalRef, ProgressbarConfig} from "ngx-bootstrap";

import {FormService} from "../../../core/form/form.service";
import {applicationContext} from "../../../../environments/environment";
import {Router} from "@angular/router";

import {IndexedDatabaseService} from "../../../core/indexed-database/indexed-database.service";
import {SessionTimerService} from "../../../core/session-timer/session-timer.service";

interface Files {
    size: number;
}

interface FormDataEntrySize {
    name: string;
    low, high: number;
}

export function getProgressbarConfig(): ProgressbarConfig {
    return Object.assign(new ProgressbarConfig(), {
        animate: true,
        striped: true,
        max: 100
    });
}

export enum SubmitStep {
    submitSending = 1,
    processSending = 2,
    sendResult = 3
}

@Component({
    selector: 'tlrz-submit-modal',
    templateUrl: './submit-modal.component.html',
    styleUrls: ['./submit-modal.component.scss'],
    providers: [{provide: ProgressbarConfig, useFactory: getProgressbarConfig}]

})
export class SubmitModalComponent implements OnInit {
    step: SubmitStep;
    /* TODO: remove 'any' type */
    formData: any;
    amount: number;
    filesCount: number;

    offset: string = applicationContext.offset;
    time: string;
    rowTime: string;
    processId: string;

    files: Files = {size: 0};

    progress: number;
    pdfURL: string;
    statusinfo: string;

    pdf: Blob;

    filename: string;

    constructor(
        public bsModalRef: BsModalRef,
        public formService: FormService,
        private router: Router,
        private storageService: IndexedDatabaseService,
        private sessionTimerService: SessionTimerService
    ) {
    }

    ngOnInit(): void {

        const currentDate = new Date();

        this.time = formatDate(currentDate, 'dd.MM.yyyy', 'en-US', this.offset) +
            ', um ' +
            formatDate(currentDate, 'HH:mm', 'en-US', this.offset);
        this.rowTime = formatDate(currentDate, 'yyyy-MM-dd-HH-mm-ss-SSS', 'en-US', this.offset)

        this.statusinfo = 'Daten werden übertragen';
    }

    public submit() {

        if (Liferay.Session.get('sessionState') == 'active') {

            this.files = {size: 0};
            let formData = new FormData();
            let formSizes: Array<FormDataEntrySize> = [];

            this.formService.objectToFormData(this.formData, formData, this.files, formSizes);

            formData.append('amount', this.amount.toString());
            formData.append('filesCount', this.filesCount.toString());
            formData.append('pdfCreationTime', this.time);
            formData.append('pdfCreationRowTime', this.rowTime);

            let processId = localStorage.getItem('processId');
            formData.append('processId', processId);

            if (this.files.size <= 100000000) {

                this.progress = 0;

                let unloadEventHandler = (event) => {
                    event.preventDefault();

                    event.returnValue = 'Ihr Antrag wird noch übermittelt. Wollen Sie die Seite wirklich verlassen?';
                };

                let refreshSessionHandler = (event) => {
                    if (Liferay && Liferay.Session) {
                        Liferay.Session.extend();
                    }
                    this.sessionTimerService.resetTimer();
                }

                window.addEventListener('beforeunload', unloadEventHandler);

                let refreshSessionHandlerToken = window.setInterval(refreshSessionHandler, 60000);

                this.formService.submitForm(formData).subscribe(
                    value => {

                        this.step = 2;

                        switch (value.type) {
                            case 'progress':
                                this.progress = parseInt(value.message);

                                if (this.progress < 100) {

                                    let lastEntry = formSizes[formSizes.length - 1];

                                    let fileIndex = 0;

                                    for (let entry of formSizes) {
                                        if (entry.high < value.bytesLoaded) {
                                            if (lastEntry.high < entry.high) {
                                                lastEntry = entry;

                                                fileIndex++;
                                            }
                                        }
                                    }

                                    if (lastEntry.name !== 'Form') {
                                      this.statusinfo = 'Datei ' + (fileIndex) + ' von ' + (formSizes.length - 1) + ' wurde erfolgreich hochgeladen';
                                    }
                                } else {
                                    this.statusinfo = 'Datei ' + (formSizes.length - 1) + ' von ' + (formSizes.length - 1) + ' wurde erfolgreich hochgeladen';

                                    this.statusinfo = 'PDF-Quittung wird erstellt';
                                }

                                break;
                            case 'body':
                                this.pdf = new Blob([value.message], {type: 'application/pdf'});

                                if (this.pdf.size > 0 && value.status === 'ok') {
                                    this.pdfURL = URL.createObjectURL(this.pdf);
                                } else {
                                    this.pdfURL = "";
                                }

                                this.time = value.xCreated;
                                this.processId = value.processId;

                                if (value.contentDisposition) {
                                    if (value.contentDisposition.indexOf('filename*=') > -1) {
                                        this.filename = value.contentDisposition.split(';')[1].split('filename*')[1].split('=UTF-8\'\'')[1];
                                        this.filename = decodeURIComponent(this.filename);
                                    } else {
                                        this.filename = value.contentDisposition.split(';')[1].split('filename')[1].split('=')[1];
                                    }

                                    this.filename = this.filename.replace(/['"]+/g, '');
                                    this.filename = this.filename.trim();
                                }

                                this.step = 3;

                                window.removeEventListener('beforeunload', unloadEventHandler);

                                window.clearInterval(refreshSessionHandlerToken);

                                localStorage.clear();
                                this.storageService.clearData();

                                break;
                        }
                    }, () => {
                        this.step = 3;

                        window.removeEventListener('beforeunload', unloadEventHandler);

                        window.clearInterval(refreshSessionHandlerToken);

                        localStorage.clear();
                        this.storageService.clearData();
                    });
            }
        }
    }

    public showPdf() {
        if (this.pdfURL) {
            if (window.navigator.msSaveOrOpenBlob) {
                //IE11
                window.navigator.msSaveOrOpenBlob(this.pdf, this.filename);
            } else {
                var downloadLink = document.createElement("a");
                downloadLink.href = this.pdfURL;
                downloadLink.download = this.filename;

                document.body.appendChild(downloadLink);
                downloadLink.click();
                document.body.removeChild(downloadLink);
            }
        }
    }

    public finishSubmit() {
        if (this.pdfURL) {
            localStorage.clear();
            this.storageService.clearData();
            this.bsModalRef.hide();
            this.router.navigate(['/']);
        } else {
            this.bsModalRef.hide();
        }
    }
}
