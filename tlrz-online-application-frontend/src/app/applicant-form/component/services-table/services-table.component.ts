import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {DatatableComponent} from '@swimlane/ngx-datatable';
import {ColDef} from 'ag-grid-community';
import {takeUntil} from 'rxjs/operators';
import {Section} from './shared/section';

import {FileInfo, ServicesTableColumns} from './shared/services-table-columns';
import {DateFormat} from "../../../ui/form/display-date/shared/date-format";
import {columnDefs} from './shared/services-table-settings';
import {AutoUnsubscribe} from "../../../core/auto-unsubscribe/auto-unsubscribe";
import {AgGridAngular} from "ag-grid-angular";

@Component({
    selector: 'app-services-table',
    templateUrl: './services-table.component.html',
    styleUrls: ['./services-table.component.scss']
})
export class ServicesTableComponent extends AutoUnsubscribe implements OnInit {

    /* TODO: remove 'any' type */
    @ViewChild('summaryGrid', {static: false}) summaryGrid: AgGridAngular;
    @ViewChild('theNgxDatatable', {static: false}) table: DatatableComponent;

    /* TODO: put all inputs inside one FormGroup */
    @Input() applicantExpensesRadio: FormControl;
    @Input() applicantExpensesGroup: FormGroup;
    @Input() expensesForChildrenGroup: FormGroup;
    @Input() expensesForPartnerGroup: FormGroup;
    @Input() expensesForChildrenRadio: FormControl;
    @Input() expensesForPartnerRadio: FormControl;

    public data: ServicesTableColumns[] = [];
    public totalData: ServicesTableColumns[] = [{
        sectionIndex: 4,
        sectionTitle: 'Title',
        number: 'Summe der Rechnungsbeträge',
        invoiceDate: '',
        serviceType: '',
        invoiceAmount: '0',
        reimbursement: '',
        fileInfoList: [{
            file: '',
            type: ''
        }]
    }];

    public columnDefs: ColDef[] = columnDefs;
    public defaultColDef = { resizable: false };
    private gridApi;
    private gridColumnApi;

    public get applicantExpensesArray(): FormArray {
        return <FormArray>this.applicantExpensesGroup.get('applicantExpensesArray');
    }

    public get expensesForChildrenArray(): FormArray {
        return <FormArray>this.expensesForChildrenGroup.get('expensesForChildrenArray');
    }

    public get expensesForPartnerArray(): FormArray {
        return <FormArray>this.expensesForPartnerGroup.get('expensesForPartnerArray');
    }

    constructor() {
        super();
    }

    public ngOnInit(): void {

        this.applicantExpensesArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(applicantExpensesArray => {
            if (this.applicantExpensesRadio.value == 'true') {
                this.addTableSection({
                    sectionIndex: 1,
                    title: 'Antragsteller',
                    rows: applicantExpensesArray
                });
            }
        });

        this.expensesForChildrenArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(expensesForChildrenArray => {

            if (this.expensesForChildrenRadio.value == 'true') {

                this.addTableSection({
                    sectionIndex: 3,
                    title: 'Kinder',
                    rows: expensesForChildrenArray
                });
            }
        });

        this.expensesForPartnerArray.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(expensesForPartnerArray => {

            if (this.expensesForPartnerRadio.value == 'true') {

                this.addTableSection({
                    sectionIndex: 2,
                    title: 'Ehegatte',
                    rows: expensesForPartnerArray
                });
            }
        });

        this.applicantExpensesRadio.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
            radioValue == 'true' ?
                this.addTableSection({
                    sectionIndex: 1,
                    title: 'Antragsteller',
                    rows: this.applicantExpensesArray.value
                }) :
                this.removeTableSection('Antragsteller');
        });

        this.expensesForChildrenRadio.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
            radioValue == 'true' ?
                this.addTableSection({
                    sectionIndex: 3,
                    title: 'Kinder',
                    rows: this.expensesForChildrenArray.value
                }) :
                this.removeTableSection('Kinder');
        });

        this.expensesForPartnerRadio.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(radioValue => {
            radioValue == 'true' ?
                this.addTableSection({
                    sectionIndex: 2,
                    title: 'Ehegatte',
                    rows: this.expensesForPartnerArray.value
                }) :
                this.removeTableSection('Ehegatte');
        });
    }

    public onColumnResized() {

        this.gridApi.resetRowHeights();
    }

    public getRowHeight(params) {
        if (params.data) {
            if (params.data.rowGroup) {
                return 35;
            } else if (params.data.number == 'Summe der Rechnungsbeträge') {
                return 55;
            } else if (params.data.fileInfoList) {
                return params.data.fileInfoList.length * 28;
            }
        }
        return 28;
    }

    public onGridReady(params) {

        this.gridApi = params.api;
        this.gridColumnApi = params.columnApi;

        this.applicantExpensesRadio.value == 'true' ?
            this.addTableSection({
                sectionIndex: 1,
                title: 'Antragsteller',
                rows: this.applicantExpensesArray.value
            }) :
            this.removeTableSection('Antragsteller');

        this.expensesForChildrenRadio.value == 'true' ?
            this.addTableSection({
                sectionIndex: 3,
                title: 'Kinder',
                rows: this.expensesForChildrenArray.value
            }) :
            this.removeTableSection('Kinder');

        this.expensesForPartnerRadio.value == 'true' ?
            this.addTableSection({
                sectionIndex: 2,
                title: 'Ehegatte',
                rows: this.expensesForPartnerArray.value
            }) :
            this.removeTableSection('Ehegatte');

        this.updateRowGroupData(this.data);
    }

    public updateRowGroupData(data: ServicesTableColumns[]) {

        const newData: ServicesTableColumns[] = [];
        const sectionTitle: string[] = [];
        this.updateTotalData(this.data);
        this.data.forEach((rowData) => {

            if(rowData.sectionTitle && (sectionTitle.indexOf(rowData.sectionTitle) === -1)) {
                sectionTitle.push(rowData.sectionTitle);

                const groupTitleRow = new ServicesTableColumns();
                groupTitleRow.rowGroup = rowData.sectionTitle;
                newData.push(groupTitleRow);
            }

            newData.push(rowData);
        });
        newData.push(this.totalData[0]);
        this.gridApi.setRowData(newData);
    }

    public updateTotalData(data: ServicesTableColumns[]) {

        let invoiceSum = 0;

        for (let row of data) {
            invoiceSum += this.currencyToNumber(row.invoiceAmount);
        }

        this.totalData[0].invoiceAmount = this.numberToCurrency(invoiceSum.toString());
    }

    public getFileType(type: string): string {

        /* TODO: move magic numbers to enum */
        switch (type) {
            case '1':
                return 'Rechnung';
            case '2':
                return 'Rezept';
        }
        return '';
    }

    public compareServices(service1: ServicesTableColumns, service2: ServicesTableColumns): number {
        return (service1.sectionIndex - service2.sectionIndex);
    }

    public pushSection(tableData: ServicesTableColumns[], section: Section) {

        /* TODO: use .map instead forEach */
        section.rows.forEach((row) => {

            /* TODO: rename variables on more explaintaition name */
            let entity = new ServicesTableColumns();

            entity.sectionTitle = section.title;
            entity.sectionIndex = section.sectionIndex;
            entity.invoiceDate = row.invoiceDate ? this.getRowDate(row.invoiceDate) : '';

            /* TODO: move to enum */
            switch (row.serviceType) {
                case "1": entity.serviceType = 'ärztl. Behandlung'; break;
                case "2": entity.serviceType = 'zahnärztl. Behandlung'; break;
                case "3": entity.serviceType = 'Fahrtkosten'; break;
                case "4": entity.serviceType = 'Heilmittel'; break;
                case "5": entity.serviceType = 'Hilfsmittel'; break;
                case "6": entity.serviceType = 'Krankenhaus'; break;
                case "7": entity.serviceType = 'Rezept'; break;
                case "8": entity.serviceType = 'Sonstige'; break;
            }

            if (row.invoiceAmount) {

                let currency = row.invoiceAmount.split(/[,.:|]/).join('.') + " €";

                currency = this.replaceAt(currency, currency.lastIndexOf('.'), ',');

                entity.invoiceAmount = currency;
            }

            if (row.reimbursement) {

                let currency = row.reimbursement.split(/[,.:|]/).join('.') + " €";

                currency = this.replaceAt(currency, currency.lastIndexOf('.'), ',');

                entity.reimbursement = currency;
            }

            entity.fileInfoList = row.files.map((fileAndType) => {
                return {
                    file: fileAndType.file ? fileAndType.file.name : "",
                    type: this.getFileType(fileAndType.type)
                } as FileInfo;
            });

            this.data.push(entity);
        });
    }

    public replaceAt(value, index, replacement) {
        return value.substr(0, index) + replacement + value.substr(index + replacement.length);
    }

    public addTableSection(section: Section) {

        this.removeTableSection(section.title);

        this.pushSection(this.data, section);

        this.data.sort((tableRow1, tableRow2) => this.compareServices(tableRow1, tableRow2));

        this.updateRowsIndexes(this.data);

        this.updateRowGroupData(this.data);
    }

    public removeTableSection(sectionTitle: string) {

        this.data = this.filterBySectionName(this.data, sectionTitle);
    }

    public updateRowsIndexes(tableRows: ServicesTableColumns[]) {

        for (let i = 0; i < tableRows.length; i++) {
            tableRows[i].number = (i + 1).toString();
        }
    }

    /* TODO: provide return type of function */
    public filterBySectionName(array: ServicesTableColumns[], sectionTitle: string) {

        return array.filter(function (value) {
            return value.sectionTitle != sectionTitle;
        });
    }

    public getRowClass(row) {
        return 'test-row-class'
    }

    private getRowDate(date: DateFormat): string {
        return date.day + '.' + date.month + '.' + date.year;
    }

    private numberToCurrency(value: string): string {

        let euro = value.slice(0, -2);
        let cent = value.slice(-2);

        euro = euro.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

        return euro + ',' + cent + " €";
    }

    private currencyToNumber(currency: string): number {

        if (currency) {

            currency = currency.replace(" €", '');
            currency = currency.split('.').join('');
            currency = currency.split(',').join('');

            return parseInt(currency);
        }
        return null;
    }
}
